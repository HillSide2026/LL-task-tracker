// Realign legacy Opened admin lifecycle rows after Opening was defined to
// complete at Opened instead of Active.

db.caseInstance.updateMany(
  {
    caseDefinitionId: 'matter-admin-opening-control',
    adminState: 'Opened',
    stage: { $in: ['Maintenance', 'Open'] },
  },
  { $set: { stage: 'Opening' } },
)
