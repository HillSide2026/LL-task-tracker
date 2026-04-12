# Accounts Lifecycle System — Backlog Plan

> **Status:** Backlog — not yet implemented
> **Added:** 2026-04-12
> **Repo:** LL-task-tracker (canonical owner)
> **Related consumer:** ll-corporate (bounded exposure only)

---

## Canonical Doctrine Statement

The Accounts Lifecycle System is a canonical financial-control lifecycle attached to Matters in ll-task-tracker. It is implemented as an additive, prefixed parallel lifecycle on CaseInstance, following the established admin lifecycle pattern. The system is matter-type-aware by design and must explicitly distinguish FLAT_FEE, HOURLY_CREDIT_CARD, HOURLY_RETAINER, SUBSCRIPTION_CREDIT_CARD, SUBSCRIPTION_RETAINER, and COUNSEL. Matter Type is mandatory and governs Accounts policy, activation prerequisites, health rules, exception handling, and closure readiness. COUNSEL is a first-class Matter Type representing work performed for another law firm in relation to that firm's client, with instructing-firm billing responsibility treated as a core accounts concern. No portal or commercial surface may redefine Accounts lifecycle semantics, and all changes must preserve the canonical ownership and additive architecture of the existing system.

---

## 1. Purpose

The Accounts Lifecycle System is a parallel matter lifecycle subsystem that manages the financial readiness, billing posture, payment exceptions, reconciliation, and closure-readiness of a Matter.

It is **not** a general accounting package.
It is **not** a generic invoice tracker.
It is **not** a replacement for the Admin Lifecycle System.

It is a controlled operational lifecycle that answers:

- Is this Matter financially ready to proceed?
- Is the required billing structure in place?
- Is payment current, at risk, or in exception?
- Are retainer and trust requirements satisfied where applicable?
- Can the Matter continue, be delivered, or be closed from an accounts standpoint?

This system must follow the same architectural doctrine as the Admin Lifecycle System: a bounded lifecycle, explicit states, explicit transitions, health evaluation, audit events, access control, and additive fields on the same CaseInstance record.

---

## 2. Governing Principle

It is **100% mandatory** that the system recognize and manage the distinction between these Matter Types:

- `FLAT_FEE`
- `HOURLY_CREDIT_CARD`
- `HOURLY_RETAINER`
- `SUBSCRIPTION_CREDIT_CARD`
- `SUBSCRIPTION_RETAINER`
- `COUNSEL`

This is not optional metadata. This is a first-class control dimension.

The Accounts Lifecycle System must be matter-type-aware by design, because each Matter Type has materially different:

- activation prerequisites
- payment structure
- trust/retainer requirements
- recurring billing behavior
- health signals
- exception paths
- closure requirements

---

## 3. COUNSEL Definition

For this system, **COUNSEL** means:

> Levine LLP performs work for another law firm, in relation to that other firm's client.

COUNSEL is a distinct Matter Type because the defining feature is not fee cadence alone, but the engagement relationship and billing responsibility.

For COUNSEL matters:
- the instructing party is another firm
- billing responsibility typically sits with the instructing firm
- the underlying client may differ from the billing party
- portal visibility may need additional constraints
- collections and payment follow-up target the instructing firm by default

COUNSEL must **not** be collapsed into subscription, flat fee, or hourly as a mere billing variation.

---

## 4. Scope Boundary

### In scope

The Accounts Lifecycle System governs:
- billing setup readiness
- payment method readiness
- retainer/trust readiness
- invoice/payment posture
- recurring charge posture
- collections / payment exception posture
- final reconciliation readiness
- account closure readiness

### Out of scope

The Accounts Lifecycle System does not replace:
- external payment processor logic
- full accounting ledger behavior
- tax engine behavior
- law society accounting package behavior
- CRM pipeline behavior
- admin matter lifecycle behavior
- legal work delivery workflow behavior

