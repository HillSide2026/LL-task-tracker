# Cross-Repo Domain Authority Map

**Version:** 1.0.0  
**Spec reference:** Cross-Repo Domain Authority and Coordination Spec

This is the canonical implementation reference for domain ownership, object boundaries,
vocabulary, write permissions, and conversion paths across all Levine Law systems.

---

## 1. Domain ownership table

| Domain | Canonical owner | System of record | Objects owned |
|---|---|---|---|
| **Commercial** | marketing/commercial system | marketing/commercial system | Contact, Opportunity |
| **Matter** | ll-task-tracker | ll-task-tracker | Matter (CaseInstance) |
| **Commerce** | commerce backend / ll-corporate serving surface | commerce backend | ProductOrder |
| **Acquisition** | public website | public website | Form submissions (transient only) |

---

## 2. Bounded objects — what each object IS

### Contact (Commercial)
A commercial identity record for a person or entity.
- Owned by: marketing/commercial system
- Creates a Matter: **No**
- Has lifecycle semantics: No (commercial record only)
- Key ID: `contactId`

### Opportunity (Commercial)
A commercial engagement opportunity moving through a commercial pipeline.
- Owned by: marketing/commercial system
- Creates a Matter: **Only through** `CreateMatterFromOpportunity` command
- Has lifecycle semantics: Pipeline stages only (`opportunityPipelineStage`)
- Key ID: `opportunityId`

### Matter (Matter domain)
A firm engagement under canonical admin control. **The only object with Matter lifecycle.**
- Owned by: ll-task-tracker
- Key ID: `matterId` (= `businessKey` in case engine API)
- Lifecycle: `matterLifecycleStage` + `matterState` + `adminState` (internal granular)
- Created by: approved Matter-domain creation paths only

### ProductOrder (Commerce)
A standalone product purchase. Not a Matter by default.
- Owned by: commerce backend / ll-corporate serving surface
- Creates a Matter: **No, not by default**
- Has lifecycle semantics: `productOrderStatus` only (not Matter stages)
- Key ID: `productOrderId`

---

## 3. Object relationship map

```
PUBLIC WEBSITE
  │
  ├─ CTA → COMMERCIAL SYSTEM
  │          │
  │          ├─ creates Contact
  │          └─ creates Opportunity ──────────────────────────┐
  │                    │                                       │
  │                    │ CreateMatterFromOpportunity (command) │
  │                    ▼                                       │
  │            LL-TASK-TRACKER                                 │
  │                    │                                       │
  │                    └─ creates Matter ◄────────────────────┘
  │                             │
  │                             │ OpportunityConvertedToMatter (event)
  │                             │ ► Commercial system records matterId on Opportunity
  │
  └─ CTA → LL-CORPORATE (Commerce)
               │
               └─ creates ProductOrder
                          │
                          │ CreateOpportunityFromProductOrder (explicit command only)
                          ▼
                  COMMERCIAL SYSTEM
                  creates Opportunity
                          │
                          │ (then normal Opportunity → Matter path if applicable)
```

---

## 4. Vocabulary — domain-qualified field names

This table is the naming authority. All code, schemas, events, and analytics must use these names.

| Concept | Canonical field name | Domain | Allowed values |
|---|---|---|---|
| Commercial pipeline stage | `opportunityPipelineStage` | Commercial | `Intake`, `Onboarding` |
| Matter lifecycle stage | `matterLifecycleStage` | Matter | `Onboarding`, `Opening`, `Maintenance`, `Closing`, `Archived` |
| Matter state (canonical, cross-domain) | `matterState` | Matter | `Pending` (at creation); others defined by Matter domain |
| Matter internal admin state | `adminState` | Matter (internal) | See `AdminState` enum in OpenAPI spec |
| Matter type | `matterType` | Matter | String classification; `PRODUCT` is explicitly excluded |
| Product order status | `productOrderStatus` | Commerce | `Created`, `Pending Payment`, `Paid`, `Fulfillment Pending`, `Fulfilled`, `Cancelled`, `Refunded` |

**Naming rule:** Never use bare shared labels (`onboarding`, `intake`, `status`) in cross-domain code,
events, or analytics without domain qualification.

**Vocabulary mapping — current implementation to spec:**

