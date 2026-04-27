# Levine Admin Lifecycle

This document is the canonical lifecycle specification for the Levine LLP
`matter-admin-opening-control` matter flow.

## Purpose

The Admin Lifecycle governs the operational state of a Levine matter from
intake through opening, maintenance, closing, and archival. It is the canonical
source for:

- stage membership
- state transitions
- stage completion rules
- queue ownership expectations

## Canonical Stages

The Admin Lifecycle contains five stages:

1. `Onboarding`
2. `Opening`
3. `Maintenance`
4. `Closing`
5. `Archived`

## Stage Membership

### `Onboarding`

- `Intake Review`
- `Awaiting Engagement`

### `Opening`

- `Ready to Open`
- `Ready for Lawyer`
- `Waiting on Client`
- `Opened`

### `Maintenance`

- `Active`
- `Maintenance Lawyer Review`
- `Maintenance Client Wait`
- `Waiting on External`

### `Closing`

- `Closing Review`
- `Closed`

### `Archived`

- `Archived`

## Completion Rules

Stage completion is defined by the canonical handoff point below. A completion
point may be either:

- the first state of the next stage, or
- the terminal completion state of the current stage

The canonical completion rules are:

- `Onboarding` completes when the matter enters `Ready to Open`.
- `Opening` completes when the matter enters `Opened`.
- `Maintenance` completes when the matter enters `Closing Review`.
- `Closing` completes when the matter enters `Closed`.
- `Archived` is terminal and completes when the matter enters `Archived`.

## Canonical Transition Path

The normal operational path is:

`Intake Review` -> `Awaiting Engagement` -> `Ready to Open` -> `Ready for Lawyer`
-> `Opened` -> `Active` -> `Closing Review` -> `Closed` -> `Archived`

The lifecycle also supports operational wait and review loops:

- `Ready to Open` or `Ready for Lawyer` -> `Waiting on Client`
- `Waiting on Client` -> `Ready to Open` or `Ready for Lawyer`
- `Active` -> `Maintenance Lawyer Review`
- `Maintenance Lawyer Review` -> `Active`
- `Active` -> `Maintenance Client Wait`
- `Maintenance Lawyer Review` -> `Maintenance Client Wait`
- `Maintenance Client Wait` -> `Active` or `Maintenance Lawyer Review`
- `Active` -> `Waiting on External`
- `Maintenance Lawyer Review` -> `Waiting on External`
- `Waiting on External` -> `Active` or `Maintenance Lawyer Review`

## Queue Expectations

Default queue ownership follows the canonical state:

- `Intake Review` -> `matter-admin-intake`
- `Awaiting Engagement` -> `matter-admin-engagement-hold`
- `Ready to Open` -> `matter-admin-ready-to-open`
- `Ready for Lawyer` -> `matter-admin-lawyer-review`
- `Waiting on Client` -> `matter-admin-client-waiting`
- `Opened` -> `matter-admin-open`
- `Active` -> `matter-admin-active`
- `Maintenance Lawyer Review` -> `matter-admin-maintenance-lawyer-review`
- `Maintenance Client Wait` -> `matter-admin-maintenance-client-waiting`
- `Waiting on External` -> `matter-admin-external-waiting`
- `Closing Review` -> `matter-admin-closing-review`
- `Closed` -> `matter-admin-closed`
- `Archived` -> `matter-admin-archived`

## Control Rules

- `Ready to Open` requires both `engagementReceived` and `conflictsCleared`.
- Lawyer review states require a `responsibleLawyerId`.
- Admin lifecycle status, stage, and queue changes must use explicit lifecycle
  transitions rather than generic patching.
- `Closed` and `Archived` do not require a next action.

## Naming Note

User conversations may refer to the last two stages as `Close` and `Archive`.
The current canonical code/seed names remain `Closing` and `Archived`.
