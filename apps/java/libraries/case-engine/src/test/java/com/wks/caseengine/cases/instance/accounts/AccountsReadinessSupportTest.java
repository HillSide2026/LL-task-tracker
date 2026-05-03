package com.wks.caseengine.cases.instance.accounts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.wks.caseengine.cases.instance.CaseInstance;

class AccountsReadinessSupportTest {

	@Test
	void shouldBlockWhenMatterTypeIsMissing() {
		AccountsReadinessEvaluation evaluation = AccountsReadinessSupport.evaluate(CaseInstance.builder().build());

		assertEquals(AccountsReadinessStatus.BLOCKED.getCode(), evaluation.getAccountsReadinessStatus());
		assertTrue(evaluation.getAccountsReadinessReasonCodes()
				.contains(AccountsHealthReasonCode.MISSING_MATTER_TYPE.getCode()));
	}

	@Test
	void shouldBlockMalformedProfile() {
		AccountsReadinessEvaluation evaluation = AccountsReadinessSupport.evaluate(completeFlatFeeCase()
				.accountsProfile(AccountsPolicyProfile.COUNSEL_PROFILE.getCode()).build());

		assertEquals(AccountsReadinessStatus.BLOCKED.getCode(), evaluation.getAccountsReadinessStatus());
		assertTrue(evaluation.getAccountsReadinessReasonCodes()
				.contains(AccountsHealthReasonCode.MALFORMED_CONFIGURATION.getCode()));
	}

	@Test
	void shouldReturnNotReadyWhenSetupIsIncomplete() {
		AccountsReadinessEvaluation evaluation = AccountsReadinessSupport.evaluate(CaseInstance.builder()
				.matterType(MatterType.HOURLY_CREDIT_CARD.getCode()).billingSetupComplete(true)
				.billingMode(BillingMode.HOURLY_CARD.getCode()).build());

		assertEquals(AccountsReadinessStatus.NOT_READY.getCode(), evaluation.getAccountsReadinessStatus());
		assertTrue(evaluation.getAccountsReadinessReasonCodes()
				.contains(AccountsHealthReasonCode.MISSING_PAYMENT_METHOD.getCode()));
	}

	@Test
	void shouldReturnReadyWhenSetupIsComplete() {
		AccountsReadinessEvaluation evaluation = AccountsReadinessSupport.evaluate(completeFlatFeeCase().build());

		assertEquals(AccountsReadinessStatus.READY.getCode(), evaluation.getAccountsReadinessStatus());
		assertTrue(evaluation.getAccountsReadinessReasonCodes().isEmpty());
	}

	@Test
	void shouldApplyEvaluationToCaseInstance() {
		CaseInstance caseInstance = completeFlatFeeCase().build();

		AccountsReadinessEvaluation evaluation = AccountsReadinessSupport.applyEvaluation(caseInstance);

		assertTrue(evaluation.isReady());
		assertEquals(AccountsReadinessStatus.READY.getCode(), caseInstance.getAccountsReadinessStatus());
		assertEquals(AccountsPolicyProfile.FLAT_FEE_PROFILE.getCode(), caseInstance.getAccountsProfile());
		assertEquals(AccountsWorkQueue.ACCOUNTS_READY, caseInstance.getAccountsQueueId());
	}

	@Test
	void shouldNotAutoCorrectMalformedProfileWhileBlockingReadiness() {
		CaseInstance caseInstance = completeFlatFeeCase()
				.accountsProfile(AccountsPolicyProfile.COUNSEL_PROFILE.getCode()).build();

		AccountsReadinessEvaluation evaluation = AccountsReadinessSupport.applyEvaluation(caseInstance);

		assertEquals(AccountsReadinessStatus.BLOCKED.getCode(), evaluation.getAccountsReadinessStatus());
		assertEquals(AccountsPolicyProfile.COUNSEL_PROFILE.getCode(), caseInstance.getAccountsProfile());
		assertTrue(caseInstance.getAccountsReadinessReasonCodes()
				.contains(AccountsHealthReasonCode.MALFORMED_CONFIGURATION.getCode()));
		assertEquals(AccountsWorkQueue.MALFORMED_ACCOUNTS_CONFIGURATION, caseInstance.getAccountsQueueId());
	}

	private CaseInstance.CaseInstanceBuilder completeFlatFeeCase() {
		return CaseInstance.builder().matterType(MatterType.FLAT_FEE.getCode()).billingSetupComplete(true)
				.flatFeeAmount("2500").billingMode(BillingMode.FLAT_FEE.getCode());
	}
}