| Current API field | Spec-canonical name | Note |
|---|---|---|
| `stage` on CaseInstance | `matterLifecycleStage` | Same values; "Open" → "Maintenance" is a pending rename |
| `adminState` on CaseInstance | `adminState` (internal) / `matterState` (canonical) | `adminState` is operational granularity; `matterState` is cross-domain projection |
| `status` on CaseInstance | Partially maps to `matterState` | `open` ≈ Active/Pending; `closed` ≈ Closed |
| `caseDefinitionId` | Relates to `matterType` | `matterType` is the classification field; `caseDefinitionId` is the template reference |

---

## 5. Role semantics

| Role | Definition | Relationship to other roles |
|---|---|---|
| **Contact** | Commercial identity in the commercial domain | May become a Client; may or may not become a Customer |
| **Customer** | Party that has completed a product purchase in the Commerce domain | Not automatically a Contact or Client |
| **Client** | Party associated with an actual Matter/engagement in the Matter domain | Not automatically a Customer |

These are distinct roles. The same person may hold multiple roles but the roles are **not automatically
equivalent** and must not be treated as interchangeable in code or data models.

---

## 6. Conversion paths — what can turn into what

| From | To | Command | Default? | Requires explicit action? |
|---|---|---|---|---|
| Opportunity | Matter | `CreateMatterFromOpportunity` | No | Yes — approved intake path |
| ProductOrder | Opportunity | `CreateOpportunityFromProductOrder` | No | Yes — engagement decision |
| ProductOrder | Matter (direct) | Not allowed | No | Not allowed by default |
| ProductOrder | linked to existing Matter | `LinkProductOrderToMatter` | No | Yes — explicit association |

**Cardinality defaults:**
- One Opportunity → zero or one primary Matter (multi-matter requires explicit approved pattern)
- One ProductOrder → zero or one Opportunity (separate engagement decision required)
- ProductOrder does not create a Matter directly

---

## 7. Reporting boundaries

### Allowed metric families

**Commercial metrics** (commercial system):
- Contact counts, Opportunity counts, pipeline stage counts
- Qualification rates, pipeline velocity, conversion rates (Opportunity → Matter)

**Matter metrics** (ll-task-tracker):
- Matters by `matterLifecycleStage`, by `matterState`, by `matterType`
- Readiness SLAs, open/closed volumes, health distributions

**Commerce metrics** (commerce backend / ll-corporate):
- ProductOrder counts by `productOrderStatus`, revenue, payment outcomes
- Fulfillment outcomes, entitlement/access metrics

### Approved cross-domain joins (explicit linkage only)

| Join | Linking field |
|---|---|
| Opportunity ↔ Matter | `originOpportunityId` on Matter / `matterId` on Opportunity |
| ProductOrder ↔ Opportunity | `originOpportunityId` on ProductOrder / `originProductOrderId` on Opportunity |
| ProductOrder ↔ Matter | `originProductOrderId` on Matter (only where explicitly linked) |

### Disallowed reporting patterns

- Counting Opportunities as Matters
- Counting ProductOrders as Matters
- Using a single unqualified "onboarding" metric across domains
- Mixing `opportunityPipelineStage` and `matterLifecycleStage` in one status dimension

---

## 8. Implementation artifact index

| Artifact | Location | Purpose |
|---|---|---|
| Domain vocabulary (OpenAPI) | `contracts/domain/vocabulary.yaml` | Authoritative enum definitions |
| Bounded object schemas (OpenAPI) | `contracts/domain/objects.yaml` | Contact, Opportunity, ProductOrder schemas |
| Conversion commands/events (OpenAPI) | `contracts/domain/conversions.yaml` | Cross-domain command/event contracts |
| Write permission matrix | `contracts/domain/write-permissions.yaml` | Machine-readable permission rules |
| Case engine API spec | `contracts/openapi/case-engine-api.yaml` | ll-task-tracker REST API including conversion endpoints |
| TypeScript commercial types | `src/lib/contracts/commercial.ts` (ll-corporate) | Contact, Opportunity types |
| TypeScript commerce types | `src/lib/contracts/commerce.ts` (ll-corporate) | ProductOrder types |
| TypeScript conversion types | `src/lib/contracts/conversions.ts` (ll-corporate) | Command/event types |
| TypeScript identity types | `src/lib/contracts/identity.ts` (ll-corporate) | Party identity and linkage |
| Dependency matrix | `contracts/dependency-matrix.yaml` | Cross-system dependency graph |