It may reference or reflect those systems but does not subsume them.

---

## 5. Architecture Fit

The existing repository pattern supports parallel lifecycles on the same MongoDB CaseInstance document, each with prefixed fields, transition commands, support classes, access support, events, and REST endpoints.

The Accounts Lifecycle System must follow that exact doctrine:
- additive only
- no rewrite of Admin lifecycle
- no shared ambiguous fields
- no unprefixed financial lifecycle state
- no duplication of canonical Matter lifecycle semantics

All Accounts fields must be namespaced with an `accounts` prefix.

---

## 6. Canonical Ownership

### ll-task-tracker (this repo)

Canonical owner of:
- Matter identity
- Matter Type
- Accounts lifecycle state
- Accounts transitions
- Accounts health evaluation
- Accounts audit events
- Accounts guard logic
- Accounts closure readiness

### ll-corporate

Bounded consumer of:
- client-safe payment/setup views
- client-safe invoice/payment prompts
- client-safe subscription/payment method steps
- client-safe status exposure where allowed

ll-corporate does **not** own:
- accounts lifecycle semantics
- accounts states
- accounts transition rules
- accounts health evaluation

---

## 7. Core Domain Model

**Matter** — canonical admin domain object.

Required additions:
- `matterType`
- `billingPartyModel`
- `billingMode`

**Accounts lifecycle namespace** — prefixed fields on CaseInstance describing the financial lifecycle posture.

**Supporting party references** — needed because not all Matters bill the same economic actor (direct client, instructing firm, or third-party payor).

---

## 8. Matter Type Model

### Required enum: `MatterType`

```
FLAT_FEE
HOURLY_CREDIT_CARD
HOURLY_RETAINER
SUBSCRIPTION_CREDIT_CARD
SUBSCRIPTION_RETAINER
COUNSEL
```

### Design rule

`matterType` is mandatory before Accounts activation. It must not be inferred late, must not remain freeform, and must be a canonical backend-controlled enum.

---

## 9. Billing Party Model

Required because COUNSEL changes who is responsible for payment.

### Required enum: `BillingPartyModel`

```
DIRECT_CLIENT
INSTRUCTING_FIRM
THIRD_PARTY_PAYOR
```

**Rule:** For most non-COUNSEL matters, default is `DIRECT_CLIENT`. For COUNSEL, default is `INSTRUCTING_FIRM`.

---

## 10. Billing Mode

`matterType` governs the relationship model. `billingMode` governs the financial mechanics. For non-COUNSEL matters, these usually align directly. For COUNSEL, `billingMode` must be explicitly set during setup.

### Required enum: `BillingMode`

```
FLAT_FEE
HOURLY_CARD
HOURLY_RETAINER
SUBSCRIPTION_CARD
SUBSCRIPTION_RETAINER
COUNSEL_HOURLY
COUNSEL_RETAINER
COUNSEL_FLAT_FEE
COUNSEL_RECURRING
```

---

## 11. Accounts Policy Framework

The Accounts Lifecycle System is implemented as **one lifecycle framework + matter-type-specific policy profiles**.

Shared across all profiles:
- stages
- states
- events
- transition endpoint pattern
- audit trail pattern
- health support pattern
- access support pattern

Driven by profile:
- transition guards
- required fields
- health rules
- allowed states
- closure conditions

### Required enum: `AccountsPolicyProfile`

```
FLAT_FEE_PROFILE
HOURLY_CREDIT_CARD_PROFILE
HOURLY_RETAINER_PROFILE
SUBSCRIPTION_CREDIT_CARD_PROFILE
SUBSCRIPTION_RETAINER_PROFILE
COUNSEL_PROFILE
```

---

## 12. Accounts Stages

### Required enum: `AccountsLifecycleStage`

