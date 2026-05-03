package com.wks.caseengine.cases.instance.accounts;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.wks.caseengine.cases.instance.CaseInstance;

public final class AccountsPolicySupport {

	private static final Set<AccountsState> SETUP_TO_ACTIVE_STATES = Set.of(AccountsState.AWAITING_BILLING_SETUP,
			AccountsState.AWAITING_PAYMENT_METHOD, AccountsState.AWAITING_RETAINER_FUNDING,
			AccountsState.AWAITING_ENGAGEMENT_TERMS, AccountsState.READY_FOR_ACTIVATION, AccountsState.ACTIVE_CURRENT,
			AccountsState.CLOSED);
	private static final Set<AccountsTransition> PHASE_3_TRANSITIONS = Set.of(AccountsTransition.ACTIVATE_ACCOUNTS);
	private static final Map<MatterType, AccountsPolicy> POLICIES = buildPolicies();

	private AccountsPolicySupport() {
	}

	public static Optional<AccountsPolicyProfile> resolvePolicy(CaseInstance caseInstance) {
		return resolvePolicyDefinition(caseInstance).map(AccountsPolicy::getProfile);
	}

	public static AccountsPolicyProfile resolvePolicy(MatterType matterType) {
		return policyFor(matterType).getProfile();
	}

	public static Optional<AccountsPolicy> resolvePolicyDefinition(CaseInstance caseInstance) {
		if (caseInstance == null) {
			return Optional.empty();
		}
		return MatterType.fromValue(caseInstance.getMatterType()).map(AccountsPolicySupport::policyFor);
	}

	public static AccountsPolicy policyFor(MatterType matterType) {
		return POLICIES.get(matterType);
	}

	public static AccountsPolicyEvaluation evaluateTransition(CaseInstance caseInstance,
			AccountsTransition transition) {
		List<String> reasonCodes = new ArrayList<>();
		Optional<AccountsPolicy> policy = resolvePolicyDefinition(caseInstance);
		if (policy.isEmpty()) {
			reasonCodes.add(AccountsHealthReasonCode.MISSING_MATTER_TYPE.getCode());
			return AccountsPolicyEvaluation.builder().reasonCodes(reasonCodes).build();
		}
		if (profileMalformed(caseInstance)) {
			reasonCodes.add(AccountsHealthReasonCode.MALFORMED_CONFIGURATION.getCode());
		}
		if (!policy.get().getValidTransitions().contains(transition)) {
			reasonCodes.add(AccountsHealthReasonCode.INVALID_ACCOUNTS_TRANSITION.getCode());
		}
		if (caseInstance != null && caseInstance.getAccountsState() != null
				&& AccountsState.fromValue(caseInstance.getAccountsState())
						.filter(policy.get().getValidStates()::contains).isEmpty()) {
			reasonCodes.add(AccountsHealthReasonCode.INVALID_ACCOUNTS_STATE.getCode());
		}
		if (transition == AccountsTransition.ACTIVATE_ACCOUNTS && caseInstance != null
				&& (AccountsState.ACTIVE_CURRENT.getCode().equals(caseInstance.getAccountsState())
						|| AccountsState.CLOSED.getCode().equals(caseInstance.getAccountsState()))) {
			reasonCodes.add(AccountsHealthReasonCode.INVALID_ACCOUNTS_TRANSITION.getCode());
		}
		return AccountsPolicyEvaluation.builder().reasonCodes(reasonCodes).build();
	}

	public static AccountsPolicyEvaluation evaluateActivation(CaseInstance caseInstance) {
		List<String> reasonCodes = new ArrayList<>();
		Optional<AccountsPolicy> policy = resolvePolicyDefinition(caseInstance);
		if (policy.isEmpty()) {
			reasonCodes.add(AccountsHealthReasonCode.MISSING_MATTER_TYPE.getCode());
			return AccountsPolicyEvaluation.builder().reasonCodes(reasonCodes).build();
		}
		if (profileMalformed(caseInstance)) {
			reasonCodes.add(AccountsHealthReasonCode.MALFORMED_CONFIGURATION.getCode());
			return AccountsPolicyEvaluation.builder().reasonCodes(reasonCodes).build();
		}

		switch (policy.get().getMatterType()) {
		case FLAT_FEE -> validateFlatFee(caseInstance, reasonCodes);
		case HOURLY_CREDIT_CARD -> validateHourlyCreditCard(caseInstance, reasonCodes);
		case HOURLY_RETAINER -> validateHourlyRetainer(caseInstance, reasonCodes);
		case SUBSCRIPTION_CREDIT_CARD -> validateSubscriptionCreditCard(caseInstance, reasonCodes);
		case SUBSCRIPTION_RETAINER -> validateSubscriptionRetainer(caseInstance, reasonCodes);
		case COUNSEL -> validateCounsel(caseInstance, reasonCodes);
		}
		return AccountsPolicyEvaluation.builder().reasonCodes(reasonCodes).build();
	}

