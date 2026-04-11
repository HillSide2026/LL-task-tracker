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
package com.wks.caseengine.cases.instance.repository;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.set;

import java.util.ArrayList;
import java.util.List;

import org.bson.BsonObjectId;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.wks.caseengine.cases.instance.admin.AdminHealth;
import com.wks.caseengine.cases.instance.CaseComment;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceFilter;
import com.wks.caseengine.db.EngineMongoDataConnection;
import com.wks.caseengine.pagination.Args;
import com.wks.caseengine.pagination.CursorPagination;
import com.wks.caseengine.pagination.PageResult;
import com.wks.caseengine.pagination.mongo.MongoCursorPagination;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;
import com.wks.caseengine.repository.MongoPaginator;

@Primary
@Component
@ConditionalOnProperty(name = "database.type", havingValue = "mongo", matchIfMissing = false)
public class CaseInstanceRepositoryImpl implements CaseInstanceRepository {

	@Autowired
	private EngineMongoDataConnection connection;

	@Autowired
	private MongoPaginator paginator;

	@Override
	public List<CaseInstance> find() {
		return paginator.apply(getCollection().find()).sort(descending("_id")).into(new ArrayList<>());
	}

	@Override
	public PageResult<CaseInstance> find(CaseInstanceFilter filters) {
		CursorPagination pagination = new MongoCursorPagination(getOperations());

		Args args = Args.of(filters.getLimit()).key("_id").cursor(filters.getCursor(), filters.getDir()).criteria(c -> {
			filters.getCaseDefsId()
					.ifPresent(a -> c.add(Criteria.where("caseDefinitionId").is(filters.getCaseDefsId().get())));
			filters.getStatus().ifPresent(a -> c.add(Criteria.where("status").is(a.getCode())));
			filters.getAdminState().ifPresent(a -> c.add(Criteria.where("adminState").is(a)));
			filters.getAdminHealth().ifPresent(a -> c.add(Criteria.where("adminHealth").is(a)));
			filters.getNextActionOwnerType().ifPresent(a -> c.add(Criteria.where("nextActionOwnerType").is(a)));
			filters.getQueueId().ifPresent(a -> c.add(Criteria.where("queueId").is(a)));
			filters.getMalformedCase().ifPresent(a -> c.add(Criteria.where("malformedCase").is(a)));
			filters.getExceptionOnly().filter(Boolean::booleanValue).ifPresent(a -> c.add(new Criteria().orOperator(
					Criteria.where("adminHealth").in(AdminHealth.AMBER.getCode(), AdminHealth.RED.getCode()),
					Criteria.where("malformedCase").is(true))));
			filters.getAdminOwnerId().ifPresent(a -> c.add(Criteria.where("adminOwnerId").is(a)));
			filters.getResponsibleLawyerId().ifPresent(a -> c.add(Criteria.where("responsibleLawyerId").is(a)));
			filters.getHealthReasonCode().ifPresent(a -> c.add(Criteria.where("healthReasonCodes").in(a)));
		});

		PageResult<CaseInstance> results = pagination.executeQuery(args, CaseInstance.class);

		return results;
	}

	@Override
	public CaseInstance get(final String businessKey) throws DatabaseRecordNotFoundException {
		Bson filter = Filters.eq("businessKey", businessKey);
		CaseInstance first = getCollection().find(filter).first();
		if (first == null) {
			throw new DatabaseRecordNotFoundException("CaseInstance", "businessKey", businessKey);
		}
		return first;
	}

	@Override
	public String save(final CaseInstance caseInstance) {
		return ((BsonObjectId) getCollection().insertOne(caseInstance).getInsertedId()).getValue().toHexString();
	}

