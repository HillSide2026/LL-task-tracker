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
package com.wks.caseengine.cases.instance.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceFilter;
import com.wks.caseengine.cases.instance.CaseInstanceNotFoundException;
import com.wks.caseengine.cases.instance.accounts.AccountsEvent;
import com.wks.caseengine.cases.instance.accounts.AccountsEventType;
import com.wks.caseengine.cases.instance.accounts.AccountsLifecycleException;
import com.wks.caseengine.cases.instance.accounts.AccountsLifecycleStage;
import com.wks.caseengine.cases.instance.accounts.AccountsReadinessEvaluation;
import com.wks.caseengine.cases.instance.accounts.AccountsReadinessStatus;
import com.wks.caseengine.cases.instance.accounts.AccountsState;
import com.wks.caseengine.cases.instance.accounts.AccountsWorkOwnerType;
import com.wks.caseengine.cases.instance.accounts.AccountsWorkQueue;
import com.wks.caseengine.cases.instance.accounts.AccountsWorkSummary;
import com.wks.caseengine.cases.instance.accounts.BillingMode;
import com.wks.caseengine.cases.instance.accounts.MatterType;
import com.wks.caseengine.cases.instance.admin.AdminLifecycleException;
import com.wks.caseengine.cases.instance.admin.AdminLifecycleSupport;
import com.wks.caseengine.cases.instance.admin.AdminState;
import com.wks.caseengine.cases.instance.admin.NextActionOwnerType;
import com.wks.caseengine.cases.instance.command.FindCaseInstanceCmd;
import com.wks.caseengine.cases.instance.command.GetCaseInstanceCmd;
import com.wks.caseengine.cases.instance.repository.CaseInstanceRepository;
import com.wks.caseengine.command.CommandExecutor;
import com.wks.caseengine.pagination.Cursor;
import com.wks.caseengine.pagination.PageResult;

@ExtendWith(MockitoExtension.class)
public class CaseInstanceServiceImplTest {

	@Mock
	private CommandExecutor commandExecutor;

	@Mock
	private CaseInstanceRepository repository;

	@InjectMocks
	private CaseInstanceServiceImpl service;

	@AfterEach
	void teardown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void shouldReturnEmptyListWhenFind() throws Exception {
		PageResult<CaseInstance> pageResult = PageResult.<CaseInstance>builder().content(new ArrayList<>()).build();
		when(commandExecutor.execute(Mockito.any(FindCaseInstanceCmd.class))).thenReturn(pageResult);
		var result = service.find(new CaseInstanceFilter(null, null, null, null, null, null, null, Cursor.empty(),
				"asc", "10"));
		assertEquals(pageResult, result);
		verify(commandExecutor).execute(Mockito.any(FindCaseInstanceCmd.class));
	}

	@Test
	void shouldRestrictLawyerFindToAssignedMatterAdminCases() throws Exception {
		setJwtSecurityContext("lawyer-sub-1", List.of("lawyer_user"));
		PageResult<CaseInstance> pageResult = PageResult.<CaseInstance>builder().content(new ArrayList<>()).build();
		when(commandExecutor.execute(Mockito.any(FindCaseInstanceCmd.class))).thenReturn(pageResult);

		service.find(new CaseInstanceFilter(null, AdminLifecycleSupport.CASE_DEFINITION_ID, null, null, null, null, null,
				Cursor.empty(), "asc", "10"));

		FindCaseInstanceCmd command = Mockito.mockingDetails(commandExecutor).getInvocations().stream()
				.map(invocation -> invocation.getArgument(0)).filter(FindCaseInstanceCmd.class::isInstance)
				.map(FindCaseInstanceCmd.class::cast).findFirst().orElseThrow();
		CaseInstanceFilter scopedFilter = extractFilter(command);
		assertEquals(AdminLifecycleSupport.CASE_DEFINITION_ID, scopedFilter.getCaseDefsId().orElse(null));
		assertEquals("lawyer-sub-1", scopedFilter.getResponsibleLawyerId().orElse(null));
	}