	private static Map<MatterType, AccountsPolicy> buildPolicies() {
		Map<MatterType, AccountsPolicy> policies = new EnumMap<>(MatterType.class);
		policies.put(MatterType.FLAT_FEE,
				policy(MatterType.FLAT_FEE, AccountsPolicyProfile.FLAT_FEE_PROFILE,
						List.of("billingSetupComplete", "flatFeeAmount", "billingMode"),
						List.of("flatFeeAmount", "billingMode")));
		policies.put(MatterType.HOURLY_CREDIT_CARD,
				policy(MatterType.HOURLY_CREDIT_CARD, AccountsPolicyProfile.HOURLY_CREDIT_CARD_PROFILE,
						List.of("billingSetupComplete", "paymentMethodAuthorized", "paymentMethodRef", "billingMode"),
						List.of("paymentMethodAuthorized", "paymentMethodRef", "billingMode")));
		policies.put(MatterType.HOURLY_RETAINER,
				policy(MatterType.HOURLY_RETAINER, AccountsPolicyProfile.HOURLY_RETAINER_PROFILE,
						List.of("billingSetupComplete", "retainerAmount", "retainerFundsReceived", "billingMode"),
						List.of("retainerAmount", "retainerFundsReceived", "billingMode")));
		policies.put(MatterType.SUBSCRIPTION_CREDIT_CARD,
				policy(MatterType.SUBSCRIPTION_CREDIT_CARD,
						AccountsPolicyProfile.SUBSCRIPTION_CREDIT_CARD_PROFILE,
						List.of("billingSetupComplete", "subscriptionPlanId", "subscriptionPlanName",
								"subscriptionActive", "paymentMethodAuthorized", "paymentMethodRef", "billingMode"),
						List.of("subscriptionPlanId|subscriptionPlanName", "subscriptionActive",
								"paymentMethodAuthorized", "paymentMethodRef", "billingMode")));
		policies.put(MatterType.SUBSCRIPTION_RETAINER,
				policy(MatterType.SUBSCRIPTION_RETAINER,
						AccountsPolicyProfile.SUBSCRIPTION_RETAINER_PROFILE,
						List.of("billingSetupComplete", "subscriptionPlanId", "subscriptionPlanName",
								"subscriptionActive", "retainerAmount", "retainerFundsReceived", "billingMode"),
						List.of("subscriptionPlanId|subscriptionPlanName", "subscriptionActive", "retainerAmount",
								"retainerFundsReceived", "billingMode")));
		policies.put(MatterType.COUNSEL,
				policy(MatterType.COUNSEL, AccountsPolicyProfile.COUNSEL_PROFILE,
						List.of("billingSetupComplete", "instructingFirmId", "instructingFirmName",
								"billingPartyModel", "counselBillingMode"),
						List.of("instructingFirmId|instructingFirmName", "billingPartyModel",
								"counselBillingMode")));
		return Map.copyOf(policies);
	}

	private static AccountsPolicy policy(MatterType matterType, AccountsPolicyProfile profile,
			List<String> requiredSetupFields, List<String> activationPrerequisites) {
		return AccountsPolicy.builder().matterType(matterType).profile(profile)
				.requiredSetupFields(requiredSetupFields).validStates(SETUP_TO_ACTIVE_STATES)
				.validTransitions(PHASE_3_TRANSITIONS).activationPrerequisites(activationPrerequisites)
				.closurePrerequisites(List.of()).build();
	}

	private static void validateFlatFee(CaseInstance caseInstance, List<String> reasonCodes) {
		requireBillingSetup(caseInstance, reasonCodes);
		if (isBlank(caseInstance.getFlatFeeAmount())) {
			reasonCodes.add(AccountsHealthReasonCode.MISSING_FLAT_FEE_SETUP.getCode());
		}
		if (!BillingMode.FLAT_FEE.getCode().equals(caseInstance.getBillingMode())) {
			reasonCodes.add(AccountsHealthReasonCode.INVALID_BILLING_MODE.getCode());
		}
	}

	private static void validateHourlyCreditCard(CaseInstance caseInstance, List<String> reasonCodes) {
		requireBillingSetup(caseInstance, reasonCodes);
		requirePaymentMethod(caseInstance, reasonCodes);
		if (!BillingMode.HOURLY_CARD.getCode().equals(caseInstance.getBillingMode())) {
			reasonCodes.add(AccountsHealthReasonCode.INVALID_BILLING_MODE.getCode());
		}
	}

