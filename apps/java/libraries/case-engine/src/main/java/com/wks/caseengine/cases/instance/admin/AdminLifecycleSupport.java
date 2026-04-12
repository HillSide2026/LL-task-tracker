package com.wks.caseengine.cases.instance.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.wks.caseengine.cases.instance.CaseAttribute;
import com.wks.caseengine.cases.instance.CaseInstance;

/**
 * Constants and helpers for the Levine LLP matter-admin lifecycle.
 */
public final class AdminLifecycleSupport {

	public static final String CASE_DEFINITION_ID = "matter-admin-opening-control";

	public static final String QUEUE_INTAKE = "matter-admin-intake";
	public static final String QUEUE_ENGAGEMENT_HOLD = "matter-admin-engagement-hold";
	public static final String QUEUE_READY_TO_OPEN = "matter-admin-ready-to-open";
	public static final String QUEUE_LAWYER_REVIEW = "matter-admin-lawyer-review";
	public static final String QUEUE_CLIENT_WAITING = "matter-admin-client-waiting";
	public static final String QUEUE_OPEN = "matter-admin-open";
	public static final String QUEUE_ACTIVE = "matter-admin-active";
	public static final String QUEUE_MAINTENANCE_LAWYER_REVIEW = "matter-admin-maintenance-lawyer-review";
	public static final String QUEUE_MAINTENANCE_CLIENT_WAITING = "matter-admin-maintenance-client-waiting";
	public static final String QUEUE_EXTERNAL_WAITING = "matter-admin-external-waiting";

	private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
	private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

	private AdminLifecycleSupport() {
	}

	public static boolean isAdminLifecycleCase(CaseInstance caseInstance) {
		return caseInstance != null && isAdminLifecycleCase(caseInstance.getCaseDefinitionId());
	}

	public static boolean isAdminLifecycleCase(String caseDefinitionId) {
		return CASE_DEFINITION_ID.equals(caseDefinitionId);
	}

	public static String expectedStageForState(String adminState) {
		AdminState state = normalizedState(adminState);
		if (state == null) {
			return null;
		}

		return switch (state) {
		case INTAKE_REVIEW, AWAITING_ENGAGEMENT -> AdminLifecycleStage.ONBOARDING.getCode();
		case READY_TO_OPEN, READY_FOR_LAWYER, WAITING_ON_CLIENT -> AdminLifecycleStage.OPENING.getCode();
		case OPENED, ACTIVE, MAINTENANCE_LAWYER_REVIEW, MAINTENANCE_CLIENT_WAIT, WAITING_ON_EXTERNAL -> AdminLifecycleStage.MAINTENANCE.getCode();
		case CLOSING_REVIEW, CLOSED -> AdminLifecycleStage.CLOSING.getCode();
		case ARCHIVED -> AdminLifecycleStage.ARCHIVED.getCode();
		};
	}

	public static String defaultQueueForState(String adminState) {
		AdminState state = normalizedState(adminState);
		if (state == null) {
			return null;
		}

		return switch (state) {
		case INTAKE_REVIEW -> QUEUE_INTAKE;
		case AWAITING_ENGAGEMENT -> QUEUE_ENGAGEMENT_HOLD;
		case READY_TO_OPEN -> QUEUE_READY_TO_OPEN;
		case READY_FOR_LAWYER -> QUEUE_LAWYER_REVIEW;
		case WAITING_ON_CLIENT -> QUEUE_CLIENT_WAITING;
		case OPENED -> QUEUE_OPEN;
		case ACTIVE -> QUEUE_ACTIVE;
		case MAINTENANCE_LAWYER_REVIEW -> QUEUE_MAINTENANCE_LAWYER_REVIEW;
		case MAINTENANCE_CLIENT_WAIT -> QUEUE_MAINTENANCE_CLIENT_WAITING;
		case WAITING_ON_EXTERNAL -> QUEUE_EXTERNAL_WAITING;
		case CLOSING_REVIEW, CLOSED, ARCHIVED -> null;
		};
	}

