# Next Steps — casehub-soc Development Roadmap

**Date:** 2026-06-29
**Status:** Greenfield scaffold complete. Build infrastructure complete. No domain implementation yet.
**Note:** IntelliJ MCP disabled due to memory leak — all semantic navigation via bash/grep/find during platform review.

---

## Current State

### ✅ Completed
- Maven multi-module structure (parent → api/ + app/)
- Git scaffolding, workspace setup, CLAUDE.md conventions
- Dependency inheritance from casehub-parent BOM (#319)
- CDI wiring fixed — `mvn install` succeeds (#6)
- App pom cleanup — PostgreSQL driver added, redundant deps removed (6624a12)
- Domain background document — SOC fundamentals, MITRE ATT&CK, incident lifecycle, compliance (DOMAIN.md)
- GitHub repo created: casehubio/soc

### 🚧 In Progress (Open Issues)
- #5: ARC42STORIES.md stub
- #4: issue-workflow setup — commit hooks and work tracking
- #3: build infrastructure — CSV entries, dashboard, README badge
- #2: parent BOM entries for casehub-soc artifacts
- #1: CI workflow — publish.yml with repository_dispatch trigger

### ❌ Missing
- **Platform capability inventory** — we have DOMAIN.md (SOC context) but no structured review of what the platform already provides
- **Application architecture** — no domain model, no SPI definitions, no agent design
- **Epic planning** — no arc42stories chapters, no incremental delivery plan
- **Reference implementation study** — haven't analyzed peer apps (casehub-aml, casehub-clinical, casehub-life) to learn proven patterns

---

## Strategic Direction — Phase 1: Learn the Platform

**The problem:** We have rich SOC domain knowledge (DOMAIN.md) but incomplete understanding of the platform capabilities we're building on top of. Before designing SOC-specific domain entities and agents, we must map what the platform already provides.

**The solution:** A systematic platform capability review — not passive reading, but active mapping of foundation primitives to SOC requirements.

### Step 1.1: Platform Capability Inventory

**Goal:** Create `docs/PLATFORM-INVENTORY.md` — a structured mapping of CaseHub foundation capabilities to SOC use cases.

**Method:**
1. Read PLATFORM.md in full (already fetched via WebFetch above)
2. Read APPLICATIONS.md in full (already fetched)
3. For each foundation module, ask: "How does SOC use this?"
4. Document the mapping with concrete examples

**Output format:**

```markdown
# Platform Capability Inventory for SOC

## casehub-platform

### Identity (`CurrentPrincipal`)
**What it provides:** Actor/tenant/groups/roles resolution
**SOC use case:** 
- Associate security alerts with tenant organizations
- Route incidents to analyst groups based on roles
- Audit trail — which analyst approved containment?

### Preferences (`PreferenceKey<T>`)
**What it provides:** Typed hierarchical configuration
**SOC use case:**
- Alert severity thresholds per tenant
- Auto-containment policies (on/off per org)
- SLA windows (P1=15min, P2=1hr, P3=24hr)

[... continue for all modules ...]
```

**Acceptance criteria:**
- Every foundation module listed in PLATFORM.md is mapped
- Every SOC use case from DOMAIN.md references at least one platform capability
- Gaps are explicitly flagged — "SOC needs X, platform doesn't provide it → file casehubio/parent issue"

### Step 1.2: Application Pattern Study

**Goal:** Learn how other applications use the platform by analyzing their source code.

**Repos to study (in priority order):**
1. **casehub-aml** — most mature application, complex domain (FinCEN compliance)
2. **casehub-clinical** — multi-agent orchestration, regulatory compliance (GCP/FDA)
3. **casehub-life** — simpler domain, good baseline for application structure
4. **casehub-drafthouse** — reference for MCP tool exposure pattern

**For each repo, map:**
- What domain entities live in `api/` (pure Java SPI)
- What JPA entities live in `app/` (persistence)
- How they wire to foundation SPIs (CDI patterns)
- What agents they define and how they coordinate
- How they expose MCP tools (if any)

**Method (without IntelliJ MCP — use bash):**
```bash
# Clone peer repos into ../
git clone https://github.com/casehubio/aml.git ~/casehub/aml
git clone https://github.com/casehubio/clinical.git ~/casehub/clinical
git clone https://github.com/casehubio/life.git ~/casehub/life
git clone https://github.com/casehubio/drafthouse.git ~/casehub/drafthouse

# For each repo:
find ~/casehub/aml/api/src -name "*.java" -type f | wc -l  # count domain SPIs
grep -r "extends LedgerEntry" ~/casehub/aml/app/src       # find ledger subclasses
grep -r "@ApplicationScoped" ~/casehub/aml/app/src        # find CDI beans
```

**Output:** `docs/PEER-PATTERNS.md` — distilled learnings from peer repos

### Step 1.3: Foundation Repo Deep-Dive

**Goal:** Read the actual source code of key foundation repos to understand implementation depth.

**Repos to study (priority order):**
1. **casehub-engine** — case orchestration, agent routing, WorkOrchestrator
2. **casehub-ledger** — audit trail, trust scoring, Bayesian Beta
3. **casehub-qhorus** — agent mesh, commitment tracking, channel semantics
4. **casehub-work** — WorkItem lifecycle, SLA breach, escalation
5. **casehub-neural-text** — CBR retrieval, RAG pipeline, CRAG

**For each, document:**
- Key SPIs we'll consume (interface names, method signatures)
- Extension points we'll implement (which SPIs casehub-soc provides)
- Wiring patterns (how to activate optional features via classpath deps)

**Output:** `docs/FOUNDATION-SPIS.md` — the contracts we're coding against

---

## Strategic Direction — Phase 2: Architecture Design

Once platform inventory is complete, design the SOC-specific architecture.

### Step 2.1: Domain Model Design

**Inputs:**
- DOMAIN.md (SOC entities: SecurityAlert, Incident, IOC, ThreatIntelReport, etc.)
- PLATFORM-INVENTORY.md (what foundation provides)
- PEER-PATTERNS.md (how other apps structure domain models)

**Output:** `docs/DOMAIN-MODEL.md` — the entities that live in `api/` and `app/`

**Key decisions:**
- Which entities are pure-Java interfaces in `api/` vs JPA @Entity in `app/`?
- Which entities extend `LedgerEntry` (need tamper evidence)?
- Which entities get CBR treatment (stored in `CaseMemoryStore`)?
- How do we model MITRE ATT&CK technique taxonomy?

### Step 2.2: Agent Architecture Design

**Inputs:**
- DOMAIN.md (three-tier agent model: core ops, intelligence, orchestration)
- FOUNDATION-SPIS.md (casehub-eidos `AgentDescriptor`, casehub-engine routing)
- EY Agentic SOC model (from DOMAIN.md)

**Output:** `docs/AGENT-ARCHITECTURE.md` — the agent fleet design

**Key decisions:**
- Which agents are rule-based (Drools) vs LLM-powered (Claude)?
- How do agents share incident context during live investigation?
- How does trust scoring partition by ATT&CK tactic?
- Which agents write to which Qhorus channels (work/observe/oversight)?

### Step 2.3: Compliance & Audit Design

**Inputs:**
- DOMAIN.md (SOC2, DORA, NIS2, GDPR Art.22 requirements)
- PLATFORM-INVENTORY.md (ledger, work, ops compliance module)

**Output:** `docs/COMPLIANCE-DESIGN.md` — how we deliver accountability

**Key decisions:**
- Which containment actions require WorkItem approval gates?
- How do we model `ActionRiskClassifier` rules?
- What ledger entries get Merkle inclusion proofs for auditors?
- How do we handle GDPR Art.17 erasure (suspect's personal data in forensic logs)?

### Step 2.4: CBR (Case-Based Reasoning) Design

**Inputs:**
- DOMAIN.md (CBR research directions)
- PLATFORM-INVENTORY.md (neural-text CaseRetriever, CRAG)
- Academic research on CBR for SIEM alert triage

**Output:** `docs/CBR-DESIGN.md` — how past incidents inform future triage

**Key decisions:**
- How do we represent incidents as cases? (feature vector: IOCs, ATT&CK techniques, severity, outcome)
- What similarity metric for retrieval? (cosine on embeddings? hybrid RRF?)
- How do we handle concept drift? (attacker TTPs evolve, old cases mislead)
- Retain phase: which incident data goes into `CaseMemoryStore`?

---

## Strategic Direction — Phase 3: Epic Planning (arc42stories)

Once architecture is designed, break into deliverable epics.

### Step 3.1: Create ARC42STORIES.md

**Structure (aligned with arc42 + incremental delivery):**

```markdown
# casehub-soc — Arc42 Stories

## Epic 1: Alert Ingestion & Triage (MVP)
**Goal:** Ingest raw SIEM alerts, triage to incidents, route to analysts

**Stories:**
1.1 SecurityAlert domain entity (api/ + app/)
1.2 CloudEvent adapter for SIEM webhook ingestion
1.3 Alert triage agent (rule-based severity classification)
1.4 Incident promotion workflow (alert → case instance)
1.5 Analyst WorkItem creation with SLA breach policy

**Platform capabilities used:**
- casehub-platform: CloudEvents streaming
- casehub-engine: CaseInstance orchestration
- casehub-work: WorkItem + SLA
- casehub-ledger: audit trail for triage decisions

**Deliverable:** Can receive SIEM alert webhook, classify severity, create analyst task

---

## Epic 2: Threat Intelligence Enrichment
**Goal:** Enrich alerts with external threat intel, map to MITRE ATT&CK

**Stories:**
2.1 IOC domain entity (IP, hash, domain, URL)
2.2 ThreatIntelReport domain entity (ATT&CK technique mapping)
2.3 Threat intelligence agent (calls external feeds, writes to observe channel)
2.4 ATT&CK technique taxonomy (enum or reference data table?)
2.5 Incident enrichment workflow (correlate IOCs with known threats)

**Platform capabilities used:**
- casehub-qhorus: observe channel for threat intel messages
- casehub-connectors: outbound HTTP for threat feed APIs
- casehub-neural-text: semantic search on IOC descriptions

**Deliverable:** Incidents show enriched context (threat actor, campaign, kill chain stage)

---

## Epic 3: Containment & Response
**Goal:** Execute containment actions with human oversight, audit decisions

**Stories:**
3.1 ContainmentAction domain entity (isolate host, revoke creds, block IP)
3.2 ActionRiskClassifier SPI implementation (high-risk actions require approval)
3.3 Containment playbook (CaseDefinition YAML for common incident types)
3.4 Containment execution agent (calls EDR APIs, writes PlannedAction)
3.5 Human approval gate (WorkItem for SOC manager authorization)

**Platform capabilities used:**
- casehub-work: WorkItem with approval gate
- casehub-engine: ActionRiskClassifier integration
- casehub-ledger: tamper-evident containment record
- casehub-worker: worker dispatch for EDR API calls

**Deliverable:** Can execute containment with audit trail and approval workflow

---

## Epic 4: Trust-Weighted Agent Routing
**Goal:** Route incidents to agents based on trust scores, learn from outcomes

**Stories:**
4.1 Incident resolution attestation (analyst confirms triage was correct/incorrect)
4.2 Trust score dimension: ATT&CK tactic (separate scores per tactic category)
4.3 Trust-weighted routing strategy (consume TrustWeightedAgentStrategy from engine-ledger)
4.4 Agent capability health probes (check if threat feed API is up)
4.5 Fallback routing (when trust data insufficient, fall back to round-robin)

**Platform capabilities used:**
- casehub-ledger: Bayesian Beta trust scoring
- casehub-eidos: AgentRegistry + CapabilityHealth
- casehub-engine: AgentRoutingStrategy SPI

**Deliverable:** Incidents routed to most-trusted agent for that ATT&CK tactic

---

## Epic 5: CBR-Based Triage Learning
**Goal:** Past incidents inform future triage via case retrieval

**Stories:**
5.1 Incident-to-case mapping (feature vector: IOCs, ATT&CK, severity, outcome)
5.2 CaseRetriever SPI wiring (connect to neural-text RAG pipeline)
5.3 Retain phase (write resolved incidents to CaseMemoryStore)
5.4 Retrieve phase (query similar past incidents during triage)
5.5 Reuse phase (pre-populate severity/playbook from retrieved case)

**Platform capabilities used:**
- casehub-neural-text: CaseRetriever, CorpusStore, CRAG
- casehub-platform: CaseMemoryStore SPI (choose backend: JPA FTS, Mem0 vector, Graphiti graph)

**Deliverable:** Triage agent shows "similar past incidents" during classification

---

## Epic 6: Compliance & Reporting (SOC2, DORA, NIS2)
**Goal:** Generate compliance evidence, demonstrate audit trail

**Stories:**
6.1 Incident timeline view (all ledger entries for one incident, causedByEntryId chain)
6.2 Merkle inclusion proof endpoint (auditor verifies containment decision wasn't altered)
6.3 DORA response time report (incidents by classification timeline, SLA compliance)
6.4 SOC2 Type II evidence export (all WorkItem approvals for containment actions)
6.5 GDPR Art.17 erasure workflow (redact suspect PII from forensic logs)

**Platform capabilities used:**
- casehub-ledger: Merkle MMR, inclusion proofs, ErasureReceiptLedgerEntry
- casehub-ops: compliance module (SOC2, DORA, NIS2 frameworks)
- casehub-work: WorkItem audit trail

**Deliverable:** Auditor can verify tamper-evident response record for any incident

---

## Epic 7: SOC Dashboard (casehub-pages)
**Goal:** Real-time SOC operator dashboard for incident overview

**Stories:**
7.1 Incident status dataset (open/investigating/contained/resolved counts)
7.2 Agent trust heatmap dataset (trust scores per agent per ATT&CK tactic)
7.3 Channel activity dataset (Qhorus message volume on work/observe/oversight)
7.4 Kill chain progression widget (MITRE ATT&CK tactics observed for active incidents)
7.5 SLA breach alert widget (incidents approaching deadline)

**Platform capabilities used:**
- casehub-pages: iframe embed, dataset contracts
- casehub-engine: CaseInstance queries
- casehub-qhorus: channel message queries
- casehub-ledger: trust score queries

**Deliverable:** Web dashboard showing live SOC operational picture

---

## Future Epics (Post-MVP)
- Epic 8: Automated Playbook Composition (casehub-blocks integration)
- Epic 9: Real-Time Event Correlation (casehub-ras Ganglion strategies)
- Epic 10: LLM Threat Narrative Synthesis (Claude agent for incident summaries)
- Epic 11: Multi-Tenant SOC-as-a-Service (tenant isolation for MSPs)
```

**Acceptance criteria for arc42stories:**
- Every epic has clear deliverable
- Every epic lists platform capabilities consumed
- Stories are independently testable (TDD-ready)
- Dependencies between epics are explicit
- MVP path is clear (Epics 1-3 = minimum viable SOC)

---

## Immediate Next Steps (Today → This Week)

### Priority 1: Platform Learning (Unblock Architecture)
1. ✅ Read PLATFORM.md (done via WebFetch)
2. ✅ Read APPLICATIONS.md (done via WebFetch)
3. ⬜ Create `docs/PLATFORM-INVENTORY.md` (Step 1.1 above)
4. ⬜ Clone peer repos (aml, clinical, life, drafthouse)
5. ⬜ Create `docs/PEER-PATTERNS.md` (Step 1.2 above)
6. ⬜ Create `docs/FOUNDATION-SPIS.md` (Step 1.3 above)

**Time estimate:** 1-2 sessions (6-12 hours of research + documentation)

### Priority 2: Architecture Design
7. ⬜ Create `docs/DOMAIN-MODEL.md` (Step 2.1)
8. ⬜ Create `docs/AGENT-ARCHITECTURE.md` (Step 2.2)
9. ⬜ Create `docs/COMPLIANCE-DESIGN.md` (Step 2.3)
10. ⬜ Create `docs/CBR-DESIGN.md` (Step 2.4)

**Time estimate:** 2-3 sessions (12-18 hours of design)

### Priority 3: Epic Planning
11. ⬜ Create `docs/ARC42STORIES.md` (Step 3.1 above — full chapter structure)
12. ⬜ Close issue #5 (already references ARC42STORIES.md)

**Time estimate:** 1 session (4-6 hours)

### Priority 4: Build Infrastructure (Parallel Track)
13. ⬜ Close issue #1 (CI workflow)
14. ⬜ Close issue #2 (parent BOM entries)
15. ⬜ Close issue #3 (CSV, dashboard, README badge)
16. ⬜ Close issue #4 (issue-workflow hooks)

**Time estimate:** 1 session (concurrent with Priority 1)

---

## Open Questions (Research During Platform Review)

### CBR Implementation
- Which `CaseMemoryStore` backend? (JPA FTS for structured queries? Mem0 for semantic? Graphiti for temporal reasoning?)
- How to version case schemas? (attack patterns evolve — old cases have different feature sets)

### Agent Coordination
- Should agents share memory during live incident? (multiple agents investigating same incident need shared context)
- How to prevent agent loops? (agent A asks agent B, B asks A → cycle detection needed?)

### Trust Scoring Granularity
- Trust per ATT&CK tactic or per technique? (14 tactics vs 200+ techniques — granularity tradeoff)
- How to handle cold-start? (new agent has no trust history — what's the fallback?)

### Compliance Evidence
- Which ledger entries get Merkle proofs? (every triage decision? only containment? only human approvals?)
- How to export W3C PROV-DM for regulatory filings?

### Platform Gaps to Propose
- Real-time streaming correlation (beyond what casehub-ras provides today)
- Drools CEP integration (temporal pattern detection for attack chains)
- Automated playbook composition from casehub-blocks
- Agent capability marketplace (runtime discovery and deployment)

---

## Success Criteria (End of Phase 3)

Before writing the first line of production code, we will have:
- ✅ Complete understanding of platform capabilities
- ✅ Proven patterns from peer application analysis
- ✅ Designed domain model (entities, SPIs, relationships)
- ✅ Designed agent architecture (coordination, trust, CBR)
- ✅ Designed compliance & audit strategy
- ✅ Incremental delivery plan (arc42stories with 7+ epics)
- ✅ Build infrastructure operational (CI, dashboards, issue tracking)

**Then and only then:** Epic 1, Story 1.1 — write the first domain entity.

---

## Tooling Workaround (IntelliJ MCP Disabled)

Until IntelliJ MCP memory leak is resolved, use bash for semantic navigation:

```bash
# Find class definitions
find ~/casehub/aml/api/src -name "*.java" -exec grep -l "class.*Entity" {} \;

# Find all @Entity classes
grep -r "@Entity" ~/casehub/aml/app/src --include="*.java" -A 2

# Find SPI implementations
grep -r "implements.*SPI" ~/casehub/engine/src --include="*.java"

# Find ledger subclasses
grep -r "extends LedgerEntry" ~/casehub/ledger/src --include="*.java" -A 5

# Find all CDI beans
grep -r "@ApplicationScoped\|@RequestScoped" ~/casehub/work/src --include="*.java"
```

---

## Appendix: Why This Sequence?

**Why platform review before domain design?**
- Risk: Reinvent foundation primitives (e.g., build our own audit trail when ledger provides it)
- Risk: Miss platform capabilities (e.g., casehub-ras for event correlation, but we roll our own)
- Benefit: Design domain model that composes cleanly with foundation SPIs

**Why peer pattern study before implementation?**
- Risk: Invent novel patterns that diverge from platform conventions
- Risk: Miss proven CDI wiring strategies that other apps use successfully
- Benefit: Copy patterns that work, avoid patterns that don't

**Why epic planning before coding?**
- Risk: Build in wrong order (start with dashboard before alert ingestion works)
- Risk: Create monolithic PRs (no incremental delivery)
- Benefit: Each epic is independently deliverable and testable

**Why this is NOT analysis paralysis:**
- Time-boxed: 1 week of research max before first code
- Concrete outputs: every step produces a document that guides implementation
- Validates platform choice: if platform doesn't support SOC requirements, we learn now (not after 6 months of implementation)

---

## Next Session Prompt

> "Start Priority 1, Step 3: Create `docs/PLATFORM-INVENTORY.md`. Use the PLATFORM.md content already fetched. For each foundation module (platform, ledger, work, qhorus, engine, eidos, neural-text, connectors, worker, ras, ops), map its capabilities to SOC use cases from DOMAIN.md. Flag gaps where SOC needs something the platform doesn't provide."
