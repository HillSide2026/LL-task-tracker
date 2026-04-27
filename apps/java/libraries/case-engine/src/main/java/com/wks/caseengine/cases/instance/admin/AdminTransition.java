package com.wks.caseengine.cases.instance.admin;

import java.io.Serializable;
import java.util.Optional;

public enum AdminTransition implements Serializable {

	SUBMIT_INTAKE_REVIEW("submitIntakeReview"),
	MARK_AWAITING_ENGAGEMENT("markAwaitingEngagement"),
	MARK_READY_TO_OPEN("markReadyToOpen"),
	SEND_TO_LAWYER_REVIEW("sendToLawyerReview"),
	LAWYER_APPROVE_OPEN("lawyerApproveOpen"),
	LAWYER_RETURN_FOR_FIXES("lawyerReturnForFixes"),
	START_CLIENT_WAIT("startClientWait"),
	RESUME_FROM_CLIENT_WAIT("resumeFromClientWait"),
	ACTIVATE_MATTER("activateMatter"),
	UPDATE_MAINTENANCE_CONTROL("updateMaintenanceControl"),
	SEND_TO_MAINTENANCE_LAWYER_REVIEW("sendToMaintenanceLawyerReview"),
	LAWYER_RETURN_TO_ACTIVE("lawyerReturnToActive"),
	START_MAINTENANCE_CLIENT_WAIT("startMaintenanceClientWait"),
	RESUME_FROM_MAINTENANCE_CLIENT_WAIT("resumeFromMaintenanceClientWait"),
	START_EXTERNAL_WAIT("startExternalWait"),
	RESUME_FROM_EXTERNAL_WAIT("resumeFromExternalWait"),
	LAWYER_REQUEST_CLIENT_FOLLOWUP("lawyerRequestClientFollowup"),
	LAWYER_REQUEST_EXTERNAL_FOLLOWUP("lawyerRequestExternalFollowup"),
	START_CLOSING_REVIEW("startClosingReview"),
	CLOSE_MATTER("closeMatter"),
	ARCHIVE_MATTER("archiveMatter");

	private final String code;

	AdminTransition(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static Optional<AdminTransition> fromValue(String value) {
		for (AdminTransition transition : values()) {
			if (transition.code.equals(value)) {
				return Optional.of(transition);
			}
		}
		return Optional.empty();
	}
}
