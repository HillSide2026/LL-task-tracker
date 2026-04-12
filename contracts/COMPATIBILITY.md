# Contract Compatibility Policy

This document defines the rules for changing contracts between LL-task-tracker and its consumers (primarily ll-corporate).

## Contract version

Current: **1.0.0**

The contract version covers:
- `contracts/openapi/case-engine-api.yaml` — REST API
- `contracts/events/schemas/*.json` — Kafka event schemas
- `contracts/dependency-matrix.yaml` — declared integration points

## Versioning rules

Contract versions follow [Semantic Versioning](https://semver.org/): `MAJOR.MINOR.PATCH`.

| Change type | Version bump required |
|---|---|
| New optional field on a response | Minor (`1.1.0`) |
| New optional query parameter | Minor |
| New enum value (additive) | Minor |
| New optional field on a request | Minor |
| New endpoint | Minor |
| Remove a field from any response | **Major** (`2.0.0`) |
| Remove or rename an endpoint | **Major** |
| Remove an enum value | **Major** |
| Change a field type | **Major** |
| Add a required field to a request | **Major** |
| Change authentication method | **Major** |
| Bug fix to documented behavior | Patch (`1.0.1`) |

## Deprecation

Before removing a field or endpoint:

1. Mark it as deprecated in the OpenAPI spec (`deprecated: true` + description note).
2. Leave it in place for a minimum of **30 days** (or one production release cycle, whichever is longer).
3. Confirm no active consumer is reading the deprecated field/endpoint before removal.
4. Treat removal as a breaking change (major bump).

## Change classification

Every PR that touches files under `contracts/` must declare one of:

| Class | Meaning |
|---|---|
| `internal-only` | No consumer impact. No cross-repo coordination needed. |
| `backward-compatible` | Additive change. Minor version bump. No consumer action required. |
| `breaking` | Breaking change. Major version bump. Both repos must coordinate rollout. |

Use the PR template to declare the class.

## Rollout model

### Backward-compatible changes

1. Bump minor version in `contracts/dependency-matrix.yaml`.
2. Deploy LL-task-tracker first.
3. ll-corporate picks up new fields when it is ready.
4. No feature flag required.

### Breaking changes

1. Bump major version in `contracts/dependency-matrix.yaml`.
2. Update `contracts/openapi/case-engine-api.yaml`.
3. Deploy LL-task-tracker with **dual-support**: serve both old and new contract simultaneously, gated by feature flag if necessary.
4. Update ll-corporate to consume the new contract.
5. After ll-corporate is stable on the new contract, remove the old contract code from LL-task-tracker.
6. Remove feature flag.

## CI enforcement

When any file under `contracts/` changes on a PR:

- The `contract-check` workflow in ll-corporate is triggered.
- ll-corporate's TypeScript contract types (`src/lib/contracts/index.ts`) are validated against the OpenAPI spec using `openapi-typescript`.
- The PR cannot merge if validation fails.
- Both repos must show green before either can merge a breaking contract change.

## Owners

Contract files (`contracts/`) require approval from a maintainer of **both** repos. See `.github/CODEOWNERS`.
