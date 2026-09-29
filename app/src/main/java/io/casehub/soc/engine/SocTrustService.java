package io.casehub.soc.engine;

import io.casehub.ledger.api.spi.ActorTrustScoreRepository;
import io.casehub.soc.domain.SocAgentDescriptors;
import io.casehub.soc.domain.SocTrustDimensions;
import io.casehub.soc.rest.dto.AgentTrustResponse;
import io.casehub.soc.rest.dto.KpiResponse;
import io.casehub.soc.rest.dto.RoutingDecisionResponse;
import io.casehub.soc.rest.dto.TrustDimensionResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SocTrustService {

    @Inject ActorTrustScoreRepository trustRepo;

    public AgentTrustResponse getAgentTrust(String agentId) {
        var descriptor = SocAgentDescriptors.descriptorsByWorkerName()
            .get(agentId.replace("soc:", ""));
        var globalScore = trustRepo.findByActorId(agentId);

        var dimensions = List.of(
                SocTrustDimensions.TRIAGE_ACCURACY,
                SocTrustDimensions.CONTAINMENT_APPROPRIATENESS)
            .stream()
            .map(dim -> {
                var ds = trustRepo.findDimensionScore(agentId, dim);
                return new TrustDimensionResponse(dim,
                    ds.map(s -> s.trustScore).orElse(0.0),
                    ds.map(s -> (long) s.decisionCount).orElse(0L));
            }).toList();

        return new AgentTrustResponse(
            agentId,
            descriptor != null ? descriptor.name() : agentId,
            descriptor != null ? descriptor.capabilities().getFirst().name() : "unknown",
            descriptor != null && descriptor.modelFamily() != null ? "LLM" : "RULE",
            globalScore.map(s -> s.trustScore).orElse(0.0),
            globalScore.map(s -> (long) s.decisionCount).orElse(0L),
            globalScore.map(s -> s.lastComputedAt).orElse(null),
            dimensions);
    }

    public List<KpiResponse> getFleetKpis() {
        var socAgentIds = SocAgentDescriptors.all().stream()
            .map(d -> d.agentId()).toList();
        var globalScores = socAgentIds.stream()
            .map(trustRepo::findByActorId)
            .filter(Optional::isPresent).map(Optional::get).toList();

        double meanTrust = globalScores.stream()
            .mapToDouble(s -> s.trustScore).average().orElse(0.0);
        int totalObservations = globalScores.stream()
            .mapToInt(s -> s.decisionCount).sum();
        long agentCount = SocAgentDescriptors.all().size();

        return List.of(
            new KpiResponse("Mean Trust", String.format("%.2f", meanTrust), ""),
            new KpiResponse("Total Observations", totalObservations, ""),
            new KpiResponse("Fleet Size", agentCount, "agents"));
    }

    // TODO(#62): restore when ledger API provides routing decision queries
    public List<RoutingDecisionResponse> getRoutingRationale(UUID caseId) {
        return List.of();
    }
}
