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
package com.wks.caseengine.cases.instance.command;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;

import com.google.gson.Gson;
import com.wks.bpm.engine.model.spi.ProcessVariable;
import com.wks.bpm.engine.model.spi.ProcessVariableType;
import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseDefinitionNotFoundException;
import com.wks.caseengine.cases.definition.CaseStage;
import com.wks.caseengine.cases.definition.CaseStatus;
import com.wks.caseengine.cases.instance.CaseAttribute;
import com.wks.caseengine.cases.instance.CaseAttributeType;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.admin.AdminEvent;
import com.wks.caseengine.cases.instance.admin.AdminEventType;
import com.wks.caseengine.cases.instance.admin.AdminLifecycleSupport;
import com.wks.caseengine.cases.instance.admin.AdminState;
import com.wks.caseengine.cases.instance.admin.NextActionOwnerType;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

import lombok.AllArgsConstructor;
import lombok.Setter;

/**
 * @author victor.franca
 *
 */
@AllArgsConstructor
@Setter
public class StartCaseInstanceWithValuesCmd implements Command<CaseInstance> {

	private CaseInstance caseInstanceParam;

	@Override
	public CaseInstance execute(CommandContext commandContext) {

		CaseDefinition caseDefinition = retrieveCaseDefinition(commandContext);

		caseInstanceParam.addAttribute(
				new CaseAttribute("createdAt", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
						CaseAttributeType.STRING.getValue()));

		String businessKey = generateBusinessKey(commandContext);

		CaseInstance.CaseInstanceBuilder caseInstanceBuilder = CaseInstance.builder().businessKey(businessKey)
				.attributes(caseInstanceParam.getAttributes()).caseDefinitionId(caseInstanceParam.getCaseDefinitionId())
				.owner(caseInstanceParam.getOwner());

		Optional<CaseStage> firstStage = caseDefinition.getStages().stream()
				.sorted(Comparator.comparing(CaseStage::getIndex)).findFirst();
		if (firstStage.isPresent()) {
			caseInstanceBuilder.stage(firstStage.get().getName());
		}

		CaseInstance preparedCaseInstance = caseInstanceBuilder.build();
		if (AdminLifecycleSupport.isAdminLifecycleCase(preparedCaseInstance)) {
			preparedCaseInstance.setStatus(CaseStatus.WIP_CASE_STATUS);
			preparedCaseInstance.setAdminOwnerId(
					preparedCaseInstance.getOwner() != null ? preparedCaseInstance.getOwner().getId() : null);
			preparedCaseInstance.setAdminOwnerName(
					preparedCaseInstance.getOwner() != null ? preparedCaseInstance.getOwner().getName() : null);
			preparedCaseInstance.setAdminState(AdminState.INTAKE_REVIEW.getCode());
			preparedCaseInstance.setStage(AdminLifecycleSupport.expectedStageForState(preparedCaseInstance.getAdminState()));
			preparedCaseInstance.setQueueId(AdminLifecycleSupport.defaultQueueForState(preparedCaseInstance.getAdminState()));
			preparedCaseInstance.setNextActionOwnerType(NextActionOwnerType.ADMIN.getCode());
			preparedCaseInstance.setNextActionOwnerRef(preparedCaseInstance.getAdminOwnerName());
			preparedCaseInstance.setNextActionSummary("Review intake for opening readiness");
			preparedCaseInstance.setLastStateChangedAt(AdminLifecycleSupport.nowTimestamp());
			preparedCaseInstance.addAdminEvent(AdminEvent.builder().eventType(AdminEventType.CASE_CREATED.getCode())
					.occurredAt(AdminLifecycleSupport.nowDate()).actorType("User")
					.actorId(commandContext.getSecurityContextTenantHolder().getUserId().orElse("system"))
					.actorName(preparedCaseInstance.getOwner() != null ? preparedCaseInstance.getOwner().getName() : null)
					.toState(preparedCaseInstance.getAdminState()).toStage(preparedCaseInstance.getStage())
					.note("Case created").build());
			AdminLifecycleSupport.normalizeLegacyState(preparedCaseInstance);
			AdminLifecycleSupport.applyEvaluation(preparedCaseInstance);
		}

		ProcessVariable caseInstanceProcessVariable = generateCaseInstanceProcessVariable(commandContext,
				preparedCaseInstance);

		commandContext.getProcessInstanceService().start(commandContext.getCaseCreationProcess(),
				Optional.of(businessKey), Optional.of(caseInstanceProcessVariable));

		return preparedCaseInstance;
	}

	private ProcessVariable generateCaseInstanceProcessVariable(CommandContext commandContext,
			CaseInstance preparedCaseInstance) {

		Gson gson = commandContext.getGsonBuilder().create();
		ProcessVariable caseInstanceProcessVariable = ProcessVariable.builder()
				.type(ProcessVariableType.JSON.getValue()).name("caseInstance")
				.value(gson.toJsonTree(preparedCaseInstance).toString()).build();

		return caseInstanceProcessVariable;
	}

	private String generateBusinessKey(CommandContext commandContext) {
		String businessKey = null;
		if (caseInstanceParam.getBusinessKey() == null) {
			businessKey = commandContext.getBusinessKeyCreator().generate();
		} else {
			businessKey = caseInstanceParam.getBusinessKey();
		}
		return businessKey;
	}

	private CaseDefinition retrieveCaseDefinition(CommandContext commandContext) {
		CaseDefinition caseDefinition;
		try {
			caseDefinition = commandContext.getCaseDefRepository().get(caseInstanceParam.getCaseDefinitionId());
		} catch (DatabaseRecordNotFoundException e) {
			throw new CaseDefinitionNotFoundException(e.getMessage(), e);
		}
		return caseDefinition;
	}

}
