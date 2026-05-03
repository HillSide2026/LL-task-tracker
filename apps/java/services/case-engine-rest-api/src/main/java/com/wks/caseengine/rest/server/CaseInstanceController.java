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
package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.wks.caseengine.cases.definition.CaseDefinitionNotFoundException;
import com.wks.caseengine.cases.instance.CaseComment;
import com.wks.caseengine.cases.instance.CaseDocument;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceCommentNotFoundException;
import com.wks.caseengine.cases.instance.CaseInstanceFilter;
import com.wks.caseengine.cases.instance.CaseInstanceNotFoundException;
import com.wks.caseengine.cases.instance.accounts.AccountsLifecycleException;
import com.wks.caseengine.cases.instance.accounts.AccountsReadinessEvaluation;
import com.wks.caseengine.cases.instance.accounts.AccountsTransition;
import com.wks.caseengine.cases.instance.accounts.AccountsTransitionRequest;
import com.wks.caseengine.cases.instance.accounts.AccountsWorkSummary;
import com.wks.caseengine.cases.instance.admin.AdminLifecycleException;
import com.wks.caseengine.cases.instance.admin.AdminTransition;
import com.wks.caseengine.cases.instance.admin.AdminTransitionRequest;
import com.wks.caseengine.cases.instance.service.CaseInstanceService;
import com.wks.caseengine.pagination.Cursor;
import com.wks.caseengine.pagination.PageResult;
import com.wks.caseengine.rest.exception.RestInvalidArgumentException;
import com.wks.caseengine.rest.exception.RestResourceNotFoundException;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("case")
@Tag(name = "Case Instance", description = "A Case Instance is created based in a Case Definition and is the 'Digital Folder' for related information, documents, communication and processes for case")
public class CaseInstanceController {

	@Autowired
	private CaseInstanceService caseInstanceService;

	@Autowired
	private GsonBuilder gsonBuilder;

	@GetMapping
	public ResponseEntity<Object> find(@RequestParam(required = false) String status,
			@RequestParam(required = false) String caseDefinitionId,
			@RequestParam(required = false) String adminState,
			@RequestParam(required = false) String adminHealth,
			@RequestParam(required = false) String nextActionOwnerType,
			@RequestParam(required = false) String queueId,
			@RequestParam(required = false) String malformedCase,
			@RequestParam(required = false) String exceptionOnly,
			@RequestParam(required = false) String adminOwnerId,
			@RequestParam(required = false) String healthReasonCode,
			@RequestParam(required = false, name = "before") String before,
			@RequestParam(required = false, name = "after") String after,
			@RequestParam(required = false, name = "sort") String sort,
			@RequestParam(required = false, name = "limit") String limit) {

		Cursor cursor = Cursor.of(before, after);

		CaseInstanceFilter filter = new CaseInstanceFilter(status, caseDefinitionId, adminState, adminHealth,
				nextActionOwnerType, queueId, malformedCase, exceptionOnly, adminOwnerId, null, healthReasonCode, cursor, sort, limit);

		PageResult<CaseInstance> data = caseInstanceService.find(filter);

		return ResponseEntity.ok(data.toJson());
	}

	@GetMapping(value = "/{businessKey}")
	public ResponseEntity<CaseInstance> get(@PathVariable final String businessKey) {
		try {
			return ResponseEntity.ok(caseInstanceService.get(businessKey));
		} catch (CaseInstanceNotFoundException e) {
			throw new RestResourceNotFoundException(e.getMessage());
		}
	}

	@PostMapping
	public ResponseEntity<CaseInstance> startCaseCreationProcess(@RequestBody final CaseInstance caseInstance) {
		try {
			return ResponseEntity.ok(caseInstanceService.startWithValues(caseInstance));
		} catch (CaseDefinitionNotFoundException e) {
			throw new RestInvalidArgumentException("caseDefinitionId", e);
		}
	}

	@PostMapping(value = "/save")
	public ResponseEntity<Void> save(@RequestBody final CaseInstance caseInstance) {
		try {
			caseInstanceService.saveWithValues(caseInstance);
		} catch (CaseDefinitionNotFoundException e) {
			throw new RestInvalidArgumentException("caseDefinitionId", e);
		}
		return ResponseEntity.noContent().build();
	}

