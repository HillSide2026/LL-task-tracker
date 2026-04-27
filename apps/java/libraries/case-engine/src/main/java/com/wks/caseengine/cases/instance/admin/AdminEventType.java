package com.wks.caseengine.cases.instance.admin;

import java.io.Serializable;
import java.util.Optional;

public enum AdminEventType implements Serializable {

	CASE_CREATED("CASE_CREATED"),
	STATE_CHANGED("STATE_CHANGED"),
	STAGE_CHANGED("STAGE_CHANGED"),
	WAIT_STARTED("WAIT_STARTED"),
	WAIT_RESUMED("WAIT_RESUMED"),
	NEXT_ACTION_SET("NEXT_ACTION_SET"),
	OWNER_ASSIGNED("OWNER_ASSIGNED"),
	LAWYER_HANDOFF_SENT("LAWYER_HANDOFF_SENT"),
	LAWYER_HANDOFF_RETURNED("LAWYER_HANDOFF_RETURNED"),
	OVERRIDE_APPLIED("OVERRIDE_APPLIED"),
	CASE_OPENED("CASE_OPENED"),
	CASE_ACTIVATED("CASE_ACTIVATED"),
	CLOSING_REVIEW_STARTED("CLOSING_REVIEW_STARTED"),
	CASE_CLOSED("CASE_CLOSED"),
	CASE_ARCHIVED("CASE_ARCHIVED");

	private final String code;

	AdminEventType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static Optional<AdminEventType> fromValue(String value) {
		for (AdminEventType eventType : values()) {
			if (eventType.code.equals(value)) {
				return Optional.of(eventType);
			}
		}
		return Optional.empty();
	}
}
