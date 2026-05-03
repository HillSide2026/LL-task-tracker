package com.wks.caseengine.cases.instance.accounts;

import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AccountsPolicy {

	private MatterType matterType;

	private AccountsPolicyProfile profile;

	private List<String> requiredSetupFields;

	private Set<AccountsState> validStates;

	private Set<AccountsTransition> validTransitions;

	private List<String> activationPrerequisites;

	private List<String> closurePrerequisites;
}