	@Test
	void shouldNotRestrictNonAdminLifecycleFindForLawyer() throws Exception {
		setJwtSecurityContext("lawyer-sub-1", List.of("lawyer_user"));
		PageResult<CaseInstance> pageResult = PageResult.<CaseInstance>builder().content(new ArrayList<>()).build();
		when(commandExecutor.execute(Mockito.any(FindCaseInstanceCmd.class))).thenReturn(pageResult);

		service.find(new CaseInstanceFilter(null, "non-admin-case-def", null, null, null, null, null, Cursor.empty(),
				"asc", "10"));

		FindCaseInstanceCmd command = Mockito.mockingDetails(commandExecutor).getInvocations().stream()
				.map(invocation -> invocation.getArgument(0)).filter(FindCaseInstanceCmd.class::isInstance)
				.map(FindCaseInstanceCmd.class::cast).findFirst().orElseThrow();
		CaseInstanceFilter scopedFilter = extractFilter(command);
		assertEquals("non-admin-case-def", scopedFilter.getCaseDefsId().orElse(null));
		assertTrue(scopedFilter.getResponsibleLawyerId().isEmpty());
	}

	@Test
	void shouldKeepOpsAdminFindTenantWideForMatterAdminCases() throws Exception {
		setJwtSecurityContext("ops-admin-sub", List.of("ops_admin"));
		PageResult<CaseInstance> pageResult = PageResult.<CaseInstance>builder().content(new ArrayList<>()).build();
		when(commandExecutor.execute(Mockito.any(FindCaseInstanceCmd.class))).thenReturn(pageResult);

		service.find(new CaseInstanceFilter(null, AdminLifecycleSupport.CASE_DEFINITION_ID, null, null, null, null, null,
				Cursor.empty(), "asc", "10"));

		FindCaseInstanceCmd command = Mockito.mockingDetails(commandExecutor).getInvocations().stream()
				.map(invocation -> invocation.getArgument(0)).filter(FindCaseInstanceCmd.class::isInstance)
				.map(FindCaseInstanceCmd.class::cast).findFirst().orElseThrow();
		CaseInstanceFilter scopedFilter = extractFilter(command);
		assertEquals(AdminLifecycleSupport.CASE_DEFINITION_ID, scopedFilter.getCaseDefsId().orElse(null));
		assertTrue(scopedFilter.getResponsibleLawyerId().isEmpty());
	}

	@Test
	void shouldHideUnassignedMatterAdminCaseFromLawyerGet() {
		setJwtSecurityContext("lawyer-sub-1", List.of("lawyer_user"));
		when(commandExecutor.execute(Mockito.any(GetCaseInstanceCmd.class))).thenReturn(CaseInstance.builder()
				.businessKey("BK-1").caseDefinitionId(AdminLifecycleSupport.CASE_DEFINITION_ID)
				.adminState(AdminState.ACTIVE.getCode()).stage("Maintenance").responsibleLawyerId("other-lawyer-sub").build());

		assertThrows(CaseInstanceNotFoundException.class, () -> service.get("BK-1"));
	}

	@Test
	void shouldAllowAssignedLawyerToGetMatterAdminCase() {
		setJwtSecurityContext("lawyer-sub-1", List.of("lawyer_user"));
		when(commandExecutor.execute(Mockito.any(GetCaseInstanceCmd.class))).thenReturn(CaseInstance.builder()
				.businessKey("BK-1").caseDefinitionId(AdminLifecycleSupport.CASE_DEFINITION_ID)
				.adminState(AdminState.MAINTENANCE_LAWYER_REVIEW.getCode()).stage("Maintenance")
				.responsibleLawyerId("lawyer-sub-1").responsibleLawyerName("Assigned Lawyer")
				.adminOwnerId("admin-1").adminOwnerName("Admin Owner")
				.nextActionOwnerType(NextActionOwnerType.LAWYER.getCode()).nextActionSummary("Lawyer review pending").build());

		CaseInstance caseInstance = service.get("BK-1");

		assertNotNull(caseInstance);
		assertEquals("BK-1", caseInstance.getBusinessKey());
	}