	private static void validateHourlyRetainer(CaseInstance caseInstance, List<String> reasonCodes) {
		requireBillingSetup(caseInstance, reasonCodes);
		requireRetainer(caseInstance, reasonCodes);
		if (!BillingMode.HOURLY_RETAINER.getCode().equals(caseInstance.getBillingMode())) {
			reasonCodes.add(AccountsHealthReasonCode.INVALID_BILLING_MODE.getCode());
		}
	}

	private static void validateSubscriptionCreditCard(CaseInstance caseInstance, List<String> reasonCodes) {
		requireBillingSetup(caseInstance, reasonCodes);
		requireSubscription(caseInstance, reasonCodes);
		requirePaymentMethod(caseInstance, reasonCodes);
		if (!BillingMode.SUBSCRIPTION_CARD.getCode().equals(caseInstance.getBillingMode())) {
			reasonCodes.add(AccountsHealthReasonCode.INVALID_BILLING_MODE.getCode());
		}
	}

	private static void validateSubscriptionRetainer(CaseInstance caseInstance, List<String> reasonCodes) {
		requireBillingSetup(caseInstance, reasonCodes);
		requireSubscription(caseInstance, reasonCodes);
		requireRetainer(caseInstance, reasonCodes);
		if (!BillingMode.SUBSCRIPTION_RETAINER.getCode().equals(caseInstance.getBillingMode())) {
			reasonCodes.add(AccountsHealthReasonCode.INVALID_BILLING_MODE.getCode());
		}
	}

	private static void validateCounsel(CaseInstance caseInstance, List<String> reasonCodes) {
		requireBillingSetup(caseInstance, reasonCodes);
		if (isBlank(caseInstance.getInstructingFirmId()) && isBlank(caseInstance.getInstructingFirmName())) {
			reasonCodes.add(AccountsHealthReasonCode.MISSING_INSTRUCTING_FIRM.getCode());
			reasonCodes.add(AccountsHealthReasonCode.MISSING_COUNSEL_CONFIGURATION.getCode());
		}
		if (!Boolean.TRUE.equals(caseInstance.getCounselBillingPartyOverride())
				&& !BillingPartyModel.INSTRUCTING_FIRM.getCode().equals(caseInstance.getBillingPartyModel())) {
			reasonCodes.add(AccountsHealthReasonCode.INVALID_BILLING_PARTY_MODEL.getCode());
		}
		if (!isCounselBillingMode(caseInstance.getCounselBillingMode())) {
			reasonCodes.add(AccountsHealthReasonCode.MISSING_COUNSEL_BILLING_MODE.getCode());
			reasonCodes.add(AccountsHealthReasonCode.MISSING_COUNSEL_CONFIGURATION.getCode());
		}
	}

	private static void requireBillingSetup(CaseInstance caseInstance, List<String> reasonCodes) {
		if (!Boolean.TRUE.equals(caseInstance.getBillingSetupComplete())) {
			reasonCodes.add(AccountsHealthReasonCode.MISSING_BILLING_SETUP.getCode());
		}
	}

	private static void requirePaymentMethod(CaseInstance caseInstance, List<String> reasonCodes) {
		if (!Boolean.TRUE.equals(caseInstance.getPaymentMethodAuthorized())
				|| isBlank(caseInstance.getPaymentMethodRef())) {
			reasonCodes.add(AccountsHealthReasonCode.MISSING_PAYMENT_METHOD.getCode());
		}
	}

	private static void requireRetainer(CaseInstance caseInstance, List<String> reasonCodes) {
		if (isBlank(caseInstance.getRetainerAmount()) || !Boolean.TRUE.equals(caseInstance.getRetainerFundsReceived())) {
			reasonCodes.add(AccountsHealthReasonCode.MISSING_RETAINER.getCode());
		}
	}

	private static void requireSubscription(CaseInstance caseInstance, List<String> reasonCodes) {
		if ((isBlank(caseInstance.getSubscriptionPlanId()) && isBlank(caseInstance.getSubscriptionPlanName()))
				|| !Boolean.TRUE.equals(caseInstance.getSubscriptionActive())) {
			reasonCodes.add(AccountsHealthReasonCode.MISSING_SUBSCRIPTION_CONFIGURATION.getCode());
		}
	}

	private static boolean isCounselBillingMode(String value) {
		return BillingMode.fromValue(value).filter(billingMode -> billingMode == BillingMode.COUNSEL_HOURLY
				|| billingMode == BillingMode.COUNSEL_RETAINER || billingMode == BillingMode.COUNSEL_FLAT_FEE
				|| billingMode == BillingMode.COUNSEL_RECURRING).isPresent();
	}

	public static boolean profileMalformed(CaseInstance caseInstance) {
		if (caseInstance == null || isBlank(caseInstance.getAccountsProfile())) {
			return false;
		}
		return AccountsPolicyProfile.fromValue(caseInstance.getAccountsProfile())
				.filter(profile -> resolvePolicy(caseInstance).filter(profile::equals).isPresent()).isEmpty();
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
