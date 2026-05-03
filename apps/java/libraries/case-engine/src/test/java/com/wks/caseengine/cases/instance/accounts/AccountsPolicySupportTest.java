package com.wks.caseengine.cases.instance.accounts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.wks.caseengine.cases.instance.CaseInstance;

class AccountsPolicySupportTest {

	@Test
	void shouldResolvePolicyProfileForEachCanonicalMatterType() {
		Map<MatterType, AccountsPolicyProfile> expectedProfiles = Map.of(
				MatterType.FLAT_FEE, AccountsPolicyProfile.FLAT_FEE_PROFILE,
				MatterType.HOURLY_CREDIT_CARD, AccountsPolicyProfile.HOURLY_CREDIT_CARD_PROFILE,
				MatterType.HOURLY_RETAINER, AccountsPolicyProfile.HOURLY_RETAINER_PROFILE,
				MatterType.SUBSCRIPTION_CREDIT_CARD, AccountsPolicyProfile.SUBSCRIPTION_CREDIT_CARD_PROFILE,
				MatterType.SUBSCRIPTION_RETAINER, AccountsPolicyProfile.SUBSCRIPTION_RETAINER_PROFILE,
				MatterType.COUNSEL, AccountsPolicyProfile.COUNSEL_PROFILE);

		expectedProfiles.forEach((matterType, profile) -> assertEquals(profile, AccountsPolicySupport
				.resolvePolicy(CaseInstance.builder().matterType(matterType.getCode()).build()).orElse(null)));
	}

	@Test
	void shouldReturnEmptyWhenMatterTypeIsMissingOrUnknown() {
		assertTrue(AccountsPolicySupport.resolvePolicy(CaseInstance.builder().build()).isEmpty());
		assertTrue(AccountsPolicySupport.resolvePolicy(CaseInstance.builder().matterType("PRODUCT").build()).isEmpty());
	}

	@Test
	void shouldBlockActivationWhenMatterTypeIsMissing() {
		AccountsPolicyEvaluation evaluation = AccountsPolicySupport.evaluateActivation(CaseInstance.builder().build());

		assertFalse(evaluation.isPassed());
		assertTrue(evaluation.getReasonCodes().contains(AccountsHealthReasonCode.MISSING_MATTER_TYPE.getCode()));
	}

	@Test
	void shouldRequireFlatFeeSetupForFlatFeeActivation() {
		AccountsPolicyEvaluation evaluation = AccountsPolicySupport.evaluateActivation(
				baseCase(MatterType.FLAT_FEE).billingMode(BillingMode.FLAT_FEE.getCode()).build());

		assertFalse(evaluation.isPassed());
		assertTrue(evaluation.getReasonCodes().contains(AccountsHealthReasonCode.MISSING_BILLING_SETUP.getCode()));
		assertTrue(evaluation.getReasonCodes().contains(AccountsHealthReasonCode.MISSING_FLAT_FEE_SETUP.getCode()));
	}

	@Test
	void shouldRequirePaymentMethodForHourlyCreditCardActivation() {
		AccountsPolicyEvaluation evaluation = AccountsPolicySupport.evaluateActivation(
				baseCase(MatterType.HOURLY_CREDIT_CARD).billingSetupComplete(true)
						.billingMode(BillingMode.HOURLY_CARD.getCode()).build());

		assertFalse(evaluation.isPassed());
		assertTrue(evaluation.getReasonCodes().contains(AccountsHealthReasonCode.MISSING_PAYMENT_METHOD.getCode()));
	}

	@Test
	void shouldRequireRetainerForHourlyRetainerActivation() {
		AccountsPolicyEvaluation evaluation = AccountsPolicySupport.evaluateActivation(
				baseCase(MatterType.HOURLY_RETAINER).billingSetupComplete(true)
						.billingMode(BillingMode.HOURLY_RETAINER.getCode()).build());

		assertFalse(evaluation.isPassed());
		assertTrue(evaluation.getReasonCodes().contains(AccountsHealthReasonCode.MISSING_RETAINER.getCode()));
	}

	@Test
	void shouldRequireSubscriptionAndPaymentMethodForSubscriptionCreditCardActivation() {
		AccountsPolicyEvaluation evaluation = AccountsPolicySupport.evaluateActivation(
				baseCase(MatterType.SUBSCRIPTION_CREDIT_CARD).billingSetupComplete(true)
						.billingMode(BillingMode.SUBSCRIPTION_CARD.getCode()).paymentMethodAuthorized(true)
						.paymentMethodRef("card-1").build());

		assertFalse(evaluation.isPassed());
		assertTrue(evaluation.getReasonCodes()
				.contains(AccountsHealthReasonCode.MISSING_SUBSCRIPTION_CONFIGURATION.getCode()));
	}

