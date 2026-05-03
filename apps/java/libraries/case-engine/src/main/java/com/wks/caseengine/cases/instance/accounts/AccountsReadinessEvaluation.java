package com.wks.caseengine.cases.instance.accounts;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AccountsReadinessEvaluation {

	private String accountsReadinessStatus;

	@Builder.Default
	private List<String> accountsReadinessReasonCodes = new ArrayList<>();

	private String accountsReadinessEvaluatedAt;

	private String accountsReadinessSummary;

	public boolean isReady() {
		return AccountsReadinessStatus.READY.getCode().equals(accountsReadinessStatus);
	}
}
