package com.wks.caseengine.jpa.entity;
import java.util.List;
import java.util.UUID;

import com.wks.caseengine.cases.instance.CaseAttribute;
import com.wks.caseengine.cases.instance.CaseComment;
import com.wks.caseengine.cases.instance.CaseDocument;
import com.wks.caseengine.cases.instance.CaseOwner;
import com.wks.caseengine.cases.instance.accounts.AccountsEvent;
import com.wks.caseengine.cases.instance.admin.AdminEvent;
import com.wks.caseengine.jpa.entity.converter.AccountsEventListConverter;
import com.wks.caseengine.jpa.entity.converter.AdminEventListConverter;
import com.wks.caseengine.jpa.entity.converter.CaseCommentListConverter;
import com.wks.caseengine.jpa.entity.converter.CaseDefAttributeConverter;
import com.wks.caseengine.jpa.entity.converter.CaseDocumentListConverter;
import com.wks.caseengine.jpa.entity.converter.CaseOwnerConverter;
import com.wks.caseengine.jpa.entity.converter.StringListConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "case_instance")
@Getter
@Setter
public class CaseInstanceEntity {
	
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uid;

    @Column(name = "business_key", unique = true, nullable = false)
    private String businessKey;

    @Column(name = "status")
    private String status;

    @Column(name = "stage")
    private String stage;
    
	@Column(name = "queue_id")
    private String queueId;

	@Column(name = "admin_state")
	private String adminState;

	@Column(name = "admin_health")
	private String adminHealth;

	@Column(name = "health_reason_codes", columnDefinition = "TEXT")
	@Convert(converter = StringListConverter.class)
	private List<String> healthReasonCodes;

	@Column(name = "health_evaluated_at")
	private String healthEvaluatedAt;

	@Column(name = "stale_since")
	private String staleSince;

	@Column(name = "malformed_case")
	private Boolean malformedCase;

	@Column(name = "admin_owner_id")
	private String adminOwnerId;

	@Column(name = "admin_owner_name")
	private String adminOwnerName;

	@Column(name = "responsible_lawyer_id")
	private String responsibleLawyerId;

	@Column(name = "responsible_lawyer_name")
	private String responsibleLawyerName;

	@Column(name = "next_action_owner_type")
	private String nextActionOwnerType;

	@Column(name = "next_action_owner_ref")
	private String nextActionOwnerRef;

	@Column(name = "next_action_summary")
	private String nextActionSummary;

	@Column(name = "next_action_due_at")
	private String nextActionDueAt;

	@Column(name = "waiting_reason_code")
	private String waitingReasonCode;

	@Column(name = "waiting_reason_text", columnDefinition = "TEXT")
	private String waitingReasonText;

	@Column(name = "waiting_since")
	private String waitingSince;

	@Column(name = "expected_response_at")
	private String expectedResponseAt;

	@Column(name = "external_party_ref")
	private String externalPartyRef;

	@Column(name = "resume_to_state")
	private String resumeToState;

	@Column(name = "last_state_changed_at")
	private String lastStateChangedAt;

	@Column(name = "opened_at")
	private String openedAt;

	@Column(name = "matter_type")
	private String matterType;

	@Column(name = "billing_party_model")
	private String billingPartyModel;

	@Column(name = "billing_mode")
	private String billingMode;

	@Column(name = "accounts_profile")
	private String accountsProfile;

	@Column(name = "billing_setup_complete")
	private Boolean billingSetupComplete;

	@Column(name = "flat_fee_amount")
	private String flatFeeAmount;

	@Column(name = "payment_method_authorized")
	private Boolean paymentMethodAuthorized;

	@Column(name = "payment_method_ref")
	private String paymentMethodRef;

	@Column(name = "retainer_amount")
	private String retainerAmount;

	@Column(name = "retainer_funds_received")
	private Boolean retainerFundsReceived;

	@Column(name = "subscription_plan_id")
	private String subscriptionPlanId;

	@Column(name = "subscription_plan_name")
	private String subscriptionPlanName;

	@Column(name = "subscription_active")
	private Boolean subscriptionActive;

	@Column(name = "instructing_firm_id")
	private String instructingFirmId;

	@Column(name = "instructing_firm_name")
	private String instructingFirmName;

	@Column(name = "counsel_billing_mode")
	private String counselBillingMode;

	@Column(name = "counsel_billing_party_override")
	private Boolean counselBillingPartyOverride;

	@Column(name = "accounts_stage")
	private String accountsStage;

	@Column(name = "accounts_state")
	private String accountsState;

	@Column(name = "accounts_health")
	private String accountsHealth;

	@Column(name = "accounts_health_reason_codes", columnDefinition = "TEXT")
	@Convert(converter = StringListConverter.class)
	private List<String> accountsHealthReasonCodes;

	@Column(name = "accounts_health_evaluated_at")
	private String accountsHealthEvaluatedAt;

	@Column(name = "accounts_stale_since")
	private String accountsStaleSince;

	@Column(name = "accounts_malformed_case")
	private Boolean accountsMalformedCase;

	@Column(name = "accounts_readiness_status")
	private String accountsReadinessStatus;

	@Column(name = "accounts_readiness_reason_codes", columnDefinition = "TEXT")
	@Convert(converter = StringListConverter.class)
	private List<String> accountsReadinessReasonCodes;

	@Column(name = "accounts_readiness_evaluated_at")
	private String accountsReadinessEvaluatedAt;

	@Column(name = "accounts_readiness_summary", columnDefinition = "TEXT")
	private String accountsReadinessSummary;

	@Column(name = "accounts_queue_id")
	private String accountsQueueId;

	@Column(name = "accounts_next_action_owner_type")
	private String accountsNextActionOwnerType;

	@Column(name = "accounts_next_action_summary", columnDefinition = "TEXT")
	private String accountsNextActionSummary;

	@Column(name = "accounts_next_action_due_at")
	private String accountsNextActionDueAt;

	@Column(name = "accounts_work_blocked")
	private Boolean accountsWorkBlocked;

	@Column(name = "accounts_work_priority")
	private String accountsWorkPriority;

	@Column(name = "accounts_events", columnDefinition = "TEXT")
	@Convert(converter = AccountsEventListConverter.class)
	private List<AccountsEvent> accountsEvents;

	@Column(name = "admin_events", columnDefinition = "TEXT")
	@Convert(converter = AdminEventListConverter.class)
	private List<AdminEvent> adminEvents;

    @Column(name = "attributes", columnDefinition = "TEXT")
    @Convert(converter = CaseDefAttributeConverter.class)
    private List<CaseAttribute> attributes;
    
    @Column(name = "documents", columnDefinition = "TEXT")
    @Convert(converter = CaseDocumentListConverter.class)
    private List<CaseDocument> documents;

	@Column(name="comments", columnDefinition = "TEXT")
	@Convert(converter = CaseCommentListConverter.class)
    private List<CaseComment> comments;
	
	@Column(name = "case_definition_id")
	private String caseDefinitionId;

	@Column(name  = "owner", columnDefinition = "TEXT")
    @Convert(converter = CaseOwnerConverter.class)
	private CaseOwner owner;
    
}
