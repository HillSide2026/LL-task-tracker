package com.wks.caseengine.cases.instance.command;

import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceNotFoundException;
import com.wks.caseengine.cases.instance.accounts.AccountsEventType;
import com.wks.caseengine.cases.instance.accounts.AccountsLifecycleStage;
import com.wks.caseengine.cases.instance.accounts.AccountsLifecycleAccessSupport;
import com.wks.caseengine.cases.instance.accounts.AccountsLifecycleException;
import com.wks.caseengine.cases.instance.accounts.AccountsLifecycleSupport;
import com.wks.caseengine.cases.instance.accounts.AccountsPolicy;
import com.wks.caseengine.cases.instance.accounts.AccountsPolicyEvaluation;
import com.wks.caseengine.cases.instance.accounts.AccountsPolicySupport;
import com.wks.caseengine.cases.instance.accounts.AccountsReadinessSupport;
import com.wks.caseengine.cases.instance.accounts.AccountsState;
import com.wks.caseengine.cases.instance.accounts.AccountsTransition;
import com.wks.caseengine.cases.instance.accounts.AccountsTransitionRequest;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransitionCaseAccountsCmd implements Command<CaseInstance> {

	private final String businessKey;
	private final AccountsTransition transition;
	private final AccountsTransitionRequest request;

	@Override
	public CaseInstance execute(CommandContext commandContext) {
		AccountsLifecycleAccessSupport.assertCanTransition();

		CaseInstance caseInstance;
		try {
			caseInstance = commandContext.getCaseInstanceRepository().get(businessKey);
		} catch (DatabaseRecordNotFoundException e) {
			throw new CaseInstanceNotFoundException(e.getMessage(), e);
		}

		AccountsLifecycleSupport.initializeShellIfMissing(caseInstance, request);

		AccountsPolicyEvaluation transitionEvaluation = AccountsPolicySupport.evaluateTransition(caseInstance, transition);
		if (!transitionEvaluation.isPassed()) {
			if (transition == AccountsTransition.ACTIVATE_ACCOUNTS) {
				AccountsReadinessSupport.applyEvaluation(caseInstance);
			}
			persist(commandContext, caseInstance);
			throw new AccountsLifecycleException("Accounts transition is not valid for this matter type",
					transitionEvaluation.getReasonCodes());
		}
		AccountsPolicySupport.resolvePolicyDefinition(caseInstance)
				.map(AccountsPolicy::getProfile)
				.ifPresent(profile -> caseInstance.setAccountsProfile(profile.getCode()));

		if (transition != AccountsTransition.ACTIVATE_ACCOUNTS) {
			persist(commandContext, caseInstance);
			throw new AccountsLifecycleException("Accounts transition " + transition.getCode() + " is not supported");
		}

		AccountsPolicyEvaluation activationEvaluation = AccountsPolicySupport.evaluateActivation(caseInstance);
		if (!activationEvaluation.isPassed()) {
			AccountsReadinessSupport.applyEvaluation(caseInstance);
			persist(commandContext, caseInstance);
			throw new AccountsLifecycleException("Accounts activation blocked by policy",
					activationEvaluation.getReasonCodes());
		}

		String fromState = caseInstance.getAccountsState();
		String fromStage = caseInstance.getAccountsStage();
		caseInstance.setAccountsStage(AccountsLifecycleStage.ACTIVE.getCode());
		caseInstance.setAccountsState(AccountsState.ACTIVE_CURRENT.getCode());
		caseInstance.addAccountsEvent(AccountsLifecycleSupport.buildEvent(caseInstance, request,
				AccountsEventType.ACCOUNTS_ACTIVATED, fromState, caseInstance.getAccountsState(), fromStage,
				caseInstance.getAccountsStage()));
		AccountsReadinessSupport.applyEvaluation(caseInstance);
		persist(commandContext, caseInstance);
		return caseInstance;
	}

	private void persist(CommandContext commandContext, CaseInstance caseInstance) {
		try {
			commandContext.getCaseInstanceRepository().update(businessKey, caseInstance);
		} catch (DatabaseRecordNotFoundException e) {
			throw new CaseInstanceNotFoundException(e.getMessage(), e);
		}
	}
}