	@Override
	public void update(final String businessKey, final CaseInstance caseInstance)
			throws DatabaseRecordNotFoundException {
		Bson filter = Filters.eq("businessKey", businessKey);
		Bson update = Updates.combine(Updates.set("status", caseInstance.getStatus()),
				Updates.set("stage", caseInstance.getStage()), Updates.set("attributes", caseInstance.getAttributes()),
				Updates.set("documents", caseInstance.getDocuments()),
				Updates.set("queueId", caseInstance.getQueueId()), Updates.set("comments", caseInstance.getComments()),
				Updates.set("adminState", caseInstance.getAdminState()),
				Updates.set("adminHealth", caseInstance.getAdminHealth()),
				Updates.set("healthReasonCodes", caseInstance.getHealthReasonCodes()),
				Updates.set("healthEvaluatedAt", caseInstance.getHealthEvaluatedAt()),
				Updates.set("staleSince", caseInstance.getStaleSince()),
				Updates.set("malformedCase", caseInstance.getMalformedCase()),
				Updates.set("adminOwnerId", caseInstance.getAdminOwnerId()),
				Updates.set("adminOwnerName", caseInstance.getAdminOwnerName()),
				Updates.set("responsibleLawyerId", caseInstance.getResponsibleLawyerId()),
				Updates.set("responsibleLawyerName", caseInstance.getResponsibleLawyerName()),
				Updates.set("nextActionOwnerType", caseInstance.getNextActionOwnerType()),
				Updates.set("nextActionOwnerRef", caseInstance.getNextActionOwnerRef()),
				Updates.set("nextActionSummary", caseInstance.getNextActionSummary()),
				Updates.set("nextActionDueAt", caseInstance.getNextActionDueAt()),
				Updates.set("waitingReasonCode", caseInstance.getWaitingReasonCode()),
				Updates.set("waitingReasonText", caseInstance.getWaitingReasonText()),
				Updates.set("waitingSince", caseInstance.getWaitingSince()),
				Updates.set("expectedResponseAt", caseInstance.getExpectedResponseAt()),
				Updates.set("externalPartyRef", caseInstance.getExternalPartyRef()),
				Updates.set("resumeToState", caseInstance.getResumeToState()),
				Updates.set("lastStateChangedAt", caseInstance.getLastStateChangedAt()),
				Updates.set("openedAt", caseInstance.getOpenedAt()),
				Updates.set("adminEvents", caseInstance.getAdminEvents()));

		CaseInstance updatedCaseInstance = getCollection().findOneAndUpdate(filter, update);
		if (updatedCaseInstance == null) {
			throw new DatabaseRecordNotFoundException("CaseInstance", "businessKey", businessKey);
		}

	}

	@Override
	public void delete(final String businessKey) throws DatabaseRecordNotFoundException {
		Bson filter = Filters.eq("businessKey", businessKey);

		CaseInstance updatedCaseInstance = getCollection().findOneAndDelete(filter);
		if (updatedCaseInstance == null) {
			throw new DatabaseRecordNotFoundException("CaseInstance", "businessKey", businessKey);
		}

	}

	@Override
	public void deleteComment(final String businessKey, final CaseComment comment)
			throws DatabaseRecordNotFoundException {

		Bson filter = Filters.eq("businessKey", businessKey);
		Bson update = Updates.pull("comments", comment);

		CaseInstance updatedCaseInstance = getCollection().findOneAndUpdate(filter, update);
		if (updatedCaseInstance == null) {
			throw new DatabaseRecordNotFoundException("CaseInstance", "businessKey", businessKey);
		}

	}

	@Override
	public void updateComment(final String businessKey, final String commentId, final String body)
			throws DatabaseRecordNotFoundException {
		Bson filter = and(eq("businessKey", businessKey), eq("comments.id", commentId));
		Bson update = set("comments.$.body", body);

		CaseInstance updatedCaseInstance = getCollection().findOneAndUpdate(filter, update);
		if (updatedCaseInstance == null) {
			throw new DatabaseRecordNotFoundException("CaseInstance", "businessKey", businessKey);
		}

	}

	protected MongoOperations getOperations() {
		return connection.getOperations();
	}

	private MongoCollection<CaseInstance> getCollection() {
		return connection.getCaseInstanceCollection();
	}

}
