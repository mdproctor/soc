package io.casehub.soc.domain;

/**
 * Five-level severity scale aligned with NIST CVSS and CISA alert classifications.
 * Maps directly to incident priority for SLA enforcement.
 */
public enum AlertSeverity {

    CRITICAL,
    HIGH,
    MEDIUM,
    LOW,
    INFORMATIONAL;

    public boolean isActionable() {
        return this != INFORMATIONAL;
    }
}