| Stage | Meaning |
|---|---|
| `SETUP` | Financial structure is not yet ready for full accounts activation |
| `ACTIVE` | Financial structure is in force and the Matter is financially current or routinely operating |
| `EXCEPTION` | The Matter has a financial issue requiring non-routine intervention |
| `CLOSED` | Accounts obligations are reconciled and financially closed |

---

## 13. Accounts States

### Required enum: `AccountsState`

```
AWAITING_BILLING_SETUP
AWAITING_PAYMENT_METHOD
AWAITING_RETAINER_FUNDING
AWAITING_ENGAGEMENT_TERMS
READY_FOR_ACTIVATION
ACTIVE_CURRENT
INVOICE_ISSUED
PAYMENT_PENDING
TRUST_DRAWDOWN_PENDING
PAST_DUE
IN_COLLECTIONS
PAYMENT_ARRANGEMENT_ACTIVE
FINAL_RECONCILIATION_PENDING
CLOSED
```

### State-to-stage mapping

| Stage | States |
|---|---|
| `SETUP` | `AWAITING_BILLING_SETUP`, `AWAITING_PAYMENT_METHOD`, `AWAITING_RETAINER_FUNDING`, `AWAITING_ENGAGEMENT_TERMS`, `READY_FOR_ACTIVATION` |
| `ACTIVE` | `ACTIVE_CURRENT`, `INVOICE_ISSUED`, `PAYMENT_PENDING`, `TRUST_DRAWDOWN_PENDING` |
| `EXCEPTION` | `PAST_DUE`, `IN_COLLECTIONS`, `PAYMENT_ARRANGEMENT_ACTIVE` |
| `CLOSED` | `FINAL_RECONCILIATION_PENDING`, `CLOSED` |

---

## 14. Transition Model

### Required enum: `AccountsTransition`

```
COMPLETE_BILLING_SETUP
AUTHORIZE_PAYMENT_METHOD
RECORD_RETAINER_FUNDED
RECORD_ENGAGEMENT_TERMS_COMPLETE
ACTIVATE_ACCOUNTS
ISSUE_INVOICE
RECORD_PAYMENT_RECEIVED
REQUEST_TRUST_DRAWDOWN
APPROVE_TRUST_DRAWDOWN
FLAG_PAST_DUE
ESCALATE_TO_COLLECTIONS
RECORD_PAYMENT_ARRANGEMENT
RETURN_TO_CURRENT
ISSUE_FINAL_ACCOUNT_STATEMENT
RECONCILE_FINAL_BALANCE
CLOSE_ACCOUNT
```

Not all transitions are valid for all Matter Types. Examples:
- `REQUEST_TRUST_DRAWDOWN` is invalid for `HOURLY_CREDIT_CARD`
- `AUTHORIZE_PAYMENT_METHOD` is not sufficient alone for `HOURLY_RETAINER`
- `RECORD_RETAINER_FUNDED` is required for retainer-based profiles
- `RECORD_ENGAGEMENT_TERMS_COMPLETE` is especially relevant for `COUNSEL`

---

## 15. Policy Matrix by Matter Type

### A. FLAT_FEE

**Financial characteristics:** fixed scope fee, one-time or milestone billing, no trust retainer required by default, no recurring subscription logic.

**Required setup:** flat fee amount, payment rule, billing contact, payment method or invoice arrangement, engagement terms completed.

**Valid primary states:** `AWAITING_BILLING_SETUP`, `AWAITING_PAYMENT_METHOD`, `READY_FOR_ACTIVATION`, `ACTIVE_CURRENT`, `INVOICE_ISSUED`, `PAYMENT_PENDING`, `PAST_DUE`, `FINAL_RECONCILIATION_PENDING`, `CLOSED`.

**Usually invalid:** `AWAITING_RETAINER_FUNDING`, `TRUST_DRAWDOWN_PENDING`.

**Activation rule:** Cannot activate until flat fee setup and payment path are complete.
**Closure rule:** Cannot close until all fee obligations are resolved.

---