	@PatchMapping(value = "/{businessKey}", consumes = "application/merge-patch+json")
	public ResponseEntity<Void> mergePatch(@PathVariable final String businessKey, @RequestBody String mergePatchJson) {

		JsonObject mergePatchObject = gsonBuilder.create().fromJson(mergePatchJson, JsonObject.class);
		if (hasAdminLifecycleFields(mergePatchObject)) {
			throw new RestInvalidArgumentException("mergePatch",
					new IllegalArgumentException("Admin lifecycle fields must be changed through explicit transition endpoints"));
		}

		CaseInstance mergePatch = gsonBuilder.create().fromJson(mergePatchJson, CaseInstance.class);

		try {
			caseInstanceService.patch(businessKey, mergePatch);
		} catch (CaseInstanceNotFoundException e) {
			throw new RestResourceNotFoundException(e.getMessage());
		} catch (AdminLifecycleException e) {
			throw new RestInvalidArgumentException("mergePatch", e);
		}
		return ResponseEntity.noContent().build();
	}

	@PostMapping(value = "/{businessKey}/transition/{transitionName}")
	public ResponseEntity<CaseInstance> transition(@PathVariable final String businessKey,
			@PathVariable final String transitionName, @RequestBody(required = false) final AdminTransitionRequest request) {
		AdminTransition transition = AdminTransition.fromValue(transitionName)
				.orElseThrow(() -> new RestInvalidArgumentException("transitionName",
						new IllegalArgumentException("Unsupported transition " + transitionName)));

		try {
			return ResponseEntity.ok(caseInstanceService.transition(businessKey, transition,
					request != null ? request : AdminTransitionRequest.builder().build()));
		} catch (CaseInstanceNotFoundException e) {
			throw new RestResourceNotFoundException(e.getMessage());
		} catch (AdminLifecycleException e) {
			throw new RestInvalidArgumentException("transition", e);
		}
	}

	@PostMapping(value = "/{businessKey}/accounts/transition")
	public ResponseEntity<CaseInstance> transitionAccounts(@PathVariable final String businessKey,
			@RequestBody(required = false) final AccountsTransitionRequest request,
			@RequestParam(required = false) String transitionName) {
		String requestedTransition = transitionName != null ? transitionName : request != null ? request.getTransition() : null;
		AccountsTransition transition = AccountsTransition.fromValue(requestedTransition)
				.orElseThrow(() -> new RestInvalidArgumentException("transitionName",
						new IllegalArgumentException("Unsupported accounts transition " + requestedTransition)));

		try {
			return ResponseEntity.ok(caseInstanceService.transitionAccounts(businessKey, transition,
					request != null ? request : AccountsTransitionRequest.builder().build()));
		} catch (CaseInstanceNotFoundException e) {
			throw new RestResourceNotFoundException(e.getMessage());
		} catch (AccountsLifecycleException e) {
			throw new RestInvalidArgumentException("accountsTransition", e);
		}
	}

	@GetMapping(value = "/{businessKey}/accounts")
	public ResponseEntity<CaseInstance> getAccounts(@PathVariable final String businessKey) {
		try {
			return ResponseEntity.ok(caseInstanceService.getAccounts(businessKey));
		} catch (CaseInstanceNotFoundException e) {
			throw new RestResourceNotFoundException(e.getMessage());
		} catch (AccountsLifecycleException e) {
			throw new RestInvalidArgumentException("accounts", e);
		}
	}

	@GetMapping(value = "/accounts/work")
	public ResponseEntity<Object> getAccountsWork(@RequestParam(required = false) String accountsReadinessStatus,
			@RequestParam(required = false) String accountsQueueId,
			@RequestParam(required = false) String accountsNextActionOwnerType,
			@RequestParam(required = false) String accountsNextActionDueBefore,
			@RequestParam(required = false) String accountsWorkBlocked,
			@RequestParam(required = false) String accountsReadinessReasonCode,
			@RequestParam(required = false) String matterType,
			@RequestParam(required = false) String adminOwnerId,
			@RequestParam(required = false) String responsibleLawyerId,
			@RequestParam(required = false, name = "before") String before,
			@RequestParam(required = false, name = "after") String after,
			@RequestParam(required = false, name = "sort") String sort,
			@RequestParam(required = false, name = "limit") String limit) {
		try {
			PageResult<CaseInstance> data = caseInstanceService.findAccountsWork(accountsWorkFilter(accountsReadinessStatus,
					accountsQueueId, accountsNextActionOwnerType, accountsNextActionDueBefore, accountsWorkBlocked,
					accountsReadinessReasonCode, matterType, adminOwnerId, responsibleLawyerId, before, after, sort, limit));
			return ResponseEntity.ok(data.toJson());
		} catch (AccountsLifecycleException e) {
			throw new RestInvalidArgumentException("accountsWork", e);
		}
	}

