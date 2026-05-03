CREATE TABLE case_definition (
    uid VARCHAR(36) DEFAULT RANDOM_UUID() PRIMARY KEY,
    id VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    form_key VARCHAR(255),
    stages_lifecycle_process_key VARCHAR(255),
    deployed BOOLEAN NOT NULL DEFAULT FALSE,
    stages TEXT,
    case_hooks TEXT,
    kanban_config TEXT
);

CREATE TABLE record_type (
    uid VARCHAR(36) DEFAULT RANDOM_UUID() PRIMARY KEY,
    id VARCHAR(255) UNIQUE NOT NULL,
    fields TEXT NOT NULL
);

CREATE TABLE record_type_instance (
    uid VARCHAR(36) DEFAULT RANDOM_UUID() PRIMARY KEY,
    record_type_id VARCHAR(255) NOT NULL,
    content TEXT NOT NULL
);

CREATE TABLE form (
    uid VARCHAR(36) DEFAULT RANDOM_UUID() PRIMARY KEY,
    form_key VARCHAR(255) UNIQUE NOT NULL,
    title VARCHAR(255),
    tool_tip VARCHAR(255),
    structure TEXT NOT NULL
);

CREATE TABLE queue (
    uid VARCHAR(36) DEFAULT RANDOM_UUID() PRIMARY KEY,
    id VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE case_instance (
    uid VARCHAR(36) DEFAULT RANDOM_UUID() PRIMARY KEY,
    business_key VARCHAR(255) UNIQUE NOT NULL,
    queue_id VARCHAR(255),
    status VARCHAR(255),
    stage VARCHAR(255),
    admin_state VARCHAR(255),
    admin_health VARCHAR(255),
    health_reason_codes TEXT,
    health_evaluated_at VARCHAR(255),
    stale_since VARCHAR(255),
    malformed_case BOOLEAN,
    admin_owner_id VARCHAR(255),
    admin_owner_name VARCHAR(255),
    responsible_lawyer_id VARCHAR(255),
    responsible_lawyer_name VARCHAR(255),
    next_action_owner_type VARCHAR(255),
    next_action_owner_ref VARCHAR(255),
    next_action_summary TEXT,
    next_action_due_at VARCHAR(255),
    waiting_reason_code VARCHAR(255),
    waiting_reason_text TEXT,
    waiting_since VARCHAR(255),
    expected_response_at VARCHAR(255),
    external_party_ref VARCHAR(255),
    resume_to_state VARCHAR(255),
    last_state_changed_at VARCHAR(255),
    opened_at VARCHAR(255),
    matter_type VARCHAR(255),
    billing_party_model VARCHAR(255),
    billing_mode VARCHAR(255),
    accounts_profile VARCHAR(255),
    billing_setup_complete BOOLEAN,
    flat_fee_amount VARCHAR(255),
    payment_method_authorized BOOLEAN,
    payment_method_ref VARCHAR(255),
    retainer_amount VARCHAR(255),
    retainer_funds_received BOOLEAN,
    subscription_plan_id VARCHAR(255),
    subscription_plan_name VARCHAR(255),
    subscription_active BOOLEAN,
    instructing_firm_id VARCHAR(255),
    instructing_firm_name VARCHAR(255),
    counsel_billing_mode VARCHAR(255),
    counsel_billing_party_override BOOLEAN,
    accounts_stage VARCHAR(255),
    accounts_state VARCHAR(255),
    accounts_health VARCHAR(255),
    accounts_health_reason_codes TEXT,
    accounts_health_evaluated_at VARCHAR(255),
    accounts_stale_since VARCHAR(255),
    accounts_malformed_case BOOLEAN,
    accounts_readiness_status VARCHAR(255),
    accounts_readiness_reason_codes TEXT,
    accounts_readiness_evaluated_at VARCHAR(255),
    accounts_readiness_summary TEXT,
    accounts_queue_id VARCHAR(255),
    accounts_next_action_owner_type VARCHAR(255),
    accounts_next_action_summary TEXT,
    accounts_next_action_due_at VARCHAR(255),
    accounts_work_blocked BOOLEAN,
    accounts_work_priority VARCHAR(255),
    accounts_events TEXT,
    admin_events TEXT,
    attributes TEXT,
    documents TEXT,
    comments TEXT,
    case_definition_id VARCHAR(255),
    owner VARCHAR(255)
);

CREATE TABLE case_email (
    uid VARCHAR(36) DEFAULT RANDOM_UUID() PRIMARY KEY,
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
