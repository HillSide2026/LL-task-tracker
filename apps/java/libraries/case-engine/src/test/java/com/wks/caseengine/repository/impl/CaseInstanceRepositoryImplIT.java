/*
 * WKS Platform - Open-Source Project
 * 
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 * 
 * WKS Platform is licensed under the MIT License.
 * 
 * © 2021 WKS Power. All rights reserved.
 * 
 * For licensing information, see the LICENSE file in the root directory of the project.
 */
package com.wks.caseengine.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.wks.caseengine.cases.definition.CaseStatus;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceFilter;
import com.wks.caseengine.cases.instance.accounts.AccountsEvent;
import com.wks.caseengine.cases.instance.accounts.AccountsEventType;
import com.wks.caseengine.cases.instance.accounts.AccountsHealth;
import com.wks.caseengine.cases.instance.accounts.AccountsHealthReasonCode;
import com.wks.caseengine.cases.instance.accounts.AccountsLifecycleStage;
import com.wks.caseengine.cases.instance.accounts.AccountsPolicyProfile;
import com.wks.caseengine.cases.instance.accounts.AccountsReadinessStatus;
import com.wks.caseengine.cases.instance.accounts.AccountsState;
import com.wks.caseengine.cases.instance.accounts.AccountsWorkOwnerType;
import com.wks.caseengine.cases.instance.accounts.AccountsWorkPriority;
import com.wks.caseengine.cases.instance.accounts.AccountsWorkQueue;
import com.wks.caseengine.cases.instance.accounts.BillingMode;
import com.wks.caseengine.cases.instance.accounts.BillingPartyModel;
import com.wks.caseengine.cases.instance.accounts.MatterType;
import com.wks.caseengine.cases.instance.repository.CaseInstanceRepositoryImpl;
import com.wks.caseengine.pagination.Cursor;
import com.wks.caseengine.pagination.PageResult;

@DataMongoTest
@ExtendWith(SpringExtension.class)
public class CaseInstanceRepositoryImplIT {

	@Autowired
	private MongoOperations operations;

	private CaseInstanceRepositoryImpl repository;

	@BeforeEach
	public void setup() {
		operations.insert(fixtures(), CaseInstance.class);
		repository = new CaseInstanceRepositoryImpl() {
			@Override
			protected MongoOperations getOperations() {
				return operations;
			}
		};
	}

	@AfterEach
	public void teadown() {
		operations.remove(new Query(), CaseInstance.class);
	}

	@Test
	public void shouldGetResultsByFindUsingPaginationFilter() throws Exception {
		CaseInstanceFilter filter = new CaseInstanceFilter("ARCHIVED_CASE_STATUS", "1", null, null, null, null, null,
				Cursor.empty(), "asc", "1");

		PageResult<CaseInstance> results = repository.find(filter);

		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals("634d1eac797f75ecc4a10052", results.first().get_id());
		assertEquals("92935", results.first().getBusinessKey());
		assertEquals("1", results.first().getCaseDefinitionId());
		assertEquals("Data Collection", results.first().getStage());
		assertEquals(CaseStatus.ARCHIVED_CASE_STATUS, results.first().getStatus());
	}

	@Test
	public void shouldGetEmptyResultsByFindUsingPaginationFilter() throws Exception {
		CaseInstanceFilter filter = new CaseInstanceFilter("ARCHIVED_CASE_STATUS", "-1", null, null, null, null, null,
				Cursor.empty(), "asc", "0");

		PageResult<CaseInstance> results = repository.find(filter);

		assertNotNull(results);
		assertEquals(0, results.size());
	}

	@Test
	public void shouldGetResultsByFindUsingPaginationFilterWithNextAndBeforeCursor() throws Exception {
		PageResult<CaseInstance> results1 = repository
				.find(new CaseInstanceFilter("CLOSED_CASE_STATUS", "1", null, null, null, null, null, Cursor.empty(), "asc", "2"));
		PageResult<CaseInstance> results2 = repository
				.find(new CaseInstanceFilter("CLOSED_CASE_STATUS", "1", null, null, null, null, null,
						Cursor.of(null, results1.next()), "asc", "2"));
		PageResult<CaseInstance> results3 = repository
				.find(new CaseInstanceFilter("CLOSED_CASE_STATUS", "1", null, null, null, null, null,
						Cursor.of(null, results2.next()), "asc", "2"));

		assertNotNull(results1);
		assertTrue(results1.hasNext());
		assertFalse(results1.hasPrevious());
		assertNotNull(results2);
		assertTrue(results2.hasNext());
		assertTrue(results2.hasPrevious());
		assertNotNull(results3);
		assertTrue(results3.hasNext());
		assertTrue(results3.hasPrevious());
	}