	@GetMapping(value = "/accounts/work/summary")
	public ResponseEntity<AccountsWorkSummary> getAccountsWorkSummary(
			@RequestParam(required = false) String accountsReadinessStatus,
			@RequestParam(required = false) String accountsQueueId,
			@RequestParam(required = false) String accountsNextActionOwnerType,
			@RequestParam(required = false) String accountsNextActionDueBefore,
			@RequestParam(required = false) String accountsWorkBlocked,
			@RequestParam(required = false) String accountsReadinessReasonCode,
			@RequestParam(required = false) String matterType,
			@RequestParam(required = false) String adminOwnerId,
			@RequestParam(required = false) String responsibleLawyerId,
			@RequestParam(required = false, name = "before") String before,
			@RequestParam(required = false, name = "after") String after,
			@RequestParam(required = false, name = "sort") String sort,
			@RequestParam(required = false, name = "limit") String limit) {
		try {
			return ResponseEntity.ok(caseInstanceService.getAccountsWorkSummary(accountsWorkFilter(accountsReadinessStatus,
					accountsQueueId, accountsNextActionOwnerType, accountsNextActionDueBefore, accountsWorkBlocked,
					accountsReadinessReasonCode, matterType, adminOwnerId, responsibleLawyerId, before, after, sort, limit)));
		} catch (AccountsLifecycleException e) {
			throw new RestInvalidArgumentException("accountsWork", e);
		}
	}

	@GetMapping(value = "/{businessKey}/accounts/history")
	public ResponseEntity<Object> getAccountsHistory(@PathVariable final String businessKey) {
		try {
			return ResponseEntity.ok(caseInstanceService.getAccountsHistory(businessKey));
		} catch (CaseInstanceNotFoundException e) {
			throw new RestResourceNotFoundException(e.getMessage());
		} catch (AccountsLifecycleException e) {
			throw new RestInvalidArgumentException("accounts", e);
		}
	}

	@GetMapping(value = "/{businessKey}/accounts/readiness")
	public ResponseEntity<AccountsReadinessEvaluation> getAccountsReadiness(@PathVariable final String businessKey) {
		try {
			return ResponseEntity.ok(caseInstanceService.getAccountsReadiness(businessKey));
		} catch (CaseInstanceNotFoundException e) {
			throw new RestResourceNotFoundException(e.getMessage());
		} catch (AccountsLifecycleException e) {
			throw new RestInvalidArgumentException("accountsReadiness", e);
		}
	}

	@PostMapping(value = "/{businessKey}/accounts/evaluate-readiness")
	public ResponseEntity<AccountsReadinessEvaluation> evaluateAccountsReadiness(@PathVariable final String businessKey) {
		try {
			return ResponseEntity.ok(caseInstanceService.evaluateAccountsReadiness(businessKey));
		} catch (CaseInstanceNotFoundException e) {
			throw new RestResourceNotFoundException(e.getMessage());
		} catch (AccountsLifecycleException e) {
			throw new RestInvalidArgumentException("accountsReadiness", e);
		}
	}

	@DeleteMapping(value = "/{businessKey}")
	public ResponseEntity<Void> delete(@PathVariable final String businessKey) {
		try {
			caseInstanceService.delete(businessKey);
		} catch (CaseInstanceNotFoundException e) {
			throw new RestResourceNotFoundException(e.getMessage());
		} catch (AdminLifecycleException e) {
			throw new RestInvalidArgumentException("businessKey", e);
		}
		return ResponseEntity.noContent().build();
	}

	@PostMapping(value = "/{businessKey}/document")
	public ResponseEntity<Void> saveDocument(@PathVariable final String businessKey,
			@RequestBody CaseDocument document) {

		try {
			caseInstanceService.saveDocument(businessKey, document);
		} catch (CaseInstanceNotFoundException e) {
			throw new RestResourceNotFoundException(e.getMessage());
		}
		return ResponseEntity.noContent().build();
	}

	@PostMapping(value = "/{businessKey}/comment")
	public ResponseEntity<Void> saveComment(@PathVariable final String businessKey,
			@RequestBody final CaseComment newComment) {

		try {
			caseInstanceService.saveComment(businessKey, newComment);
		} catch (CaseInstanceNotFoundException e) {
			throw new RestResourceNotFoundException(e.getMessage());
		}
		return ResponseEntity.noContent().build();
	}

