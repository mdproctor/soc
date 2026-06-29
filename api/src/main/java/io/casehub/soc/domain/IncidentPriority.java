package io.casehub.soc.domain;

/**
 * Incident priority determines SLA windows and escalation chains.
 * Mapped from alert severity + asset criticality during triage.
 */
public enum IncidentPriority {

    P1("Critical — immediate response required"),
    P2("High — rapid response required"),
    P3("Medium — standard response"),
    P4("Low — routine handling");

    private final String description;

    IncidentPriority(String description) {
        this.description = description;
    }

    public String description() { return description; }
}
