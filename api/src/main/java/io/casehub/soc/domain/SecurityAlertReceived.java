package io.casehub.soc.domain;

/**
 * CDI event fired when a new security alert is ingested.
 * Downstream observers handle triage, deduplication, and case creation.
 */
public record SecurityAlertReceived(SecurityAlert alert) {

    public SecurityAlertReceived {
        java.util.Objects.requireNonNull(alert, "alert");
    }
}
