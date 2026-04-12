# Contract Operations

This document covers everything needed to operate the cross-repo contract coordination
between LL-task-tracker and ll-corporate. Read this before making any change to files
under `contracts/`, `opa/`, or any shared API endpoint.

---

## Ownership

| Role | Owner | Scope |
|---|---|---|
| Contract author | @HillSide2026 | LL-task-tracker `contracts/` — source of truth |
| Contract consumer | @HillSide2026 | ll-corporate `src/lib/contracts/` |
| CI/workflow owner | @HillSide2026 | Both repos `.github/workflows/` |
| Release coordinator | @HillSide2026 | Approve breaking changes, sign off rollout plan |

Any PR touching `contracts/`, `opa/`, or a shared endpoint requires review from the
contract author AND the contract consumer before merge (enforced by CODEOWNERS in both repos).

---

## Branch protection setup (GitHub rulesets)

These rules must be configured manually in GitHub repository settings.
They are not enforced until configured.

### LL-task-tracker

Navigate to: **Settings → Rules → Rulesets → New ruleset**

- Target branches: `main`
- Required: Require a pull request before merging
- Required approvals: 1
- Required status checks:
  - `validate-contracts` (from `contracts-ci.yml`)
  - `trigger-downstream` (from `contracts-ci.yml`)
- Dismiss stale reviews on new commits: enabled
- Require review from CODEOWNERS: enabled
- Block force push: enabled

### ll-corporate

Navigate to: **Settings → Rules → Rulesets → New ruleset**

- Target branches: `main`
- Required: Require a pull request before merging
- Required approvals: 1
- Required status checks:
  - `Check` (from `check.yml`)
  - `Contract Check` (from `contract-check.yml`)
- Dismiss stale reviews on new commits: enabled
- Require review from CODEOWNERS: enabled
- Block force push: enabled

---

## Cross-repo dispatch token setup

The `contracts-ci.yml` workflow in LL-task-tracker needs a GitHub token that can
trigger workflows in ll-corporate.

1. Go to GitHub → Settings → Developer settings → Personal access tokens → Fine-grained tokens
2. Create a token with:
   - Repository access: ll-corporate only
   - Permissions: Actions (Read and Write), Contents (Read)
   - Expiry: 90 days (rotate quarterly)
3. Add to LL-task-tracker: Settings → Secrets and variables → Actions → New repository secret
   - Name: `CROSS_REPO_DISPATCH_TOKEN`
   - Value: the token from step 2

Without this secret, the cross-repo check will warn but not block. After the secret is set,
the trigger-downstream job will block merge if ll-corporate fails.

---

## How to make a contract change

### Step 1 — Classify the change

Read `COMPATIBILITY.md` and decide:

- `internal-only` — no consumer impact, no special process
- `backward-compatible` — additive change, minor version bump, deploy LL-task-tracker first
- `breaking` — major version bump, full rollout plan required (see below)

### Step 2 — Make the change

Update `contracts/openapi/case-engine-api.yaml` to reflect the new shape.

For backward-compatible changes, bump the minor version in `contracts/dependency-matrix.yaml`:
```yaml
contract_version: "1.1.0"   # was 1.0.0
```

For breaking changes, bump the major version:
```yaml
contract_version: "2.0.0"   # was 1.0.0
```

### Step 3 — Update ll-corporate

Open a PR in ll-corporate that:
- Updates `src/lib/contracts/index.ts` to match the new shapes
- Updates `src/lib/contracts/schemas.ts` (Zod validators) to match
- Bumps `SUPPORTED_CONTRACT_VERSION` in `src/lib/contracts/version.ts`
- Passes `pnpm run build` and `pnpm run test`

Link the ll-corporate PR in the LL-task-tracker PR description.

### Step 4 — CI gate

The `contracts-ci.yml` workflow will automatically:
1. Lint the OpenAPI spec
2. Validate JSON event schemas
3. Trigger ll-corporate's `contract-check.yml`
4. Wait for it to complete
5. Block merge if it fails

Both PRs must be green before either merges.

---

## Breaking change rollout

For changes where `breaking` is declared:

### Deployment sequence

```
1. Deploy LL-task-tracker with dual-support
   (serve both old and new contract behind a feature flag or via content negotiation)
2. Confirm LL-task-tracker is stable
3. Deploy ll-corporate to consume the new contract
4. Monitor for errors for 48 hours
5. Remove old contract support from LL-task-tracker
6. Remove feature flag
```

