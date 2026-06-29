package io.casehub.soc.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IncidentTest {

    @Test
    void minimalIncident() {
        var now = Instant.now();
        var incident = new Incident(
                UUID.randomUUID(), "Possible phishing attack",
                IncidentStatus.DETECTED, IncidentPriority.P2,
                AlertSeverity.HIGH,
                null, null, null, null,
                now, null);

        assertEquals(IncidentStatus.DETECTED, incident.status());
        assertEquals(List.of(), incident.triggeringAlertIds());
        assertEquals(List.of(), incident.iocs());
        assertEquals(List.of(), incident.attackTechniques());
    }

    @Test
    void activeStatusesAreNotTerminal() {
        for (IncidentStatus status : IncidentStatus.values()) {
            if (status.isActive()) {
                assertFalse(status.isTerminal(),
                        status + " should not be both active and terminal");
            }
        }
    }

    @Test
    void terminalStatusesAreNotActive() {
        for (IncidentStatus status : IncidentStatus.values()) {
            if (status.isTerminal()) {
                assertFalse(status.isActive(),
                        status + " should not be both terminal and active");
            }
        }
    }

    @Test
    void everyStatusIsEitherActiveOrTerminal() {
        for (IncidentStatus status : IncidentStatus.values()) {
            assertTrue(status.isActive() || status.isTerminal(),
                    status + " must be either active or terminal");
        }
    }

    @Test
    void expectedActiveStatuses() {
        assertTrue(IncidentStatus.DETECTED.isActive());
        assertTrue(IncidentStatus.TRIAGING.isActive());
        assertTrue(IncidentStatus.INVESTIGATING.isActive());
        assertTrue(IncidentStatus.CONTAINING.isActive());
        assertTrue(IncidentStatus.ERADICATING.isActive());
        assertTrue(IncidentStatus.RECOVERING.isActive());
    }

    @Test
    void expectedTerminalStatuses() {
        assertTrue(IncidentStatus.RESOLVED.isTerminal());
        assertTrue(IncidentStatus.CLOSED.isTerminal());
        assertTrue(IncidentStatus.FALSE_POSITIVE.isTerminal());
    }

    @Test
    void collectionsDefensivelyCopied() {
        var alertIds = new java.util.ArrayList<>(List.of(UUID.randomUUID()));
        var incident = new Incident(
                UUID.randomUUID(), "Test",
                IncidentStatus.DETECTED, IncidentPriority.P3,
                AlertSeverity.MEDIUM,
                alertIds, null, null, null,
                Instant.now(), null);

        alertIds.add(UUID.randomUUID());
        assertEquals(1, incident.triggeringAlertIds().size());
    }

    @Test
    void nullRequiredFieldsRejected() {
        var now = Instant.now();
        assertThrows(NullPointerException.class, () ->
                new Incident(null, "T", IncidentStatus.DETECTED,
                        IncidentPriority.P1, AlertSeverity.HIGH,
                        null, null, null, null, now, null));
        assertThrows(NullPointerException.class, () ->
                new Incident(UUID.randomUUID(), null, IncidentStatus.DETECTED,
                        IncidentPriority.P1, AlertSeverity.HIGH,
                        null, null, null, null, now, null));
        assertThrows(NullPointerException.class, () ->
                new Incident(UUID.randomUUID(), "T", null,
                        IncidentPriority.P1, AlertSeverity.HIGH,
                        null, null, null, null, now, null));
    }

    @Test
    void incidentPriorityHasDescription() {
        for (IncidentPriority p : IncidentPriority.values()) {
            assertNotNull(p.description());
            assertFalse(p.description().isBlank());
        }
    }
}
