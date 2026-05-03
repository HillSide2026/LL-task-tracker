package com.wks.caseengine.cases.instance.accounts;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountsControlEvaluation {

	private String accountsHealth;

	@Builder.Default
	private List<String> accountsHealthReasonCodes = new ArrayList<>();

	private String accountsHealthEvaluatedAt;

	private String accountsStaleSince;

	private Boolean accountsMalformedCase;
}
