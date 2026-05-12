package wks.authz

import future.keywords

test_not_allow_when_not_contain_role_for_user_manager if {
    not allow with input as { 
        "realm_access": { "roles": ["client_case"] },
        "host": "firm.levinellp.ca",
        "allowed_origin": "firm.levinellp.ca",
        "org": "firm",        
        "method": "POST",
        "path": "form"
    }
}

test_allow_when_contain_role_for_user_manager if {
    allow with input as { 
        "realm_access": { "roles": ["mgmt_form"] },
        "host": "firm.levinellp.ca",
        "allowed_origin": "firm.levinellp.ca",
        "org": "firm",        
        "method": "GET",
        "path": "form"
    }
}