	@Test
	void shouldRejectDeleteForMatterAdminCase() {
		setJwtSecurityContext("ops-admin-sub", List.of("ops_admin"));
		when(commandExecutor.execute(Mockito.any(GetCaseInstanceCmd.class))).thenReturn(CaseInstance.builder()
				.businessKey("BK-1").caseDefinitionId(AdminLifecycleSupport.CASE_DEFINITION_ID)
				.adminState(AdminState.ACTIVE.getCode()).stage("Maintenance").adminOwnerId("admin-1")
				.nextActionOwnerType(NextActionOwnerType.ADMIN.getCode()).nextActionSummary("Control active").build());

		assertThrows(AdminLifecycleException.class, () -> service.delete("BK-1"));
	}

	@Test
	void shouldPersistLegacyNormalizationDuringFindRefresh() throws Exception {
		setJwtSecurityContext("ops-admin-sub", List.of("ops_admin"));
		PageResult<CaseInstance> pageResult = PageResult.<CaseInstance>builder().content(new ArrayList<>(List.of(
				CaseInstance.builder().businessKey("BK-1").caseDefinitionId(AdminLifecycleSupport.CASE_DEFINITION_ID)
						.adminState("Open").stage("Maintenance").nextActionOwnerType(NextActionOwnerType.ADMIN.getCode())
						.nextActionSummary("Continue control").adminOwnerId("admin-1").build())))
				.build();
		when(commandExecutor.execute(Mockito.any(FindCaseInstanceCmd.class))).thenReturn(pageResult);

		PageResult<CaseInstance> result = service.find(
				new CaseInstanceFilter(null, AdminLifecycleSupport.CASE_DEFINITION_ID, null, null, null, null, null,
						Cursor.empty(), "asc", "10"));

		verify(repository).update(Mockito.eq("BK-1"), Mockito.argThat(caseInstance ->
				AdminState.OPENED.getCode().equals(caseInstance.getAdminState())
						&& "Opening".equals(caseInstance.getStage())
						&& caseInstance.getAdminHealth() != null));
		assertEquals(AdminState.OPENED.getCode(), result.first().getAdminState());
		assertEquals("Opening", result.first().getStage());
	}

	@Test
	void shouldGetAccountsAndPersistShellStateWhenMissing() throws Exception {
		setJwtSecurityContext("accounts-sub", List.of("accounts_manager"));
		when(commandExecutor.execute(Mockito.any(GetCaseInstanceCmd.class)))
				.thenReturn(CaseInstance.builder().businessKey("BK-1").build());

		CaseInstance result = service.getAccounts("BK-1");

		verify(repository).update(Mockito.eq("BK-1"), Mockito.argThat(caseInstance ->
				AccountsLifecycleStage.SETUP.getCode().equals(caseInstance.getAccountsStage())
						&& AccountsState.AWAITING_BILLING_SETUP.getCode().equals(caseInstance.getAccountsState())
						&& caseInstance.getAccountsEvents().size() == 1));
		assertEquals(AccountsLifecycleStage.SETUP.getCode(), result.getAccountsStage());
		assertEquals(AccountsState.AWAITING_BILLING_SETUP.getCode(), result.getAccountsState());
		assertEquals(AccountsEventType.ACCOUNTS_INITIALIZED.getCode(), result.getAccountsEvents().get(0).getEventType());
	}

