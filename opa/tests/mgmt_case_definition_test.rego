package wks.authz

import future.keywords

test_deny_when_method_not_get_and_user_profile if {
    not allow with input as { 
        "realm_access": { "roles": ["client_case"] },
        "host": "matters.levinellp.ca",
        "allowed_origin": "matters.levinellp.ca",
        "org": "matters",        
        "method": "POST",
        "path": "case-definition"
    }
}

test_allow_when_method_get_and_user_profile if {
    allow with input as { 
        "realm_access": { "roles": ["client_case"] },
        "host": "matters.levinellp.ca",
        "allowed_origin": "matters.levinellp.ca",
        "org": "matters",        
        "method": "GET",
        "path": "case-definition"
    }
}