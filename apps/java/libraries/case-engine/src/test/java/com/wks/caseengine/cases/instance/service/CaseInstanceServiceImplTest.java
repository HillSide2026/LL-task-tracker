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
				AdminState.OPENED.getCode().equals(caseInstance.getAdminState()) && caseInstance.getAdminHealth() != null));
		assertEquals(AdminState.OPENED.getCode(), result.first().getAdminState());
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