	@Test
	void shouldReturnAccountsHistoryEvents() {
		setJwtSecurityContext("accounts-sub", List.of("billing_user"));
		AccountsEvent existingEvent = AccountsEvent.builder().eventType(AccountsEventType.ACCOUNTS_INITIALIZED.getCode())
				.toState(AccountsState.AWAITING_BILLING_SETUP.getCode()).build();
		when(commandExecutor.execute(Mockito.any(GetCaseInstanceCmd.class)))
				.thenReturn(CaseInstance.builder().businessKey("BK-1").accountsStage(AccountsLifecycleStage.SETUP.getCode())
						.accountsState(AccountsState.AWAITING_BILLING_SETUP.getCode())
						.accountsEvents(List.of(existingEvent)).build());

		List<AccountsEvent> history = service.getAccountsHistory("BK-1");

		assertEquals(1, history.size());
		assertEquals(AccountsEventType.ACCOUNTS_INITIALIZED.getCode(), history.get(0).getEventType());
	}

	@Test
	void shouldRejectAccountsViewForUnauthorizedRole() {
		setJwtSecurityContext("client-sub", List.of("client_case"));

		assertThrows(AccountsLifecycleException.class, () -> service.getAccounts("BK-1"));
	}

	@Test
	void shouldReturnStoredAccountsReadiness() {
		setJwtSecurityContext("accounts-sub", List.of("accounts_manager"));
		when(commandExecutor.execute(Mockito.any(GetCaseInstanceCmd.class))).thenReturn(CaseInstance.builder()
				.businessKey("BK-1").accountsState(AccountsState.AWAITING_BILLING_SETUP.getCode())
				.accountsReadinessStatus(AccountsReadinessStatus.NOT_READY.getCode())
				.accountsReadinessSummary("Accounts setup is incomplete").build());

		AccountsReadinessEvaluation readiness = service.getAccountsReadiness("BK-1");

		assertEquals(AccountsReadinessStatus.NOT_READY.getCode(), readiness.getAccountsReadinessStatus());
	}

	@Test
	void shouldEvaluateAndPersistAccountsReadiness() throws Exception {
		setJwtSecurityContext("accounts-sub", List.of("accounts_manager"));
		when(commandExecutor.execute(Mockito.any(GetCaseInstanceCmd.class))).thenReturn(CaseInstance.builder()
				.businessKey("BK-1").matterType(MatterType.FLAT_FEE.getCode()).billingSetupComplete(true)
				.flatFeeAmount("2500").billingMode(BillingMode.FLAT_FEE.getCode()).build());

		AccountsReadinessEvaluation readiness = service.evaluateAccountsReadiness("BK-1");

		assertEquals(AccountsReadinessStatus.READY.getCode(), readiness.getAccountsReadinessStatus());
		verify(repository).update(Mockito.eq("BK-1"), Mockito.argThat(caseInstance ->
				AccountsReadinessStatus.READY.getCode().equals(caseInstance.getAccountsReadinessStatus())));
	}

	@Test
	void shouldReturnAccountsWork() {
		setJwtSecurityContext("accounts-sub", List.of("accounts_manager"));
		PageResult<CaseInstance> pageResult = PageResult.<CaseInstance>builder()
				.content(List.of(CaseInstance.builder().businessKey("BK-1")
						.accountsQueueId(AccountsWorkQueue.MISSING_RETAINER).build()))
				.build();
		when(repository.find(Mockito.any(CaseInstanceFilter.class))).thenReturn(pageResult);

		PageResult<CaseInstance> result = service.findAccountsWork(CaseInstanceFilter.builder()
				.status(java.util.Optional.empty()).caseDefsId(java.util.Optional.empty())
				.adminState(java.util.Optional.empty()).adminHealth(java.util.Optional.empty())
				.nextActionOwnerType(java.util.Optional.empty()).queueId(java.util.Optional.empty())
				.malformedCase(java.util.Optional.empty()).exceptionOnly(java.util.Optional.empty())
				.adminOwnerId(java.util.Optional.empty()).responsibleLawyerId(java.util.Optional.empty())
				.healthReasonCode(java.util.Optional.empty()).matterType(java.util.Optional.empty())
				.accountsReadinessStatus(java.util.Optional.empty()).accountsQueueId(java.util.Optional.empty())
				.accountsNextActionOwnerType(java.util.Optional.empty())
				.accountsNextActionDueBefore(java.util.Optional.empty()).accountsWorkBlocked(java.util.Optional.empty())
				.accountsReadinessReasonCode(java.util.Optional.empty()).cursor(Cursor.empty())
				.dir(org.springframework.data.domain.Sort.Direction.ASC).limit(10).build());

		assertEquals(1, result.size());
		assertEquals(AccountsWorkQueue.MISSING_RETAINER, result.first().getAccountsQueueId());
	}

