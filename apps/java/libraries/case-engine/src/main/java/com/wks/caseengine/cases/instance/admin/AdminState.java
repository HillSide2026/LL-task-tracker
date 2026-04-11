package com.wks.caseengine.cases.instance.admin;

import java.io.Serializable;
import java.util.Optional;

import com.google.gson.annotations.SerializedName;

public enum AdminState implements Serializable {

	@SerializedName("Intake Review")
	INTAKE_REVIEW("Intake Review"),

	@SerializedName("Awaiting Engagement")
	AWAITING_ENGAGEMENT("Awaiting Engagement"),

	@SerializedName("Ready to Open")
	READY_TO_OPEN("Ready to Open"),

	@SerializedName("Ready for Lawyer")
	READY_FOR_LAWYER("Ready for Lawyer"),

	@SerializedName("Waiting on Client")
	WAITING_ON_CLIENT("Waiting on Client"),

	@SerializedName("Opened")
	OPENED("Opened"),

	@SerializedName("Active")
	ACTIVE("Active"),

	@SerializedName("Maintenance Lawyer Review")
	MAINTENANCE_LAWYER_REVIEW("Maintenance Lawyer Review"),

	@SerializedName("Maintenance Client Wait")
	MAINTENANCE_CLIENT_WAIT("Maintenance Client Wait"),

	@SerializedName("Waiting on External")
	WAITING_ON_EXTERNAL("Waiting on External"),

	@SerializedName("Closing Review")
	CLOSING_REVIEW("Closing Review"),

	@SerializedName("Closed")
	CLOSED("Closed"),

	@SerializedName("Archived")
	ARCHIVED("Archived");

	private final String code;

	AdminState(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static Optional<AdminState> fromValue(String value) {
		if ("Open".equals(value)) {
			return Optional.of(OPENED);
		}
		for (AdminState state : values()) {
			if (state.code.equals(value)) {
				return Optional.of(state);
			}
		}
		return Optional.empty();
	}
}
