package io.casehub.soc.domain;

/**
 * Capability tag constants for SOC agent registration. Used by
 * {@code AgentDescriptor} and case definition YAML bindings.
 */
public final class SocCapabilities {

    private SocCapabilities() {}

    // Triage
    public static final String ALERT_CLASSIFICATION = "soc:alert-classification";
    public static final String SEVERITY_ASSESSMENT = "soc:severity-assessment";

    // Intelligence
    public static final String IOC_CORRELATION = "soc:ioc-correlation";
    public static final String ATTCK_MAPPING = "soc:attck-mapping";
    public static final String FEED_ENRICHMENT = "soc:feed-enrichment";

    // Investigation
    public static final String LOG_ANALYSIS = "soc:log-analysis";
    public static final String EVIDENCE_COLLECTION = "soc:evidence-collection";
    public static final String TIMELINE_RECONSTRUCTION = "soc:timeline-reconstruction";

    // Containment
    public static final String HOST_ISOLATION = "soc:host-isolation";
    public static final String CREDENTIAL_REVOCATION = "soc:credential-revocation";
    public static final String NETWORK_SEGMENTATION = "soc:network-segmentation";
    public static final String IP_BLOCKING = "soc:ip-blocking";
}
