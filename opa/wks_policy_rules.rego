package wks.authz

import future.keywords

default allow = false

has_client_role := {"client_case", "client_task", "client_record", "ops_admin", "lawyer_user"}
has_manager_role := {"mgmt_form", "mgmt_case_def", "mgmt_record_type", "mgmt_bpm_engine", "mgmt_bpm_engine_type", "mgmt_process_engine", "ops_manager"}
has_email_to_case_role := {"email_to_case"}

allow {
    input.path == "case"
    method_allowed(["GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS", "HEAD"])
    check_origin_request
    is_user_profile
}

allow {
    input.path == "case-definition"
    method_allowed(["GET", "OPTIONS"])
    check_origin_request
    is_user_profile
}

allow {
    input.path == "record-type"
    method_allowed(["GET", "OPTIONS"])
    check_origin_request
    is_user_profile
}

allow {
    input.path == "record"
    method_allowed(["GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS", "HEAD"])
    check_origin_request
    is_user_profile
}

allow {
    input.path == "task"
    method_allowed(["GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS", "HEAD"])
    check_origin_request
    is_user_profile
}

allow {
    input.path == "form"
    method_allowed(["GET", "OPTIONS"])
    check_origin_request
    is_user_profile
}

allow {
    input.path == "variable"
    method_allowed(["GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS", "HEAD"])
    check_origin_request
    is_user_profile
}

allow {
    input.path == "process-instance"
    method_allowed(["GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS", "HEAD"])
    check_origin_request
    is_user_profile
}

allow {
    input.path == "process-definition"
    method_allowed(["POST", "OPTIONS", "HEAD"])
    check_origin_request
    is_user_profile
}

allow {
    input.path == "queue"
    method_allowed(["GET", "OPTIONS"])
    check_origin_request
    is_user_profile
}

allow {
    input.path == "case-email"
    method_allowed(["GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS", "HEAD"])
    check_origin_request
    is_user_profile
}

allow {
    input.path == "case-email"
    method_allowed(["GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS", "HEAD"])
    is_email_to_case_profile
}

allow {
    input.path == "record-type"
    method_allowed(["GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS", "HEAD"])
    check_origin_request
    is_manager_profile
}

allow {
    input.path == "form"
    method_allowed(["GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS", "HEAD"])
    check_origin_request
    is_manager_profile
}

allow {
    input.path == "process-definition"
    method_allowed(["GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS", "HEAD"])
    check_origin_request
    is_manager_profile
}

allow {
    input.path == "deployment"
    method_allowed(["GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS", "HEAD"])
    check_origin_request
    is_manager_profile
}

allow {
    input.path == "case-definition"
    method_allowed(["GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS", "HEAD"])
    check_origin_request
    is_manager_profile
}

allow {
    input.path == "message"
    method_allowed(["POST", "OPTIONS", "HEAD"])
    check_origin_request
    is_manager_profile
}

allow {
    input.path == "queue"
    method_allowed(["GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS", "HEAD"])
    check_origin_request
    is_manager_profile
}

allow {
    input.path == "storage"
    method_allowed(["GET", "POST", "OPTIONS", "HEAD"])
}

allow {
    input.path == "bpm-engine"
    method_allowed(["GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS", "HEAD"])
    check_origin_request
    is_manager_profile
}

allow {
    input.path == "bpm-engine-type"
    method_allowed(["GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS", "HEAD"])
    check_origin_request
    is_manager_profile
}

check_origin_request {
    input.allowed_origin == "localhost"
    input.host == "localhost"
}

check_origin_request {
    input.allowed_origin == "localhost"
    input.host == ""
}

check_origin_request {
    not is_null(input.org)
    startswith(input.host, input.org)
    input.allowed_origin == input.host
}

is_user_profile {
    some role in input.realm_access.roles
    has_client_role[role]
}

is_manager_profile {
    some role in input.realm_access.roles
    has_manager_role[role]
}

is_email_to_case_profile {
    some role in input.realm_access.roles
    has_email_to_case_role[role]
}

method_allowed(methods) {
    input.method in methods
}

method_allowed(methods) {
    input.method == "OPTION"
    "OPTIONS" in methods
}
