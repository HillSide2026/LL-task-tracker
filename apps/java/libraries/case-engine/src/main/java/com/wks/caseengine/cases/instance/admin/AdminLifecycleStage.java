package com.wks.caseengine.cases.instance.admin;

import java.io.Serializable;
import java.util.Optional;

import com.google.gson.annotations.SerializedName;

public enum AdminLifecycleStage implements Serializable {

	@SerializedName("Onboarding")
	ONBOARDING("Onboarding"),

	@SerializedName("Opening")
	OPENING("Opening"),

	@SerializedName("Open")
	OPEN("Open"),

	@SerializedName("Closing")
	CLOSING("Closing"),

	@SerializedName("Archived")
	ARCHIVED("Archived");

	private final String code;

	AdminLifecycleStage(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static Optional<AdminLifecycleStage> fromValue(String value) {
		for (AdminLifecycleStage stage : values()) {
			if (stage.code.equals(value)) {
				return Optional.of(stage);
			}
		}
		return Optional.empty();
	}
}
