package com.wks.caseengine.cases.instance.accounts;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public final class AccountsLifecycleAccessSupport {

	public static final String ROLE_BILLING_USER = "billing_user";
	public static final String ROLE_ACCOUNTS_MANAGER = "accounts_manager";
	public static final String ROLE_OPS_ADMIN = "ops_admin";
	public static final String ROLE_OPS_MANAGER = "ops_manager";

	private AccountsLifecycleAccessSupport() {
	}

	public static void assertCanTransition() {
		if (!canTransition()) {
			throw new AccountsLifecycleException("Current user is not allowed to perform accounts transitions for this matter");
		}
	}

	public static void assertCanView() {
		if (!canTransition()) {
			throw new AccountsLifecycleException("Current user is not allowed to view accounts lifecycle data for this matter");
		}
	}

	public static boolean canTransition() {
		return hasRole(ROLE_ACCOUNTS_MANAGER) || hasRole(ROLE_BILLING_USER) || hasRole(ROLE_OPS_ADMIN)
				|| hasRole(ROLE_OPS_MANAGER);
	}

	public static Optional<String> currentUserId() {
		return currentJwt().map(jwt -> jwt.getClaimAsString("sub"));
	}

	public static Set<String> currentRoles() {
		return currentJwt().map(AccountsLifecycleAccessSupport::extractRealmRoles).orElse(Set.of());
	}

	public static boolean hasRole(String role) {
		return currentRoles().contains(role);
	}

	@SuppressWarnings("unchecked")
	private static Set<String> extractRealmRoles(Jwt jwt) {
		Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
		if (realmAccess == null) {
			return Set.of();
		}
		Object roles = realmAccess.get("roles");
		if (!(roles instanceof List<?> roleList)) {
			return Set.of();
		}
		return roleList.stream().map(String::valueOf).collect(Collectors.toSet());
	}

	private static Optional<Jwt> currentJwt() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return Optional.empty();
		}
		Object principal = authentication.getPrincipal();
		if (principal instanceof Jwt jwt) {
			return Optional.of(jwt);
		}
		Object credentials = authentication.getCredentials();
		if (credentials instanceof Jwt jwt) {
			return Optional.of(jwt);
		}
		return Optional.empty();
	}
}
