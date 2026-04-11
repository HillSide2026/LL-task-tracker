# LL Task Tracker

Levine LLP's matter platform for internal matter opening, lawyer review, client/external wait tracking, and ongoing matter maintenance.

This repository has been re-homed as `LL-task-tracker`. It currently combines a Levine LLP matter-management layer with an upstream-derived case management and workflow platform. The visible product surface is the Levine LLP matter portal; some lower-level service names, Java packages, environment variable names, and policy paths still retain upstream WKS identifiers while the codebase is being separated safely.

## Current Product Surface

- Matter portal: `https://matters.levinellp.ca`
- Deployment entrypoint: `deployments/levinellp`
- Primary UI app: `apps/react/case-portal`
- Primary backend API: `apps/java/services/case-engine-rest-api`
- Case engine library: `apps/java/libraries/case-engine`
- Matter seed data: `apps/java/services/demo-data-loader/data`
- Authorization policy: `opa/wks_policy_rules.rego`

## What This Repo Runs

The Levine LLP matter platform uses:

- React for the matter portal.
- Spring Boot services for case, form, record, storage, and workflow APIs.
- Camunda 7 for the current Levine deployment workflow runtime.
- MongoDB for tenant matter data.
- Keycloak for identity and realm roles.
- OPA for API authorization decisions.
- MinIO-compatible object storage for matter documents.
- Traefik for the Levine deployment edge router.

Camunda 8, Novu, websocket, and email-to-case support still exist in the repository as upstream-derived platform capabilities. They should be reviewed before being treated as part of the Levine production operating model.

## Levine LLP Matter Layer

The matter-specific layer is centered around the `matter-admin-opening-control` case definition and lifecycle.

It includes:

- Intake review.
- Engagement and conflicts readiness.
- Responsible lawyer assignment.
- Lawyer opening review.
- Client and external waiting states.
- Matter activation.
- Maintenance follow-up and exception queues.

Important implementation areas:

- `apps/java/libraries/case-engine/src/main/java/com/wks/caseengine/cases/instance/admin`
- `apps/java/libraries/case-engine/src/main/java/com/wks/caseengine/cases/instance/command/TransitionCaseAdminCmd.java`
- `apps/react/case-portal/src/common/adminLifecycle.js`
- `apps/react/case-portal/src/routes/MainRoutes.js`
- `apps/react/case-portal/src/views/dashboard`
- `apps/java/services/demo-data-loader/data/camunda7/levinellp`
- `apps/java/services/demo-data-loader/data/mongodb/mongo-levinellp-matter-collections.json`

## Deployment

The Levine deployment guide is the source of truth for operator setup:

```sh
deployments/levinellp/README.md
```

Start the Levine stack from the repository root:

```sh
docker compose \
  --env-file deployments/levinellp/.env \
  -f deployments/levinellp/docker-compose.camunda7.yml \
  up -d --build
```

Bootstrap the initial realm, roles, demo user, matter definitions, forms, queues, and Camunda process:

```sh
docker compose \
  --env-file deployments/levinellp/.env \
  -f deployments/levinellp/docker-compose.camunda7.yml \
  --profile bootstrap \
  run --rm demo-data-loader
```

## Local Development

The root `docker-compose*.yaml` files and `scripts/` helpers are retained for local developer stacks. They still include upstream-compatible service and environment names. Use the Levine deployment folder for deployment-facing instructions.

For local Java builds:

```sh
cd apps/java
mvn -DskipTests package
```

For the React matter portal:

```sh
cd apps/react/case-portal
npm install
npm run start
```

## Auth Model

The matter portal currently uses these Levine operational roles:

- `ops_admin`
- `ops_manager`
- `lawyer_user`

The underlying platform also contains generic client, management, and email-to-case roles. Those are still used by the inherited authorization policy and should be rationalized in a later auth cleanup.

## Upstream Derivation

This codebase remains substantially derived from WKS Platform internals. See [UPSTREAM.md](UPSTREAM.md) for provenance, retained identifiers, and deferred cleanup areas.

The original MIT license notice is preserved in [LICENSE](LICENSE).

## Cleanup Boundary

Current cleanup work should stay focused on branding, operator clarity, seed-data ownership, and matter lifecycle isolation. Avoid broad Java package renames such as `com.wks`, deep service ID changes, or monorepo restructuring until the platform/product boundary is stable.