### B. HOURLY_CREDIT_CARD

**Financial characteristics:** time-based billing, card-backed collection, no trust retainer, recurring invoicing may occur but not subscription semantics.

**Required setup:** billing contact, card authorization, rate/billing basis, invoice cadence, engagement terms completed.

**Valid primary states:** `AWAITING_BILLING_SETUP`, `AWAITING_PAYMENT_METHOD`, `READY_FOR_ACTIVATION`, `ACTIVE_CURRENT`, `INVOICE_ISSUED`, `PAYMENT_PENDING`, `PAST_DUE`, `IN_COLLECTIONS`, `PAYMENT_ARRANGEMENT_ACTIVE`, `FINAL_RECONCILIATION_PENDING`, `CLOSED`.

**Invalid:** `AWAITING_RETAINER_FUNDING`, `TRUST_DRAWDOWN_PENDING`.

**Activation rule:** Cannot activate without a valid authorized card or approved billing exception.
**Health sensitivity:** Repeated card failures move rapidly to amber/red.

---

### C. HOURLY_RETAINER

**Financial characteristics:** time-based billing, trust-funded / retainer-funded, replenishment and trust sufficiency matter.

**Required setup:** retainer amount, retainer minimum, trust account reference, replenishment rule, billing contact, engagement terms completed.

**Valid primary states:** `AWAITING_BILLING_SETUP`, `AWAITING_RETAINER_FUNDING`, `READY_FOR_ACTIVATION`, `ACTIVE_CURRENT`, `TRUST_DRAWDOWN_PENDING`, `INVOICE_ISSUED`, `PAST_DUE`, `IN_COLLECTIONS`, `FINAL_RECONCILIATION_PENDING`, `CLOSED`.

**Invalid:** `AWAITING_PAYMENT_METHOD` as sole route to readiness (unless an auxiliary card is also collected).

**Activation rule:** Cannot activate until minimum retainer is funded.
**Closure rule:** Cannot close until trust is reconciled and balances are resolved.

---

### D. SUBSCRIPTION_CREDIT_CARD

**Financial characteristics:** recurring fixed charge, card-backed autopay, coverage tied to recurring payment posture.

**Required setup:** plan, cadence, card authorization, start date, renewal/cancellation rule, engagement terms completed.

**Valid primary states:** `AWAITING_BILLING_SETUP`, `AWAITING_PAYMENT_METHOD`, `READY_FOR_ACTIVATION`, `ACTIVE_CURRENT`, `PAYMENT_PENDING`, `PAST_DUE`, `PAYMENT_ARRANGEMENT_ACTIVE`, `FINAL_RECONCILIATION_PENDING`, `CLOSED`.

**Usually not used as primary control:** `INVOICE_ISSUED`, `TRUST_DRAWDOWN_PENDING`.

**Activation rule:** Cannot activate without successful initial payment or explicit deferred-start approval.
**Exception rule:** Failed recurring payment moves to grace/exception path.

---

### E. SUBSCRIPTION_RETAINER

**Financial characteristics:** recurring service relationship, retainer-backed or replenishment-backed, hybrid recurring + trust sufficiency logic.

**Required setup:** plan, cadence, retainer minimum, replenishment rule, trust account reference, engagement terms completed.

**Valid primary states:** `AWAITING_BILLING_SETUP`, `AWAITING_RETAINER_FUNDING`, `READY_FOR_ACTIVATION`, `ACTIVE_CURRENT`, `TRUST_DRAWDOWN_PENDING`, `PAST_DUE`, `PAYMENT_ARRANGEMENT_ACTIVE`, `FINAL_RECONCILIATION_PENDING`, `CLOSED`.

**Activation rule:** Cannot activate until retainer minimum is funded and recurring settings are complete.
**Exception rule:** Falling below threshold or failed replenishment moves to exception posture.

---

### F. COUNSEL

