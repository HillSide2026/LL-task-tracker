package com.wks.caseengine.cases.instance.accounts;

import java.io.Serializable;
import java.util.Optional;

import com.google.gson.annotations.SerializedName;

public enum AccountsLifecycleStage implements Serializable {

	@SerializedName("SETUP")
	SETUP("SETUP"),

	@SerializedName("ACTIVE")
	ACTIVE("ACTIVE"),

	@SerializedName("EXCEPTION")
	EXCEPTION("EXCEPTION"),

	@SerializedName("CLOSED")
	CLOSED("CLOSED");

	private final String code;

	AccountsLifecycleStage(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static Optional<AccountsLifecycleStage> fromValue(String value) {
		for (AccountsLifecycleStage stage : values()) {
			if (stage.code.equals(value)) {
				return Optional.of(stage);
			}
		}
		return Optional.empty();
	}
}