	@Test
	public void shouldFilterMatterAdminExceptionsByOwnerAndHealthReason() throws Exception {
		CaseInstanceFilter filter = new CaseInstanceFilter("WIP_CASE_STATUS", "matter-admin-opening-control", null, null,
				null, null, null, "true", "admin-1", null, "LAWYER_RESPONSE_STALE", Cursor.empty(), "asc", "10");

		PageResult<CaseInstance> results = repository.find(filter);

		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals("MATTER-100", results.first().getBusinessKey());
	}

	@Test
	public void shouldFilterMatterAdminCasesByResponsibleLawyer() throws Exception {
		CaseInstanceFilter filter = new CaseInstanceFilter("WIP_CASE_STATUS", "matter-admin-opening-control", null, null,
				null, null, null, null, null, "lawyer-sub-1", null, Cursor.empty(), "asc", "10");

		PageResult<CaseInstance> results = repository.find(filter);

		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals("MATTER-100", results.first().getBusinessKey());
	}

	@Test
	public void shouldRoundTripAccountsFoundationFields() throws Exception {
		CaseInstance existingCase = repository.get("MATTER-100");
		existingCase.setMatterType(MatterType.HOURLY_RETAINER.getCode());
		existingCase.setBillingPartyModel(BillingPartyModel.DIRECT_CLIENT.getCode());
		existingCase.setBillingMode(BillingMode.HOURLY_RETAINER.getCode());
		existingCase.setAccountsProfile(AccountsPolicyProfile.HOURLY_RETAINER_PROFILE.getCode());
		existingCase.setBillingSetupComplete(true);
		existingCase.setFlatFeeAmount("2500");
		existingCase.setPaymentMethodAuthorized(true);
		existingCase.setPaymentMethodRef("card-1");
		existingCase.setRetainerAmount("5000");
		existingCase.setRetainerFundsReceived(true);
		existingCase.setSubscriptionPlanId("plan-1");
		existingCase.setSubscriptionPlanName("Monthly");
		existingCase.setSubscriptionActive(true);
		existingCase.setInstructingFirmId("firm-1");
		existingCase.setInstructingFirmName("Client Firm");
		existingCase.setCounselBillingMode(BillingMode.COUNSEL_RETAINER.getCode());
		existingCase.setCounselBillingPartyOverride(false);
		existingCase.setAccountsStage(AccountsLifecycleStage.SETUP.getCode());
		existingCase.setAccountsState(AccountsState.AWAITING_RETAINER_FUNDING.getCode());
		existingCase.setAccountsHealth(AccountsHealth.AMBER.getCode());
		existingCase.setAccountsHealthReasonCodes(List.of(AccountsHealthReasonCode.MISSING_RETAINER.getCode()));
		existingCase.setAccountsHealthEvaluatedAt("2026-05-02T10:15:30-04:00");
		existingCase.setAccountsStaleSince("2026-05-02T10:15:30-04:00");
		existingCase.setAccountsMalformedCase(false);
		existingCase.setAccountsReadinessStatus(AccountsReadinessStatus.READY.getCode());
		existingCase.setAccountsReadinessReasonCodes(List.of());
		existingCase.setAccountsReadinessEvaluatedAt("2026-05-02T10:16:30-04:00");
		existingCase.setAccountsReadinessSummary("Accounts setup is ready for administrative opening");
		existingCase.setAccountsQueueId(AccountsWorkQueue.ACCOUNTS_READY);
		existingCase.setAccountsNextActionOwnerType(AccountsWorkOwnerType.ACCOUNTS.getCode());
		existingCase.setAccountsNextActionSummary("Accounts setup is ready for administrative opening");
		existingCase.setAccountsNextActionDueAt("2026-05-05");
		existingCase.setAccountsWorkBlocked(false);
		existingCase.setAccountsWorkPriority(AccountsWorkPriority.NORMAL.getCode());
		existingCase.setAccountsEvents(List.of(AccountsEvent.builder()
				.eventType(AccountsEventType.ACCOUNTS_INITIALIZED.getCode())
				.toState(AccountsState.AWAITING_RETAINER_FUNDING.getCode())
				.toStage(AccountsLifecycleStage.SETUP.getCode()).build()));

		repository.update("MATTER-100", existingCase);

		CaseInstance updatedCase = repository.get("MATTER-100");
		assertEquals(MatterType.HOURLY_RETAINER.getCode(), updatedCase.getMatterType());
		assertEquals(BillingPartyModel.DIRECT_CLIENT.getCode(), updatedCase.getBillingPartyModel());
		assertEquals(BillingMode.HOURLY_RETAINER.getCode(), updatedCase.getBillingMode());
		assertEquals(AccountsPolicyProfile.HOURLY_RETAINER_PROFILE.getCode(), updatedCase.getAccountsProfile());
		assertEquals(true, updatedCase.getBillingSetupComplete());
		assertEquals("2500", updatedCase.getFlatFeeAmount());
		assertEquals(true, updatedCase.getPaymentMethodAuthorized());
		assertEquals("card-1", updatedCase.getPaymentMethodRef());
		assertEquals("5000", updatedCase.getRetainerAmount());
		assertEquals(true, updatedCase.getRetainerFundsReceived());
		assertEquals("plan-1", updatedCase.getSubscriptionPlanId());
		assertEquals("Monthly", updatedCase.getSubscriptionPlanName());
		assertEquals(true, updatedCase.getSubscriptionActive());
		assertEquals("firm-1", updatedCase.getInstructingFirmId());
		assertEquals("Client Firm", updatedCase.getInstructingFirmName());
		assertEquals(BillingMode.COUNSEL_RETAINER.getCode(), updatedCase.getCounselBillingMode());
		assertEquals(false, updatedCase.getCounselBillingPartyOverride());
		assertEquals(AccountsLifecycleStage.SETUP.getCode(), updatedCase.getAccountsStage());
		assertEquals(AccountsState.AWAITING_RETAINER_FUNDING.getCode(), updatedCase.getAccountsState());
		assertEquals(AccountsHealth.AMBER.getCode(), updatedCase.getAccountsHealth());
		assertEquals(List.of(AccountsHealthReasonCode.MISSING_RETAINER.getCode()),
				updatedCase.getAccountsHealthReasonCodes());
		assertEquals("2026-05-02T10:15:30-04:00", updatedCase.getAccountsHealthEvaluatedAt());
		assertEquals("2026-05-02T10:15:30-04:00", updatedCase.getAccountsStaleSince());
		assertEquals(false, updatedCase.getAccountsMalformedCase());
		assertEquals(AccountsReadinessStatus.READY.getCode(), updatedCase.getAccountsReadinessStatus());
		assertEquals(List.of(), updatedCase.getAccountsReadinessReasonCodes());
		assertEquals("2026-05-02T10:16:30-04:00", updatedCase.getAccountsReadinessEvaluatedAt());
		assertEquals("Accounts setup is ready for administrative opening", updatedCase.getAccountsReadinessSummary());
		assertEquals(AccountsWorkQueue.ACCOUNTS_READY, updatedCase.getAccountsQueueId());
		assertEquals(AccountsWorkOwnerType.ACCOUNTS.getCode(), updatedCase.getAccountsNextActionOwnerType());
		assertEquals("Accounts setup is ready for administrative opening", updatedCase.getAccountsNextActionSummary());
		assertEquals("2026-05-05", updatedCase.getAccountsNextActionDueAt());
		assertEquals(false, updatedCase.getAccountsWorkBlocked());
		assertEquals(AccountsWorkPriority.NORMAL.getCode(), updatedCase.getAccountsWorkPriority());
		assertEquals(1, updatedCase.getAccountsEvents().size());
		assertEquals(AccountsEventType.ACCOUNTS_INITIALIZED.getCode(),
				updatedCase.getAccountsEvents().get(0).getEventType());
	}