**Financial characteristics:** another firm engages Levine LLP regarding that firm's client; billing counterparty is normally the instructing firm; billing mode must be chosen explicitly; may be hourly, retainer, flat fee, or recurring.

**Required setup:** instructing firm identified, billing party confirmed, billing contact set, engagement terms completed, counsel billing mode set, any mode-specific prerequisites completed.

**Required counsel-specific fields:** `instructingFirmId`, `instructingFirmName`, `underlyingClientRef` or permitted label, `counselBillingMode`.

**Valid primary states:** `AWAITING_BILLING_SETUP`, `AWAITING_ENGAGEMENT_TERMS`, `AWAITING_PAYMENT_METHOD`, `AWAITING_RETAINER_FUNDING`, `READY_FOR_ACTIVATION`, `ACTIVE_CURRENT`, `INVOICE_ISSUED`, `PAYMENT_PENDING`, `PAST_DUE`, `PAYMENT_ARRANGEMENT_ACTIVE`, `FINAL_RECONCILIATION_PENDING`, `CLOSED`.

**Activation rule:** Cannot activate until instructing firm, billing responsibility, and counsel billing mode are complete.
**Default billing party:** `INSTRUCTING_FIRM`.
**Collections rule:** Collections and follow-up target the instructing firm, not the underlying client.

---

## 16. Accounts Health Model

### Required enum: `AccountsHealth`

```
GREEN
AMBER
RED
```

### Required enum: `AccountsHealthReasonCode`

```
MISSING_BILLING_SETUP
MISSING_PAYMENT_METHOD
MISSING_RETAINER
LOW_TRUST_BALANCE
PAST_DUE_INVOICE
REPEATED_PAYMENT_FAILURE
COLLECTIONS_ACTIVE
MISSING_BILLING_CONTACT
MISSING_INSTRUCTING_FIRM
MISSING_COUNSEL_BILLING_MODE
FINAL_RECONCILIATION_OUTSTANDING
MALFORMED_CONFIGURATION
```

### Example health rules

**All types:**
- Missing required setup field for profile → `RED`
- Stale setup longer than configured threshold → `AMBER`
- Final reconciliation outstanding while closing → `AMBER` or `RED`

**Retainer profiles:**
- Trust below minimum threshold → `AMBER`
- Trust critically below threshold → `RED`

**Card profiles:**
- Payment method missing → `RED`
- Recurring payment failure → `AMBER`
- Repeated failure / overdue beyond threshold → `RED`

**COUNSEL:**
- Instructing firm missing → `RED`
- Counsel billing mode missing → `RED`
- Billing party mismatch → `RED`

---

## 17. Staleness Model

Required fields:
- `accountsStaleSince`
- `accountsHealthEvaluatedAt`

Example stale rules:
- Setup incomplete beyond configured number of days → `AMBER`
- Past due without follow-up beyond configured number of days → `RED`
- Final reconciliation pending too long → `AMBER`

---

## 18. Malformed Case Model

Required field: `accountsMalformedCase`

Set `true` where the Matter configuration is structurally invalid for its profile. Malformed cases are non-activatable.

Examples:
- `HOURLY_RETAINER` with no retainer requirement configured
- `SUBSCRIPTION_CREDIT_CARD` with no subscription cadence
- `COUNSEL` with no instructing firm
- `COUNSEL` with no billing mode
- `FLAT_FEE` with no fee amount

---

## 19. CaseInstance Field Additions

All fields below are additive and prefixed. No existing fields are modified or removed.

**Core accounts lifecycle fields:**
```
accountsStage
accountsState
accountsHealth
accountsHealthReasonCodes
accountsHealthEvaluatedAt
accountsStaleSince
accountsMalformedCase
accountsEvents
```

**Ownership and responsibility fields:**
```
accountsOwnerId
accountsOwnerName
billingContactId
billingContactName
billingPartyModel
```