	@PutMapping(value = "/{businessKey}/comment/{commentId}")
	public ResponseEntity<Void> udpateComment(@PathVariable final String businessKey,
			@PathVariable final String commentId, @RequestBody final CaseComment comment) {

		try {
			caseInstanceService.updateComment(businessKey, commentId, comment.getBody());
		} catch (CaseInstanceNotFoundException e) {
			throw new RestResourceNotFoundException(e.getMessage());
		}
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping(value = "/{businessKey}/comment/{commentId}")
	public ResponseEntity<Void> deleteComment(@PathVariable final String businessKey,
			@PathVariable final String commentId) {

		try {
			caseInstanceService.deleteComment(businessKey, commentId);
		} catch (CaseInstanceNotFoundException | CaseInstanceCommentNotFoundException e) {
			throw new RestResourceNotFoundException(e.getMessage());
		}
		return ResponseEntity.noContent().build();
	}

	private boolean hasAdminLifecycleFields(JsonObject mergePatchObject) {
		if (mergePatchObject == null) {
			return false;
		}

		String[] blockedFields = { "adminState", "adminHealth", "healthReasonCodes", "healthEvaluatedAt", "staleSince",
				"malformedCase", "adminOwnerId", "adminOwnerName", "responsibleLawyerId", "responsibleLawyerName",
				"nextActionOwnerType", "nextActionOwnerRef", "nextActionSummary", "nextActionDueAt", "waitingReasonCode",
				"waitingReasonText", "waitingSince", "expectedResponseAt", "externalPartyRef", "resumeToState",
				"lastStateChangedAt", "openedAt", "adminEvents", "accountsStage", "accountsState", "accountsHealth",
				"accountsHealthReasonCodes", "accountsHealthEvaluatedAt", "accountsStaleSince",
				"accountsMalformedCase", "accountsReadinessStatus", "accountsReadinessReasonCodes",
				"accountsReadinessEvaluatedAt", "accountsReadinessSummary", "accountsQueueId",
				"accountsNextActionOwnerType", "accountsNextActionSummary", "accountsNextActionDueAt",
				"accountsWorkBlocked", "accountsWorkPriority", "accountsEvents" };

		for (String blockedField : blockedFields) {
			if (mergePatchObject.has(blockedField)) {
				return true;
			}
		}
		return false;
	}

	private CaseInstanceFilter accountsWorkFilter(String accountsReadinessStatus, String accountsQueueId,
			String accountsNextActionOwnerType, String accountsNextActionDueBefore, String accountsWorkBlocked,
			String accountsReadinessReasonCode, String matterType, String adminOwnerId, String responsibleLawyerId,
			String before, String after, String sort, String limit) {
		return CaseInstanceFilter.builder().adminOwnerId(java.util.Optional.ofNullable(adminOwnerId))
				.responsibleLawyerId(java.util.Optional.ofNullable(responsibleLawyerId))
				.matterType(java.util.Optional.ofNullable(matterType))
				.accountsReadinessStatus(java.util.Optional.ofNullable(accountsReadinessStatus))
				.accountsQueueId(java.util.Optional.ofNullable(accountsQueueId))
				.accountsNextActionOwnerType(java.util.Optional.ofNullable(accountsNextActionOwnerType))
				.accountsNextActionDueBefore(java.util.Optional.ofNullable(accountsNextActionDueBefore))
				.accountsWorkBlocked(parseBoolean(accountsWorkBlocked))
				.accountsReadinessReasonCode(java.util.Optional.ofNullable(accountsReadinessReasonCode))
				.accountsWorkOnly(java.util.Optional.of(true))
				.status(java.util.Optional.empty()).caseDefsId(java.util.Optional.empty())
				.adminState(java.util.Optional.empty()).adminHealth(java.util.Optional.empty())
				.nextActionOwnerType(java.util.Optional.empty()).queueId(java.util.Optional.empty())
				.malformedCase(java.util.Optional.empty()).exceptionOnly(java.util.Optional.empty())
				.healthReasonCode(java.util.Optional.empty()).cursor(Cursor.of(before, after))
				.dir(sort == null || sort.isBlank() ? org.springframework.data.domain.Sort.Direction.ASC
						: org.springframework.data.domain.Sort.Direction.fromString(sort))
				.limit(parseLimit(limit)).build();
	}

	private java.util.Optional<Boolean> parseBoolean(String value) {
		if (value == null || value.isBlank()) {
			return java.util.Optional.empty();
		}
		return java.util.Optional.of(Boolean.valueOf(value));
	}

	private Integer parseLimit(String value) {
		try {
			return Integer.valueOf(value);
		} catch (Exception e) {
			return 10;
		}
	}

}
