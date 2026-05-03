package com.wks.caseengine.cases.instance.accounts;

import java.util.ArrayList;
import java.util.List;

import com.wks.caseengine.cases.instance.CaseInstance;

public final class AccountsReadinessSupport {

	private AccountsReadinessSupport() {
	}

	public static AccountsReadinessEvaluation evaluate(CaseInstance caseInstance) {
		List<String> reasonCodes = new ArrayList<>();
		AccountsReadinessStatus status;

		if (caseInstance == null || isBlank(caseInstance.getMatterType())) {
			reasonCodes.add(AccountsHealthReasonCode.MISSING_MATTER_TYPE.getCode());
			status = AccountsReadinessStatus.BLOCKED;
		} else if (AccountsPolicySupport.resolvePolicyDefinition(caseInstance).isEmpty()) {
			reasonCodes.add(AccountsHealthReasonCode.MALFORMED_CONFIGURATION.getCode());
			status = AccountsReadinessStatus.BLOCKED;
		} else if (AccountsPolicySupport.profileMalformed(caseInstance)) {
			reasonCodes.add(AccountsHealthReasonCode.MALFORMED_CONFIGURATION.getCode());
			status = AccountsReadinessStatus.BLOCKED;
		} else {
			AccountsPolicyEvaluation policyEvaluation = AccountsPolicySupport.evaluateActivation(caseInstance);
			reasonCodes.addAll(policyEvaluation.getReasonCodes());
			status = policyEvaluation.isPassed() ? AccountsReadinessStatus.READY : AccountsReadinessStatus.NOT_READY;
		}

		String summary = switch (status) {
		case READY -> "Accounts setup is ready for administrative opening";
		case NOT_READY -> "Accounts setup is incomplete";
		case BLOCKED -> "Accounts readiness cannot be evaluated until configuration is corrected";
		case NOT_EVALUATED -> "Accounts readiness has not been evaluated";
		};

		return AccountsReadinessEvaluation.builder().accountsReadinessStatus(status.getCode())
				.accountsReadinessReasonCodes(reasonCodes).accountsReadinessEvaluatedAt(AccountsLifecycleSupport.nowTimestamp())
				.accountsReadinessSummary(summary).build();
	}

	public static AccountsReadinessEvaluation applyEvaluation(CaseInstance caseInstance) {
		AccountsReadinessEvaluation evaluation = evaluate(caseInstance);
		if (caseInstance != null) {
			caseInstance.setAccountsReadinessStatus(evaluation.getAccountsReadinessStatus());
			caseInstance.setAccountsReadinessReasonCodes(evaluation.getAccountsReadinessReasonCodes());
			caseInstance.setAccountsReadinessEvaluatedAt(evaluation.getAccountsReadinessEvaluatedAt());
			caseInstance.setAccountsReadinessSummary(evaluation.getAccountsReadinessSummary());
			AccountsWorkSupport.applyWorkSignals(caseInstance, evaluation);
			if (!AccountsPolicySupport.profileMalformed(caseInstance)) {
				AccountsPolicySupport.resolvePolicy(caseInstance)
						.ifPresent(profile -> caseInstance.setAccountsProfile(profile.getCode()));
			}
		}
		return evaluation;
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
