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
package com.wks.caseengine.rest.server;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import com.wks.bpm.engine.client.facade.BpmEngineClientFacade;
import com.wks.caseengine.cases.definition.CaseDefinitionNotFoundException;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceNotFoundException;
import com.wks.caseengine.cases.instance.accounts.AccountsEvent;
import com.wks.caseengine.cases.instance.accounts.AccountsEventType;
import com.wks.caseengine.cases.instance.accounts.AccountsHealthReasonCode;
import com.wks.caseengine.cases.instance.accounts.AccountsLifecycleException;
import com.wks.caseengine.cases.instance.accounts.AccountsLifecycleStage;
import com.wks.caseengine.cases.instance.accounts.AccountsReadinessEvaluation;
import com.wks.caseengine.cases.instance.accounts.AccountsReadinessStatus;
import com.wks.caseengine.cases.instance.accounts.AccountsState;
import com.wks.caseengine.cases.instance.accounts.AccountsTransition;
import com.wks.caseengine.cases.instance.accounts.AccountsWorkQueue;
import com.wks.caseengine.cases.instance.accounts.AccountsWorkSummary;
import com.wks.caseengine.cases.instance.admin.AdminLifecycleException;
import com.wks.caseengine.cases.instance.admin.AdminTransition;
import com.wks.caseengine.cases.instance.service.CaseInstanceService;
import com.wks.caseengine.pagination.PageResult;
import com.wks.caseengine.rest.config.GsonConfiguration;
import com.wks.caseengine.rest.mocks.MockSecurityContext;

