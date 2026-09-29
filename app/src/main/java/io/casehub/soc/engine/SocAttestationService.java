package io.casehub.soc.engine;

import io.casehub.api.spi.CaseOutcomeEvent;
import io.casehub.api.spi.CaseOutcomeObserver;
import io.casehub.ledger.api.model.AttestationVerdict;
import io.casehub.ledger.api.model.LedgerAttestation;
import io.casehub.ledger.api.spi.LedgerEntryRepository;
// TODO(#62): restore WorkerDecisionEntry + CaseLedgerEntryRepository when ledger API provides routing decision queries
import io.casehub.platform.api.identity.ActorType;
import io.casehub.soc.domain.SocCaseCapabilities;
import io.casehub.soc.domain.SocTrustDimensions;
import io.casehub.soc.engine.cbr.SocCaseOutcomeFilter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.Clock;
import java.util.List;
import java.util.Map;

import java.util.UUID;

@ApplicationScoped
public class SocAttestationService implements CaseOutcomeObserver {

    private static final Logger LOG = Logger.getLogger(SocAttestationService.class);
    private static final String SYSTEM_ATTESTOR = "system:soc-attestation";
    private final LedgerEntryRepository ledgerRepo;
    private final Clock clock;

    @Inject
    SocAttestationService(LedgerEntryRepository ledgerRepo, Clock clock) {
        this.ledgerRepo = ledgerRepo;
        this.clock = clock;
    }

    @Override
    public void onOutcome(CaseOutcomeEvent event) {
        if (!SocCaseOutcomeFilter.isSuccessfulIncidentInvestigation(event)) {return;}
        processOutcome(event);}

    // TODO(#62): restore when ledger API provides WorkerDecisionEntry queries
    void processOutcome(CaseOutcomeEvent event) {
        LOG.warnf("Attestation stubbed — WorkerDecisionEntry API removed in ledger restructuring (see #62). caseId=%s", event.caseId());
    }

    private static AttestationVerdict triageVerdict(String analystOutcome) {
        return "FALSE_POSITIVE".equals(analystOutcome)
                ? AttestationVerdict.FLAGGED : AttestationVerdict.SOUND;
    }

    private static String resolveAnalystOutcome(Map<String, Object> snapshot, String outcomeLabel) {
        Object outcome = snapshot.get("analystOutcome");
        if (outcome instanceof String s && !s.isBlank()) return s;
        LOG.warnf("analystOutcome missing from context — inferring from outcomeLabel=%s", outcomeLabel);
        return switch (outcomeLabel) {
            case "false-positive" -> "FALSE_POSITIVE";
            case "escalated" -> "ESCALATE";
            default -> "CONFIRM_SEVERITY";
        };
    }

    private static String resolveAnalystId(Map<String, Object> snapshot) {
        Object id = snapshot.get("analystId");
        if (id instanceof String s && !s.isBlank()) return s;
        return SYSTEM_ATTESTOR;
    }
}
