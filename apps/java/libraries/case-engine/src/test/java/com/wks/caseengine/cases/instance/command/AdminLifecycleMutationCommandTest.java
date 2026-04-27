package com.wks.caseengine.cases.instance.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import com.wks.bpm.engine.model.spi.ProcessInstance;
import com.wks.api.security.context.SecurityContextTenantHolderImpl;
import com.wks.bpm.engine.model.spi.ProcessVariable;
import com.wks.caseengine.cases.businesskey.GenericBusinessKeyGenerator;
import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseStage;
import com.wks.caseengine.cases.instance.CaseAttribute;
import com.wks.caseengine.cases.instance.CaseAttributeType;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseOwner;
import com.wks.caseengine.cases.instance.admin.AdminHealth;
import com.wks.caseengine.cases.instance.admin.AdminHealthReasonCode;
import com.wks.caseengine.cases.instance.admin.AdminLifecycleSupport;
import com.wks.caseengine.cases.instance.admin.AdminState;
import com.wks.caseengine.cases.instance.admin.NextActionOwnerType;
import com.wks.caseengine.cases.definition.repository.CaseDefinitionRepository;
import com.wks.caseengine.cases.instance.repository.CaseInstanceRepository;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.process.instance.ProcessInstanceService;

@ExtendWith(MockitoExtension.class)
class AdminLifecycleMutationCommandTest {

	@Mock
	private CaseInstanceRepository caseInstanceRepository;

	@Mock
	private CaseDefinitionRepository caseDefinitionRepository;

	@Mock
	private ProcessInstanceService processInstanceService;

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
		commandContext.setCaseDefRepository(caseDefinitionRepository);
		commandContext.setProcessInstanceService(processInstanceService);
		commandContext.setBusinessKeyCreator(new GenericBusinessKeyGenerator() {
			@Override
			public String generate() {
				return "BK-100";
			}
		});
		commandContext.setCaseCreationProcess("case-instance-create");
		commandContext.setGsonBuilder(new GsonBuilder());
	}

	@Test
	void shouldNormalizeAndEvaluateAdminLifecycleCaseOnPatch() throws Exception {
		CaseInstance existingCase = CaseInstance.builder().businessKey("BK-1")
				.caseDefinitionId(AdminLifecycleSupport.CASE_DEFINITION_ID).adminState("Open").stage("Maintenance")
				.nextActionSummary("Continue control").nextActionOwnerType(NextActionOwnerType.ADMIN.getCode()).build();
		when(caseInstanceRepository.get("BK-1")).thenReturn(existingCase);

		new PatchCaseInstanceCmd("BK-1", CaseInstance.builder().build()).execute(commandContext);

		verify(caseInstanceRepository).update(eq("BK-1"), caseInstanceCaptor.capture());
		CaseInstance updatedCase = caseInstanceCaptor.getValue();
		assertEquals(AdminState.OPENED.getCode(), updatedCase.getAdminState());
		assertEquals("Opening", updatedCase.getStage());
		assertEquals(AdminHealth.RED.getCode(), updatedCase.getAdminHealth());
		assertTrue(updatedCase.getHealthReasonCodes().contains(AdminHealthReasonCode.UNOWNED_ACTIVE_CASE.getCode()));
		assertNotNull(updatedCase.getHealthEvaluatedAt());
	}

	@Test
	void shouldNormalizeAndEvaluateAdminLifecycleCaseOnSave() {
		CaseInstance caseInstance = CaseInstance.builder().businessKey("BK-2")
				.caseDefinitionId(AdminLifecycleSupport.CASE_DEFINITION_ID).adminState("Open").stage("Maintenance")
				.nextActionSummary("Continue control").nextActionOwnerType(NextActionOwnerType.ADMIN.getCode()).build();
		when(caseInstanceRepository.save(any(CaseInstance.class))).thenReturn("saved-id");

		new SaveCaseInstanceWithValuesCmd(caseInstance).execute(commandContext);

		verify(caseInstanceRepository).save(caseInstanceCaptor.capture());
		CaseInstance savedCase = caseInstanceCaptor.getValue();
		assertEquals(AdminState.OPENED.getCode(), savedCase.getAdminState());
		assertEquals("Opening", savedCase.getStage());
		assertEquals(AdminHealth.RED.getCode(), savedCase.getAdminHealth());
		assertNotNull(savedCase.getHealthEvaluatedAt());
	}

	@Test
	void shouldInitializeAdminLifecycleCaseWithHealthOnStart() throws Exception {
		setJwtSecurityContext("ops-admin-sub", List.of("ops_admin"));
		CaseDefinition caseDefinition = CaseDefinition.builder().id(AdminLifecycleSupport.CASE_DEFINITION_ID)
				.stages(List.of(CaseStage.builder().id("0").index(0).name("Onboarding").build())).build();
		when(caseDefinitionRepository.get(AdminLifecycleSupport.CASE_DEFINITION_ID)).thenReturn(caseDefinition);
		when(processInstanceService.start(any(), any(), org.mockito.ArgumentMatchers.<Optional<ProcessVariable>>any()))
				.thenReturn(ProcessInstance.builder().id("process-instance-1").build());

		CaseInstance caseInstanceParam = CaseInstance.builder().caseDefinitionId(AdminLifecycleSupport.CASE_DEFINITION_ID)
				.owner(CaseOwner.builder().id("admin-1").name("Admin Owner").build()).attributes(new ArrayList<>()).build();

		CaseInstance preparedCase = new StartCaseInstanceWithValuesCmd(caseInstanceParam).execute(commandContext);

		assertEquals("BK-100", preparedCase.getBusinessKey());
		assertEquals(AdminState.INTAKE_REVIEW.getCode(), preparedCase.getAdminState());
		assertEquals("Onboarding", preparedCase.getStage());
		assertEquals(AdminHealth.GREEN.getCode(), preparedCase.getAdminHealth());
		assertFalse(preparedCase.getMalformedCase());
		assertTrue(preparedCase.getAttributes().stream().map(CaseAttribute::getName).toList().contains("createdAt"));
	}

	private void setJwtSecurityContext(String sub, List<String> roles) {
		Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(300), Map.of("alg", "none"),
				Map.of("sub", sub, "realm_access", Map.of("roles", roles)));
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken("user", jwt, List.of()));
	}
}
