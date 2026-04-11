package wks.authz

import future.keywords

test_case_route_allowed_for_ops_admin if {
    allow with input as {
        "realm_access": { "roles": ["ops_admin"] },
        "host": "app.wkspower.local",
        "allowed_origin": "app.wkspower.local",
        "org": "app",
        "method": "GET",
        "path": "case"
    }
}

test_case_route_allowed_for_lawyer_user if {
    allow with input as {
        "realm_access": { "roles": ["lawyer_user"] },
        "host": "app.wkspower.local",
        "allowed_origin": "app.wkspower.local",
        "org": "app",
        "method": "GET",
        "path": "case"
    }
}

test_case_route_not_allowed_without_user_or_manager_role if {
    not allow with input as {
        "realm_access": { "roles": ["email_to_case"] },
        "host": "app.wkspower.local",
        "allowed_origin": "app.wkspower.local",
        "org": "app",
        "method": "GET",
        "path": "case"
    }
}
