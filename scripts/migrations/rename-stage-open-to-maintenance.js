/**
 * Migration: rename AdminLifecycleStage "Open" → "Maintenance"
 *
 * Applies to all CaseInstance documents in MongoDB that have stage: "Open".
 * Run once against the target environment before or immediately after deploying
 * the application version that introduces the MAINTENANCE enum value.
 *
 * Safe to re-run — the $set only fires on documents that still have stage: "Open".
 *
 * Usage:
 *   mongosh <connection-string> scripts/migrations/rename-stage-open-to-maintenance.js
 *
 * Or via mongosh --eval:
 *   mongosh <connection-string> --file scripts/migrations/rename-stage-open-to-maintenance.js
 */

const DB_NAME = "levinellp";
const COLLECTION = "caseInstance";

const db = db.getSiblingDB(DB_NAME);
const collection = db.getCollection(COLLECTION);

const result = collection.updateMany(
  { stage: "Open" },
  { $set: { stage: "Maintenance" } }
);

print(`Migration complete.`);
print(`  Matched:  ${result.matchedCount}`);
print(`  Modified: ${result.modifiedCount}`);

if (result.matchedCount === 0) {
  print("  No documents required migration (already migrated or none existed).");
}
