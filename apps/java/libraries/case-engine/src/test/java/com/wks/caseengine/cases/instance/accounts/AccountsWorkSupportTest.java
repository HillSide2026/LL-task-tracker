package com.wks.caseengine.cases.instance.accounts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.wks.caseengine.cases.instance.CaseInstance;

class AccountsWorkSupportTest {

	@Test
	void shouldMapMissingRetainerToAccountsOwnedWorkQueueWithoutChangingAccountsState() {
		CaseInstance caseInstance = CaseInstance.builder()
				.accountsState(AccountsState.AWAITING_RETAINER_FUNDING.getCode()).build();
		AccountsReadinessEvaluation readiness = AccountsReadinessEvaluation.builder()
				.accountsReadinessStatus(AccountsReadinessStatus.NOT_READY.getCode())
				.accountsReadinessReasonCodes(List.of(AccountsHealthReasonCode.MISSING_RETAINER.getCode())).build();

		AccountsWorkSupport.applyWorkSignals(caseInstance, readiness);

		assertEquals(AccountsState.AWAITING_RETAINER_FUNDING.getCode(), caseInstance.getAccountsState());
		assertEquals(AccountsWorkQueue.MISSING_RETAINER, caseInstance.getAccountsQueueId());
		assertEquals(AccountsWorkOwnerType.ACCOUNTS.getCode(), caseInstance.getAccountsNextActionOwnerType());
		assertEquals(false, caseInstance.getAccountsWorkBlocked());
		assertEquals(AccountsWorkPriority.HIGH.getCode(), caseInstance.getAccountsWorkPriority());
	}

	@Test
	void shouldMapMalformedConfigurationToBlockedSystemWork() {
		CaseInstance caseInstance = CaseInstance.builder().build();
		AccountsReadinessEvaluation readiness = AccountsReadinessEvaluation.builder()
				.accountsReadinessStatus(AccountsReadinessStatus.BLOCKED.getCode())
				.accountsReadinessReasonCodes(List.of(AccountsHealthReasonCode.MALFORMED_CONFIGURATION.getCode())).build();

		AccountsWorkSupport.applyWorkSignals(caseInstance, readiness);

		assertEquals(AccountsWorkQueue.MALFORMED_ACCOUNTS_CONFIGURATION, caseInstance.getAccountsQueueId());
		assertEquals(AccountsWorkOwnerType.SYSTEM.getCode(), caseInstance.getAccountsNextActionOwnerType());
		assertEquals(true, caseInstance.getAccountsWorkBlocked());
		assertEquals(AccountsWorkPriority.URGENT.getCode(), caseInstance.getAccountsWorkPriority());
	}

	@Test
	void shouldMapReadyMatterToReadyQueueWithNoDueDate() {
		CaseInstance caseInstance = CaseInstance.builder().build();
		AccountsReadinessEvaluation readiness = AccountsReadinessEvaluation.builder()
				.accountsReadinessStatus(AccountsReadinessStatus.READY.getCode())
				.accountsReadinessReasonCodes(List.of()).build();

		AccountsWorkSupport.applyWorkSignals(caseInstance, readiness);

		assertEquals(AccountsWorkQueue.ACCOUNTS_READY, caseInstance.getAccountsQueueId());
		assertEquals(AccountsWorkOwnerType.ACCOUNTS.getCode(), caseInstance.getAccountsNextActionOwnerType());
		assertEquals(null, caseInstance.getAccountsNextActionDueAt());
		assertEquals(AccountsWorkPriority.NORMAL.getCode(), caseInstance.getAccountsWorkPriority());
	}
}
