package com.wks.caseengine.cases.instance.accounts;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AccountsPolicyEvaluation {

	@Builder.Default
	private List<String> reasonCodes = new ArrayList<>();

	public boolean isPassed() {
		return reasonCodes == null || reasonCodes.isEmpty();
	}
}
