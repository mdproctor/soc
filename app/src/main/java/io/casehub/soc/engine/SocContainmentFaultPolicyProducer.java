package io.casehub.soc.engine;

import io.casehub.desiredstate.api.FaultPolicy;
import io.casehub.desiredstate.api.SituationRecompiler;
import io.casehub.desiredstate.api.FaultType;
import io.casehub.desiredstate.api.NodeType;
import io.casehub.desiredstate.api.ThresholdFaultPolicy;
import io.casehub.desiredstate.api.TypedFaultPolicy;
import io.casehub.soc.domain.SocContainmentNodeTypes;
import io.casehub.soc.domain.SocDivergenceReviewSpec;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.util.List;
import java.util.Set;

@ApplicationScoped
public class SocContainmentFaultPolicyProducer {

    @Produces
    @ApplicationScoped
    public List<FaultPolicy>

    faultPolicies() {
        ThresholdFaultPolicy policy = ThresholdFaultPolicy.builder()
                                                          .faultTypes(Set.of(FaultType.PROVISION_FAILED, FaultType.NODE_DEGRADED))
                                                          .nodeTypes(Set.of(
                                                                  SocContainmentNodeTypes.ISOLATE_HOST, SocContainmentNodeTypes.BLOCK_IP,
                                                                  SocContainmentNodeTypes.BLOCK_DOMAIN, SocContainmentNodeTypes.REVOKE_CREDENTIALS,
                                                                  SocContainmentNodeTypes.ROTATE_API_KEY, SocContainmentNodeTypes.DISABLE_USER_ACCOUNT,
                                                                  SocContainmentNodeTypes.NETWORK_SEGMENTATION, SocContainmentNodeTypes.WIPE_ENDPOINT))
                                                          .tier(1, TypedFaultPolicy.of(NodeType.of("soc:retry-noop"), (t, f, g, a) -> List.of()))
                                                          .tier(2, FaultPolicy.addReviewNode(SocDivergenceReviewSpec::fromFaultEvent))
                                                          .namespace("soc-containment-verification")
                                                          .build();
        return List.of(policy);
    }

    @Produces
    @ApplicationScoped
    public List<SituationRecompiler>

    situationRecompilers() {
        return List.of();
    }
}
