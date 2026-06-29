package io.casehub.soc.domain;

/**
 * MITRE ATT&CK Enterprise tactics — the 14 categories of adversary behaviour.
 * Used for capability tagging, trust dimension scoping, and kill chain tracking.
 */
public enum AttackTactic {

    RECONNAISSANCE("TA0043", "Reconnaissance"),
    RESOURCE_DEVELOPMENT("TA0042", "Resource Development"),
    INITIAL_ACCESS("TA0001", "Initial Access"),
    EXECUTION("TA0002", "Execution"),
    PERSISTENCE("TA0003", "Persistence"),
    PRIVILEGE_ESCALATION("TA0004", "Privilege Escalation"),
    DEFENSE_EVASION("TA0005", "Defense Evasion"),
    CREDENTIAL_ACCESS("TA0006", "Credential Access"),
    DISCOVERY("TA0007", "Discovery"),
    LATERAL_MOVEMENT("TA0008", "Lateral Movement"),
    COLLECTION("TA0009", "Collection"),
    COMMAND_AND_CONTROL("TA0011", "Command and Control"),
    EXFILTRATION("TA0010", "Exfiltration"),
    IMPACT("TA0040", "Impact");

    private final String mitreId;
    private final String displayName;

    AttackTactic(String mitreId, String displayName) {
        this.mitreId = mitreId;
        this.displayName = displayName;
    }

    public String mitreId() { return mitreId; }
    public String displayName() { return displayName; }
}
