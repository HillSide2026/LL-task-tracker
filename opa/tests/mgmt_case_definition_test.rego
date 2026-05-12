package wks.authz

import future.keywords

test_deny_when_method_not_get_and_user_profile if {
    not allow with input as { 
        "realm_access": { "roles": ["client_case"] },
        "host": "firm.levinellp.ca",
        "allowed_origin": "firm.levinellp.ca",
        "org": "firm",        
        "method": "POST",
        "path": "case-definition"
    }
}

test_allow_when_method_get_and_user_profile if {
    allow with input as { 
        "realm_access": { "roles": ["client_case"] },
        "host": "firm.levinellp.ca",
        "allowed_origin": "firm.levinellp.ca",
        "org": "firm",        
        "method": "GET",
        "path": "case-definition"
    }
}