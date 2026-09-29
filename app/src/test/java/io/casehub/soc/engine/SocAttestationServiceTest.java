package io.casehub.soc.engine;

import io.casehub.api.spi.CaseOutcomeEvent;
import io.casehub.ledger.api.model.LedgerAttestation;
import io.casehub.ledger.api.model.LedgerEntry;
import io.casehub.ledger.api.spi.LedgerEntryRepository;
import io.casehub.soc.domain.SocCaseTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SocAttestationServiceTest {

    private StubLedgerEntryRepository ledgerRepo;
    private SocAttestationService service;
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-08-10T12:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        ledgerRepo = new StubLedgerEntryRepository();
        service = new SocAttestationService(ledgerRepo, FIXED_CLOCK);
    }

    // ── Filtering ──────────────────────────────────────────────────────

    @Test
    void nonSocCase_noAttestationsWritten() {
        service.onOutcome(event("aml-investigation", "resolved",
                Map.of("analystOutcome", "CONFIRM_SEVERITY")));
        assertThat(ledgerRepo.savedAttestations).isEmpty();
    }

    @Test
    void nonSuccessOutcome_noAttestationsWritten() {
        service.onOutcome(event(SocCaseTypes.INCIDENT_INVESTIGATION, "FAULTED",
                Map.of("analystOutcome", "CONFIRM_SEVERITY")));
        assertThat(ledgerRepo.savedAttestations).isEmpty();
    }

    // ── Verdict mapping: triage-accuracy ────────────────────────────────

    @Test
    @Disabled("Pending #62 — WorkerDecisionEntry removed from ledger API; processOutcome is stubbed")
    void triageAccuracyVerdict_perAnalystOutcome() {}

    // ── Containment-appropriateness ─────────────────────────────────────

    @Test
    @Disabled("Pending #62 — WorkerDecisionEntry removed from ledger API; processOutcome is stubbed")
    void confirmSeverity_containmentWorker_writesTriageAndContainmentAttestations() {}

    @Test
    @Disabled("Pending #62 — WorkerDecisionEntry removed from ledger API; processOutcome is stubbed")
    void downgrade_containmentWorker_containmentIsFlagged() {}

    @Test
    @Disabled("Pending #62 — WorkerDecisionEntry removed from ledger API; processOutcome is stubbed")
    void escalate_containmentWorker_noContainmentAttestation() {}

    // ── Attestation fields ──────────────────────────────────────────────

    @Test
    @Disabled("Pending #62 — WorkerDecisionEntry removed from ledger API; processOutcome is stubbed")
    void attestationFields_populatedCorrectly() {}

    @Test
    @Disabled("Pending #62 — WorkerDecisionEntry removed from ledger API; processOutcome is stubbed")
    void tenancyIdPassedToSaveAttestation() {}

    // ── Fallbacks ───────────────────────────────────────────────────────

    @Test
    @Disabled("Pending #62 — WorkerDecisionEntry removed from ledger API; processOutcome is stubbed")
    void missingAnalystOutcome_infersFromOutcomeLabel() {}

    @Test
    @Disabled("Pending #62 — WorkerDecisionEntry removed from ledger API; processOutcome is stubbed")
    void missingAnalystId_usesSystemFallback() {}

    @Test
    void noWorkerDecisions_noAttestationsWritten() {
        service.onOutcome(event(SocCaseTypes.INCIDENT_INVESTIGATION, "resolved",
                UUID.randomUUID(), "tenant-1",
                Map.of("analystOutcome", "CONFIRM_SEVERITY")));
        assertThat(ledgerRepo.savedAttestations).isEmpty();
    }

    // ── Idempotency ─────────────────────────────────────────────────────

    @Test
    @Disabled("Pending #62 — WorkerDecisionEntry removed from ledger API; processOutcome is stubbed")
    void duplicateEvent_idempotencyGuardSkipsExistingAttestations() {}

    // ── Multiple workers ────────────────────────────────────────────────

    @Test
    @Disabled("Pending #62 — WorkerDecisionEntry removed from ledger API; processOutcome is stubbed")
    void multipleWorkers_eachGetsTriageAttestation() {}

    // ── Helpers ──────────────────────────────────────────────────────────

    private static CaseOutcomeEvent event(String caseType, String outcomeLabel,
            Map<String, Object> snapshot) {
        return event(caseType, outcomeLabel, UUID.randomUUID(), "tenant-1", snapshot);
    }

    private static CaseOutcomeEvent event(String caseType, String outcomeLabel,
            UUID caseId, String tenancyId, Map<String, Object> snapshot) {
        return new CaseOutcomeEvent(caseType, tenancyId, caseId, snapshot,
                outcomeLabel, Instant.now(), Map.of());
    }

    static class StubLedgerEntryRepository implements LedgerEntryRepository {
        final List<LedgerAttestation> savedAttestations = new ArrayList<>();
        final List<String> savedTenancyIds = new ArrayList<>();

        @Override
        public LedgerAttestation saveAttestation(LedgerAttestation attestation, String tenancyId) {
            savedAttestations.add(attestation);
            savedTenancyIds.add(tenancyId);
            return attestation;
        }

        @Override
        public List<LedgerAttestation> findAttestationsByEntryId(UUID entryId, String tenancyId) {
            return List.of();
        }

        @Override public LedgerEntry save(LedgerEntry e, String t) { return e; }
        @Override public List<LedgerEntry> findBySubjectId(UUID s, String t) { return List.of(); }
        @Override public List<LedgerEntry> findBySubjectIdAndTimeRange(UUID s, Instant f, Instant to, String t) { return List.of(); }
        @Override public Optional<LedgerEntry> findLatestBySubjectId(UUID s, String t) { return Optional.empty(); }
        @Override public Optional<LedgerEntry> findEntryById(UUID i, String t) { return Optional.empty(); }
        @Override public List<LedgerEntry> findByActorId(String a, Instant f, Instant to, String t) { return List.of(); }
        @Override public List<LedgerEntry> findByActorRole(String r, Instant f, Instant to, String t) { return List.of(); }
        @Override public List<LedgerEntry> findCausedBy(UUID e, String t) { return List.of(); }
        @Override public List<LedgerAttestation> findAttestationsByEntryIdAndCapabilityTag(UUID e, String c, String t) { return List.of(); }
        @Override public List<LedgerAttestation> findAttestationsByEntryIdGlobal(UUID e, String t) { return List.of(); }
        @Override public List<LedgerAttestation> findAttestationsByAttestorIdAndCapabilityTag(String a, String c, String t) { return List.of(); }
    }
}
