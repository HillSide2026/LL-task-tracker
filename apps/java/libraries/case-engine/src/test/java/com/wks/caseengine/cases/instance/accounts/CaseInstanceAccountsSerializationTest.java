package com.wks.caseengine.cases.instance.accounts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wks.caseengine.cases.instance.CaseInstance;

class CaseInstanceAccountsSerializationTest {

	@Test
	void shouldRoundTripCanonicalAccountsFoundationFieldsThroughJson() {
		Gson gson = new GsonBuilder().create();
		CaseInstance source = CaseInstance.builder().businessKey("BK-1")
				.matterType(MatterType.COUNSEL.getCode())
				.billingPartyModel(BillingPartyModel.INSTRUCTING_FIRM.getCode())
				.billingMode(BillingMode.COUNSEL_HOURLY.getCode())
				.accountsProfile(AccountsPolicyProfile.COUNSEL_PROFILE.getCode())
				.billingSetupComplete(true)
				.flatFeeAmount("2500")
				.paymentMethodAuthorized(true)
				.paymentMethodRef("card-1")
				.retainerAmount("5000")
				.retainerFundsReceived(true)
				.subscriptionPlanId("plan-1")
				.subscriptionPlanName("Monthly")
				.subscriptionActive(true)
				.instructingFirmId("firm-1")
				.instructingFirmName("Client Firm")
				.counselBillingMode(BillingMode.COUNSEL_HOURLY.getCode())
				.counselBillingPartyOverride(false)
				.accountsStage(AccountsLifecycleStage.SETUP.getCode())
				.accountsState(AccountsState.AWAITING_BILLING_SETUP.getCode())
				.accountsHealth(AccountsHealth.AMBER.getCode())
				.accountsHealthReasonCodes(List.of(AccountsHealthReasonCode.MISSING_BILLING_SETUP.getCode()))
				.accountsHealthEvaluatedAt("2026-05-02T10:15:30-04:00")
				.accountsStaleSince("2026-05-02T10:15:30-04:00")
				.accountsMalformedCase(false)
				.accountsReadinessStatus(AccountsReadinessStatus.READY.getCode())
				.accountsReadinessReasonCodes(List.of())
				.accountsReadinessEvaluatedAt("2026-05-02T10:16:30-04:00")
				.accountsReadinessSummary("Accounts setup is ready for administrative opening")
				.accountsQueueId(AccountsWorkQueue.ACCOUNTS_READY)
				.accountsNextActionOwnerType(AccountsWorkOwnerType.ACCOUNTS.getCode())
				.accountsNextActionSummary("Accounts setup is ready for administrative opening")
				.accountsNextActionDueAt("2026-05-05")
				.accountsWorkBlocked(false)
				.accountsWorkPriority(AccountsWorkPriority.NORMAL.getCode())
				.accountsEvents(List.of(AccountsEvent.builder().eventType(AccountsEventType.ACCOUNTS_INITIALIZED.getCode())
						.occurredAt(new Date()).actorId("accounts-user").toState(AccountsState.AWAITING_BILLING_SETUP.getCode())
						.toStage(AccountsLifecycleStage.SETUP.getCode()).note("Initialized shell").build()))
				.build();

		CaseInstance parsed = gson.fromJson(gson.toJson(source), CaseInstance.class);

		assertEquals(MatterType.COUNSEL.getCode(), parsed.getMatterType());
		assertEquals(BillingPartyModel.INSTRUCTING_FIRM.getCode(), parsed.getBillingPartyModel());
		assertEquals(BillingMode.COUNSEL_HOURLY.getCode(), parsed.getBillingMode());
		assertEquals(AccountsPolicyProfile.COUNSEL_PROFILE.getCode(), parsed.getAccountsProfile());
		assertEquals(true, parsed.getBillingSetupComplete());
		assertEquals("2500", parsed.getFlatFeeAmount());
		assertEquals(true, parsed.getPaymentMethodAuthorized());
		assertEquals("card-1", parsed.getPaymentMethodRef());
		assertEquals("5000", parsed.getRetainerAmount());
		assertEquals(true, parsed.getRetainerFundsReceived());
		assertEquals("plan-1", parsed.getSubscriptionPlanId());
		assertEquals("Monthly", parsed.getSubscriptionPlanName());
		assertEquals(true, parsed.getSubscriptionActive());
		assertEquals("firm-1", parsed.getInstructingFirmId());
		assertEquals("Client Firm", parsed.getInstructingFirmName());
		assertEquals(BillingMode.COUNSEL_HOURLY.getCode(), parsed.getCounselBillingMode());
		assertEquals(false, parsed.getCounselBillingPartyOverride());
		assertEquals(AccountsLifecycleStage.SETUP.getCode(), parsed.getAccountsStage());
		assertEquals(AccountsState.AWAITING_BILLING_SETUP.getCode(), parsed.getAccountsState());
		assertEquals(AccountsHealth.AMBER.getCode(), parsed.getAccountsHealth());
		assertEquals(List.of(AccountsHealthReasonCode.MISSING_BILLING_SETUP.getCode()),
				parsed.getAccountsHealthReasonCodes());
		assertEquals("2026-05-02T10:15:30-04:00", parsed.getAccountsHealthEvaluatedAt());
		assertEquals("2026-05-02T10:15:30-04:00", parsed.getAccountsStaleSince());
		assertEquals(false, parsed.getAccountsMalformedCase());
		assertEquals(AccountsReadinessStatus.READY.getCode(), parsed.getAccountsReadinessStatus());
		assertEquals(List.of(), parsed.getAccountsReadinessReasonCodes());
		assertEquals("2026-05-02T10:16:30-04:00", parsed.getAccountsReadinessEvaluatedAt());
		assertEquals("Accounts setup is ready for administrative opening", parsed.getAccountsReadinessSummary());
		assertEquals(AccountsWorkQueue.ACCOUNTS_READY, parsed.getAccountsQueueId());
		assertEquals(AccountsWorkOwnerType.ACCOUNTS.getCode(), parsed.getAccountsNextActionOwnerType());
		assertEquals("Accounts setup is ready for administrative opening", parsed.getAccountsNextActionSummary());
		assertEquals("2026-05-05", parsed.getAccountsNextActionDueAt());
		assertEquals(false, parsed.getAccountsWorkBlocked());
		assertEquals(AccountsWorkPriority.NORMAL.getCode(), parsed.getAccountsWorkPriority());
		assertEquals(1, parsed.getAccountsEvents().size());
		assertEquals(AccountsEventType.ACCOUNTS_INITIALIZED.getCode(), parsed.getAccountsEvents().get(0).getEventType());
	}
}
