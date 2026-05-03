package com.wks.caseengine.cases.instance.accounts;

import java.time.LocalDate;
import java.util.List;

import com.wks.caseengine.cases.instance.CaseInstance;

public final class AccountsWorkSupport {

	private AccountsWorkSupport() {
	}

	public static void applyWorkSignals(CaseInstance caseInstance, AccountsReadinessEvaluation readiness) {
		if (caseInstance == null || readiness == null) {
			return;
		}
		List<String> reasons = readiness.getAccountsReadinessReasonCodes() != null
				? readiness.getAccountsReadinessReasonCodes()
				: List.of();
		String queueId = deriveQueueId(readiness.getAccountsReadinessStatus(), reasons);
		AccountsWorkOwnerType ownerType = deriveOwnerType(reasons);
		boolean blocked = AccountsReadinessStatus.BLOCKED.getCode().equals(readiness.getAccountsReadinessStatus());

		caseInstance.setAccountsQueueId(queueId);
		caseInstance.setAccountsNextActionOwnerType(ownerType.getCode());
		caseInstance.setAccountsNextActionSummary(deriveSummary(queueId));
		caseInstance.setAccountsNextActionDueAt(deriveDueAt(readiness.getAccountsReadinessStatus()));
		caseInstance.setAccountsWorkBlocked(blocked);
		caseInstance.setAccountsWorkPriority(derivePriority(readiness.getAccountsReadinessStatus()).getCode());
	}

	public static String deriveQueueId(String readinessStatus, List<String> reasonCodes) {
		List<String> reasons = reasonCodes != null ? reasonCodes : List.of();
		if (reasons.contains(AccountsHealthReasonCode.MALFORMED_CONFIGURATION.getCode())
				|| AccountsReadinessStatus.BLOCKED.getCode().equals(readinessStatus)) {
			return AccountsWorkQueue.MALFORMED_ACCOUNTS_CONFIGURATION;
		}
		if (reasons.contains(AccountsHealthReasonCode.MISSING_COUNSEL_CONFIGURATION.getCode())
				|| reasons.contains(AccountsHealthReasonCode.MISSING_INSTRUCTING_FIRM.getCode())
				|| reasons.contains(AccountsHealthReasonCode.MISSING_COUNSEL_BILLING_MODE.getCode())
				|| reasons.contains(AccountsHealthReasonCode.INVALID_BILLING_PARTY_MODEL.getCode())) {
			return AccountsWorkQueue.COUNSEL_BILLING_SETUP;
		}
		if (reasons.contains(AccountsHealthReasonCode.MISSING_SUBSCRIPTION_CONFIGURATION.getCode())) {
			return AccountsWorkQueue.MISSING_SUBSCRIPTION_SETUP;
		}
		if (reasons.contains(AccountsHealthReasonCode.MISSING_RETAINER.getCode())) {
			return AccountsWorkQueue.MISSING_RETAINER;
		}
		if (reasons.contains(AccountsHealthReasonCode.MISSING_PAYMENT_METHOD.getCode())) {
			return AccountsWorkQueue.MISSING_PAYMENT_METHOD;
		}
		if (AccountsReadinessStatus.READY.getCode().equals(readinessStatus)) {
			return AccountsWorkQueue.ACCOUNTS_READY;
		}
		if (AccountsReadinessStatus.NOT_READY.getCode().equals(readinessStatus)) {
			return AccountsWorkQueue.ACCOUNTS_NOT_READY;
		}
		return AccountsWorkQueue.ACCOUNTS_BLOCKED;
	}

	private static AccountsWorkOwnerType deriveOwnerType(List<String> reasonCodes) {
		if (reasonCodes.contains(AccountsHealthReasonCode.MISSING_PAYMENT_METHOD.getCode())) {
			return AccountsWorkOwnerType.CLIENT;
		}
		if (reasonCodes.contains(AccountsHealthReasonCode.MISSING_INSTRUCTING_FIRM.getCode())
				|| reasonCodes.contains(AccountsHealthReasonCode.INVALID_BILLING_PARTY_MODEL.getCode())) {
			return AccountsWorkOwnerType.INSTRUCTING_FIRM;
		}
		if (reasonCodes.contains(AccountsHealthReasonCode.MALFORMED_CONFIGURATION.getCode())) {
			return AccountsWorkOwnerType.SYSTEM;
		}
		return AccountsWorkOwnerType.ACCOUNTS;
	}

	private static String deriveSummary(String queueId) {
		return switch (queueId) {
		case AccountsWorkQueue.ACCOUNTS_READY -> "Accounts setup is ready for administrative opening";
		case AccountsWorkQueue.MISSING_PAYMENT_METHOD -> "Collect or authorize payment method";
		case AccountsWorkQueue.MISSING_RETAINER -> "Confirm retainer setup and funding";
		case AccountsWorkQueue.MISSING_SUBSCRIPTION_SETUP -> "Complete subscription setup";
		case AccountsWorkQueue.COUNSEL_BILLING_SETUP -> "Complete counsel billing setup";
		case AccountsWorkQueue.MALFORMED_ACCOUNTS_CONFIGURATION -> "Correct malformed accounts configuration";
		default -> "Complete accounts setup";
		};
	}

	private static String deriveDueAt(String readinessStatus) {
		if (AccountsReadinessStatus.READY.getCode().equals(readinessStatus)) {
			return null;
		}
		int days = AccountsReadinessStatus.BLOCKED.getCode().equals(readinessStatus) ? 0 : 3;
		return LocalDate.now().plusDays(days).toString();
	}

	private static AccountsWorkPriority derivePriority(String readinessStatus) {
		if (AccountsReadinessStatus.BLOCKED.getCode().equals(readinessStatus)) {
			return AccountsWorkPriority.URGENT;
		}
		if (AccountsReadinessStatus.NOT_READY.getCode().equals(readinessStatus)) {
			return AccountsWorkPriority.HIGH;
		}
		return AccountsWorkPriority.NORMAL;
	}
}
