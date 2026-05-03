-- Incremental schema patch for existing JPA/PostgreSQL deployments.
-- Apply before enabling the canonical accounts foundation fields against an existing database.

ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS matter_type TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS billing_party_model TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS billing_mode TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_profile TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS billing_setup_complete BOOLEAN;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS flat_fee_amount TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS payment_method_authorized BOOLEAN;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS payment_method_ref TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS retainer_amount TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS retainer_funds_received BOOLEAN;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS subscription_plan_id TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS subscription_plan_name TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS subscription_active BOOLEAN;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS instructing_firm_id TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS instructing_firm_name TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS counsel_billing_mode TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS counsel_billing_party_override BOOLEAN;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_stage TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_state TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_health TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_health_reason_codes TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_health_evaluated_at TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_stale_since TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_malformed_case BOOLEAN;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_readiness_status TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_readiness_reason_codes TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_readiness_evaluated_at TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_readiness_summary TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_queue_id TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_next_action_owner_type TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_next_action_summary TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_next_action_due_at TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_work_blocked BOOLEAN;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_work_priority TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_events TEXT;
