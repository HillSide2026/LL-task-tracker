## Summary

<!-- What does this PR do? One or two sentences. -->

## Change classification

<!-- Select one. -->

- [ ] `internal-only` — no impact on shared contracts or consumer behavior
- [ ] `backward-compatible` — additive contract change (minor version bump)
- [ ] `breaking` — breaking contract change (major version bump + rollout plan required)

## Contract checklist

<!-- Complete if any file under contracts/, opa/, or a shared API/endpoint changed. -->

**Did you change any of the following?**

- [ ] `contracts/openapi/case-engine-api.yaml`
- [ ] `contracts/events/schemas/`
- [ ] `contracts/dependency-matrix.yaml`
- [ ] OPA policy rules (`opa/wks_policy_rules.rego`)
- [ ] An existing REST endpoint (path, method, request/response shape)
- [ ] A domain enum or field name

**If yes:**

- [ ] Contract version bumped in `contracts/dependency-matrix.yaml`
- [ ] `contracts/COMPATIBILITY.md` updated if rules changed
- [ ] `contracts/openapi/case-engine-api.yaml` updated to reflect the change
- [ ] ll-corporate `src/lib/contracts/` can be updated consistently (link PR or issue: _____)
- [ ] Confirmed backward-compatible OR have a rollout plan (see below)

## Rollout plan (breaking changes only)

<!-- Delete this section if not breaking. -->

**Old contract supported until:** ____  
**Dual-support / feature flag:** ____  
**ll-corporate migration PR:** ____  
**Both repos green before merge?** [ ] Yes

## Testing

- [ ] Unit tests pass (`mvn test`)
- [ ] Integration tests pass
- [ ] Deployed to staging and tested against ll-corporate (if contract change)

## Linked issues

<!-- Closes #___  /  Related: ll-corporate#___ -->
