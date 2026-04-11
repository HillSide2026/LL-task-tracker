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
				"lastStateChangedAt", "openedAt", "adminEvents" };

		for (String blockedField : blockedFields) {
			if (mergePatchObject.has(blockedField)) {
				return true;
			}
		}
		return false;
	}

}
