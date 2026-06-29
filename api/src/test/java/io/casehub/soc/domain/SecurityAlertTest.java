package io.casehub.soc.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SecurityAlertTest {

    @Test
    void minimalAlertRequiresOnlyMandatoryFields() {
        var alert = new SecurityAlert(
                UUID.randomUUID(), "splunk", null,
                Instant.now(), null,
                AlertSeverity.HIGH, null,
                null, null);

        assertNotNull(alert.alertId());
        assertEquals("splunk", alert.source());
        assertEquals(AlertSeverity.HIGH, alert.severity());
        assertEquals(List.of(), alert.affectedAssets());
    }

    @Test
    void affectedAssetsAreDefensivelyCopied() {
        var mutable = new java.util.ArrayList<>(List.of("server-01", "server-02"));
        var alert = new SecurityAlert(
                UUID.randomUUID(), "crowdstrike", "CS-12345",
                Instant.now(), "{}", AlertSeverity.CRITICAL,
                "malware", mutable, "Ransomware detected");

        mutable.add("server-03");
        assertEquals(2, alert.affectedAssets().size());
        assertThrows(UnsupportedOperationException.class,
                () -> alert.affectedAssets().add("server-04"));
    }

    @Test
    void nullAlertIdThrows() {
        assertThrows(NullPointerException.class, () ->
                new SecurityAlert(null, "splunk", null,
                        Instant.now(), null, AlertSeverity.LOW, null, null, null));
    }

    @Test
    void nullSourceThrows() {
        assertThrows(NullPointerException.class, () ->
                new SecurityAlert(UUID.randomUUID(), null, null,
                        Instant.now(), null, AlertSeverity.LOW, null, null, null));
    }

    @Test
    void nullSeverityThrows() {
        assertThrows(NullPointerException.class, () ->
                new SecurityAlert(UUID.randomUUID(), "splunk", null,
                        Instant.now(), null, null, null, null, null));
    }

    @Test
    void informationalSeverityIsNotActionable() {
        assertFalse(AlertSeverity.INFORMATIONAL.isActionable());
        assertTrue(AlertSeverity.LOW.isActionable());
        assertTrue(AlertSeverity.CRITICAL.isActionable());
    }
}
