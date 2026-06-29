package io.casehub.soc.domain;

/**
 * Incident lifecycle status following the NIST SP 800-61 incident response phases.
 *
 * <p>Terminal/active classification follows the LIFECYCLE.md convention:
 * consumer code must use {@link #isTerminal()} and {@link #isActive()},
 * never enumerate statuses explicitly to check liveness.
 */
public enum IncidentStatus {

    DETECTED(false, true),
    TRIAGING(false, true),
    INVESTIGATING(false, true),
    CONTAINING(false, true),
    ERADICATING(false, true),
    RECOVERING(false, true),
    RESOLVED(true, false),
    CLOSED(true, false),
    FALSE_POSITIVE(true, false);

    private final boolean terminal;
    private final boolean active;

    IncidentStatus(boolean terminal, boolean active) {
        this.terminal = terminal;
        this.active = active;
    }

    public boolean isTerminal() { return terminal; }
    public boolean isActive() { return active; }
}