	public static boolean isWaitingState(String adminState) {
		AdminState state = normalizedState(adminState);
		return state == AdminState.WAITING_ON_CLIENT || state == AdminState.MAINTENANCE_CLIENT_WAIT
				|| state == AdminState.WAITING_ON_EXTERNAL;
	}

	public static boolean isOpeningWaitState(String adminState) {
		return normalizedState(adminState) == AdminState.WAITING_ON_CLIENT;
	}

	public static boolean isMaintenanceClientWaitState(String adminState) {
		return normalizedState(adminState) == AdminState.MAINTENANCE_CLIENT_WAIT;
	}

	public static boolean isExternalWaitState(String adminState) {
		return normalizedState(adminState) == AdminState.WAITING_ON_EXTERNAL;
	}

	public static boolean isMaintenanceLawyerReviewState(String adminState) {
		return normalizedState(adminState) == AdminState.MAINTENANCE_LAWYER_REVIEW;
	}

	public static boolean isOpeningLawyerReviewState(String adminState) {
		return normalizedState(adminState) == AdminState.READY_FOR_LAWYER;
	}

	public static boolean isOpenStageState(String adminState) {
		AdminState state = normalizedState(adminState);
		return state == AdminState.OPENED || state == AdminState.ACTIVE
				|| state == AdminState.MAINTENANCE_LAWYER_REVIEW || state == AdminState.MAINTENANCE_CLIENT_WAIT
				|| state == AdminState.WAITING_ON_EXTERNAL;
	}

	public static String displayStateLabel(String adminState) {
		AdminState state = normalizedState(adminState);
		if (state == null) {
			return adminState;
		}
		return switch (state) {
		case MAINTENANCE_LAWYER_REVIEW -> AdminState.READY_FOR_LAWYER.getCode();
		case MAINTENANCE_CLIENT_WAIT -> AdminState.WAITING_ON_CLIENT.getCode();
		default -> state.getCode();
		};
	}

	public static boolean normalizeLegacyState(CaseInstance caseInstance) {
		if (caseInstance == null) {
			return false;
		}

		boolean changed = false;
		if ("Open".equals(caseInstance.getAdminState())) {
			caseInstance.setAdminState(AdminState.OPENED.getCode());
			changed = true;
		}
		if ("Open".equals(caseInstance.getResumeToState())) {
			caseInstance.setResumeToState(AdminState.OPENED.getCode());
			changed = true;
		}
		return changed;
	}

	public static boolean isNonTerminalState(String adminState) {
		AdminState state = normalizedState(adminState);
		return state != AdminState.CLOSED && state != AdminState.ARCHIVED;
	}

	public static boolean isTruthyAttribute(CaseInstance caseInstance, String attributeName) {
		return getAttributeValue(caseInstance, attributeName).map(AdminLifecycleSupport::parseBoolean).orElse(false);
	}

	public static Optional<String> getAttributeValue(CaseInstance caseInstance, String attributeName) {
		if (caseInstance == null || caseInstance.getAttributes() == null) {
			return Optional.empty();
		}

		return caseInstance.getAttributes().stream().filter(attr -> attributeName.equals(attr.getName())).map(CaseAttribute::getValue)
				.filter(Objects::nonNull).findFirst();
	}

	public static boolean parseBoolean(String value) {
		if (value == null) {
			return false;
		}
		return "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || "1".equalsIgnoreCase(value);
	}

	public static LocalDate parseDate(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		List<DateTimeFormatter> formatters = new ArrayList<>();
		formatters.add(ISO_DATE);
		formatters.add(ISO_DATE_TIME);

		for (DateTimeFormatter formatter : formatters) {
			try {
				if (formatter == ISO_DATE) {
					return LocalDate.parse(value, formatter);
				}
				return LocalDateTime.parse(value, formatter).toLocalDate();
			} catch (DateTimeParseException ignored) {
				// keep trying
			}
		}

		try {
			return OffsetDateTime.parse(value).toLocalDate();
		} catch (DateTimeParseException ignored) {
			return null;
		}
	}

	public static String nowTimestamp() {
		return OffsetDateTime.now().toString();
	}

