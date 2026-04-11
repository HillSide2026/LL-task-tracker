package com.wks.caseengine.cases.instance.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import com.google.gson.GsonBuilder;
import com.wks.api.security.context.SecurityContextTenantHolderImpl;
import com.wks.caseengine.cases.definition.CaseStatus;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.admin.AdminEventType;
import com.wks.caseengine.cases.instance.admin.AdminLifecycleException;
import com.wks.caseengine.cases.instance.admin.AdminLifecycleSupport;
import com.wks.caseengine.cases.instance.admin.AdminState;
import com.wks.caseengine.cases.instance.admin.AdminTransition;
import com.wks.caseengine.cases.instance.admin.AdminTransitionRequest;
import com.wks.caseengine.cases.instance.admin.NextActionOwnerType;
import com.wks.caseengine.cases.instance.repository.CaseInstanceRepository;
import com.wks.caseengine.command.CommandContext;

@ExtendWith(MockitoExtension.class)
class TransitionCaseAdminCmdTest {

	@Mock
	private CaseInstanceRepository caseInstanceRepository;

	@Captor
	private ArgumentCaptor<CaseInstance> caseInstanceCaptor;

	private CommandContext commandContext;

	@BeforeEach
	void setup() {
		commandContext = new CommandContext();
		SecurityContextTenantHolderImpl tenantHolder = new SecurityContextTenantHolderImpl();
		tenantHolder.setTenantId("wks");
		tenantHolder.setUserId("ops-admin-sub");
		commandContext.setSecurityContextTenantHolder(tenantHolder);
		commandContext.setCaseInstanceRepository(caseInstanceRepository);
		commandContext.setGsonBuilder(new GsonBuilder());
		setJwtSecurityContext("ops-admin-sub", List.of("ops_admin"));
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void shouldActivateOpenedMatterAndEmitCaseActivatedEvent() throws Exception {
		CaseInstance existingCase = CaseInstance.builder().businessKey("BK-1")
				.caseDefinitionId(AdminLifecycleSupport.CASE_DEFINITION_ID).adminState(AdminState.OPENED.getCode())
				.stage("Open").status("WIP_CASE_STATUS").queueId(AdminLifecycleSupport.QUEUE_OPEN)
				.adminOwnerId("admin-1").adminOwnerName("Admin Owner")
				.responsibleLawyerId("lawyer-sub-1").responsibleLawyerName("Assigned Lawyer")
				.nextActionOwnerType(NextActionOwnerType.ADMIN.getCode()).nextActionSummary("Activate control").build();
		when(caseInstanceRepository.get("BK-1")).thenReturn(existingCase);

		CaseInstance updatedCase = new TransitionCaseAdminCmd("BK-1", AdminTransition.ACTIVATE_MATTER,
				AdminTransitionRequest.builder().nextActionSummary("Matter is now active").build()).execute(commandContext);

		verify(caseInstanceRepository).update(eq("BK-1"), caseInstanceCaptor.capture());
		assertEquals(AdminState.ACTIVE.getCode(), updatedCase.getAdminState());
		assertEquals(AdminLifecycleSupport.QUEUE_ACTIVE, updatedCase.getQueueId());
		assertEquals("Matter is now active", updatedCase.getNextActionSummary());
		assertTrue(updatedCase.getAdminEvents().stream()
				.anyMatch(event -> AdminEventType.CASE_ACTIVATED.getCode().equals(event.getEventType())));
		assertEquals(AdminState.ACTIVE.getCode(), caseInstanceCaptor.getValue().getAdminState());
	}

	@Test
	void shouldRejectInvalidMaintenanceResumeTarget() throws Exception {
		CaseInstance existingCase = CaseInstance.builder().businessKey("BK-1")
				.caseDefinitionId(AdminLifecycleSupport.CASE_DEFINITION_ID)
				.adminState(AdminState.MAINTENANCE_CLIENT_WAIT.getCode()).resumeToState(AdminState.READY_TO_OPEN.getCode())
				.stage("Open").status("WIP_CASE_STATUS").adminOwnerId("admin-1").adminOwnerName("Admin Owner")
				.nextActionOwnerType(NextActionOwnerType.CLIENT.getCode()).nextActionSummary("Await client response")
				.waitingReasonCode("CLIENT_DOCS").waitingReasonText("Await client documents")
				.waitingSince("2026-04-01").expectedResponseAt("2026-04-15").build();
		when(caseInstanceRepository.get("BK-1")).thenReturn(existingCase);

		assertThrows(AdminLifecycleException.class,
				() -> new TransitionCaseAdminCmd("BK-1", AdminTransition.RESUME_FROM_MAINTENANCE_CLIENT_WAIT,
						AdminTransitionRequest.builder().nextActionSummary("Resume active work").build())
						.execute(commandContext));
	}

	@Test
	void shouldKeepProtectedFieldsStableDuringMaintenanceControlUpdate() throws Exception {
		CaseInstance existingCase = CaseInstance.builder().businessKey("BK-1")
				.caseDefinitionId(AdminLifecycleSupport.CASE_DEFINITION_ID).adminState(AdminState.ACTIVE.getCode())
				.stage("Open").status("WIP_CASE_STATUS").queueId(AdminLifecycleSupport.QUEUE_ACTIVE)
				.adminOwnerId("admin-1").adminOwnerName("Admin Owner")
				.responsibleLawyerId("lawyer-sub-1").responsibleLawyerName("Assigned Lawyer")
				.nextActionOwnerType(NextActionOwnerType.ADMIN.getCode()).nextActionOwnerRef("Admin Owner")
				.nextActionSummary("Maintain matter control").nextActionDueAt("2026-04-15").build();
		when(caseInstanceRepository.get("BK-1")).thenReturn(existingCase);

		CaseInstance updatedCase = new TransitionCaseAdminCmd("BK-1", AdminTransition.UPDATE_MAINTENANCE_CONTROL,
				AdminTransitionRequest.builder().adminOwnerId("admin-2").adminOwnerName("Admin Backup")
						.responsibleLawyerId("lawyer-sub-2").responsibleLawyerName("Backup Lawyer")
						.nextActionOwnerType(NextActionOwnerType.LAWYER.getCode()).nextActionOwnerRef("Backup Lawyer")
						.nextActionSummary("Lawyer follow-up needed").nextActionDueAt("2026-04-20")
						.externalPartyRef("Vendor A").queueId("should-not-change").build())
				.execute(commandContext);

		verify(caseInstanceRepository).update(eq("BK-1"), caseInstanceCaptor.capture());
		assertEquals(AdminState.ACTIVE.getCode(), updatedCase.getAdminState());
		assertEquals("Open", updatedCase.getStage());
		assertEquals(CaseStatus.WIP_CASE_STATUS, updatedCase.getStatus());
		assertEquals(AdminLifecycleSupport.QUEUE_ACTIVE, updatedCase.getQueueId());
		assertEquals("admin-2", updatedCase.getAdminOwnerId());
		assertEquals("lawyer-sub-2", updatedCase.getResponsibleLawyerId());
		assertEquals(NextActionOwnerType.LAWYER.getCode(), updatedCase.getNextActionOwnerType());
		assertEquals("Vendor A", updatedCase.getExternalPartyRef());
		List<String> eventTypes = updatedCase.getAdminEvents().stream().map(event -> event.getEventType()).toList();
		assertTrue(eventTypes.contains(AdminEventType.OWNER_ASSIGNED.getCode()), eventTypes.toString());
		assertTrue(eventTypes.contains(AdminEventType.NEXT_ACTION_SET.getCode()), eventTypes.toString());
		assertEquals(AdminLifecycleSupport.QUEUE_ACTIVE, caseInstanceCaptor.getValue().getQueueId());
	}

	private void setJwtSecurityContext(String sub, List<String> roles) {
		Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(300), Map.of("alg", "none"),
				Map.of("sub", sub, "realm_access", Map.of("roles", roles)));
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken("user", jwt, List.of()));
	}
}
