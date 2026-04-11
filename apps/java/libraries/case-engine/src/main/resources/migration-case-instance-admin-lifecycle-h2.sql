-- Incremental schema patch for existing JPA/H2 deployments.
-- Apply before enabling matter-admin opening-control fields against an existing database.

ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS admin_state VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS admin_health VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS health_reason_codes TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS health_evaluated_at VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS stale_since VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS malformed_case BOOLEAN;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS admin_owner_id VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS admin_owner_name VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS responsible_lawyer_id VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS responsible_lawyer_name VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS next_action_owner_type VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS next_action_owner_ref VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS next_action_summary TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS next_action_due_at VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS waiting_reason_code VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS waiting_reason_text TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS waiting_since VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS expected_response_at VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS external_party_ref VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS resume_to_state VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS last_state_changed_at VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS opened_at VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS admin_events TEXT;
