package com.wks.caseengine.cases.instance.accounts;

import com.wks.caseengine.cases.instance.CaseInstance;

public final class AccountsHealthSupport {

	private AccountsHealthSupport() {
	}

	public static AccountsControlEvaluation evaluate(CaseInstance caseInstance) {
		return AccountsControlEvaluation.builder()
				.accountsHealth(caseInstance != null ? caseInstance.getAccountsHealth() : null)
				.accountsHealthReasonCodes(caseInstance != null && caseInstance.getAccountsHealthReasonCodes() != null
						? caseInstance.getAccountsHealthReasonCodes()
						: java.util.List.of())
				.accountsHealthEvaluatedAt(caseInstance != null ? caseInstance.getAccountsHealthEvaluatedAt() : null)
				.accountsStaleSince(caseInstance != null ? caseInstance.getAccountsStaleSince() : null)
				.accountsMalformedCase(caseInstance != null ? caseInstance.getAccountsMalformedCase() : null).build();
	}
}