	@Test
	public void shouldFilterAccountsWorkByQueueOwnerDueAndReason() throws Exception {
		CaseInstance existingCase = repository.get("MATTER-100");
		existingCase.setAccountsReadinessStatus(AccountsReadinessStatus.NOT_READY.getCode());
		existingCase.setAccountsQueueId(AccountsWorkQueue.MISSING_RETAINER);
		existingCase.setAccountsNextActionOwnerType(AccountsWorkOwnerType.ACCOUNTS.getCode());
		existingCase.setAccountsNextActionDueAt("2026-05-05");
		existingCase.setAccountsWorkBlocked(false);
		existingCase.setAccountsReadinessReasonCodes(List.of(AccountsHealthReasonCode.MISSING_RETAINER.getCode()));
		repository.update("MATTER-100", existingCase);

		CaseInstanceFilter filter = CaseInstanceFilter.builder().status(java.util.Optional.empty())
				.caseDefsId(java.util.Optional.empty()).adminState(java.util.Optional.empty())
				.adminHealth(java.util.Optional.empty()).nextActionOwnerType(java.util.Optional.empty())
				.queueId(java.util.Optional.empty()).malformedCase(java.util.Optional.empty())
				.exceptionOnly(java.util.Optional.empty()).adminOwnerId(java.util.Optional.empty())
				.responsibleLawyerId(java.util.Optional.empty()).healthReasonCode(java.util.Optional.empty())
				.matterType(java.util.Optional.empty())
				.accountsReadinessStatus(java.util.Optional.of(AccountsReadinessStatus.NOT_READY.getCode()))
				.accountsQueueId(java.util.Optional.of(AccountsWorkQueue.MISSING_RETAINER))
				.accountsNextActionOwnerType(java.util.Optional.of(AccountsWorkOwnerType.ACCOUNTS.getCode()))
				.accountsNextActionDueBefore(java.util.Optional.of("2026-05-06"))
				.accountsWorkBlocked(java.util.Optional.of(false))
				.accountsReadinessReasonCode(java.util.Optional.of(AccountsHealthReasonCode.MISSING_RETAINER.getCode()))
				.cursor(Cursor.empty()).dir(org.springframework.data.domain.Sort.Direction.ASC).limit(10).build();

		PageResult<CaseInstance> results = repository.find(filter);

		assertEquals(1, results.size());
		assertEquals("MATTER-100", results.first().getBusinessKey());
	}

