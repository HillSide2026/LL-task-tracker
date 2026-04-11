// Rewrite legacy Step 1 admin lifecycle state values after the canonical rename from Open -> Opened.
// Run against each tenant case database before enabling Step 2A maintenance control.

db.caseInstance.updateMany(
  { adminState: 'Open' },
  { $set: { adminState: 'Opened' } },
)

db.caseInstance.updateMany(
  { resumeToState: 'Open' },
  { $set: { resumeToState: 'Opened' } },
)