### Rollback decision matrix

| Scenario | Action |
|---|---|
| LL-task-tracker new + ll-corporate old | Supported during transition window. LL-task-tracker must serve both. |
| LL-task-tracker old + ll-corporate new | NOT supported. Roll ll-corporate back immediately. |
| Both rolled back | Clean. No action required. |
| Both on new | Target state. |

If ll-corporate is deployed before LL-task-tracker is updated, it will receive
unexpected shapes that fail Zod validation and throw `ContractViolationError`.
These errors are observable (see Observability section) and loud by design.

### Rollback procedure

**ll-corporate:** Redeploy the previous build artifact (no data migration needed).

**LL-task-tracker:** Redeploy previous Docker image. During the transition window,
the old contract is still served, so this is always safe.

---

## Observability

### What to monitor

| Signal | What it means | Where to look |
|---|---|---|
| `ContractViolationError` in ll-corporate logs | API response shape doesn't match declared contract | ll-corporate application logs / Vercel logs |
| `ApiError` (4xx/5xx) in ll-corporate logs | LL-task-tracker returned an error | ll-corporate application logs |
| `contract-check` workflow failure | ll-corporate types diverged from LL-task-tracker contract | GitHub Actions tab in ll-corporate |
| `trigger-downstream` job failure | Cross-repo gate failed | GitHub Actions tab in LL-task-tracker |
| HTTP 401/403 on case engine calls | Auth token missing or expired, or OPA denied | LL-task-tracker case-engine logs |

### Controlled failure test (run after any contract change)

1. Deploy a version of ll-corporate with an intentionally wrong type for one field.
2. Confirm `ContractViolationError` is thrown and logged.
3. Confirm the error is surfaced (not silently swallowed).
4. Roll back.

### Deprecated contract usage

When a field is deprecated in the OpenAPI spec:
- Add a log warning in the ll-corporate adapter when that field is read.
- Monitor logs to confirm the field is not being used before removal.

---

## Audit trail

For any contract-changing PR, you should be able to answer:

| Question | Where to find the answer |
|---|---|
| What changed? | PR diff in `contracts/openapi/case-engine-api.yaml` |
| Who approved it? | PR review timeline (both repos) |
| Did ll-corporate validate? | `trigger-downstream` job log in LL-task-tracker CI |
| Which contract version was produced? | `contracts/dependency-matrix.yaml` at merge commit |
| What rollout sequence was intended? | PR description (from the PR template) |

If any of these are unanswerable from the PR, the PR should not have merged.

---

## Bypass procedure (emergency only)

Bypassing the contract gate is allowed only when:
1. A production incident requires an immediate hotfix
2. The bypass is explicitly approved by the contract owner (@HillSide2026)
3. A follow-up issue is filed within 24 hours to restore validation

### How to bypass

In GitHub repository Settings → Rules → Rulesets, the contract owner can be added
to the bypass list for the `main` branch ruleset. This should be removed after use.

**Every bypass must be documented.** Log the bypass in the PR description:
```
EMERGENCY BYPASS: [reason]
Follow-up issue: #___
Approved by: @HillSide2026
```

Routine use of bypass defeats the entire mechanism. If bypasses are happening frequently,
the process has a friction problem that needs to be fixed, not routinely bypassed.

---

## Acceptance test checklist

Before marking the coordination system as operational, run all five:

- [ ] **Compatible change:** Make an additive change in LL-task-tracker `contracts/`. Confirm `contracts-ci.yml` triggers ll-corporate's `contract-check.yml`, it passes, and the PR merges cleanly.
- [ ] **Breaking change blocked:** Make a breaking change (remove a field) without updating ll-corporate. Confirm ll-corporate's `contract-check.yml` fails and LL-task-tracker's PR cannot merge.
- [ ] **Downstream failure blocks upstream:** Intentionally break ll-corporate's TypeScript types. Confirm `Contract Check` fails, and that LL-task-tracker's `trigger-downstream` step reports failure.
- [ ] **Rollback test:** Deploy new LL-task-tracker with new contract while ll-corporate is still on the old contract. Confirm ll-corporate still works (dual-support window). Then roll LL-task-tracker back. Confirm both work.
- [ ] **Security review:** Confirm all workflow files have `permissions:` blocks, CODEOWNERS is enforced, CROSS_REPO_DISPATCH_TOKEN has minimum scope, and no workflow can self-approve changes to itself.
