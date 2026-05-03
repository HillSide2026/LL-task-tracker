package com.wks.caseengine.cases.instance.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import com.wks.caseengine.cases.definition.CaseStatus;
import com.wks.caseengine.cases.instance.CaseComment;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceFilter;
import com.wks.caseengine.cases.instance.admin.AdminHealth;
import com.wks.caseengine.jpa.entity.CaseInstanceEntity;
import com.wks.caseengine.pagination.Cursor;
import com.wks.caseengine.pagination.PageResult;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;
import com.wks.caseengine.repository.JpaPaginator;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Component
@ConditionalOnProperty(name = "database.type", havingValue = "jpa", matchIfMissing = false)
public class CaseInstanceJpaRepositoryImpl implements CaseInstanceRepository {

	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
    private JpaPaginator paginator;

	@Override
	public List<CaseInstance> find() {
		 TypedQuery<CaseInstanceEntity> query = entityManager.createQuery("SELECT c FROM CaseInstanceEntity c ORDER BY c.id DESC", CaseInstanceEntity.class);
		 query = paginator.apply(query);
		 return query.getResultList().stream()
	                .map(this::toDomain)
	                .collect(Collectors.toList());
	}

	@Override
	public PageResult<CaseInstance> find(CaseInstanceFilter filters) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
	    CriteriaQuery<CaseInstanceEntity> cq = cb.createQuery(CaseInstanceEntity.class);
	    Root<CaseInstanceEntity> root = cq.from(CaseInstanceEntity.class);

	    List<Predicate> predicates = new ArrayList<>();

	    filters.getCaseDefsId().ifPresent(businessKey -> 
	        predicates.add(cb.equal(root.get("caseDefinitionId"), businessKey))
	    );

	    filters.getStatus().ifPresent(status -> 
	        predicates.add(cb.equal(root.get("status"), status.getCode()))
	    );

	    filters.getAdminState().ifPresent(adminState -> predicates.add(cb.equal(root.get("adminState"), adminState)));
	    filters.getAdminHealth().ifPresent(adminHealth -> predicates.add(cb.equal(root.get("adminHealth"), adminHealth)));
	    filters.getNextActionOwnerType()
	            .ifPresent(nextActionOwnerType -> predicates.add(cb.equal(root.get("nextActionOwnerType"), nextActionOwnerType)));
	    filters.getQueueId().ifPresent(queueId -> predicates.add(cb.equal(root.get("queueId"), queueId)));
	    filters.getMalformedCase()
	            .ifPresent(malformedCase -> predicates.add(cb.equal(root.get("malformedCase"), malformedCase)));
	    filters.getExceptionOnly().filter(Boolean::booleanValue).ifPresent(a -> predicates.add(cb.or(
	            root.get("adminHealth").in(AdminHealth.AMBER.getCode(), AdminHealth.RED.getCode()),
	            cb.isTrue(root.get("malformedCase")))));
	    filters.getAdminOwnerId().ifPresent(adminOwnerId -> predicates.add(cb.equal(root.get("adminOwnerId"), adminOwnerId)));
	    filters.getResponsibleLawyerId()
	            .ifPresent(responsibleLawyerId -> predicates.add(cb.equal(root.get("responsibleLawyerId"), responsibleLawyerId)));
	    filters.getHealthReasonCode().ifPresent(healthReasonCode -> predicates
	            .add(cb.like(root.get("healthReasonCodes").as(String.class), "%" + healthReasonCode + "%")));
	    filters.getMatterType().ifPresent(matterType -> predicates.add(cb.equal(root.get("matterType"), matterType)));
	    filters.getAccountsReadinessStatus()
	            .ifPresent(status -> predicates.add(cb.equal(root.get("accountsReadinessStatus"), status)));
	    filters.getAccountsQueueId().ifPresent(queueId -> predicates.add(cb.equal(root.get("accountsQueueId"), queueId)));
	    filters.getAccountsNextActionOwnerType()
	            .ifPresent(ownerType -> predicates.add(cb.equal(root.get("accountsNextActionOwnerType"), ownerType)));
	    filters.getAccountsNextActionDueBefore()
	            .ifPresent(dueBefore -> predicates.add(cb.lessThanOrEqualTo(root.get("accountsNextActionDueAt").as(String.class), dueBefore)));
	    filters.getAccountsWorkBlocked()
	            .ifPresent(blocked -> predicates.add(cb.equal(root.get("accountsWorkBlocked"), blocked)));
	    filters.getAccountsReadinessReasonCode().ifPresent(reasonCode -> predicates
	            .add(cb.like(root.get("accountsReadinessReasonCodes").as(String.class), "%" + reasonCode + "%")));
	    filters.getAccountsWorkOnly().filter(Boolean::booleanValue)
	            .ifPresent(a -> predicates.add(cb.isNotNull(root.get("accountsQueueId"))));

