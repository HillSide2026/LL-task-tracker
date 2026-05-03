package com.wks.caseengine.cases.instance.command;

import java.util.Objects;

import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceNotFoundException;
import com.wks.caseengine.cases.instance.accounts.AccountsReadinessEvaluation;
import com.wks.caseengine.cases.instance.accounts.AccountsReadinessSupport;
import com.wks.caseengine.cases.instance.admin.AdminEvent;
import com.wks.caseengine.cases.instance.admin.AdminEventType;
import com.wks.caseengine.cases.instance.admin.AdminLifecycleAccessSupport;
import com.wks.caseengine.cases.instance.admin.AdminLifecycleException;
import com.wks.caseengine.cases.instance.admin.AdminLifecycleSupport;
import com.wks.caseengine.cases.instance.admin.AdminState;
import com.wks.caseengine.cases.instance.admin.AdminTransition;
import com.wks.caseengine.cases.instance.admin.AdminTransitionRequest;
import com.wks.caseengine.cases.instance.admin.NextActionOwnerType;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransitionCaseAdminCmd implements Command<CaseInstance> {

	private final String businessKey;
	private final AdminTransition transition;
	private final AdminTransitionRequest request;

	@Override
	public CaseInstance execute(CommandContext commandContext) {
		CaseInstance caseInstance;
		try {
			caseInstance = commandContext.getCaseInstanceRepository().get(businessKey);
		} catch (DatabaseRecordNotFoundException e) {
			throw new CaseInstanceNotFoundException(e.getMessage(), e);
		}

		if (!AdminLifecycleSupport.isAdminLifecycleCase(caseInstance)) {
			throw new AdminLifecycleException("Admin lifecycle transitions are only available for matter-admin opening control cases");
		}

		normalizeTopLevelOwners(caseInstance);
		AdminLifecycleSupport.synchronizeDerivedFields(caseInstance);
		AdminLifecycleAccessSupport.assertCanTransition(caseInstance, transition);

		switch (transition) {
		case SUBMIT_INTAKE_REVIEW -> applyInitialState(caseInstance);
		case MARK_AWAITING_ENGAGEMENT -> markAwaitingEngagement(commandContext, caseInstance);
		case MARK_READY_TO_OPEN -> markReadyToOpen(commandContext, caseInstance);
		case SEND_TO_LAWYER_REVIEW -> sendToOpeningLawyerReview(commandContext, caseInstance);
		case LAWYER_APPROVE_OPEN -> lawyerApproveOpen(commandContext, caseInstance);
		case LAWYER_RETURN_FOR_FIXES -> lawyerReturnForFixes(commandContext, caseInstance);
		case START_CLIENT_WAIT -> startOpeningClientWait(commandContext, caseInstance);
		case RESUME_FROM_CLIENT_WAIT -> resumeOpeningClientWait(commandContext, caseInstance);
		case ACTIVATE_MATTER -> activateMatter(commandContext, caseInstance);
		case UPDATE_MAINTENANCE_CONTROL -> updateMaintenanceControl(commandContext, caseInstance);
		case SEND_TO_MAINTENANCE_LAWYER_REVIEW -> sendToMaintenanceLawyerReview(commandContext, caseInstance);
		case LAWYER_RETURN_TO_ACTIVE -> lawyerReturnToActive(commandContext, caseInstance);
		case START_MAINTENANCE_CLIENT_WAIT -> startMaintenanceClientWait(commandContext, caseInstance);
		case RESUME_FROM_MAINTENANCE_CLIENT_WAIT -> resumeMaintenanceWait(commandContext, caseInstance);
		case START_EXTERNAL_WAIT -> startExternalWait(commandContext, caseInstance, AdminState.ACTIVE);
		case RESUME_FROM_EXTERNAL_WAIT -> resumeMaintenanceWait(commandContext, caseInstance);
		case LAWYER_REQUEST_CLIENT_FOLLOWUP -> lawyerRequestClientFollowup(commandContext, caseInstance);
		case LAWYER_REQUEST_EXTERNAL_FOLLOWUP -> lawyerRequestExternalFollowup(commandContext, caseInstance);
		case START_CLOSING_REVIEW -> startClosingReview(commandContext, caseInstance);
		case CLOSE_MATTER -> closeMatter(commandContext, caseInstance);
		case ARCHIVE_MATTER -> archiveMatter(commandContext, caseInstance);
		default -> throw new AdminLifecycleException("Unsupported admin lifecycle transition");
		}

		AdminLifecycleSupport.applyEvaluation(caseInstance);
		try {
			commandContext.getCaseInstanceRepository().update(businessKey, caseInstance);
		} catch (DatabaseRecordNotFoundException e) {
			throw new CaseInstanceNotFoundException(e.getMessage(), e);
		}
		return caseInstance;
	}

	private void applyInitialState(CaseInstance caseInstance) {
		moveToState(null, caseInstance, AdminState.INTAKE_REVIEW, null);
		applyNextAction(null, caseInstance, NextActionOwnerType.ADMIN,
				buildOwnerRef(caseInstance.getAdminOwnerId(), caseInstance.getAdminOwnerName()),
				"Review intake for opening readiness", null);
	}

	private void markAwaitingEngagement(CommandContext commandContext, CaseInstance caseInstance) {
		requireState(caseInstance, AdminState.INTAKE_REVIEW);
		moveToState(commandContext, caseInstance, AdminState.AWAITING_ENGAGEMENT, request.getQueueId());
		applyNextAction(commandContext, caseInstance,
				NextActionOwnerType.fromValue(defaultIfBlank(request.getNextActionOwnerType(),
						NextActionOwnerType.CLIENT.getCode())).orElse(NextActionOwnerType.CLIENT),
				request.getNextActionOwnerRef(), defaultIfBlank(request.getNextActionSummary(), "Await engagement materials"),
				request.getNextActionDueAt());
	}

	private void markReadyToOpen(CommandContext commandContext, CaseInstance caseInstance) {
		requireState(caseInstance, AdminState.INTAKE_REVIEW, AdminState.AWAITING_ENGAGEMENT);
		requireOpeningReadiness(caseInstance);
		requireAccountsReadinessForOpening(commandContext, caseInstance);
		moveToState(commandContext, caseInstance, AdminState.READY_TO_OPEN, request.getQueueId());
		clearWaiting(caseInstance);
		applyNextAction(commandContext, caseInstance, NextActionOwnerType.ADMIN,
				buildOwnerRef(caseInstance.getAdminOwnerId(), caseInstance.getAdminOwnerName()),
				defaultIfBlank(request.getNextActionSummary(), "Prepare matter for lawyer review"),
				request.getNextActionDueAt());
	}

	private void sendToOpeningLawyerReview(CommandContext commandContext, CaseInstance caseInstance) {
		requireState(caseInstance, AdminState.READY_TO_OPEN);
		applyRequestedControlReferences(caseInstance);
		normalizeTopLevelOwners(caseInstance);
		String previousLawyerId = caseInstance.getResponsibleLawyerId();
		applyResponsibleLawyer(caseInstance);
		if (isBlank(caseInstance.getResponsibleLawyerId())) {
			throw new AdminLifecycleException("responsibleLawyerId is required before sending to lawyer review");
		}
		moveToState(commandContext, caseInstance, AdminState.READY_FOR_LAWYER, request.getQueueId());
		if (!Objects.equals(previousLawyerId, caseInstance.getResponsibleLawyerId())) {
			caseInstance.addAdminEvent(buildEvent(commandContext, caseInstance, AdminEventType.OWNER_ASSIGNED,
					caseInstance.getLastStateChangedAt(), request.getReasonCode(), request.getNote(),
					AdminState.READY_TO_OPEN.getCode(), caseInstance.getAdminState(),
					AdminLifecycleSupport.expectedStageForState(AdminState.READY_TO_OPEN.getCode()), caseInstance.getStage()));
		}
		applyNextAction(commandContext, caseInstance, NextActionOwnerType.LAWYER,
				defaultIfBlank(request.getNextActionOwnerRef(),
						buildOwnerRef(caseInstance.getResponsibleLawyerId(), caseInstance.getResponsibleLawyerName())),
				defaultIfBlank(request.getNextActionSummary(), "Lawyer review required before open"),
				request.getNextActionDueAt());
		caseInstance.addAdminEvent(buildEvent(commandContext, caseInstance, AdminEventType.LAWYER_HANDOFF_SENT,
				caseInstance.getLastStateChangedAt(), request.getReasonCode(), request.getNote(),
				AdminState.READY_TO_OPEN.getCode(), caseInstance.getAdminState(),
				AdminLifecycleSupport.expectedStageForState(AdminState.READY_TO_OPEN.getCode()), caseInstance.getStage()));
	}

	private void lawyerApproveOpen(CommandContext commandContext, CaseInstance caseInstance) {
		requireState(caseInstance, AdminState.READY_FOR_LAWYER);
		moveToState(commandContext, caseInstance, AdminState.OPENED, request.getQueueId());
		caseInstance.setOpenedAt(AdminLifecycleSupport.nowTimestamp());
		clearWaiting(caseInstance);
		applyNextAction(commandContext, caseInstance, NextActionOwnerType.ADMIN,
				buildOwnerRef(caseInstance.getAdminOwnerId(), caseInstance.getAdminOwnerName()),
				defaultIfBlank(request.getNextActionSummary(), "Matter opened and ready for maintenance activation"),
				request.getNextActionDueAt());
		caseInstance.addAdminEvent(buildEvent(commandContext, caseInstance, AdminEventType.CASE_OPENED,
				caseInstance.getOpenedAt(), request.getReasonCode(), request.getNote(),
				AdminState.READY_FOR_LAWYER.getCode(), caseInstance.getAdminState(),
				AdminLifecycleSupport.expectedStageForState(AdminState.READY_FOR_LAWYER.getCode()), caseInstance.getStage()));
	}

	private void lawyerReturnForFixes(CommandContext commandContext, CaseInstance caseInstance) {
		requireState(caseInstance, AdminState.READY_FOR_LAWYER);
		requireAccountsReadinessForOpening(commandContext, caseInstance);
		moveToState(commandContext, caseInstance, AdminState.READY_TO_OPEN, request.getQueueId());
		clearWaiting(caseInstance);
		applyNextAction(commandContext, caseInstance, NextActionOwnerType.ADMIN,
				buildOwnerRef(caseInstance.getAdminOwnerId(), caseInstance.getAdminOwnerName()),
				defaultIfBlank(request.getNextActionSummary(), "Lawyer returned the matter for admin fixes"),
				request.getNextActionDueAt());
		caseInstance.addAdminEvent(buildEvent(commandContext, caseInstance, AdminEventType.LAWYER_HANDOFF_RETURNED,
				caseInstance.getLastStateChangedAt(), request.getReasonCode(), request.getNote(),
				AdminState.READY_FOR_LAWYER.getCode(), caseInstance.getAdminState(),
				AdminLifecycleSupport.expectedStageForState(AdminState.READY_FOR_LAWYER.getCode()), caseInstance.getStage()));
	}

	private void startOpeningClientWait(CommandContext commandContext, CaseInstance caseInstance) {
		requireState(caseInstance, AdminState.READY_TO_OPEN, AdminState.READY_FOR_LAWYER);
		AdminState resumeState = requiredState(caseInstance.getAdminState());
		startWait(commandContext, caseInstance, AdminState.WAITING_ON_CLIENT, resumeState, NextActionOwnerType.CLIENT,
				defaultIfBlank(request.getNextActionSummary(), "Await client response"), request.getQueueId());
	}

	private void resumeOpeningClientWait(CommandContext commandContext, CaseInstance caseInstance) {
		requireState(caseInstance, AdminState.WAITING_ON_CLIENT);
		String targetState = required(caseInstance.getResumeToState(), "resumeToState is required to resume from waiting");
		AdminState resumeState = requiredState(targetState);
		moveToState(commandContext, caseInstance, resumeState, request.getQueueId());
		clearWaiting(caseInstance);
		if (AdminState.READY_FOR_LAWYER == resumeState) {
			applyResponsibleLawyer(caseInstance);
			applyNextAction(commandContext, caseInstance, NextActionOwnerType.LAWYER,
					buildOwnerRef(caseInstance.getResponsibleLawyerId(), caseInstance.getResponsibleLawyerName()),
					defaultIfBlank(request.getNextActionSummary(), "Client responded and lawyer review is needed"),
					request.getNextActionDueAt());
		} else {
			applyNextAction(commandContext, caseInstance, NextActionOwnerType.ADMIN,
					buildOwnerRef(caseInstance.getAdminOwnerId(), caseInstance.getAdminOwnerName()),
					defaultIfBlank(request.getNextActionSummary(), "Client responded and opening work can resume"),
					request.getNextActionDueAt());
		}
		caseInstance.addAdminEvent(buildEvent(commandContext, caseInstance, AdminEventType.WAIT_RESUMED,
				caseInstance.getLastStateChangedAt(), request.getReasonCode(), request.getNote(),
				AdminState.WAITING_ON_CLIENT.getCode(), caseInstance.getAdminState(),
				AdminLifecycleSupport.expectedStageForState(AdminState.WAITING_ON_CLIENT.getCode()), caseInstance.getStage()));
	}

	private void activateMatter(CommandContext commandContext, CaseInstance caseInstance) {
		requireState(caseInstance, AdminState.OPENED);
		moveToState(commandContext, caseInstance, AdminState.ACTIVE, request.getQueueId());
		clearWaiting(caseInstance);
		applyNextAction(commandContext, caseInstance, NextActionOwnerType.ADMIN,
				buildOwnerRef(caseInstance.getAdminOwnerId(), caseInstance.getAdminOwnerName()),
				defaultIfBlank(request.getNextActionSummary(), "Active matter control in progress"),
				request.getNextActionDueAt());
		caseInstance.addAdminEvent(buildEvent(commandContext, caseInstance, AdminEventType.CASE_ACTIVATED,
				caseInstance.getLastStateChangedAt(), request.getReasonCode(), request.getNote(),
				AdminState.OPENED.getCode(), caseInstance.getAdminState(),
				AdminLifecycleSupport.expectedStageForState(AdminState.OPENED.getCode()), caseInstance.getStage()));
	}

	private void updateMaintenanceControl(CommandContext commandContext, CaseInstance caseInstance) {
		requireState(caseInstance, AdminState.OPENED, AdminState.ACTIVE, AdminState.MAINTENANCE_LAWYER_REVIEW,
				AdminState.MAINTENANCE_CLIENT_WAIT, AdminState.WAITING_ON_EXTERNAL);
		String previousAdminOwnerId = caseInstance.getAdminOwnerId();
		String previousAdminOwnerName = caseInstance.getAdminOwnerName();
		String previousLawyerId = caseInstance.getResponsibleLawyerId();
		String previousLawyerName = caseInstance.getResponsibleLawyerName();
		String previousNextActionOwnerType = caseInstance.getNextActionOwnerType();
		String previousNextActionOwnerRef = caseInstance.getNextActionOwnerRef();
		String previousNextActionSummary = caseInstance.getNextActionSummary();
		String previousNextActionDueAt = caseInstance.getNextActionDueAt();
		String previousExternalPartyRef = caseInstance.getExternalPartyRef();

		applyRequestedControlReferences(caseInstance);
		normalizeTopLevelOwners(caseInstance);

		boolean ownerChanged = !Objects.equals(previousAdminOwnerId, caseInstance.getAdminOwnerId())
				|| !Objects.equals(previousAdminOwnerName, caseInstance.getAdminOwnerName())
				|| !Objects.equals(previousLawyerId, caseInstance.getResponsibleLawyerId())
				|| !Objects.equals(previousLawyerName, caseInstance.getResponsibleLawyerName());
		if (ownerChanged) {
			caseInstance.addAdminEvent(buildEvent(commandContext, caseInstance, AdminEventType.OWNER_ASSIGNED,
					AdminLifecycleSupport.nowTimestamp(), request.getReasonCode(), request.getNote(),
					caseInstance.getAdminState(), caseInstance.getAdminState(), caseInstance.getStage(), caseInstance.getStage()));
		}

		boolean nextActionRequested = request.getNextActionOwnerType() != null || request.getNextActionOwnerRef() != null
				|| request.getNextActionSummary() != null || request.getNextActionDueAt() != null;
		if (nextActionRequested) {
			String requestedNextActionOwnerType = defaultIfBlank(request.getNextActionOwnerType(), caseInstance.getNextActionOwnerType());
			String requestedNextActionOwnerRef = defaultIfBlank(request.getNextActionOwnerRef(), caseInstance.getNextActionOwnerRef());
			String requestedNextActionSummary = defaultIfBlank(request.getNextActionSummary(), caseInstance.getNextActionSummary());
			String requestedNextActionDueAt = defaultIfBlank(request.getNextActionDueAt(), caseInstance.getNextActionDueAt());
			boolean nextActionChanged = !Objects.equals(previousNextActionOwnerType, requestedNextActionOwnerType)
					|| !Objects.equals(previousNextActionOwnerRef, requestedNextActionOwnerRef)
					|| !Objects.equals(previousNextActionSummary, requestedNextActionSummary)
					|| !Objects.equals(previousNextActionDueAt, requestedNextActionDueAt);
			if (nextActionChanged) {
				NextActionOwnerType nextActionOwnerType = resolveNextActionOwnerType(requestedNextActionOwnerType,
						NextActionOwnerType.ADMIN);
				applyNextAction(commandContext, caseInstance, nextActionOwnerType, requestedNextActionOwnerRef,
						requestedNextActionSummary, requestedNextActionDueAt);
			}
		}

		boolean externalPartyChanged = !Objects.equals(previousExternalPartyRef, caseInstance.getExternalPartyRef());
		if (!ownerChanged && !nextActionRequested && externalPartyChanged) {
			caseInstance.addAdminEvent(buildEvent(commandContext, caseInstance, AdminEventType.OVERRIDE_APPLIED,
					AdminLifecycleSupport.nowTimestamp(), request.getReasonCode(), request.getNote(),
					caseInstance.getAdminState(), caseInstance.getAdminState(), caseInstance.getStage(), caseInstance.getStage()));
		}
	}

	private void sendToMaintenanceLawyerReview(CommandContext commandContext, CaseInstance caseInstance) {
		requireState(caseInstance, AdminState.ACTIVE);
		applyRequestedControlReferences(caseInstance);
		normalizeTopLevelOwners(caseInstance);
		applyResponsibleLawyer(caseInstance);
		if (isBlank(caseInstance.getResponsibleLawyerId())) {
			throw new AdminLifecycleException("responsibleLawyerId is required before sending to maintenance lawyer review");
		}
		moveToState(commandContext, caseInstance, AdminState.MAINTENANCE_LAWYER_REVIEW, request.getQueueId());
		applyNextAction(commandContext, caseInstance, NextActionOwnerType.LAWYER,
				defaultIfBlank(request.getNextActionOwnerRef(),
						buildOwnerRef(caseInstance.getResponsibleLawyerId(), caseInstance.getResponsibleLawyerName())),
				defaultIfBlank(request.getNextActionSummary(), "Lawyer review required for active matter"),
				request.getNextActionDueAt());
		caseInstance.addAdminEvent(buildEvent(commandContext, caseInstance, AdminEventType.LAWYER_HANDOFF_SENT,
				caseInstance.getLastStateChangedAt(), request.getReasonCode(), request.getNote(),
				AdminState.ACTIVE.getCode(), caseInstance.getAdminState(),
				AdminLifecycleSupport.expectedStageForState(AdminState.ACTIVE.getCode()), caseInstance.getStage()));
	}

	private void lawyerReturnToActive(CommandContext commandContext, CaseInstance caseInstance) {
		requireState(caseInstance, AdminState.MAINTENANCE_LAWYER_REVIEW);
		moveToState(commandContext, caseInstance, AdminState.ACTIVE, request.getQueueId());
		clearWaiting(caseInstance);
		applyNextAction(commandContext, caseInstance, NextActionOwnerType.ADMIN,
				buildOwnerRef(caseInstance.getAdminOwnerId(), caseInstance.getAdminOwnerName()),
				defaultIfBlank(request.getNextActionSummary(), "Lawyer review complete and matter is active"),
				request.getNextActionDueAt());
		caseInstance.addAdminEvent(buildEvent(commandContext, caseInstance, AdminEventType.LAWYER_HANDOFF_RETURNED,
				caseInstance.getLastStateChangedAt(), request.getReasonCode(), request.getNote(),
				AdminState.MAINTENANCE_LAWYER_REVIEW.getCode(), caseInstance.getAdminState(),
				AdminLifecycleSupport.expectedStageForState(AdminState.MAINTENANCE_LAWYER_REVIEW.getCode()),
				caseInstance.getStage()));
	}

	private void startMaintenanceClientWait(CommandContext commandContext, CaseInstance caseInstance) {
		requireState(caseInstance, AdminState.ACTIVE);
		startWait(commandContext, caseInstance, AdminState.MAINTENANCE_CLIENT_WAIT, AdminState.ACTIVE,
				NextActionOwnerType.CLIENT, defaultIfBlank(request.getNextActionSummary(), "Await client response"),
				request.getQueueId());
	}

	private void lawyerRequestClientFollowup(CommandContext commandContext, CaseInstance caseInstance) {
		requireState(caseInstance, AdminState.MAINTENANCE_LAWYER_REVIEW);
		startWait(commandContext, caseInstance, AdminState.MAINTENANCE_CLIENT_WAIT,
				AdminState.MAINTENANCE_LAWYER_REVIEW, NextActionOwnerType.CLIENT,
				defaultIfBlank(request.getNextActionSummary(), "Lawyer requested client follow-up"), request.getQueueId());
	}

	private void lawyerRequestExternalFollowup(CommandContext commandContext, CaseInstance caseInstance) {
		requireState(caseInstance, AdminState.MAINTENANCE_LAWYER_REVIEW);
		startExternalWait(commandContext, caseInstance, AdminState.MAINTENANCE_LAWYER_REVIEW);
	}

	private void startClosingReview(CommandContext commandContext, CaseInstance caseInstance) {
		requireState(caseInstance, AdminState.ACTIVE);
		moveToState(commandContext, caseInstance, AdminState.CLOSING_REVIEW, request.getQueueId());
		clearWaiting(caseInstance);
		applyNextAction(commandContext, caseInstance, NextActionOwnerType.ADMIN,
				buildOwnerRef(caseInstance.getAdminOwnerId(), caseInstance.getAdminOwnerName()),
				defaultIfBlank(request.getNextActionSummary(), "Complete final closing review"), request.getNextActionDueAt());
		caseInstance.addAdminEvent(buildEvent(commandContext, caseInstance, AdminEventType.CLOSING_REVIEW_STARTED,
				caseInstance.getLastStateChangedAt(), request.getReasonCode(), request.getNote(),
				AdminState.ACTIVE.getCode(), caseInstance.getAdminState(),
				AdminLifecycleSupport.expectedStageForState(AdminState.ACTIVE.getCode()), caseInstance.getStage()));
	}

	private void closeMatter(CommandContext commandContext, CaseInstance caseInstance) {
		requireState(caseInstance, AdminState.CLOSING_REVIEW);
		moveToState(commandContext, caseInstance, AdminState.CLOSED, request.getQueueId());
		clearWaiting(caseInstance);
		clearNextAction(caseInstance);
		caseInstance.addAdminEvent(buildEvent(commandContext, caseInstance, AdminEventType.CASE_CLOSED,
				caseInstance.getLastStateChangedAt(), request.getReasonCode(), request.getNote(),
				AdminState.CLOSING_REVIEW.getCode(), caseInstance.getAdminState(),
				AdminLifecycleSupport.expectedStageForState(AdminState.CLOSING_REVIEW.getCode()), caseInstance.getStage()));
	}

	private void archiveMatter(CommandContext commandContext, CaseInstance caseInstance) {
		requireState(caseInstance, AdminState.CLOSED);
		moveToState(commandContext, caseInstance, AdminState.ARCHIVED, request.getQueueId());
		clearWaiting(caseInstance);
		clearNextAction(caseInstance);
		caseInstance.addAdminEvent(buildEvent(commandContext, caseInstance, AdminEventType.CASE_ARCHIVED,
				caseInstance.getLastStateChangedAt(), request.getReasonCode(), request.getNote(),
				AdminState.CLOSED.getCode(), caseInstance.getAdminState(),
				AdminLifecycleSupport.expectedStageForState(AdminState.CLOSED.getCode()), caseInstance.getStage()));
	}

	private void startWait(CommandContext commandContext, CaseInstance caseInstance, AdminState waitState,
			AdminState resumeState, NextActionOwnerType nextActionOwnerType, String defaultNextActionSummary, String queueIdOverride) {
		moveToState(commandContext, caseInstance, waitState, queueIdOverride);
		caseInstance.setWaitingReasonCode(required(request.getWaitingReasonCode(), "waitingReasonCode is required"));
		caseInstance.setWaitingReasonText(defaultIfBlank(request.getWaitingReasonText(), request.getWaitingReasonCode()));
		caseInstance.setWaitingSince(defaultIfBlank(request.getWaitingSince(), AdminLifecycleSupport.nowTimestamp()));
		caseInstance.setExpectedResponseAt(required(request.getExpectedResponseAt(), "expectedResponseAt is required"));
		caseInstance.setResumeToState(resumeState.getCode());
		applyNextAction(commandContext, caseInstance, nextActionOwnerType,
				defaultIfBlank(request.getNextActionOwnerRef(), caseInstance.getNextActionOwnerRef()),
				defaultIfBlank(request.getNextActionSummary(), defaultNextActionSummary),
				request.getExpectedResponseAt());
		caseInstance.addAdminEvent(buildEvent(commandContext, caseInstance, AdminEventType.WAIT_STARTED,
				caseInstance.getLastStateChangedAt(), request.getReasonCode(), request.getNote(), resumeState.getCode(),
				caseInstance.getAdminState(), AdminLifecycleSupport.expectedStageForState(resumeState.getCode()),
				caseInstance.getStage()));
	}

	private void startExternalWait(CommandContext commandContext, CaseInstance caseInstance, AdminState resumeState) {
		moveToState(commandContext, caseInstance, AdminState.WAITING_ON_EXTERNAL, request.getQueueId());
		caseInstance.setWaitingReasonCode(required(request.getWaitingReasonCode(), "waitingReasonCode is required"));
		caseInstance.setWaitingReasonText(defaultIfBlank(request.getWaitingReasonText(), request.getWaitingReasonCode()));
		caseInstance.setWaitingSince(defaultIfBlank(request.getWaitingSince(), AdminLifecycleSupport.nowTimestamp()));
		caseInstance.setExpectedResponseAt(required(request.getExpectedResponseAt(), "expectedResponseAt is required"));
		caseInstance.setResumeToState(resumeState.getCode());
		caseInstance.setExternalPartyRef(required(defaultIfBlank(request.getExternalPartyRef(), caseInstance.getExternalPartyRef()),
				"externalPartyRef is required"));
		applyNextAction(commandContext, caseInstance, NextActionOwnerType.EXTERNAL,
				defaultIfBlank(request.getNextActionOwnerRef(), caseInstance.getExternalPartyRef()),
				defaultIfBlank(request.getNextActionSummary(), "Await external response"), request.getExpectedResponseAt());
		caseInstance.addAdminEvent(buildEvent(commandContext, caseInstance, AdminEventType.WAIT_STARTED,
				caseInstance.getLastStateChangedAt(), request.getReasonCode(), request.getNote(), resumeState.getCode(),
				caseInstance.getAdminState(), AdminLifecycleSupport.expectedStageForState(resumeState.getCode()),
				caseInstance.getStage()));
	}

	private void resumeMaintenanceWait(CommandContext commandContext, CaseInstance caseInstance) {
		requireState(caseInstance, AdminState.MAINTENANCE_CLIENT_WAIT, AdminState.WAITING_ON_EXTERNAL);
		String targetState = required(caseInstance.getResumeToState(), "resumeToState is required to resume from waiting");
		AdminState resumeState = requiredState(targetState);
		if (!AdminLifecycleSupport.isValidMaintenanceResumeTarget(resumeState.getCode())) {
			throw new AdminLifecycleException("resumeToState " + targetState + " is not a valid maintenance wait target");
		}
		String waitingState = caseInstance.getAdminState();
		moveToState(commandContext, caseInstance, resumeState, request.getQueueId());
		clearWaiting(caseInstance);
		if (AdminState.MAINTENANCE_LAWYER_REVIEW == resumeState) {
			applyResponsibleLawyer(caseInstance);
			applyNextAction(commandContext, caseInstance, NextActionOwnerType.LAWYER,
					buildOwnerRef(caseInstance.getResponsibleLawyerId(), caseInstance.getResponsibleLawyerName()),
					defaultIfBlank(request.getNextActionSummary(), "Follow-up received and lawyer review is needed"),
					request.getNextActionDueAt());
		} else {
			applyNextAction(commandContext, caseInstance, NextActionOwnerType.ADMIN,
					buildOwnerRef(caseInstance.getAdminOwnerId(), caseInstance.getAdminOwnerName()),
					defaultIfBlank(request.getNextActionSummary(), "Follow-up received and active work can resume"),
					request.getNextActionDueAt());
		}
		caseInstance.addAdminEvent(buildEvent(commandContext, caseInstance, AdminEventType.WAIT_RESUMED,
				caseInstance.getLastStateChangedAt(), request.getReasonCode(), request.getNote(), waitingState,
				caseInstance.getAdminState(), AdminLifecycleSupport.expectedStageForState(waitingState), caseInstance.getStage()));
	}

	private void normalizeTopLevelOwners(CaseInstance caseInstance) {
		if (isBlank(caseInstance.getAdminOwnerId()) && caseInstance.getOwner() != null) {
			caseInstance.setAdminOwnerId(caseInstance.getOwner().getId());
		}
		if (isBlank(caseInstance.getAdminOwnerName()) && caseInstance.getOwner() != null) {
			caseInstance.setAdminOwnerName(caseInstance.getOwner().getName());
		}
		if (isBlank(caseInstance.getResponsibleLawyerId())) {
			caseInstance.setResponsibleLawyerId(
					AdminLifecycleSupport.getAttributeValue(caseInstance, "responsibleLawyerId").orElse(null));
		}
		if (isBlank(caseInstance.getResponsibleLawyerName())) {
			caseInstance.setResponsibleLawyerName(
					AdminLifecycleSupport.getAttributeValue(caseInstance, "responsibleLawyerName").orElse(null));
		}
	}

	private void applyRequestedControlReferences(CaseInstance caseInstance) {
		if (request.getAdminOwnerId() != null) {
			caseInstance.setAdminOwnerId(normalizeBlank(request.getAdminOwnerId()));
		}
		if (request.getAdminOwnerName() != null) {
			caseInstance.setAdminOwnerName(normalizeBlank(request.getAdminOwnerName()));
		}
		if (request.getResponsibleLawyerId() != null) {
			caseInstance.setResponsibleLawyerId(normalizeBlank(request.getResponsibleLawyerId()));
		}
		if (request.getResponsibleLawyerName() != null) {
			caseInstance.setResponsibleLawyerName(normalizeBlank(request.getResponsibleLawyerName()));
		}
		if (request.getExternalPartyRef() != null) {
			caseInstance.setExternalPartyRef(normalizeBlank(request.getExternalPartyRef()));
		}
	}

	private void applyResponsibleLawyer(CaseInstance caseInstance) {
		if (isBlank(caseInstance.getResponsibleLawyerId())) {
			caseInstance.setResponsibleLawyerId(
					AdminLifecycleSupport.getAttributeValue(caseInstance, "responsibleLawyerId").orElse(null));
		}
		if (isBlank(caseInstance.getResponsibleLawyerName())) {
			caseInstance.setResponsibleLawyerName(
					AdminLifecycleSupport.getAttributeValue(caseInstance, "responsibleLawyerName").orElse(null));
		}
	}

	private void requireOpeningReadiness(CaseInstance caseInstance) {
		if (!AdminLifecycleSupport.isTruthyAttribute(caseInstance, "engagementReceived")
				|| !AdminLifecycleSupport.isTruthyAttribute(caseInstance, "conflictsCleared")) {
			throw new AdminLifecycleException("engagementReceived and conflictsCleared must both be complete before moving to Ready to Open");
		}
	}

	private void requireAccountsReadinessForOpening(CommandContext commandContext, CaseInstance caseInstance) {
		AccountsReadinessEvaluation readiness = AccountsReadinessSupport.applyEvaluation(caseInstance);
		if (!readiness.isReady()) {
			try {
				commandContext.getCaseInstanceRepository().update(businessKey, caseInstance);
			} catch (DatabaseRecordNotFoundException e) {
				throw new CaseInstanceNotFoundException(e.getMessage(), e);
			}
			throw new AdminLifecycleException("Accounts readiness must be READY before moving to Ready to Open",
					readiness.getAccountsReadinessReasonCodes());
		}
	}

	private void requireState(CaseInstance caseInstance, AdminState... allowedStates) {
		for (AdminState allowedState : allowedStates) {
			if (allowedState.getCode().equals(caseInstance.getAdminState())) {
				return;
			}
		}
		throw new AdminLifecycleException("Transition " + transition.getCode() + " is not allowed from " + caseInstance.getAdminState());
	}

	private void moveToState(CommandContext commandContext, CaseInstance caseInstance, AdminState targetState, String queueIdOverride) {
		String previousState = caseInstance.getAdminState();
		String previousStage = caseInstance.getStage();
		String timestamp = AdminLifecycleSupport.nowTimestamp();

		caseInstance.setAdminState(targetState.getCode());
		caseInstance.setStage(AdminLifecycleSupport.expectedStageForState(targetState.getCode()));
		caseInstance.setStatus(AdminLifecycleSupport.expectedStatusForState(targetState.getCode()));
		caseInstance.setQueueId(defaultIfBlank(queueIdOverride, AdminLifecycleSupport.defaultQueueForState(targetState.getCode())));
		caseInstance.setLastStateChangedAt(timestamp);

		if (!Objects.equals(previousState, caseInstance.getAdminState())) {
			caseInstance.addAdminEvent(buildEvent(commandContext, caseInstance, AdminEventType.STATE_CHANGED, timestamp, request.getReasonCode(),
					request.getNote(), previousState, caseInstance.getAdminState(), previousStage, caseInstance.getStage()));
		}
		if (!Objects.equals(previousStage, caseInstance.getStage())) {
			caseInstance.addAdminEvent(buildEvent(commandContext, caseInstance, AdminEventType.STAGE_CHANGED, timestamp, request.getReasonCode(),
					request.getNote(), previousState, caseInstance.getAdminState(), previousStage, caseInstance.getStage()));
		}
	}

	private void applyNextAction(CommandContext commandContext, CaseInstance caseInstance, NextActionOwnerType nextActionOwnerType,
			String nextActionOwnerRef, String nextActionSummary, String nextActionDueAt) {
		caseInstance.setNextActionOwnerType(nextActionOwnerType.getCode());
		caseInstance.setNextActionOwnerRef(nextActionOwnerRef);
		caseInstance.setNextActionSummary(nextActionSummary);
		caseInstance.setNextActionDueAt(nextActionDueAt);
		caseInstance.addAdminEvent(buildEvent(commandContext, caseInstance, AdminEventType.NEXT_ACTION_SET,
				caseInstance.getLastStateChangedAt(), request.getReasonCode(), request.getNote(), caseInstance.getAdminState(),
				caseInstance.getAdminState(), caseInstance.getStage(), caseInstance.getStage()));
	}

	private void clearWaiting(CaseInstance caseInstance) {
		caseInstance.setWaitingReasonCode(null);
		caseInstance.setWaitingReasonText(null);
		caseInstance.setWaitingSince(null);
		caseInstance.setExpectedResponseAt(null);
		caseInstance.setResumeToState(null);
	}

	private void clearNextAction(CaseInstance caseInstance) {
		caseInstance.setNextActionOwnerType(null);
		caseInstance.setNextActionOwnerRef(null);
		caseInstance.setNextActionSummary(null);
		caseInstance.setNextActionDueAt(null);
	}

	private String buildOwnerRef(String ownerId, String ownerName) {
		if (!isBlank(ownerName) && !isBlank(ownerId)) {
			return ownerName + " (" + ownerId + ")";
		}
		if (!isBlank(ownerName)) {
			return ownerName;
		}
		return ownerId;
	}

	private NextActionOwnerType resolveNextActionOwnerType(String requestedValue, NextActionOwnerType defaultValue) {
		if (isBlank(requestedValue)) {
			return defaultValue;
		}
		return NextActionOwnerType.fromValue(requestedValue)
				.orElseThrow(() -> new AdminLifecycleException("Unsupported nextActionOwnerType " + requestedValue));
	}

	private AdminState requiredState(String stateValue) {
		return AdminState.fromValue(stateValue)
				.orElseThrow(() -> new AdminLifecycleException("Unsupported resumeToState " + stateValue));
	}

	private AdminEvent buildEvent(CommandContext commandContext, CaseInstance caseInstance, AdminEventType eventType, String timestamp,
			String reasonCode, String note, String fromState, String toState, String fromStage, String toStage) {
		return AdminEvent.builder().eventType(eventType.getCode()).occurredAt(AdminLifecycleSupport.nowDate())
				.actorType(defaultIfBlank(request.getActorType(), "User"))
				.actorId(commandContext != null
						? commandContext.getSecurityContextTenantHolder().getUserId().orElse("system")
						: "system")
				.actorName(request.getActorName()).fromState(fromState).toState(toState).fromStage(fromStage).toStage(toStage)
				.reasonCode(reasonCode).note(note).build();
	}

	private String required(String value, String message) {
		if (isBlank(value)) {
			throw new AdminLifecycleException(message);
		}
		return value;
	}

	private String defaultIfBlank(String value, String defaultValue) {
		return isBlank(value) ? defaultValue : value;
	}

	private String normalizeBlank(String value) {
		return isBlank(value) ? null : value;
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
