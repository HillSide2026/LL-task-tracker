-- Incremental schema patch for existing JPA/H2 deployments.
-- Apply before enabling the canonical accounts foundation fields against an existing database.

ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS matter_type VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS billing_party_model VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS billing_mode VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_profile VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS billing_setup_complete BOOLEAN;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS flat_fee_amount VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS payment_method_authorized BOOLEAN;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS payment_method_ref VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS retainer_amount VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS retainer_funds_received BOOLEAN;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS subscription_plan_id VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS subscription_plan_name VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS subscription_active BOOLEAN;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS instructing_firm_id VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS instructing_firm_name VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS counsel_billing_mode VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS counsel_billing_party_override BOOLEAN;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_stage VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_state VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_health VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_health_reason_codes TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_health_evaluated_at VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_stale_since VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_malformed_case BOOLEAN;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_readiness_status VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_readiness_reason_codes TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_readiness_evaluated_at VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_readiness_summary TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_queue_id VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_next_action_owner_type VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_next_action_summary TEXT;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_next_action_due_at VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_work_blocked BOOLEAN;
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_work_priority VARCHAR(255);
ALTER TABLE case_instance ADD COLUMN IF NOT EXISTS accounts_events TEXT;
