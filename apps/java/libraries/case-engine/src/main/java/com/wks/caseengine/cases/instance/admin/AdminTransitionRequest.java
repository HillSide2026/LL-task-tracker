package com.wks.caseengine.cases.instance.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTransitionRequest {

	private String actorType;

	private String actorName;

	private String note;

	private String reasonCode;

	private String adminOwnerId;

	private String adminOwnerName;

	private String responsibleLawyerId;

	private String responsibleLawyerName;

	private String nextActionOwnerType;

	private String nextActionOwnerRef;

	private String nextActionSummary;

	private String nextActionDueAt;

	private String waitingReasonCode;

	private String waitingReasonText;

	private String waitingSince;

	private String expectedResponseAt;

	private String externalPartyRef;

	private String queueId;
}