**Policy/profile fields:**
```
matterType
billingMode
accountsProfile
requiresRetainer
requiresAuthorizedCard
trustEnabled
recurringBillingEnabled
```

**Financial setup fields:**
```
flatFeeAmount
invoiceCadence
subscriptionPlanCode
subscriptionStartDate
subscriptionRenewalDate
paymentMethodType
paymentMethodRef
retainerAmount
retainerMinimum
retainerFundedAt
replenishmentRule
trustAccountRef
trustBalanceAsOf
lastInvoiceId
lastInvoiceIssuedAt
lastInvoiceDueAt
lastPaymentReceivedAt
pastDueSince
collectionsEscalatedAt
finalReconciliationDueAt
accountClosedAt
```

**COUNSEL-specific fields:**
```
instructingFirmId
instructingFirmName
underlyingClientRef
counselBillingMode
```

---

## 20. Event Model

### Required enum: `AccountsEventType`

```
BILLING_SETUP_COMPLETED
PAYMENT_METHOD_AUTHORIZED
RETAINER_FUNDED
ENGAGEMENT_TERMS_COMPLETED
ACCOUNTS_ACTIVATED
INVOICE_ISSUED
PAYMENT_RECEIVED
TRUST_DRAWDOWN_REQUESTED
TRUST_DRAWDOWN_APPROVED
ACCOUNT_FLAGGED_PAST_DUE
COLLECTIONS_ESCALATED
PAYMENT_ARRANGEMENT_RECORDED
ACCOUNT_RETURNED_TO_CURRENT
FINAL_ACCOUNT_STATEMENT_ISSUED
FINAL_RECONCILIATION_COMPLETED
ACCOUNT_CLOSED
HEALTH_REEVALUATED
```

### Required event model fields

- timestamp
- actor id
- actor name
- transition/event type
- previous state
- new state
- note / reason
- relevant financial references

---

## 21. Access Control

A dedicated `AccountsLifecycleAccessSupport` must be created.

**Recommended roles:**
- `billing_user`
- `accounts_manager`
- `ops_admin`
- `ops_manager`
- `lawyer_user` — limited visibility only

**Access principles:**
- Only authorized accounts roles can perform financial transitions
- Lawyers may view some status but not necessarily perform collections or reconciliation actions
- Client portal users never directly mutate canonical accounts states
- COUNSEL matters may require narrower visibility rules due to instructing-firm sensitivity

---

## 22. Transition Guards

Guards must be profile-specific.

**`ACTIVATE_ACCOUNTS` is guarded by:**
- `matterType` present
- Required setup complete for profile
- No malformed configuration
- No mandatory missing counterparties
- No unresolved blocking financial prerequisites

**`REQUEST_TRUST_DRAWDOWN` is guarded by:**
- Trust-enabled profile only
- Retainer-funded profile only
- Trust account reference present

**`ESCALATE_TO_COLLECTIONS` is guarded by:**
- Past due threshold met
- Not already in collections
- Authorized role

**`CLOSE_ACCOUNT` is guarded by:**
- Final balance resolved
- Final reconciliation complete
- No open blocking payment exception

---

## 23. Cross-Lifecycle Coordination

Use domain events through Spring event publication from transition commands. Use direct guard coupling only where policy must be absolute.

| Trigger | Action |
|---|---|
| Admin matter reaches onboarding / readiness milestones | Accounts may require setup initiation |
| Accounts → `ACTIVE_CURRENT` | Admin receives "financially ready" signal |
| Accounts → `EXCEPTION` | Admin receives "financially blocked" signal |
| Accounts → `CLOSED` | Admin receives "reconciliation complete" signal |
| Accounts state = exception | Delivery system may block `markReadyToDeliver` under configured policy |
| Admin enters closing stage | Accounts must have issued final statement before admin fully closes |

---

## 24. REST API Plan

**Required endpoints:**
```
POST /case/{businessKey}/accounts/transition
GET  /case/{businessKey}/accounts
GET  /case/{businessKey}/accounts/history
```

