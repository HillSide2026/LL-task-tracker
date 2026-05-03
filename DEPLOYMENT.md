# Deployment

## System Boundary

LL-task-tracker owns the Levine LLP internal operational task-management
system at:

```text
https://tasks.levinellp.ca
```

The `tasks.levinellp.ca` subdomain belongs to this app. It is separate from:

- `ll-corporate`, the primary client-facing app associated with `https://levinellp.ca`
- `NDA-Esq`, the NDA Generator mounted at `https://levinellp.ca/nda`

This app should not implement routes for `/nda` or for client portal pages on the
root `levinellp.ca` domain. Client-facing portal views may be surfaced by
`ll-corporate`, but workflow state, task ownership, and operational task-system
records remain owned by this repository.