	public static Date nowDate() {
		return new Date();
	}

	public static AdminControlEvaluation evaluate(CaseInstance caseInstance) {
		if (caseInstance == null) {
			return AdminControlEvaluation.builder().build();
		}
		normalizeLegacyState(caseInstance);

		if (!isAdminLifecycleCase(caseInstance)) {
			return AdminControlEvaluation.builder().adminHealth(caseInstance.getAdminHealth())
					.healthReasonCodes(caseInstance.getHealthReasonCodes()).healthEvaluatedAt(caseInstance.getHealthEvaluatedAt())
					.staleSince(caseInstance.getStaleSince()).malformedCase(caseInstance.getMalformedCase()).build();
		}

		List<String> reasons = new ArrayList<>();
		boolean malformed = false;
		String staleSince = null;
		LocalDate today = LocalDate.now();
		AdminState currentState = normalizedState(caseInstance.getAdminState());

		if (caseInstance.getAdminState() == null || caseInstance.getAdminState().isBlank()) {
			reasons.add(AdminHealthReasonCode.MISSING_ADMIN_STATE.getCode());
			malformed = true;
		}

		if (isNonTerminalState(caseInstance.getAdminState()) && (caseInstance.getStage() == null || caseInstance.getStage().isBlank())) {
			reasons.add(AdminHealthReasonCode.MISSING_STAGE.getCode());
			malformed = true;
		}

		String expectedStage = expectedStageForState(caseInstance.getAdminState());
		if (expectedStage != null && caseInstance.getStage() != null && !expectedStage.equals(caseInstance.getStage())) {
			reasons.add(AdminHealthReasonCode.STAGE_STATE_MISMATCH.getCode());
			malformed = true;
		}

		if (isNonTerminalState(caseInstance.getAdminState())
				&& (caseInstance.getNextActionSummary() == null || caseInstance.getNextActionSummary().isBlank())) {
			reasons.add(AdminHealthReasonCode.MISSING_NEXT_ACTION_SUMMARY.getCode());
			malformed = true;
		}

		if (isNonTerminalState(caseInstance.getAdminState())
				&& (caseInstance.getNextActionOwnerType() == null || caseInstance.getNextActionOwnerType().isBlank())) {
			reasons.add(AdminHealthReasonCode.MISSING_NEXT_ACTION_OWNER.getCode());
			malformed = true;
		}

		if ((currentState == AdminState.READY_FOR_LAWYER || currentState == AdminState.MAINTENANCE_LAWYER_REVIEW)
				&& (caseInstance.getResponsibleLawyerId() == null || caseInstance.getResponsibleLawyerId().isBlank())) {
			reasons.add(AdminHealthReasonCode.MISSING_RESPONSIBLE_LAWYER.getCode());
			malformed = true;
		}

		if (isWaitingState(caseInstance.getAdminState())) {
			if (caseInstance.getWaitingSince() == null || caseInstance.getWaitingSince().isBlank()) {
				reasons.add(AdminHealthReasonCode.MISSING_WAITING_SINCE.getCode());
				malformed = true;
			}
			if (caseInstance.getResumeToState() == null || caseInstance.getResumeToState().isBlank()) {
				reasons.add(AdminHealthReasonCode.MISSING_RESUME_TO_STATE.getCode());
				malformed = true;
			}
			if (caseInstance.getExpectedResponseAt() == null || caseInstance.getExpectedResponseAt().isBlank()) {
				reasons.add(AdminHealthReasonCode.MISSING_EXPECTED_RESPONSE_AT.getCode());
				malformed = true;
			}
			if (isExternalWaitState(caseInstance.getAdminState())
					&& (caseInstance.getExternalPartyRef() == null || caseInstance.getExternalPartyRef().isBlank())) {
				reasons.add(AdminHealthReasonCode.MISSING_EXTERNAL_PARTY_REF.getCode());
				malformed = true;
			}
			if ((isMaintenanceClientWaitState(caseInstance.getAdminState()) || isExternalWaitState(caseInstance.getAdminState()))
					&& !isValidMaintenanceResumeTarget(caseInstance.getResumeToState())) {
				reasons.add(AdminHealthReasonCode.INVALID_RESUME_TARGET.getCode());
				malformed = true;
			}
		}

		if ((currentState == AdminState.OPENED || currentState == AdminState.ACTIVE)
				&& (caseInstance.getAdminOwnerId() == null || caseInstance.getAdminOwnerId().isBlank())) {
			reasons.add(AdminHealthReasonCode.UNOWNED_ACTIVE_CASE.getCode());
			malformed = true;
		}

		LocalDate nextActionDueAt = parseDate(caseInstance.getNextActionDueAt());
		if (nextActionDueAt != null) {
			long daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(today, nextActionDueAt);
			if (daysUntilDue < 0) {
				reasons.add(AdminHealthReasonCode.DUE_RISK.getCode());
				staleSince = caseInstance.getNextActionDueAt();
			} else if (daysUntilDue <= 2) {
				reasons.add(AdminHealthReasonCode.DUE_RISK.getCode());
			}
			if (currentState == AdminState.MAINTENANCE_LAWYER_REVIEW && daysUntilDue <= 2) {
				reasons.add(AdminHealthReasonCode.LAWYER_RESPONSE_STALE.getCode());
			}
		}

		LocalDate waitingSince = parseDate(caseInstance.getWaitingSince());
		if (waitingSince != null && isWaitingState(caseInstance.getAdminState())) {
			long waitingDays = java.time.temporal.ChronoUnit.DAYS.between(waitingSince, today);
			if (waitingDays >= 10) {
				reasons.add(AdminHealthReasonCode.WAITING_STALE.getCode());
				staleSince = caseInstance.getWaitingSince();
			} else if (waitingDays >= 5) {
				reasons.add(AdminHealthReasonCode.WAITING_STALE.getCode());
			}
			if (isExternalWaitState(caseInstance.getAdminState()) && waitingDays >= 5) {
				reasons.add(AdminHealthReasonCode.EXTERNAL_RESPONSE_STALE.getCode());
			}
		}

		if (malformed) {
			reasons.add(AdminHealthReasonCode.CONTROL_INCOMPLETE.getCode());
		}

		List<String> distinctReasons = reasons.stream().distinct().toList();
		String health = AdminHealth.GREEN.getCode();
		boolean overdueDueRisk = distinctReasons.contains(AdminHealthReasonCode.DUE_RISK.getCode()) && nextActionDueAt != null
				&& nextActionDueAt.isBefore(today);
		boolean overdueWaitingRisk = distinctReasons.contains(AdminHealthReasonCode.WAITING_STALE.getCode())
				&& waitingSince != null && java.time.temporal.ChronoUnit.DAYS.between(waitingSince, today) >= 10;
		if (malformed || overdueDueRisk || overdueWaitingRisk) {
			health = AdminHealth.RED.getCode();
		} else if (!distinctReasons.isEmpty()) {
			health = AdminHealth.AMBER.getCode();
		}

		return AdminControlEvaluation.builder().adminHealth(health).healthReasonCodes(distinctReasons)
				.healthEvaluatedAt(nowTimestamp()).staleSince(staleSince).malformedCase(malformed).build();
	}

	public static void applyEvaluation(CaseInstance caseInstance) {
		AdminControlEvaluation evaluation = evaluate(caseInstance);
		caseInstance.setAdminHealth(evaluation.getAdminHealth());
		caseInstance.setHealthReasonCodes(evaluation.getHealthReasonCodes());
		caseInstance.setHealthEvaluatedAt(evaluation.getHealthEvaluatedAt());
		caseInstance.setStaleSince(evaluation.getStaleSince());
		caseInstance.setMalformedCase(evaluation.getMalformedCase());
	}

	public static AdminState normalizedState(String adminState) {
		return AdminState.fromValue(adminState).orElse(null);
	}

	public static boolean isValidMaintenanceResumeTarget(String resumeToState) {
		AdminState state = normalizedState(resumeToState);
		return state == AdminState.ACTIVE || state == AdminState.MAINTENANCE_LAWYER_REVIEW;
	}
}
