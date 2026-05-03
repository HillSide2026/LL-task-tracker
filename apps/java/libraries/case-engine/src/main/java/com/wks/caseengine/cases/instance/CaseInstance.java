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
package com.wks.caseengine.cases.instance;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.wks.caseengine.cases.definition.CaseStatus;
import com.wks.caseengine.cases.instance.accounts.AccountsEvent;
import com.wks.caseengine.cases.instance.admin.AdminEvent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Document("caseInstance")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseInstance {

	private String _id;

	private String businessKey;

	private String caseDefinitionId;

	private String stage;

	private CaseOwner owner;

	@Default
	private List<CaseComment> comments = new ArrayList<>();

	private List<CaseDocument> documents;

	private List<CaseAttribute> attributes;

	private String status;

	private String queueId;

	private String adminState;

	private String adminHealth;

	@Default
	private List<String> healthReasonCodes = new ArrayList<>();

	private String healthEvaluatedAt;

	private String staleSince;

	private Boolean malformedCase;

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

	private String resumeToState;

	private String lastStateChangedAt;

	private String openedAt;

	private String matterType;

	private String billingPartyModel;

	private String billingMode;

	private String accountsProfile;

	private Boolean billingSetupComplete;

	private String flatFeeAmount;

	private Boolean paymentMethodAuthorized;

	private String paymentMethodRef;

	private String retainerAmount;

	private Boolean retainerFundsReceived;

	private String subscriptionPlanId;

	private String subscriptionPlanName;

	private Boolean subscriptionActive;

	private String instructingFirmId;

	private String instructingFirmName;

	private String counselBillingMode;

	private Boolean counselBillingPartyOverride;

	private String accountsStage;

	private String accountsState;

	private String accountsHealth;

	@Default
	private List<String> accountsHealthReasonCodes = new ArrayList<>();

	private String accountsHealthEvaluatedAt;

	private String accountsStaleSince;

	private Boolean accountsMalformedCase;

	private String accountsReadinessStatus;

	@Default
	private List<String> accountsReadinessReasonCodes = new ArrayList<>();

	private String accountsReadinessEvaluatedAt;

	private String accountsReadinessSummary;

	private String accountsQueueId;

	private String accountsNextActionOwnerType;

	private String accountsNextActionSummary;

	private String accountsNextActionDueAt;

	private Boolean accountsWorkBlocked;

	private String accountsWorkPriority;

	@Default
	private List<AccountsEvent> accountsEvents = new ArrayList<>();

	@Default
	private List<AdminEvent> adminEvents = new ArrayList<>();

	public CaseInstance(String _id, String businessKey, String caseDefinitionId, String stage, String status) {
		super();
		this._id = _id;
		this.businessKey = businessKey;
		this.caseDefinitionId = caseDefinitionId;
		this.stage = stage;
		this.status = status;
	}

	public String getId() {
		return businessKey;
	}

	public void setStatus(CaseStatus status) {
		this.status = status != null ? status.getCode() : null;
	}

	public void addDocument(final CaseDocument document) {
		if (documents == null) {
			this.documents = new ArrayList<>();
		}

		this.documents.add(document);
	}

	public void addComment(final CaseComment comment) {
		if (comments == null) {
			this.comments = new ArrayList<>();
		}

		this.comments.add(comment);
	}

	public void addAttribute(final CaseAttribute attribute) {
		if (attributes == null) {
			this.attributes = new ArrayList<>();
		}

		this.attributes.add(attribute);
	}

	public CaseStatus getStatus() {
		return CaseStatus.fromValue(status).orElse(null);
	}

	public void addAdminEvent(final AdminEvent adminEvent) {
		if (adminEvents == null) {
			this.adminEvents = new ArrayList<>();
		}

		this.adminEvents.add(adminEvent);
	}

	public void addAccountsEvent(final AccountsEvent accountsEvent) {
		if (accountsEvents == null) {
			this.accountsEvents = new ArrayList<>();
		}

		this.accountsEvents.add(accountsEvent);
	}

}
