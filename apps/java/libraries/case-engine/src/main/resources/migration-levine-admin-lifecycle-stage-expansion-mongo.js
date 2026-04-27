// Expand the Levine admin lifecycle case definition to the canonical five
// stages and ensure the closing/archive queues exist.

db.caseDefinition.updateOne(
  { id: 'matter-admin-opening-control' },
  {
    $set: {
      stages: [
        { id: '0', index: 0, name: 'Onboarding' },
        { id: '1', index: 1, name: 'Opening' },
        { id: '2', index: 2, name: 'Maintenance' },
        { id: '3', index: 3, name: 'Closing' },
        { id: '4', index: 4, name: 'Archived' },
      ],
    },
  },
)

;[
  {
    id: 'matter-admin-closing-review',
    name: 'Matter Admin Closing Review',
    description: 'Matter Admin Closing Review',
  },
  {
    id: 'matter-admin-closed',
    name: 'Matter Admin Closed',
    description: 'Matter Admin Closed',
  },
  {
    id: 'matter-admin-archived',
    name: 'Matter Admin Archived',
    description: 'Matter Admin Archived',
  },
].forEach((queue) => {
  db.queue.updateOne({ id: queue.id }, { $setOnInsert: queue }, { upsert: true })
})
