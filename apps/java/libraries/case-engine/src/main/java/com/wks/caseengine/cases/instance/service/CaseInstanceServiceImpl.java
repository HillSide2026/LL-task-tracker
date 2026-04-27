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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.cases.instance.CaseComment;
import com.wks.caseengine.cases.instance.CaseDocument;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceFilter;
import com.wks.caseengine.cases.instance.CaseInstanceNotFoundException;
import com.wks.caseengine.cases.instance.admin.AdminLifecycleAccessSupport;
import com.wks.caseengine.cases.instance.admin.AdminLifecycleSupport;
import com.wks.caseengine.cases.instance.admin.AdminTransition;
import com.wks.caseengine.cases.instance.admin.AdminTransitionRequest;
import com.wks.caseengine.cases.instance.command.CreateCaseInstanceCommentCmd;
import com.wks.caseengine.cases.instance.command.CreateCaseInstanceDocumentCmd;
import com.wks.caseengine.cases.instance.command.DeleteCaseInstanceCmd;
import com.wks.caseengine.cases.instance.command.DeleteCaseInstanceCommentCmd;
import com.wks.caseengine.cases.instance.command.FindCaseInstanceCmd;
import com.wks.caseengine.cases.instance.command.GetCaseInstanceCmd;
import com.wks.caseengine.cases.instance.command.PatchCaseInstanceCmd;
import com.wks.caseengine.cases.instance.command.SaveCaseInstanceWithValuesCmd;
import com.wks.caseengine.cases.instance.command.StartCaseInstanceWithValuesCmd;
import com.wks.caseengine.cases.instance.command.TransitionCaseAdminCmd;
import com.wks.caseengine.cases.instance.command.UpdateCaseInstanceCommentCmd;
import com.wks.caseengine.command.CommandExecutor;
import com.wks.caseengine.pagination.PageResult;
import com.wks.caseengine.cases.instance.repository.CaseInstanceRepository;

@Component
public class CaseInstanceServiceImpl implements CaseInstanceService {

	@Autowired
	private CommandExecutor commandExecutor;

	@Autowired
	private CaseInstanceRepository caseInstanceRepository;

	@Override
	public PageResult<CaseInstance> find(CaseInstanceFilter filters) {
		PageResult<CaseInstance> result = commandExecutor.execute(new FindCaseInstanceCmd(applyVisibilityScope(filters)));
		if (result.content() != null) {
			result.content().removeIf(caseInstance -> !AdminLifecycleAccessSupport.canViewCase(caseInstance));
			result.content().forEach(this::refreshAdminControl);
		}
		return result;
	}

	@Override
	public CaseInstance get(final String businessKey) {
		CaseInstance caseInstance = commandExecutor.execute(new GetCaseInstanceCmd(businessKey));
		if (!AdminLifecycleAccessSupport.canViewCase(caseInstance)) {
			throw new CaseInstanceNotFoundException("CaseInstance not found for businessKey " + businessKey, null);
		}
		refreshAdminControl(caseInstance);
		return caseInstance;
	}

	@Override
	@Transactional
	public CaseInstance startWithValues(final CaseInstance caseInstance) {
		return commandExecutor.execute(new StartCaseInstanceWithValuesCmd(caseInstance));
	}

	@Override
	@Transactional
	public void saveWithValues(final CaseInstance caseInstance) {
		commandExecutor.execute(new SaveCaseInstanceWithValuesCmd(caseInstance));
	}

	@Override
	@Transactional
	public CaseInstance patch(final String businessKey, final CaseInstance mergePatch) {
		assertCanAccessCase(businessKey);
		return commandExecutor.execute(new PatchCaseInstanceCmd(businessKey, mergePatch));
	}

	@Override
	@Transactional
	public CaseInstance transition(final String businessKey, final AdminTransition transition,
			final AdminTransitionRequest request) {
		assertCanAccessCase(businessKey);
		return commandExecutor.execute(new TransitionCaseAdminCmd(businessKey, transition, request));
	}

	@Override
	@Transactional
	public void delete(final String businessKey) {
		CaseInstance caseInstance = assertCanAccessCase(businessKey);
		if (AdminLifecycleSupport.isAdminLifecycleCase(caseInstance)) {
			throw new com.wks.caseengine.cases.instance.admin.AdminLifecycleException(
					"Admin lifecycle matters cannot be deleted; use controlled close/archive flows");
		}
		commandExecutor.execute(new DeleteCaseInstanceCmd(businessKey));
	}

	@Override
	@Transactional
	public void saveDocument(final String businessKey, final CaseDocument document) {
		assertCanAccessCase(businessKey);
		commandExecutor.execute(new CreateCaseInstanceDocumentCmd(businessKey, document));
	}