**Optional:**
```
POST /case/{businessKey}/accounts/health/recalculate
```

**`AccountsTransitionRequest` should include:**
- requested transition
- actor context
- note / reason
- relevant financial values as needed by transition
- optimistic validation inputs if required

---

## 25. OpenAPI / Contract Requirements

Additive only. Required schema additions:
- `MatterType`
- `BillingPartyModel`
- `BillingMode`
- `AccountsLifecycleStage`
- `AccountsState`
- `AccountsHealth`
- `AccountsHealthReasonCode`
- COUNSEL-specific fields
- Payment/trust fields as bounded DTOs

These contracts must be reflected in ll-corporate TypeScript types. ll-corporate consumes but does not author Accounts semantics.

---

## 26. UI / Surface Plan

### Internal UI

Required views:
- Accounts summary panel on Matter
- Financial setup checklist
- State and health banner
- Transition action controls
- Event history
- Exception queue
- Reconciliation queue

### Client portal UI

Bounded exposure only:
- Payment method setup
- Invoice/payment prompts
- Subscription payment status where applicable

For COUNSEL matters, portal exposure must be explicitly controlled because the economic actor may be another firm rather than the underlying client.

---

## 27. Reporting Requirements

Must distinguish:
- Matter counts by `matterType`
- Accounts state distribution
- Exception/past due counts
- Retainer sufficiency risk
- Recurring payment failures
- COUNSEL matters billed to instructing firms
- Closure blocked by accounts

Must **not** mix:
- Matter lifecycle reporting
- Commercial opportunity pipeline reporting
- Non-matter ProductOrder reporting

---

## 28. Non-Negotiable Rules

1. `matterType` is mandatory before Accounts activation.
2. Accounts logic must branch by policy profile.
3. `COUNSEL` is a first-class Matter Type.
4. `COUNSEL` requires instructing-firm configuration.
5. Product purchases remain outside the Matter domain.
6. Portal surfaces do not define accounts states.
7. Accounts does not redefine Matter lifecycle.
8. All Accounts fields are prefixed and additive.
9. Malformed profile configurations are non-activatable.
10. Final closure requires accounts reconciliation completion.

---

## 29. Required Java Backend Artifacts

### Enums / models
- `AccountsLifecycleStage`
- `AccountsState`
- `AccountsTransition`
- `AccountsEventType`
- `AccountsHealth`
- `AccountsHealthReasonCode`
- `MatterType`
- `BillingPartyModel`
- `BillingMode`
- `AccountsPolicyProfile`

### Support classes
- `AccountsLifecycleSupport`
- `AccountsLifecycleAccessSupport`
- `AccountsPolicySupport` (policy resolver)
- `AccountsHealthSupport`

### Transport / domain objects
- `AccountsTransitionRequest`
- `AccountsEvent`
- `AccountsControlEvaluation`
- `AccountsLifecycleException`

### Command
- `TransitionCaseAccountsCmd`

### REST
- `CaseAccountsController` or additive controller methods

### Persistence impact
- Additive `CaseInstance` fields only

---

## 30. Policy Resolution Design

Given a `MatterType` and, where needed, `BillingMode`, the policy resolver must produce:
- profile
- required setup fields
- valid states
- valid transitions
- blocking guards
- health evaluation rules
- closure conditions

**Recommended implementation:** A centralized `AccountsPolicy resolvePolicy(CaseInstance)` resolver — not scattered conditionals across controllers. All transition and health logic depends on that resolved policy. This prevents lifecycle drift and inconsistent branching.

---

## 31. Acceptance Criteria

The implementation is acceptable only if:

