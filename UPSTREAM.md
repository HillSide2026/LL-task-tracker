# Upstream Platform Notice

This repository has been re-homed as `LL-task-tracker` for Levine LLP's matter platform, but it remains substantially derived from WKS Platform.

The WKS-derived layer includes the generic case engine, BPM engine abstractions, Camunda 7 and Camunda 8 clients/workers, storage API, OPA authorization integration, Keycloak seed machinery, React case portal shell, and local development compose stack.

## Retained Upstream Identifiers

The following identifiers are intentionally retained for compatibility during the current cleanup phase:

- Java package namespaces under `com.wks`.
- Maven coordinates under `com.wks`.
- OPA package/path `wks.authz` and `/v1/data/wks/authz/allow`.
- Runtime environment variables such as `WKS_CASE_API_URL`.
- Keycloak client IDs such as `wks-portal`, `wks-external-tasks`, and `wks-email-to-case`.
- Camunda element template IDs under `com.wks.camunda.template`.
- Existing WKS copyright/license notices in source headers and `LICENSE`.

These are implementation identifiers, not the product identity exposed to Levine LLP operators or users.

## Levine LLP Layer

The Levine-specific layer currently includes:

- `deployments/levinellp`.
- The `matter-admin-opening-control` case definition.
- Matter-admin forms, queues, and Camunda processes.
- Admin lifecycle state, health, owner, next-action, and transition logic.
- Portal dashboard routes for matter intake, lawyer review, client wait, external wait, active matters, and maintenance exceptions.
- Roles `ops_admin`, `ops_manager`, and `lawyer_user`.

## Deferred Cleanup

The next structural cleanup phase should review:

- Whether to rename Java packages and Maven coordinates away from `com.wks`.
- Whether to rename OPA packages and deployment policy paths.
- Whether to rename Keycloak client IDs.
- Whether Camunda 8, Novu, websocket, and email-to-case components are active Levine requirements.
- Whether to split the matter lifecycle into a separate module instead of keeping it inside the generic case engine.
- Whether to remove or archive the legacy WKS documentation app under `apps/react/docs`.
