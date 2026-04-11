package wks.authz

import future.keywords

test_not_allow_when_not_contain_role_for_user_manager if {
    not allow with input as { 
        "realm_access": { "roles": ["client_case"] },
        "host": "matters.levinellp.ca",
        "allowed_origin": "matters.levinellp.ca",
        "org": "matters",        
        "method": "POST",
        "path": "form"
    }
}

test_allow_when_contain_role_for_user_manager if {
    allow with input as { 
        "realm_access": { "roles": ["mgmt_form"] },
        "host": "matters.levinellp.ca",
        "allowed_origin": "matters.levinellp.ca",
        "org": "matters",        
        "method": "GET",
        "path": "form"
    }
}