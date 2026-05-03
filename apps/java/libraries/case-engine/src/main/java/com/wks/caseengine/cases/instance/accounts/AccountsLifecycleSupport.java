package com.wks.caseengine.cases.instance.accounts;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;

import com.wks.caseengine.cases.instance.CaseInstance;

public final class AccountsLifecycleSupport {

	private AccountsLifecycleSupport() {
	}

	public static boolean initializeShellIfMissing(CaseInstance caseInstance, AccountsTransitionRequest request) {
		if (caseInstance == null || caseInstance.getAccountsState() != null) {
			return false;
		}

		caseInstance.setAccountsStage(AccountsLifecycleStage.SETUP.getCode());
		caseInstance.setAccountsState(AccountsState.AWAITING_BILLING_SETUP.getCode());
		caseInstance.setAccountsMalformedCase(false);
		if (caseInstance.getAccountsHealthReasonCodes() == null) {
			caseInstance.setAccountsHealthReasonCodes(new ArrayList<>());
		}
		caseInstance.addAccountsEvent(buildEvent(caseInstance, request, AccountsEventType.ACCOUNTS_INITIALIZED, null,
				caseInstance.getAccountsState(), null, caseInstance.getAccountsStage()));
		return true;
	}

	public static AccountsEvent buildEvent(CaseInstance caseInstance, AccountsTransitionRequest request,
			AccountsEventType eventType, String fromState, String toState, String fromStage, String toStage) {
		AccountsTransitionRequest safeRequest = request != null ? request : AccountsTransitionRequest.builder().build();
		return AccountsEvent.builder().eventType(eventType.getCode()).occurredAt(new Date())
				.actorType(safeRequest.getActorType())
				.actorId(AccountsLifecycleAccessSupport.currentUserId().orElse(null))
				.actorName(safeRequest.getActorName()).fromState(fromState).toState(toState).fromStage(fromStage)
				.toStage(toStage).reasonCode(safeRequest.getReasonCode()).note(safeRequest.getNote()).build();
	}

	public static String nowTimestamp() {
		return OffsetDateTime.now().toString();
	}
}
