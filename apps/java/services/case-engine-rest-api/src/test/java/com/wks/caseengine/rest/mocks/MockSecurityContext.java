/*
 * WKS Platform - Open-Source Project
 * 
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 * 
 * WKS Platform is licensed under the MIT License.
 * 
 * © 2021 WKS Power. All rights reserved.
 * 
 * For licensing information, see the LICENSE file in the root directory of the project.
 */
package com.wks.caseengine.rest.mocks;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;

public class MockSecurityContext implements SecurityContext {

	private static final long serialVersionUID = 1L;

	private Authentication authz;

	public MockSecurityContext(String org, String allowedOrigem) {
		this(org, allowedOrigem, "ops-manager-sub", List.of("ops_manager"));
	}

	public MockSecurityContext(String org, String allowedOrigem, String sub, List<String> roles) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("org", org);
		claims.put("sub", sub);
		claims.put("allowed-origins", Arrays.asList(allowedOrigem));
		Map<String, Object> realmAccess = new HashMap<>();
		realmAccess.put("roles", roles);
		claims.put("realm_access", realmAccess);
		this.authz = new MockAuthentication(claims);
	}

	@Override
	public Authentication getAuthentication() {
		return authz;
	}

	@Override
	public void setAuthentication(Authentication authentication) {
		this.authz = authentication;
	}

	static class MockAuthentication implements Authentication {

		private static final long serialVersionUID = 1L;

		private Jwt credentials;

		public MockAuthentication(Map<String, Object> claims) {
			String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJvcmciOiJ3a3MifQ.oH4f1nqG55Qlznibk9AMC5y3CHvixL45tEhdN8dnBDk";
			Map<String, Object> headers = new HashMap<>();
			headers.put("alg", "HS256");
			headers.put("typ", "JWT");
			this.credentials = new Jwt(token, null, null, headers, claims);
		}

		@Override
		public String getName() {
			return credentials.getClaimAsString("sub");
		}

		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			return null;
		}

		@Override
		public Object getCredentials() {
			return credentials;
		}

		@Override
		public Object getDetails() {
			return null;
		}

		@Override
		public Object getPrincipal() {
			return null;
		}

		@Override
		public boolean isAuthenticated() {
			return true;
		}

		@Override
		public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		}
	}

}
