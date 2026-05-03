CREATE TABLE IF NOT EXISTS  case_definition (
    uid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id text UNIQUE NOT NULL,
    name TEXT NOT NULL,
    form_key TEXT,
    stages_lifecycle_process_key TEXT,
    deployed BOOLEAN NOT NULL DEFAULT FALSE,
    stages text,
    case_hooks TEXT,
    kanban_config TEXT
);

CREATE TABLE IF NOT EXISTS  record_type (
    uid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id TEXT UNIQUE NOT NULL,
    fields TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS  record_type_instance (
    uid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    record_type_id TEXT NOT NULL,
    content TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS  form (
    uid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    form_key TEXT UNIQUE NOT NULL,
    title TEXT,
    tool_tip TEXT,
    structure TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS  queue (
    uid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS  case_instance (
    uid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_key TEXT UNIQUE NOT NULL,
    queue_id TEXT,
    status TEXT,
    stage TEXT,
    admin_state TEXT,
    admin_health TEXT,
    health_reason_codes TEXT,
    health_evaluated_at TEXT,
    stale_since TEXT,
    malformed_case BOOLEAN,
    admin_owner_id TEXT,
    admin_owner_name TEXT,
    responsible_lawyer_id TEXT,
    responsible_lawyer_name TEXT,
    next_action_owner_type TEXT,
    next_action_owner_ref TEXT,
    next_action_summary TEXT,
    next_action_due_at TEXT,
    waiting_reason_code TEXT,
    waiting_reason_text TEXT,
    waiting_since TEXT,
    expected_response_at TEXT,
    external_party_ref TEXT,
    resume_to_state TEXT,
    last_state_changed_at TEXT,
    opened_at TEXT,
    matter_type TEXT,
    billing_party_model TEXT,
    billing_mode TEXT,
    accounts_profile TEXT,
    billing_setup_complete BOOLEAN,
    flat_fee_amount TEXT,
    payment_method_authorized BOOLEAN,
    payment_method_ref TEXT,
    retainer_amount TEXT,
    retainer_funds_received BOOLEAN,
    subscription_plan_id TEXT,
    subscription_plan_name TEXT,
    subscription_active BOOLEAN,
    instructing_firm_id TEXT,
    instructing_firm_name TEXT,
    counsel_billing_mode TEXT,
    counsel_billing_party_override BOOLEAN,
    accounts_stage TEXT,
    accounts_state TEXT,
    accounts_health TEXT,
    accounts_health_reason_codes TEXT,
    accounts_health_evaluated_at TEXT,
    accounts_stale_since TEXT,
    accounts_malformed_case BOOLEAN,
    accounts_readiness_status TEXT,
    accounts_readiness_reason_codes TEXT,
    accounts_readiness_evaluated_at TEXT,
    accounts_readiness_summary TEXT,
    accounts_queue_id TEXT,
    accounts_next_action_owner_type TEXT,
    accounts_next_action_summary TEXT,
    accounts_next_action_due_at TEXT,
    accounts_work_blocked BOOLEAN,
    accounts_work_priority TEXT,
    accounts_events TEXT,
    admin_events TEXT,
    attributes TEXT,
    documents TEXT,
    comments TEXT,
    case_definition_id TEXT,
    owner TEXT
);

CREATE TABLE IF NOT EXISTS  case_email (
    uid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    case_instance_business_key VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    body TEXT,
    sender VARCHAR(255),
    recipient VARCHAR(255),
    received_date_time TIMESTAMP,
    has_attachments BOOLEAN,
    to_email VARCHAR(255),
    from_email VARCHAR(255),
    body_preview TEXT,
    importance VARCHAR(50),
    case_definition_id VARCHAR(255),
    outbound BOOLEAN,
    status VARCHAR(50)
);
