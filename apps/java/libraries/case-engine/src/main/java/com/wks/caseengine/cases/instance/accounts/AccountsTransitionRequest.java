package com.wks.caseengine.cases.instance.accounts;

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
public class AccountsTransitionRequest {

	private String transition;

	private String actorType;

	private String actorName;

	private String note;

	private String reasonCode;
}