	@Test
	void shouldSummarizeAccountsWorkAcrossMatters() {
		setJwtSecurityContext("accounts-sub", List.of("accounts_manager"));
		when(repository.find(Mockito.any(CaseInstanceFilter.class))).thenReturn(PageResult.<CaseInstance>builder()
				.content(List.of(
						CaseInstance.builder().businessKey("BK-1").accountsQueueId(AccountsWorkQueue.MISSING_RETAINER)
								.accountsNextActionOwnerType(AccountsWorkOwnerType.ACCOUNTS.getCode())
								.accountsReadinessStatus(AccountsReadinessStatus.NOT_READY.getCode())
								.accountsNextActionDueAt("2000-01-01").accountsWorkBlocked(false).build(),
						CaseInstance.builder().businessKey("BK-2")
								.accountsQueueId(AccountsWorkQueue.MALFORMED_ACCOUNTS_CONFIGURATION)
								.accountsNextActionOwnerType(AccountsWorkOwnerType.SYSTEM.getCode())
								.accountsReadinessStatus(AccountsReadinessStatus.BLOCKED.getCode())
								.accountsWorkBlocked(true).build()))
				.build());

		AccountsWorkSummary summary = service.getAccountsWorkSummary(CaseInstanceFilter.builder()
				.status(java.util.Optional.empty()).caseDefsId(java.util.Optional.empty())
				.adminState(java.util.Optional.empty()).adminHealth(java.util.Optional.empty())
				.nextActionOwnerType(java.util.Optional.empty()).queueId(java.util.Optional.empty())
				.malformedCase(java.util.Optional.empty()).exceptionOnly(java.util.Optional.empty())
				.adminOwnerId(java.util.Optional.empty()).responsibleLawyerId(java.util.Optional.empty())
				.healthReasonCode(java.util.Optional.empty()).matterType(java.util.Optional.empty())
				.accountsReadinessStatus(java.util.Optional.empty()).accountsQueueId(java.util.Optional.empty())
				.accountsNextActionOwnerType(java.util.Optional.empty())
				.accountsNextActionDueBefore(java.util.Optional.empty()).accountsWorkBlocked(java.util.Optional.empty())
				.accountsReadinessReasonCode(java.util.Optional.empty()).cursor(Cursor.empty())
				.dir(org.springframework.data.domain.Sort.Direction.ASC).limit(10).build());

		assertEquals(2, summary.getTotal());
		assertEquals(1, summary.getBlocked());
		assertEquals(1, summary.getDueOrOverdue());
		assertEquals(1, summary.getByQueue().get(AccountsWorkQueue.MISSING_RETAINER));
		assertEquals(1, summary.getByOwner().get(AccountsWorkOwnerType.SYSTEM.getCode()));
	}

	private void setJwtSecurityContext(String sub, List<String> roles) {
		Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(300), Map.of("alg", "none"),
				Map.of("sub", sub, "realm_access", Map.of("roles", roles)));
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken("user", jwt, List.of()));
	}

	private CaseInstanceFilter extractFilter(FindCaseInstanceCmd command) throws Exception {
		Field field = FindCaseInstanceCmd.class.getDeclaredField("caseFilter");
		field.setAccessible(true);
		return (CaseInstanceFilter) field.get(command);
	}

}