	@Override
	@Transactional
	public void saveComment(final String businessKey, final CaseComment comment) {
		assertCanAccessCase(businessKey);
		commandExecutor.execute(new CreateCaseInstanceCommentCmd(businessKey, comment));
	}

	@Override
	@Transactional
	public void updateComment(final String businessKey, final String commentId, final String body) {
		assertCanAccessCase(businessKey);
		commandExecutor.execute(new UpdateCaseInstanceCommentCmd(businessKey, commentId, body));
	}

	@Override
	@Transactional
	public void deleteComment(final String businessKey, final String commentId) {
		assertCanAccessCase(businessKey);
		commandExecutor.execute(new DeleteCaseInstanceCommentCmd(businessKey, commentId));
	}

	private void refreshAdminControl(final CaseInstance caseInstance) {
		if (AdminLifecycleSupport.isAdminLifecycleCase(caseInstance)) {
			String previousState = caseInstance.getAdminState();
			String previousResumeState = caseInstance.getResumeToState();
			String previousStage = caseInstance.getStage();
			String previousStatus = caseInstance.getStatus() != null ? caseInstance.getStatus().getCode() : null;
			String previousQueueId = caseInstance.getQueueId();
			String previousHealth = caseInstance.getAdminHealth();
			String previousStaleSince = caseInstance.getStaleSince();
			Boolean previousMalformed = caseInstance.getMalformedCase();
			var previousReasons = caseInstance.getHealthReasonCodes() != null ? List.copyOf(caseInstance.getHealthReasonCodes()) : List.<String>of();

			boolean normalized = AdminLifecycleSupport.synchronizeDerivedFields(caseInstance);
			AdminLifecycleSupport.applyEvaluation(caseInstance);
			boolean evaluationChanged = !equalsNullable(previousHealth, caseInstance.getAdminHealth())
					|| !equalsNullable(previousStaleSince, caseInstance.getStaleSince())
					|| !equalsNullable(previousMalformed, caseInstance.getMalformedCase())
					|| !previousReasons.equals(caseInstance.getHealthReasonCodes())
					|| !equalsNullable(previousState, caseInstance.getAdminState())
					|| !equalsNullable(previousResumeState, caseInstance.getResumeToState())
					|| !equalsNullable(previousStage, caseInstance.getStage())
					|| !equalsNullable(previousStatus, caseInstance.getStatus() != null ? caseInstance.getStatus().getCode() : null)
					|| !equalsNullable(previousQueueId, caseInstance.getQueueId());
			if (normalized || evaluationChanged) {
				try {
					caseInstanceRepository.update(caseInstance.getBusinessKey(), caseInstance);
				} catch (Exception ignored) {
					// Best-effort read-side normalization for legacy records.
				}
			}
		}
	}

	private CaseInstanceFilter applyVisibilityScope(final CaseInstanceFilter filters) {
		if (!AdminLifecycleAccessSupport.shouldRestrictToAssignedLawyerCases()) {
			return filters;
		}
		String userId = AdminLifecycleAccessSupport.currentUserId().orElse(null);
		boolean adminLifecycleQuery = filters.getCaseDefsId()
				.filter(AdminLifecycleSupport.CASE_DEFINITION_ID::equals).isPresent();
		if (!adminLifecycleQuery) {
			return filters;
		}
		return CaseInstanceFilter.builder().status(filters.getStatus())
				.caseDefsId(filters.getCaseDefsId().isPresent() ? filters.getCaseDefsId()
						: java.util.Optional.of(AdminLifecycleSupport.CASE_DEFINITION_ID))
				.adminState(filters.getAdminState()).adminHealth(filters.getAdminHealth())
				.nextActionOwnerType(filters.getNextActionOwnerType()).queueId(filters.getQueueId())
				.malformedCase(filters.getMalformedCase()).exceptionOnly(filters.getExceptionOnly())
				.adminOwnerId(filters.getAdminOwnerId()).responsibleLawyerId(java.util.Optional.ofNullable(userId))
				.healthReasonCode(filters.getHealthReasonCode()).dir(filters.getDir()).limit(filters.getLimit())
				.cursor(filters.getCursor()).build();
	}

	private boolean equalsNullable(Object left, Object right) {
		return java.util.Objects.equals(left, right);
	}

	private CaseInstance assertCanAccessCase(final String businessKey) {
		CaseInstance caseInstance = commandExecutor.execute(new GetCaseInstanceCmd(businessKey));
		if (!AdminLifecycleAccessSupport.canViewCase(caseInstance)) {
			throw new CaseInstanceNotFoundException("CaseInstance not found for businessKey " + businessKey, null);
		}
		return caseInstance;
	}

}
