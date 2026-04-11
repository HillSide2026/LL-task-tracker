-- Incremental schema patch for existing JPA/PostgreSQL deployments.
-- Apply before enabling matter-admin opening-control fields against an existing database.

ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS admin_state TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS admin_health TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS health_reason_codes TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS health_evaluated_at TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS stale_since TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS malformed_case BOOLEAN;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS admin_owner_id TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS admin_owner_name TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS responsible_lawyer_id TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS responsible_lawyer_name TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS next_action_owner_type TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS next_action_owner_ref TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS next_action_summary TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS next_action_due_at TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS waiting_reason_code TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS waiting_reason_text TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS waiting_since TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS expected_response_at TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS external_party_ref TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS resume_to_state TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS last_state_changed_at TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS opened_at TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS admin_events TEXT;
