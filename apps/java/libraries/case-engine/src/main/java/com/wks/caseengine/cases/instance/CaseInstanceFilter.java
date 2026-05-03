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

import java.util.Optional;

import org.springframework.data.domain.Sort.Direction;

import com.wks.caseengine.cases.definition.CaseStatus;
import com.wks.caseengine.pagination.Cursor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CaseInstanceFilter {

	@Default
	private Optional<CaseStatus> status = Optional.empty();
	@Default
	private Optional<String> caseDefsId = Optional.empty();
	@Default
	private Optional<String> adminState = Optional.empty();
	@Default
	private Optional<String> adminHealth = Optional.empty();
	@Default
	private Optional<String> nextActionOwnerType = Optional.empty();
	@Default
	private Optional<String> queueId = Optional.empty();
	@Default
	private Optional<Boolean> malformedCase = Optional.empty();
	@Default
	private Optional<Boolean> exceptionOnly = Optional.empty();
	@Default
	private Optional<String> adminOwnerId = Optional.empty();
	@Default
	private Optional<String> responsibleLawyerId = Optional.empty();
	@Default
	private Optional<String> healthReasonCode = Optional.empty();
	@Default
	private Optional<String> matterType = Optional.empty();
	@Default
	private Optional<String> accountsReadinessStatus = Optional.empty();
	@Default
	private Optional<String> accountsQueueId = Optional.empty();
	@Default
	private Optional<String> accountsNextActionOwnerType = Optional.empty();
	@Default
	private Optional<String> accountsNextActionDueBefore = Optional.empty();
	@Default
	private Optional<Boolean> accountsWorkBlocked = Optional.empty();
	@Default
	private Optional<String> accountsReadinessReasonCode = Optional.empty();
	@Default
	private Optional<Boolean> accountsWorkOnly = Optional.empty();
	private Direction dir;
	private Integer limit;
	private Cursor cursor;

	public CaseInstanceFilter(String status, String caseDefsId, String adminState, String adminHealth,
			String nextActionOwnerType, String queueId, String malformedCase, Cursor cursor, String dir, String limit) {
		this(status, caseDefsId, adminState, adminHealth, nextActionOwnerType, queueId, malformedCase, null, null, null, null,
				cursor, dir, limit);
	}

	public CaseInstanceFilter(String status, String caseDefsId, String adminState, String adminHealth,
			String nextActionOwnerType, String queueId, String malformedCase, String exceptionOnly, String adminOwnerId,
			String responsibleLawyerId, String healthReasonCode, Cursor cursor, String dir, String limit) {
		super();
		this.cursor = cursor;
		this.dir = dir == null || dir.isBlank() ? Direction.ASC : Direction.fromString(dir);
		this.limit = parseInt(limit);
		this.status = CaseStatus.fromValue(status);
		this.caseDefsId = Optional.ofNullable(caseDefsId);
		this.adminState = Optional.ofNullable(adminState);
		this.adminHealth = Optional.ofNullable(adminHealth);
		this.nextActionOwnerType = Optional.ofNullable(nextActionOwnerType);
		this.queueId = Optional.ofNullable(queueId);
		this.malformedCase = parseBoolean(malformedCase);
		this.exceptionOnly = parseBoolean(exceptionOnly);
		this.adminOwnerId = Optional.ofNullable(adminOwnerId);
		this.responsibleLawyerId = Optional.ofNullable(responsibleLawyerId);
		this.healthReasonCode = Optional.ofNullable(healthReasonCode);
		this.matterType = Optional.empty();
		this.accountsReadinessStatus = Optional.empty();
		this.accountsQueueId = Optional.empty();
		this.accountsNextActionOwnerType = Optional.empty();
		this.accountsNextActionDueBefore = Optional.empty();
		this.accountsWorkBlocked = Optional.empty();
		this.accountsReadinessReasonCode = Optional.empty();
		this.accountsWorkOnly = Optional.empty();
	}

	private Integer parseInt(String limit) {
		try {
			return Integer.valueOf(limit);
		} catch (Exception e) {
			return 10;
		}
	}

	private Optional<Boolean> parseBoolean(String value) {
		if (value == null || value.isBlank()) {
			return Optional.empty();
		}
		return Optional.of(Boolean.valueOf(value));
	}

}
