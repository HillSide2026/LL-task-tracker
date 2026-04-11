package com.wks.caseengine.cases.instance.admin;

import java.io.Serializable;
import java.util.Optional;

public enum AdminHealthReasonCode implements Serializable {

	DUE_RISK("DUE_RISK"),
	WAITING_STALE("WAITING_STALE"),
	CONTROL_INCOMPLETE("CONTROL_INCOMPLETE"),
	MISSING_ADMIN_STATE("MISSING_ADMIN_STATE"),
	MISSING_STAGE("MISSING_STAGE"),
	MISSING_NEXT_ACTION_SUMMARY("MISSING_NEXT_ACTION_SUMMARY"),
	MISSING_NEXT_ACTION_OWNER("MISSING_NEXT_ACTION_OWNER"),
	MISSING_RESPONSIBLE_LAWYER("MISSING_RESPONSIBLE_LAWYER"),
	MISSING_WAITING_SINCE("MISSING_WAITING_SINCE"),
	MISSING_RESUME_TO_STATE("MISSING_RESUME_TO_STATE"),
	MISSING_EXPECTED_RESPONSE_AT("MISSING_EXPECTED_RESPONSE_AT"),
	STAGE_STATE_MISMATCH("STAGE_STATE_MISMATCH"),
	LAWYER_RESPONSE_STALE("LAWYER_RESPONSE_STALE"),
	EXTERNAL_RESPONSE_STALE("EXTERNAL_RESPONSE_STALE"),
	UNOWNED_ACTIVE_CASE("UNOWNED_ACTIVE_CASE"),
	INVALID_RESUME_TARGET("INVALID_RESUME_TARGET"),
	MISSING_EXTERNAL_PARTY_REF("MISSING_EXTERNAL_PARTY_REF");

	private final String code;

	AdminHealthReasonCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static Optional<AdminHealthReasonCode> fromValue(String value) {
		for (AdminHealthReasonCode reason : values()) {
			if (reason.code.equals(value)) {
				return Optional.of(reason);
			}
		}
		return Optional.empty();
	}
}