- [ ] A Matter cannot activate Accounts without a valid `matterType`
- [ ] Each mandatory Matter Type follows a distinct policy path
- [ ] `COUNSEL` cannot activate without instructing firm and billing configuration
- [ ] Retainer-based types enforce retainer/trust prerequisites
- [ ] Card-based types enforce payment method prerequisites
- [ ] Subscription types enforce recurring billing prerequisites
- [ ] Health evaluation reflects profile-specific rules
- [ ] `accountsMalformedCase` prevents activation where required configuration is missing
- [ ] All transitions are audited in `accountsEvents`
- [ ] No changes break or rewrite the Admin lifecycle pattern
- [ ] All changes are additive on `CaseInstance`
- [ ] API contracts are explicit and typed
- [ ] ll-corporate consumes but does not author Accounts semantics

---

## 32. Sequencing Plan

| Phase | Scope |
|---|---|
| **Phase 1 — Canonical model** | Enums, CaseInstance fields, policy resolver, no-op serialization coverage |
| **Phase 2 — Accounts framework** | Stages, states, transition command, access support, event model, REST endpoint |
| **Phase 3 — Matter-type policy** | Flat fee, hourly CC, hourly retainer, subscription CC, subscription retainer, counsel profiles |
| **Phase 4 — Health and malformed evaluation** | Health rules, reason codes, stale-state logic, malformed configuration detection |
| **Phase 5 — Cross-lifecycle coordination** | Event publication, admin/accounts coordination, optional delivery guards |
| **Phase 6 — Contracts and UI** | OpenAPI, TypeScript contracts, internal UI surfaces, bounded portal exposure |
| **Phase 7 — Reporting and backlog hardening** | Dashboards, exception queues, reconciliation queue, metrics by matter type |

---

## 33. Verified Facts, Inferred Assumptions, Unknowns

### Verified facts
- The existing lifecycle pattern supports additive, prefixed parallel lifecycles on the same CaseInstance document
- The admin implementation uses separate support/access/command/event patterns and is intended to be repeatable
- The Accounts system must distinguish the six mandatory Matter Types
- COUNSEL means work performed for another firm and that firm's client

### Inferred assumptions
- COUNSEL usually bills the instructing firm, not the underlying client
- Trust logic is required for retainer-based profiles
- Subscription logic must be distinct from ordinary invoicing
- Portal exposure for COUNSEL must be more constrained than standard direct-client matters

### Unknowns to resolve during detailed implementation
- Exact trust accounting integration depth
- Exact payment processor integration
- Whether card failure retry/grace windows are firm-configurable
- Whether counsel portal access exists at all in some matters
- Whether invoice generation is internal, external, or synchronized from another system
- Exact role mapping in Keycloak for billing personnel

---

## 34. Codex Backlog Epics

When converting to tracked backlog, create epics in this order:

1. **Accounts Canonical Model** — enums, `matterType`, `billingMode`, `billingPartyModel`, policy profiles, CaseInstance field additions
2. **Accounts Lifecycle Framework** — stages, states, transitions, support class, access support, event model, exception class
3. **Matter Type Policy Engine** — `AccountsPolicySupport` resolver, per-profile guard logic, required-field validation
4. **Counsel Profile Support** — instructing firm model, counsel billing mode, billing party override, visibility constraints
5. **Accounts Health and Malformed Evaluation** — `AccountsHealthSupport`, reason codes, staleness logic, malformed detection
6. **Accounts Transition API** — `TransitionCaseAccountsCmd`, REST controller, request/response contract
7. **Cross-Lifecycle Coordination** — Spring event publication, admin/accounts signals, optional delivery guard
8. **Internal Accounts UI** — summary panel, setup checklist, health banner, action controls, event history
9. **Portal Accounts Exposure** — bounded client views, payment method setup, invoice prompts, COUNSEL visibility gate
10. **Reporting and Operational Queues** — exception queue, reconciliation queue, matter-type metrics, closure-blocked view

Each epic decomposes into: backend model tasks, command/guard tasks, API tasks, contract tasks, UI tasks, acceptance-test tasks.
