# Design Journal — epic-1-domain-model

### Domain boundary redesign · 2026-06-29 · §Epic 1: Domain Vocabulary & Platform Integration

**Decision:** SOC does not create custom entity types for alerts, incidents, or cases. After platform review, SecurityAlert, Incident, and all JPA/webhook infrastructure were removed. The platform provides CloudEvent routing (RAS), case lifecycle (CaseInstance), and investigation state (CaseContext). SOC provides domain vocabulary, detection logic, risk classification, and declarative YAML.

**What was built:**
- Domain vocabulary: AlertSeverity, IocType/Ioc, AttackTactic/AttackTechnique, SocGroups, SocCapabilities, SocActionType
- SiemAlertGanglion — first RAS detector, classifies SIEM/EDR alerts by severity via CloudEvent extensions
- SocActionRiskClassifier — risk classification for containment actions (NEVER/ALWAYS/threshold policies)
- SocCaseHub — YamlCaseHub subclass loading incident-investigation.yaml
- ras-situations.yaml — two situation definitions (soc-siem-alert-critical, soc-brute-force)
- incident-investigation.yaml — case definition with 3 capabilities, 4 bindings, 3 goals

**Platform gap discovered:** casehub-ras runtime requires SituationStore CDI bean — no implementation exists. SOC is the first RAS consumer. RAS API (Ganglion SPI) works without the runtime.

### What was removed · 2026-06-29 · §Epic 1: Domain Vocabulary & Platform Integration

SecurityAlert, SecurityAlertReceived, SecurityAlertEntity, SecurityAlertRepository, AlertIngestionResource, AlertCloudEventObserver, V2000 Flyway migration — all duplicated platform capabilities (RAS event routing, platform-streams-webhook, CaseTrigger).

AlertSeverity was kept — type-safe severity vocabulary with domain semantics (isActionable()).
