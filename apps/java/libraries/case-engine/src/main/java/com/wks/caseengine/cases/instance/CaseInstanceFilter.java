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
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CaseInstanceFilter {

	private Optional<CaseStatus> status;
	private Optional<String> caseDefsId;
	private Optional<String> adminState;
	private Optional<String> adminHealth;
	private Optional<String> nextActionOwnerType;
	private Optional<String> queueId;
	private Optional<Boolean> malformedCase;
	private Optional<Boolean> exceptionOnly;
	private Optional<String> adminOwnerId;
	private Optional<String> responsibleLawyerId;
	private Optional<String> healthReasonCode;
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
