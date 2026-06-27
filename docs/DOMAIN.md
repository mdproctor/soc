# SOC Domain Background

This document gives a new session enough context to start designing and researching without external reading. It covers what a SOC is, how the industry works, where multi-agent coordination fits, and what makes CaseHub's approach different.

---

## What a Security Operations Center Is

A SOC is the nerve centre of an organisation's cyber defence. It monitors, detects, triages, investigates, and responds to security incidents 24/7. Traditional SOCs are staffed in tiers:

| Tier | Role | Typical work |
|------|------|-------------|
| Tier 1 | Alert triage | Filter false positives, assign severity, escalate real threats |
| Tier 2 | Investigation | Deep-dive analysis, forensic evidence collection, IOC correlation |
| Tier 3 | Threat hunting | Proactive threat discovery, adversary emulation, intelligence-driven sweeps |
| SOC Manager | Orchestration | Resource allocation, SLA enforcement, compliance reporting, escalation authority |

The fundamental problem: alert volume. A mid-size enterprise SOC processes 10,000–50,000 alerts per day. 82% of analysts report concern that real threats are being missed in the noise. Average dwell time (attacker present but undetected) is still measured in weeks.

---

## The Multi-Agent SOC Vision

The industry is moving from monolithic SIEM/SOAR to specialised agent ensembles. EY's 2026 Agentic SOC model defines three tiers of AI agents:

**Core Operational Agents** — real-time execution:
- Alert triage agent: filters false positives, assigns severity (automates 80–90% of Tier 1)
- Incident response agent: coordinates containment (endpoint isolation, credential revocation, network segmentation)
- Log analysis agent: parses and correlates logs across heterogeneous sources

**Intelligence Agents** — strategic depth:
- Threat intelligence agent: correlates IOCs with MITRE ATT&CK techniques, enriches alerts with external threat feeds
- Forensics agent: manages evidence collection, chain of custody, timeline reconstruction
- Vulnerability assessment agent: maps CVEs to asset inventory, prioritises patching

**Orchestration Agents** — coordination:
- Supervisor agent: decomposes complex incidents into subtasks, routes to specialists
- Playbook agent: selects and adapts response playbooks based on incident type
- Compliance agent: ensures response actions meet regulatory requirements, generates evidence

This three-tier model maps directly to CaseHub's channel architecture: `work` (core ops), `observe` (intelligence), `oversight` (orchestration/governance).

---

## The Incident Response Lifecycle

NIST SP 800-61 defines four phases. Each maps to CaseHub primitives:

### 1. Preparation
- Playbook definition → CaseHub `CaseDefinition` (YAML case plan models)
- Agent capability registry → `casehub-eidos` `AgentDescriptor` + `CapabilityHealth`
- Security posture baseline → `casehub-desiredstate` `DesiredStateGraph`

### 2. Detection & Analysis
- Alert ingestion → `casehub-platform` stream modules (kafka, webhook, poll) firing `CloudEvent`
- Situation detection → `casehub-ras` `Ganglion` detection strategies (DroolsCEP, Bayesian, LLM)
- Alert correlation → CBR `CaseRetriever` — retrieve similar past incidents
- Severity assessment → multi-agent debate (blocks debate pattern)
- Evidence gathering → `casehub-qhorus` typed messages on `observe` channel

### 3. Containment, Eradication, Recovery
- Containment authorization → `ActionRiskClassifier` + oversight gate on `oversight` channel
- Containment execution → `Worker` primitives with `WorkerResult` + `PlannedAction`
- Human approval → `casehub-work` `WorkItem` with SLA breach policy
- Recovery verification → `casehub-desiredstate` reconciliation loop

### 4. Post-Incident Activity
- Incident record → `casehub-ledger` tamper-evident Merkle chain
- Trust update → Bayesian Beta scoring based on incident resolution accuracy
- Knowledge retention → CBR `Retain` — incident stored for future triage
- Compliance evidence → `LedgerVerificationService` inclusion proofs

---

## MITRE ATT&CK Framework

MITRE ATT&CK is the industry-standard knowledge base of adversary tactics and techniques. It organises attack behaviour into 14 tactics (reconnaissance, initial access, execution, persistence, privilege escalation, defence evasion, credential access, discovery, lateral movement, collection, command & control, exfiltration, impact, resource development) with hundreds of techniques under each.

SOC agents should map detected behaviour to ATT&CK technique IDs. This enables:
- Standardised threat classification across the agent fleet
- CBR retrieval keyed on ATT&CK technique (not just IOC hashes)
- Trust scoring per agent per ATT&CK tactic (an agent may be strong on credential access but weak on lateral movement)
- Kill chain progression tracking — which tactics have been observed for this incident?

**Design implication:** capability tags in `casehub-eidos` should align with ATT&CK tactics. Trust dimensions should measure accuracy per tactic category.

---

## Key Domain Entities (Starting Point — Refine During Research)

