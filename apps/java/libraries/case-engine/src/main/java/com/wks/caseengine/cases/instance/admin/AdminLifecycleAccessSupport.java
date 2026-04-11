package com.wks.caseengine.cases.instance.admin;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import com.wks.caseengine.cases.instance.CaseInstance;

public final class AdminLifecycleAccessSupport {

	public static final String ROLE_OPS_ADMIN = "ops_admin";
	public static final String ROLE_OPS_MANAGER = "ops_manager";
	public static final String ROLE_LAWYER_USER = "lawyer_user";

	private AdminLifecycleAccessSupport() {
	}

	public static Optional<String> currentUserId() {
		return currentJwt().map(jwt -> jwt.getClaimAsString("sub"));
	}

	public static boolean isOpsAdmin() {
		return hasRole(ROLE_OPS_ADMIN);
	}

	public static boolean isOpsManager() {
		return hasRole(ROLE_OPS_MANAGER);
	}

	public static boolean isLawyerUser() {
		return hasRole(ROLE_LAWYER_USER);
	}

	public static boolean isOpsAdminOrManager() {
		return isOpsAdmin() || isOpsManager();
	}

	public static boolean canViewCase(CaseInstance caseInstance) {
		if (caseInstance == null) {
			return false;
		}
		if (!AdminLifecycleSupport.isAdminLifecycleCase(caseInstance)) {
			return true;
		}
		if (isOpsAdminOrManager()) {
			return true;
		}
		if (!isLawyerUser()) {
			return false;
		}
		return currentUserId().map(userId -> userId.equals(caseInstance.getResponsibleLawyerId())).orElse(false);
	}

	public static boolean shouldRestrictToAssignedLawyerCases() {
		return isLawyerUser() && !isOpsAdminOrManager();
	}

	public static void assertCanTransition(CaseInstance caseInstance, AdminTransition transition) {
		if (isOpsManager()) {
			return;
		}

		switch (transition) {
		case LAWYER_APPROVE_OPEN:
		case LAWYER_RETURN_FOR_FIXES:
		case LAWYER_RETURN_TO_ACTIVE:
		case LAWYER_REQUEST_CLIENT_FOLLOWUP:
		case LAWYER_REQUEST_EXTERNAL_FOLLOWUP:
			if (isAssignedLawyer(caseInstance)) {
				return;
			}
			throw new AdminLifecycleException("Current user is not allowed to perform lawyer transitions for this matter");
		default:
			if (isOpsAdmin()) {
				return;
			}
			throw new AdminLifecycleException("Current user is not allowed to perform admin transitions for this matter");
		}
	}

	public static boolean hasRole(String role) {
		return currentRoles().contains(role);
	}

	public static Set<String> currentRoles() {
		return currentJwt().map(AdminLifecycleAccessSupport::extractRealmRoles).orElse(Set.of());
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

	private static boolean isAssignedLawyer(CaseInstance caseInstance) {
		if (!isLawyerUser()) {
			return false;
		}
		return currentUserId().map(userId -> userId.equals(caseInstance.getResponsibleLawyerId())).orElse(false);
	}

	private static Optional<Jwt> currentJwt() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return Optional.empty();
		}
		if (authentication.getCredentials() instanceof Jwt jwt) {
			return Optional.of(jwt);
		}
		if (authentication.getPrincipal() instanceof Jwt jwt) {
			return Optional.of(jwt);
		}
		return Optional.empty();
	}
}