	@Test
	void shouldRequireSubscriptionAndRetainerForSubscriptionRetainerActivation() {
		AccountsPolicyEvaluation evaluation = AccountsPolicySupport.evaluateActivation(
				baseCase(MatterType.SUBSCRIPTION_RETAINER).billingSetupComplete(true)
						.billingMode(BillingMode.SUBSCRIPTION_RETAINER.getCode()).subscriptionActive(true)
						.subscriptionPlanId("plan-1").build());

		assertFalse(evaluation.isPassed());
		assertTrue(evaluation.getReasonCodes().contains(AccountsHealthReasonCode.MISSING_RETAINER.getCode()));
	}

	@Test
	void shouldRequireCounselSpecificConfiguration() {
		AccountsPolicyEvaluation evaluation = AccountsPolicySupport.evaluateActivation(
				baseCase(MatterType.COUNSEL).billingSetupComplete(true)
						.billingPartyModel(BillingPartyModel.DIRECT_CLIENT.getCode()).build());

		assertFalse(evaluation.isPassed());
		assertTrue(evaluation.getReasonCodes().contains(AccountsHealthReasonCode.MISSING_INSTRUCTING_FIRM.getCode()));
		assertTrue(evaluation.getReasonCodes()
				.contains(AccountsHealthReasonCode.MISSING_COUNSEL_BILLING_MODE.getCode()));
		assertTrue(evaluation.getReasonCodes().contains(AccountsHealthReasonCode.INVALID_BILLING_PARTY_MODEL.getCode()));
	}

	@Test
	void shouldPassCounselActivationWhenCounselConfigurationIsComplete() {
		AccountsPolicyEvaluation evaluation = AccountsPolicySupport.evaluateActivation(
				baseCase(MatterType.COUNSEL).billingSetupComplete(true)
						.billingPartyModel(BillingPartyModel.INSTRUCTING_FIRM.getCode()).instructingFirmName("Client Firm")
						.counselBillingMode(BillingMode.COUNSEL_HOURLY.getCode()).build());

		assertTrue(evaluation.isPassed());
	}

	@Test
	void shouldAllowCounselBillingPartyOverrideWhenExplicit() {
		AccountsPolicyEvaluation evaluation = AccountsPolicySupport.evaluateActivation(
				baseCase(MatterType.COUNSEL).billingSetupComplete(true)
						.billingPartyModel(BillingPartyModel.DIRECT_CLIENT.getCode()).counselBillingPartyOverride(true)
						.instructingFirmName("Client Firm").counselBillingMode(BillingMode.COUNSEL_FLAT_FEE.getCode())
						.build());

		assertTrue(evaluation.isPassed());
	}

	@Test
	void shouldRejectInvalidProfileTransition() {
		AccountsPolicyEvaluation evaluation = AccountsPolicySupport.evaluateTransition(
				completeFlatFeeCase().build(), AccountsTransition.ISSUE_INVOICE);

		assertFalse(evaluation.isPassed());
		assertTrue(evaluation.getReasonCodes().contains(AccountsHealthReasonCode.INVALID_ACCOUNTS_TRANSITION.getCode()));
	}

	@Test
	void shouldBlockMalformedProfileDuringActivationEvaluation() {
		AccountsPolicyEvaluation evaluation = AccountsPolicySupport.evaluateActivation(
				completeFlatFeeCase().accountsProfile(AccountsPolicyProfile.COUNSEL_PROFILE.getCode()).build());

		assertFalse(evaluation.isPassed());
		assertTrue(evaluation.getReasonCodes().contains(AccountsHealthReasonCode.MALFORMED_CONFIGURATION.getCode()));
	}

	@Test
	void shouldRejectRepeatedActivationFromActiveCurrent() {
		AccountsPolicyEvaluation evaluation = AccountsPolicySupport.evaluateTransition(
				completeFlatFeeCase().accountsState(AccountsState.ACTIVE_CURRENT.getCode()).build(),
				AccountsTransition.ACTIVATE_ACCOUNTS);

		assertFalse(evaluation.isPassed());
		assertTrue(evaluation.getReasonCodes().contains(AccountsHealthReasonCode.INVALID_ACCOUNTS_TRANSITION.getCode()));
	}

	private CaseInstance.CaseInstanceBuilder baseCase(MatterType matterType) {
		return CaseInstance.builder().matterType(matterType.getCode())
				.accountsState(AccountsState.AWAITING_BILLING_SETUP.getCode());
	}

	private CaseInstance.CaseInstanceBuilder completeFlatFeeCase() {
		return baseCase(MatterType.FLAT_FEE).billingSetupComplete(true).flatFeeAmount("2500")
				.billingMode(BillingMode.FLAT_FEE.getCode());
	}
}