@WebMvcTest(controllers = CaseInstanceController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GsonConfiguration.class)
public class CaseInstanceControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CaseInstanceService caseInstanceService;

	@MockBean
	private BpmEngineClientFacade bpmEngineClientFacade;

	@BeforeEach
	public void setup() {
		SecurityContextHolder.setContext(new MockSecurityContext("wks", "localhost"));
	}

	@AfterEach
	private void teardown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void shouldSaveCaseInstance() throws Exception {
		when(caseInstanceService.startWithValues(Mockito.any())).thenReturn(CaseInstance.builder().build());
		this.mockMvc
				.perform(post("/case").contentType(MediaType.APPLICATION_JSON).content("{\"caseDefinitionId\":\"CD-1\"}"))
				.andExpect(status().isOk());
	}

	@Test
	public void shouldRespond404_whenSavingCaseInstanceWithNoCaseDefinitionId() throws Exception {
		when(caseInstanceService.startWithValues(Mockito.any())).thenThrow(new CaseDefinitionNotFoundException());
		this.mockMvc
				.perform(post("/case").contentType(MediaType.APPLICATION_JSON).content("{\"caseDefinitionId\":\"CD-1\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void shouldDeleteCaseInstance() throws Exception {
		this.mockMvc.perform(delete("/case/{businessKey}", "CI-1")).andExpect(status().isNoContent());
	}

	@Test
	public void shouldMergePatchCaseInstance() throws Exception {
		when(caseInstanceService.patch(Mockito.eq("1"), Mockito.any())).thenReturn(CaseInstance.builder().build());
		this.mockMvc
				.perform(patch("/case/{businessKey}", "1").contentType("application/merge-patch+json").content("{}"))
				.andExpect(status().isNoContent());
	}

	@Test
	public void shouldRejectStatusPatchForAdminLifecycleCase() throws Exception {
		when(caseInstanceService.patch(Mockito.eq("BK-1"), Mockito.any())).thenThrow(new AdminLifecycleException(
				"Admin lifecycle status, stage, and queue changes must be performed through explicit transition endpoints"));

		this.mockMvc.perform(patch("/case/{businessKey}", "BK-1").contentType("application/merge-patch+json")
				.content("{\"status\":\"CLOSED_CASE_STATUS\"}")).andExpect(status().isBadRequest());
	}

	@Test
	public void shouldRejectQueuePatchForAdminLifecycleCase() throws Exception {
		when(caseInstanceService.patch(Mockito.eq("BK-1"), Mockito.any())).thenThrow(new AdminLifecycleException(
				"Admin lifecycle status, stage, and queue changes must be performed through explicit transition endpoints"));

		this.mockMvc.perform(patch("/case/{businessKey}", "BK-1").contentType("application/merge-patch+json")
				.content("{\"queueId\":\"matter-admin-active\"}")).andExpect(status().isBadRequest());
	}

	@Test
	public void shouldRejectAdminLifecycleFieldsInGenericPatch() throws Exception {
		this.mockMvc.perform(patch("/case/{businessKey}", "1").contentType("application/merge-patch+json")
				.content("{\"adminState\":\"Ready to Open\"}")).andExpect(status().isBadRequest());
	}

	@Test
	public void shouldGetCaseInstance() throws Exception {
		when(caseInstanceService.get("1")).thenReturn(CaseInstance.builder().build());
		this.mockMvc.perform(get("/case/{businessKey}", "1")).andExpect(status().isOk());
	}

	@Test
	public void shouldTransitionAdminLifecycleCase() throws Exception {
		CaseInstance caseInstance = CaseInstance.builder().caseDefinitionId("matter-admin-opening-control")
				.adminState("Intake Review").stage("Onboarding").build();
		when(caseInstanceService.transition(Mockito.eq("BK-1"), Mockito.eq(AdminTransition.MARK_AWAITING_ENGAGEMENT),
				Mockito.any())).thenReturn(caseInstance);
		this.mockMvc.perform(post("/case/{businessKey}/transition/{transitionName}", "BK-1", "markAwaitingEngagement")
				.contentType(MediaType.APPLICATION_JSON).content("{\"nextActionSummary\":\"Await engagement materials\"}"))
				.andExpect(status().isOk());
	}

	@Test
	public void shouldCloseAdminLifecycleCase() throws Exception {
		CaseInstance caseInstance = CaseInstance.builder().caseDefinitionId("matter-admin-opening-control")
				.adminState("Closed").stage("Closing").status("CLOSED_CASE_STATUS").build();
		when(caseInstanceService.transition(Mockito.eq("BK-2"), Mockito.eq(AdminTransition.CLOSE_MATTER), Mockito.any()))
				.thenReturn(caseInstance);
		this.mockMvc.perform(post("/case/{businessKey}/transition/{transitionName}", "BK-2", "closeMatter")
				.contentType(MediaType.APPLICATION_JSON).content("{\"note\":\"Matter closure confirmed\"}"))
				.andExpect(status().isOk());
	}

	@Test
	public void shouldFindCaseInstance() throws Exception {
		when(caseInstanceService.find(Mockito.any())).thenReturn(PageResult.<CaseInstance>builder().content(List.of()).build());
		this.mockMvc.perform(get("/case")).andExpect(status().isOk());
	}

	@Test
	public void shouldPassServerSideExceptionFiltersToFind() throws Exception {
		when(caseInstanceService.find(Mockito.any())).thenReturn(PageResult.<CaseInstance>builder().content(List.of()).build());

		this.mockMvc.perform(get("/case").param("caseDefinitionId", "matter-admin-opening-control")
				.param("exceptionOnly", "true").param("adminOwnerId", "admin-1")
				.param("healthReasonCode", "LAWYER_RESPONSE_STALE")).andExpect(status().isOk());

		Mockito.verify(caseInstanceService).find(Mockito.argThat(filter -> filter.getExceptionOnly().orElse(false)
				&& filter.getAdminOwnerId().orElse("").equals("admin-1")
				&& filter.getHealthReasonCode().orElse("").equals("LAWYER_RESPONSE_STALE")));
	}

	@Test
	public void shouldRejectMaintenanceLawyerTransitionWhenLawyerIsNotAssigned() throws Exception {
		SecurityContextHolder.setContext(
				new MockSecurityContext("wks", "localhost", "lawyer-sub-1", List.of("lawyer_user")));
		when(caseInstanceService.transition(Mockito.eq("BK-1"), Mockito.eq(AdminTransition.LAWYER_RETURN_TO_ACTIVE),
				Mockito.any())).thenThrow(new AdminLifecycleException(
						"Current user is not allowed to perform lawyer transitions for this matter"));

		this.mockMvc.perform(post("/case/{businessKey}/transition/{transitionName}", "BK-1", "lawyerReturnToActive")
				.contentType(MediaType.APPLICATION_JSON).content("{\"nextActionSummary\":\"Resume active work\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void shouldAllowMaintenanceLawyerTransitionWhenLawyerIsAssigned() throws Exception {
		SecurityContextHolder.setContext(
				new MockSecurityContext("wks", "localhost", "lawyer-sub-1", List.of("lawyer_user")));
		CaseInstance caseInstance = CaseInstance.builder().caseDefinitionId("matter-admin-opening-control")
				.adminState("Maintenance Lawyer Review").stage("Maintenance").responsibleLawyerId("lawyer-sub-1")
				.responsibleLawyerName("Assigned Lawyer").adminOwnerId("admin-1").adminOwnerName("Admin Owner").build();
		when(caseInstanceService.transition(Mockito.eq("BK-1"), Mockito.eq(AdminTransition.LAWYER_RETURN_TO_ACTIVE),
				Mockito.any())).thenReturn(caseInstance);

		this.mockMvc.perform(post("/case/{businessKey}/transition/{transitionName}", "BK-1", "lawyerReturnToActive")
				.contentType(MediaType.APPLICATION_JSON).content("{\"nextActionSummary\":\"Resume active work\"}"))
				.andExpect(status().isOk());
	}

	@Test
	public void shouldRejectInvalidMaintenanceResumeTarget() throws Exception {
		when(caseInstanceService.transition(Mockito.eq("BK-1"),
				Mockito.eq(AdminTransition.RESUME_FROM_MAINTENANCE_CLIENT_WAIT), Mockito.any()))
				.thenThrow(new AdminLifecycleException("resumeToState Ready to Open is not a valid maintenance wait target"));

		this.mockMvc.perform(post("/case/{businessKey}/transition/{transitionName}", "BK-1",
				"resumeFromMaintenanceClientWait").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nextActionSummary\":\"Resume maintenance work\"}")).andExpect(status().isBadRequest());
	}

	@Test
	public void shouldReturnAccountsReasonCodesWhenAdminReadyToOpenGateBlocks() throws Exception {
		when(caseInstanceService.transition(Mockito.eq("BK-1"), Mockito.eq(AdminTransition.MARK_READY_TO_OPEN),
				Mockito.any())).thenThrow(new AdminLifecycleException(
						"Accounts readiness must be READY before moving to Ready to Open",
						List.of(AccountsHealthReasonCode.MISSING_BILLING_SETUP.getCode())));

		this.mockMvc.perform(post("/case/{businessKey}/transition/{transitionName}", "BK-1", "markReadyToOpen")
				.contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.reasonCodes[0]").value(AccountsHealthReasonCode.MISSING_BILLING_SETUP.getCode()));
	}

	@Test
	public void shouldGetAccountsShell() throws Exception {
		when(caseInstanceService.getAccounts("BK-1")).thenReturn(CaseInstance.builder().businessKey("BK-1")
				.accountsStage(AccountsLifecycleStage.SETUP.getCode())
				.accountsState(AccountsState.AWAITING_BILLING_SETUP.getCode()).build());

		this.mockMvc.perform(get("/case/{businessKey}/accounts", "BK-1")).andExpect(status().isOk());
	}

	@Test
	public void shouldGetAccountsHistory() throws Exception {
		when(caseInstanceService.getAccountsHistory("BK-1")).thenReturn(List.of(AccountsEvent.builder()
				.eventType(AccountsEventType.ACCOUNTS_INITIALIZED.getCode()).build()));

		this.mockMvc.perform(get("/case/{businessKey}/accounts/history", "BK-1")).andExpect(status().isOk());
	}

	@Test
	public void shouldRejectUnsupportedAccountsTransition() throws Exception {
		when(caseInstanceService.transitionAccounts(Mockito.eq("BK-1"), Mockito.eq(AccountsTransition.ACTIVATE_ACCOUNTS),
				Mockito.any())).thenThrow(new AccountsLifecycleException(
						"Accounts activation blocked by policy",
						List.of(AccountsHealthReasonCode.MISSING_BILLING_SETUP.getCode())));

		this.mockMvc.perform(post("/case/{businessKey}/accounts/transition", "BK-1")
				.contentType(MediaType.APPLICATION_JSON).content("{\"transition\":\"activateAccounts\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.reasonCodes[0]").value(AccountsHealthReasonCode.MISSING_BILLING_SETUP.getCode()));
	}

	@Test
	public void shouldGetAccountsReadiness() throws Exception {
		when(caseInstanceService.getAccountsReadiness("BK-1")).thenReturn(AccountsReadinessEvaluation.builder()
				.accountsReadinessStatus(AccountsReadinessStatus.NOT_EVALUATED.getCode()).build());

		this.mockMvc.perform(get("/case/{businessKey}/accounts/readiness", "BK-1")).andExpect(status().isOk());
	}

	@Test
	public void shouldEvaluateAccountsReadiness() throws Exception {
		when(caseInstanceService.evaluateAccountsReadiness("BK-1")).thenReturn(AccountsReadinessEvaluation.builder()
				.accountsReadinessStatus(AccountsReadinessStatus.READY.getCode()).build());

		this.mockMvc.perform(post("/case/{businessKey}/accounts/evaluate-readiness", "BK-1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accountsReadinessStatus").value(AccountsReadinessStatus.READY.getCode()));
	}

	@Test
	public void shouldGetAccountsWork() throws Exception {
		when(caseInstanceService.findAccountsWork(Mockito.any())).thenReturn(PageResult.<CaseInstance>builder()
				.content(List.of(CaseInstance.builder().businessKey("BK-1")
						.accountsQueueId(AccountsWorkQueue.MISSING_RETAINER).build()))
				.build());

		this.mockMvc.perform(get("/case/accounts/work").param("accountsQueueId", AccountsWorkQueue.MISSING_RETAINER))
				.andExpect(status().isOk());

		Mockito.verify(caseInstanceService).findAccountsWork(Mockito.argThat(filter -> filter.getAccountsQueueId()
				.orElse("").equals(AccountsWorkQueue.MISSING_RETAINER)));
	}

	@Test
	public void shouldGetAccountsWorkSummary() throws Exception {
		when(caseInstanceService.getAccountsWorkSummary(Mockito.any())).thenReturn(AccountsWorkSummary.builder()
				.total(2).byQueue(java.util.Map.of(AccountsWorkQueue.MISSING_RETAINER, 2L)).build());

		this.mockMvc.perform(get("/case/accounts/work/summary")).andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(2));
	}

	@Test
	public void shouldHideAdminLifecycleCaseFromClientRole() throws Exception {
		SecurityContextHolder
				.setContext(new MockSecurityContext("wks", "localhost", "client-sub-1", List.of("client_case")));
		when(caseInstanceService.get("BK-1")).thenThrow(new CaseInstanceNotFoundException());

		this.mockMvc.perform(get("/case/{businessKey}", "BK-1")).andExpect(status().isNotFound());
	}

	@Test
	public void shouldRejectCommentForUnassignedLawyerOnAdminLifecycleCase() throws Exception {
		SecurityContextHolder.setContext(
				new MockSecurityContext("wks", "localhost", "lawyer-sub-1", List.of("lawyer_user")));
		Mockito.doThrow(new CaseInstanceNotFoundException()).when(caseInstanceService).saveComment(Mockito.eq("BK-1"),
				Mockito.any());

		this.mockMvc.perform(post("/case/{businessKey}/comment", "BK-1").contentType(MediaType.APPLICATION_JSON)
				.content("{}")).andExpect(status().isNotFound());
	}

	@Test
	public void shouldRejectDeleteForAdminLifecycleCase() throws Exception {
		Mockito.doThrow(new AdminLifecycleException(
				"Admin lifecycle matters cannot be deleted; use controlled close/archive flows"))
				.when(caseInstanceService).delete("BK-1");

		this.mockMvc.perform(delete("/case/{businessKey}", "BK-1")).andExpect(status().isBadRequest());
	}

	@Test
	public void shouldSaveDocument() throws Exception {
		this.mockMvc
				.perform(
						post("/case/{businessKey}/document", "BK-1").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isNoContent());
	}

	@Test
	public void shouldSaveComment() throws Exception {
		this.mockMvc
				.perform(post("/case/{businessKey}/comment", "BK-1").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isNoContent());
	}

	@Test
	public void shouldUpdateComment() throws Exception {
		this.mockMvc.perform(put("/case/{businessKey}/comment/{commentId}", "1", "1")
				.contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isNoContent());
	}

	@Test
	public void shouldDeleteComment() throws Exception {
		this.mockMvc.perform(delete("/case/{businessKey}/comment/{commentId}", "BK-1", "Comment-1"))
				.andExpect(status().isNoContent());
	}

}