| Entity | What it represents | CaseHub mapping |
|--------|-------------------|-----------------|
| `SecurityAlert` | Raw alert from SIEM/EDR/IDS — the trigger | `CloudEvent` via stream adapter |
| `Incident` | Confirmed threat — promoted from alert after triage | `CaseInstance` via `casehub-engine` |
| `IOC` (Indicator of Compromise) | Observable artefact: IP, hash, domain, URL, email | Domain record in `api/` |
| `ThreatIntelReport` | Enriched analysis: ATT&CK mapping, attribution, confidence | Agent `WorkerResult` on `observe` channel |
| `ContainmentAction` | Irreversible response: isolate host, revoke creds, block IP | `PlannedAction` gated by `ActionRiskClassifier` |
| `ForensicEvidence` | Digital evidence with chain of custody | `LedgerEntry` subclass (tamper-evident) |
| `Playbook` | Pre-defined response procedure for an incident type | `CaseDefinition` (YAML) |
| `AnalystWorkItem` | Human analyst review/approval task | `WorkItem` with SLA |

---

## Compliance Frameworks

### SOC 2 Type II
Service organisation controls for security, availability, processing integrity, confidentiality, and privacy. Requires demonstrable, auditable controls — not just policies. The tamper-evident ledger directly addresses the "demonstrable" requirement.

### DORA (EU Digital Operational Resilience Act)
Effective January 2025. Requires financial entities to detect, contain, and recover from ICT-related incidents. Mandates incident classification, response timelines, and reporting to competent authorities. The SLA breach policy and commitment lifecycle directly enforce DORA response windows.

### NIS2 (EU Network and Information Security Directive)
Broader than DORA — applies to essential and important entities across multiple sectors. Requires incident detection, response, and reporting capabilities. Multi-sector applicability makes CaseHub SOC relevant beyond financial services.

### GDPR Art. 22 (Automated Decision-Making)
When AI agents make containment decisions (isolate a host, block a user), this may constitute automated decision-making affecting individuals. The audit ledger's `causedByEntryId` chain provides the decision record that Art. 22 requires.

---

## Competitive Landscape

| Platform | Approach | What CaseHub adds |
|----------|----------|-------------------|
| **Torq Socrates** | AI SOC analyst orchestrating specialist agents. 90% Tier-1 automation. | No trust scoring, no CBR, no normative accountability |
| **Stellar Cyber** | Autonomous multi-agent: detection, correlation, scoring, response | No commitment-based coordination, no formal oversight gates |
| **Darktrace Antigena** | Self-learning AI for autonomous response | Black-box decisions, no tamper-evident audit trail |
| **Palo Alto XSOAR** | SOAR with playbook automation | Rule-based, no adaptive trust routing, no agent debate |
| **CrowdStrike Charlotte AI** | LLM-powered threat hunting assistant | Single-agent, no multi-agent coordination |

**The gap CaseHub fills:** No existing platform combines trust-weighted agent routing, CBR-based triage learning, commitment-based normative accountability, and tamper-evident audit in a single stack. The closest is EY's conceptual Agentic SOC model — but it's a blueprint, not an implementation.

---

## Market Context

- Global AI-in-cybersecurity spending: $24.8B (2024) → projected $146.5B by 2034
- 73% of enterprises already using or developing agentic AI for cybersecurity (Cyber Security Tribe 2026 survey)
- 82% of analysts concerned about missing real threats due to alert volume
- Workforce shortage approaching 4 million professionals worldwide
- Average cost of a data breach: $4.88M (2024, IBM)

---

## Research Directions — What to Explore

These are starting points for the first design session. Research the internet and Google Scholar for each.

### Core Architecture
- How do existing Agentic SOC implementations handle agent coordination failure?
- What inter-agent communication protocols exist beyond A2A and MCP?
- How should SOC agents share memory/context during an active incident?

### CBR for Incident Triage
- Academic work on CBR applied to SIEM alert correlation
- How to represent incidents as cases — which features matter for similarity matching?
- How to handle concept drift — attack patterns evolve, old cases become misleading

### AI Fusion Opportunities
- LLM agents for threat narrative synthesis from raw log data
- LLM agents for natural language IOC correlation across unstructured threat intel feeds
- Automated MITRE ATT&CK technique classification from raw alert data
- LLM-powered playbook generation from incident post-mortems
- Anomaly explanation — why did the ML model flag this? LLM translates model features to analyst-readable rationale

### Real-Time Event Processing
- Drools CEP (Complex Event Processing) for temporal attack pattern detection
- How does `casehub-ras` (Reticular Activating System) fit for real-time situation awareness?
- Sliding window correlation — multiple low-severity alerts becoming a high-severity incident

### Dashboard & Visualisation
- What do SOC analysts actually look at? What information density is optimal?
- Kill chain visualisation — mapping incident progression through ATT&CK stages
- Trust score visualisation — how to make agent reliability transparent to human operators

### Platform Capabilities to Propose
Think about what SOC needs that the platform doesn't have yet. File as `casehubio/parent` issues:
- Temporal event correlation SPI (beyond what casehub-ras provides today)
- Automated playbook composition from building blocks
- Real-time threat feed integration adapters
- Agent capability marketplace — discover and deploy new specialist agents at runtime
- Confidence-weighted alert aggregation — multiple agents vote on severity