	public static List<CaseInstance> fixtures() {
		List<CaseInstance> items = new ArrayList<>();
		items.add(
				new CaseInstance("634d1eac797f75ecc4a10052", "92935", "1", "Data Collection", "ARCHIVED_CASE_STATUS"));
		items.add(new CaseInstance("634d1eb2797f75ecc4a10059", "1711", "1", "Data Collection", "WIP_CASE_STATUS"));
		items.add(new CaseInstance("634d1eb2797f75ecc4a1005e", "98228", "1", "Data Collection", "CLOSED_CASE_STATUS"));
		items.add(new CaseInstance("634d1eb3797f75ecc4a10063", "65422", "1", "Data Collection", "CLOSED_CASE_STATUS"));
		items.add(new CaseInstance("634d1ee0797f75ecc4a10076", "992", "1", "Data Collection", "CLOSED_CASE_STATUS"));

		items.add(new CaseInstance("634d227d797f75ecc4a10124", "40187", "1", "Data Collection", "CLOSED_CASE_STATUS"));
		items.add(new CaseInstance("634e8b48ee448937ec2a31ac", "95622", "1", "Data Collection", "CLOSED_CASE_STATUS"));
		items.add(new CaseInstance("634e8b4aee448937ec2a31b1", "88595", "1", "Data Collection", "CLOSED_CASE_STATUS"));
		items.add(new CaseInstance("634e8b5aee448937ec2a31cc", "63618", "1", "Stage 1", "CLOSED_CASE_STATUS"));
		items.add(new CaseInstance("634e8cb8ee448937ec2a32ce", "25003", "1", "Review", "CLOSED_CASE_STATUS"));
		items.add(CaseInstance.builder()._id("734e8cb8ee448937ec2a32ce").businessKey("MATTER-100")
				.caseDefinitionId("matter-admin-opening-control").stage("Maintenance").status("WIP_CASE_STATUS")
				.adminState("Maintenance Lawyer Review").adminHealth("Red")
				.healthReasonCodes(List.of("LAWYER_RESPONSE_STALE")).malformedCase(false).adminOwnerId("admin-1")
				.adminOwnerName("Admin Owner").responsibleLawyerId("lawyer-sub-1")
				.responsibleLawyerName("Assigned Lawyer").queueId("matter-admin-maintenance-lawyer-review")
				.nextActionOwnerType("Lawyer").nextActionSummary("Lawyer response pending").build());
		return items;
	}

}