	    Cursor cursor = filters.getCursor();
	    if (cursor != null) {
	        if (cursor.hasPrevious()) {
	            predicates.add(cb.greaterThan(root.get("uid"), UUID.fromString(cursor.previous())));
	        } else if (cursor.hasNext()) {
	            predicates.add(cb.lessThan(root.get("uid"), UUID.fromString(cursor.next())));
	        }
	    }

	    if (cursor != null && cursor.hasPrevious()) {
	        cq.orderBy(cb.asc(root.get("uid")));
	    } else {
	        cq.orderBy(cb.desc(root.get("uid")));
	    }

	    if (!predicates.isEmpty()) {
	    	cq.where(predicates.toArray(new Predicate[0]));
	    }

	    TypedQuery<CaseInstanceEntity> query = entityManager.createQuery(cq);
	    query.setMaxResults(filters.getLimit());

	    List<CaseInstance> results = query.getResultList().stream()
	            .map(this::toDomain)
	            .collect(Collectors.toList());

	    String nextCursor = results.isEmpty() ? null : results.get(results.size() - 1).getId();
	    String previousCursor = results.isEmpty() ? null : results.get(0).getId();

	    return new PageResult<>(results, true, false, nextCursor, previousCursor, Direction.ASC, filters.getLimit());
	}

	@Override
	public CaseInstance get(final String businessKey) throws DatabaseRecordNotFoundException {
		Optional<CaseInstanceEntity> entity = entityManager
				.createQuery("SELECT c FROM CaseInstanceEntity c WHERE c.businessKey = :businessKey", CaseInstanceEntity.class)
				.setParameter("businessKey", businessKey).getResultStream().findFirst();

		return entity.map(this::toDomain)
				.orElseThrow(() -> new DatabaseRecordNotFoundException("CaseInstance", "businessKey", businessKey));
	}

	@Override
	public String save(final CaseInstance caseInstance) {
		CaseInstanceEntity entity = toEntity(caseInstance);
		entityManager.persist(entity);
		return entity.getUid().toString();
	}

	@Override
	public void update(final String businessKey, final CaseInstance caseInstance) throws DatabaseRecordNotFoundException {
		CaseInstanceEntity entity = entityManager
				.createQuery("SELECT c FROM CaseInstanceEntity c WHERE c.businessKey = :businessKey", CaseInstanceEntity.class)
				.setParameter("businessKey", businessKey).getResultStream().findFirst()
				.orElseThrow(() -> new DatabaseRecordNotFoundException("CaseInstance", "businessKey", businessKey));

		CaseStatus status = caseInstance.getStatus();
		if (status != null) {
			entity.setStatus(status.getCode());
		}
		
		entity.setStage(caseInstance.getStage());
		entity.setAttributes(caseInstance.getAttributes());
		entity.setDocuments(caseInstance.getDocuments());
		entity.setQueueId(caseInstance.getQueueId());
		entity.setComments(caseInstance.getComments());
		entity.setAdminState(caseInstance.getAdminState());
		entity.setAdminHealth(caseInstance.getAdminHealth());
		entity.setHealthReasonCodes(caseInstance.getHealthReasonCodes());
		entity.setHealthEvaluatedAt(caseInstance.getHealthEvaluatedAt());
		entity.setStaleSince(caseInstance.getStaleSince());
		entity.setMalformedCase(caseInstance.getMalformedCase());
		entity.setAdminOwnerId(caseInstance.getAdminOwnerId());
		entity.setAdminOwnerName(caseInstance.getAdminOwnerName());
		entity.setResponsibleLawyerId(caseInstance.getResponsibleLawyerId());
		entity.setResponsibleLawyerName(caseInstance.getResponsibleLawyerName());
		entity.setNextActionOwnerType(caseInstance.getNextActionOwnerType());
		entity.setNextActionOwnerRef(caseInstance.getNextActionOwnerRef());
		entity.setNextActionSummary(caseInstance.getNextActionSummary());
		entity.setNextActionDueAt(caseInstance.getNextActionDueAt());
		entity.setWaitingReasonCode(caseInstance.getWaitingReasonCode());
		entity.setWaitingReasonText(caseInstance.getWaitingReasonText());
		entity.setWaitingSince(caseInstance.getWaitingSince());
		entity.setExpectedResponseAt(caseInstance.getExpectedResponseAt());
		entity.setExternalPartyRef(caseInstance.getExternalPartyRef());
		entity.setResumeToState(caseInstance.getResumeToState());
		entity.setLastStateChangedAt(caseInstance.getLastStateChangedAt());
		entity.setOpenedAt(caseInstance.getOpenedAt());
		entity.setMatterType(caseInstance.getMatterType());
		entity.setBillingPartyModel(caseInstance.getBillingPartyModel());
		entity.setBillingMode(caseInstance.getBillingMode());
		entity.setAccountsProfile(caseInstance.getAccountsProfile());
		entity.setBillingSetupComplete(caseInstance.getBillingSetupComplete());
		entity.setFlatFeeAmount(caseInstance.getFlatFeeAmount());
		entity.setPaymentMethodAuthorized(caseInstance.getPaymentMethodAuthorized());
		entity.setPaymentMethodRef(caseInstance.getPaymentMethodRef());
		entity.setRetainerAmount(caseInstance.getRetainerAmount());
		entity.setRetainerFundsReceived(caseInstance.getRetainerFundsReceived());
		entity.setSubscriptionPlanId(caseInstance.getSubscriptionPlanId());
		entity.setSubscriptionPlanName(caseInstance.getSubscriptionPlanName());
		entity.setSubscriptionActive(caseInstance.getSubscriptionActive());
		entity.setInstructingFirmId(caseInstance.getInstructingFirmId());
		entity.setInstructingFirmName(caseInstance.getInstructingFirmName());
		entity.setCounselBillingMode(caseInstance.getCounselBillingMode());
		entity.setCounselBillingPartyOverride(caseInstance.getCounselBillingPartyOverride());
		entity.setAccountsStage(caseInstance.getAccountsStage());
		entity.setAccountsState(caseInstance.getAccountsState());
		entity.setAccountsHealth(caseInstance.getAccountsHealth());
		entity.setAccountsHealthReasonCodes(caseInstance.getAccountsHealthReasonCodes());
		entity.setAccountsHealthEvaluatedAt(caseInstance.getAccountsHealthEvaluatedAt());
		entity.setAccountsStaleSince(caseInstance.getAccountsStaleSince());
		entity.setAccountsMalformedCase(caseInstance.getAccountsMalformedCase());
		entity.setAccountsReadinessStatus(caseInstance.getAccountsReadinessStatus());
		entity.setAccountsReadinessReasonCodes(caseInstance.getAccountsReadinessReasonCodes());
		entity.setAccountsReadinessEvaluatedAt(caseInstance.getAccountsReadinessEvaluatedAt());
		entity.setAccountsReadinessSummary(caseInstance.getAccountsReadinessSummary());
		entity.setAccountsQueueId(caseInstance.getAccountsQueueId());
		entity.setAccountsNextActionOwnerType(caseInstance.getAccountsNextActionOwnerType());
		entity.setAccountsNextActionSummary(caseInstance.getAccountsNextActionSummary());
		entity.setAccountsNextActionDueAt(caseInstance.getAccountsNextActionDueAt());
		entity.setAccountsWorkBlocked(caseInstance.getAccountsWorkBlocked());
		entity.setAccountsWorkPriority(caseInstance.getAccountsWorkPriority());
		entity.setAccountsEvents(caseInstance.getAccountsEvents());
		entity.setAdminEvents(caseInstance.getAdminEvents());

		entityManager.merge(entity);
	}

	@Override
	public void delete(final String businessKey) throws DatabaseRecordNotFoundException {
		CaseInstanceEntity entity = entityManager
				.createQuery("SELECT c FROM CaseInstanceEntity c WHERE c.businessKey = :businessKey",
						CaseInstanceEntity.class)
				.setParameter("businessKey", businessKey).getResultStream().findFirst()
				.orElseThrow(() -> new DatabaseRecordNotFoundException("CaseInstance", "businessKey", businessKey));

		entityManager.remove(entity);
	}

	@Override
	public void deleteComment(final String businessKey, final CaseComment comment)
			throws DatabaseRecordNotFoundException {

		CaseInstanceEntity entity = entityManager
				.createQuery("SELECT c FROM CaseInstanceEntity c WHERE c.businessKey = :businessKey",
						CaseInstanceEntity.class)
				.setParameter("businessKey", businessKey).getResultStream().findFirst()
				.orElseThrow(() -> new DatabaseRecordNotFoundException("CaseInstance", "businessKey", businessKey));

		entity.getComments().removeIf(c -> c.getId().equals(comment.getId()));
		entityManager.merge(entity);
	}

	@Override
	public void updateComment(final String businessKey, final String commentId, final String body)
			throws DatabaseRecordNotFoundException {
		CaseInstanceEntity entity = entityManager
				.createQuery("SELECT c FROM CaseInstanceEntity c WHERE c.businessKey = :businessKey",
						CaseInstanceEntity.class)
				.setParameter("businessKey", businessKey).getResultStream().findFirst()
				.orElseThrow(() -> new DatabaseRecordNotFoundException("CaseInstance", "businessKey", businessKey));

		entity.getComments().forEach(comment -> {
			if (comment.getId().equals(commentId)) {
				comment.setBody(body);
			}
		});

		entityManager.merge(entity);
	}

	private CaseInstance toDomain(CaseInstanceEntity entity) {
		CaseInstance caseInstance = new CaseInstance();
		caseInstance.set_id(entity.getUid().toString());
		caseInstance.setBusinessKey(entity.getBusinessKey());
		caseInstance.setStatus(CaseStatus.fromValue(entity.getStatus()).orElse(null));
		caseInstance.setStage(entity.getStage());
		caseInstance.setAttributes(entity.getAttributes());
		caseInstance.setDocuments(entity.getDocuments());
		caseInstance.setQueueId(entity.getQueueId());
		caseInstance.setComments(entity.getComments());
		caseInstance.setOwner(entity.getOwner());
		caseInstance.setCaseDefinitionId(entity.getCaseDefinitionId());
		caseInstance.setAdminState(entity.getAdminState());
		caseInstance.setAdminHealth(entity.getAdminHealth());
		caseInstance.setHealthReasonCodes(entity.getHealthReasonCodes());
		caseInstance.setHealthEvaluatedAt(entity.getHealthEvaluatedAt());
		caseInstance.setStaleSince(entity.getStaleSince());
		caseInstance.setMalformedCase(entity.getMalformedCase());
		caseInstance.setAdminOwnerId(entity.getAdminOwnerId());
		caseInstance.setAdminOwnerName(entity.getAdminOwnerName());
		caseInstance.setResponsibleLawyerId(entity.getResponsibleLawyerId());
		caseInstance.setResponsibleLawyerName(entity.getResponsibleLawyerName());
		caseInstance.setNextActionOwnerType(entity.getNextActionOwnerType());
		caseInstance.setNextActionOwnerRef(entity.getNextActionOwnerRef());
		caseInstance.setNextActionSummary(entity.getNextActionSummary());
		caseInstance.setNextActionDueAt(entity.getNextActionDueAt());
		caseInstance.setWaitingReasonCode(entity.getWaitingReasonCode());
		caseInstance.setWaitingReasonText(entity.getWaitingReasonText());
		caseInstance.setWaitingSince(entity.getWaitingSince());
		caseInstance.setExpectedResponseAt(entity.getExpectedResponseAt());
		caseInstance.setExternalPartyRef(entity.getExternalPartyRef());
		caseInstance.setResumeToState(entity.getResumeToState());
		caseInstance.setLastStateChangedAt(entity.getLastStateChangedAt());
		caseInstance.setOpenedAt(entity.getOpenedAt());
		caseInstance.setMatterType(entity.getMatterType());
		caseInstance.setBillingPartyModel(entity.getBillingPartyModel());
		caseInstance.setBillingMode(entity.getBillingMode());
		caseInstance.setAccountsProfile(entity.getAccountsProfile());
		caseInstance.setBillingSetupComplete(entity.getBillingSetupComplete());
		caseInstance.setFlatFeeAmount(entity.getFlatFeeAmount());
		caseInstance.setPaymentMethodAuthorized(entity.getPaymentMethodAuthorized());
		caseInstance.setPaymentMethodRef(entity.getPaymentMethodRef());
		caseInstance.setRetainerAmount(entity.getRetainerAmount());
		caseInstance.setRetainerFundsReceived(entity.getRetainerFundsReceived());
		caseInstance.setSubscriptionPlanId(entity.getSubscriptionPlanId());
		caseInstance.setSubscriptionPlanName(entity.getSubscriptionPlanName());
		caseInstance.setSubscriptionActive(entity.getSubscriptionActive());
		caseInstance.setInstructingFirmId(entity.getInstructingFirmId());
		caseInstance.setInstructingFirmName(entity.getInstructingFirmName());
		caseInstance.setCounselBillingMode(entity.getCounselBillingMode());
		caseInstance.setCounselBillingPartyOverride(entity.getCounselBillingPartyOverride());
		caseInstance.setAccountsStage(entity.getAccountsStage());
		caseInstance.setAccountsState(entity.getAccountsState());
		caseInstance.setAccountsHealth(entity.getAccountsHealth());
		caseInstance.setAccountsHealthReasonCodes(entity.getAccountsHealthReasonCodes());
		caseInstance.setAccountsHealthEvaluatedAt(entity.getAccountsHealthEvaluatedAt());
		caseInstance.setAccountsStaleSince(entity.getAccountsStaleSince());
		caseInstance.setAccountsMalformedCase(entity.getAccountsMalformedCase());
		caseInstance.setAccountsReadinessStatus(entity.getAccountsReadinessStatus());
		caseInstance.setAccountsReadinessReasonCodes(entity.getAccountsReadinessReasonCodes());
		caseInstance.setAccountsReadinessEvaluatedAt(entity.getAccountsReadinessEvaluatedAt());
		caseInstance.setAccountsReadinessSummary(entity.getAccountsReadinessSummary());
		caseInstance.setAccountsQueueId(entity.getAccountsQueueId());
		caseInstance.setAccountsNextActionOwnerType(entity.getAccountsNextActionOwnerType());
		caseInstance.setAccountsNextActionSummary(entity.getAccountsNextActionSummary());
		caseInstance.setAccountsNextActionDueAt(entity.getAccountsNextActionDueAt());
		caseInstance.setAccountsWorkBlocked(entity.getAccountsWorkBlocked());
		caseInstance.setAccountsWorkPriority(entity.getAccountsWorkPriority());
		caseInstance.setAccountsEvents(entity.getAccountsEvents());
		caseInstance.setAdminEvents(entity.getAdminEvents());
		return caseInstance;
	}

	private CaseInstanceEntity toEntity(CaseInstance caseInstance) {
		CaseInstanceEntity entity = new CaseInstanceEntity();
		entity.setBusinessKey(caseInstance.getBusinessKey());
		
		if (caseInstance.getStatus() != null) {
			entity.setStatus(caseInstance.getStatus().getCode());
		}
		
		entity.setStage(caseInstance.getStage());
		entity.setAttributes(caseInstance.getAttributes());
		entity.setDocuments(caseInstance.getDocuments());
		entity.setQueueId(caseInstance.getQueueId());
		entity.setComments(caseInstance.getComments());
		entity.setOwner(caseInstance.getOwner());
		entity.setCaseDefinitionId(caseInstance.getCaseDefinitionId());
		entity.setAdminState(caseInstance.getAdminState());
		entity.setAdminHealth(caseInstance.getAdminHealth());
		entity.setHealthReasonCodes(caseInstance.getHealthReasonCodes());
		entity.setHealthEvaluatedAt(caseInstance.getHealthEvaluatedAt());
		entity.setStaleSince(caseInstance.getStaleSince());
		entity.setMalformedCase(caseInstance.getMalformedCase());
		entity.setAdminOwnerId(caseInstance.getAdminOwnerId());
		entity.setAdminOwnerName(caseInstance.getAdminOwnerName());
		entity.setResponsibleLawyerId(caseInstance.getResponsibleLawyerId());
		entity.setResponsibleLawyerName(caseInstance.getResponsibleLawyerName());
		entity.setNextActionOwnerType(caseInstance.getNextActionOwnerType());
		entity.setNextActionOwnerRef(caseInstance.getNextActionOwnerRef());
		entity.setNextActionSummary(caseInstance.getNextActionSummary());
		entity.setNextActionDueAt(caseInstance.getNextActionDueAt());
		entity.setWaitingReasonCode(caseInstance.getWaitingReasonCode());
		entity.setWaitingReasonText(caseInstance.getWaitingReasonText());
		entity.setWaitingSince(caseInstance.getWaitingSince());
		entity.setExpectedResponseAt(caseInstance.getExpectedResponseAt());
		entity.setExternalPartyRef(caseInstance.getExternalPartyRef());
		entity.setResumeToState(caseInstance.getResumeToState());
		entity.setLastStateChangedAt(caseInstance.getLastStateChangedAt());
		entity.setOpenedAt(caseInstance.getOpenedAt());
		entity.setMatterType(caseInstance.getMatterType());
		entity.setBillingPartyModel(caseInstance.getBillingPartyModel());
		entity.setBillingMode(caseInstance.getBillingMode());
		entity.setAccountsProfile(caseInstance.getAccountsProfile());
		entity.setBillingSetupComplete(caseInstance.getBillingSetupComplete());
		entity.setFlatFeeAmount(caseInstance.getFlatFeeAmount());
		entity.setPaymentMethodAuthorized(caseInstance.getPaymentMethodAuthorized());
		entity.setPaymentMethodRef(caseInstance.getPaymentMethodRef());
		entity.setRetainerAmount(caseInstance.getRetainerAmount());
		entity.setRetainerFundsReceived(caseInstance.getRetainerFundsReceived());
		entity.setSubscriptionPlanId(caseInstance.getSubscriptionPlanId());
		entity.setSubscriptionPlanName(caseInstance.getSubscriptionPlanName());
		entity.setSubscriptionActive(caseInstance.getSubscriptionActive());
		entity.setInstructingFirmId(caseInstance.getInstructingFirmId());
		entity.setInstructingFirmName(caseInstance.getInstructingFirmName());
		entity.setCounselBillingMode(caseInstance.getCounselBillingMode());
		entity.setCounselBillingPartyOverride(caseInstance.getCounselBillingPartyOverride());
		entity.setAccountsStage(caseInstance.getAccountsStage());
		entity.setAccountsState(caseInstance.getAccountsState());
		entity.setAccountsHealth(caseInstance.getAccountsHealth());
		entity.setAccountsHealthReasonCodes(caseInstance.getAccountsHealthReasonCodes());
		entity.setAccountsHealthEvaluatedAt(caseInstance.getAccountsHealthEvaluatedAt());
		entity.setAccountsStaleSince(caseInstance.getAccountsStaleSince());
		entity.setAccountsMalformedCase(caseInstance.getAccountsMalformedCase());
		entity.setAccountsReadinessStatus(caseInstance.getAccountsReadinessStatus());
		entity.setAccountsReadinessReasonCodes(caseInstance.getAccountsReadinessReasonCodes());
		entity.setAccountsReadinessEvaluatedAt(caseInstance.getAccountsReadinessEvaluatedAt());
		entity.setAccountsReadinessSummary(caseInstance.getAccountsReadinessSummary());
		entity.setAccountsQueueId(caseInstance.getAccountsQueueId());
		entity.setAccountsNextActionOwnerType(caseInstance.getAccountsNextActionOwnerType());
		entity.setAccountsNextActionSummary(caseInstance.getAccountsNextActionSummary());
		entity.setAccountsNextActionDueAt(caseInstance.getAccountsNextActionDueAt());
		entity.setAccountsWorkBlocked(caseInstance.getAccountsWorkBlocked());
		entity.setAccountsWorkPriority(caseInstance.getAccountsWorkPriority());
		entity.setAccountsEvents(caseInstance.getAccountsEvents());
		entity.setAdminEvents(caseInstance.getAdminEvents());
		
		return entity;
	}

}
