package com.wks.caseengine.cases.instance.accounts;

import java.io.Serializable;
import java.util.Optional;

import com.google.gson.annotations.SerializedName;

public enum AccountsReadinessStatus implements Serializable {

	@SerializedName("NOT_EVALUATED")
	NOT_EVALUATED("NOT_EVALUATED"),

	@SerializedName("NOT_READY")
	NOT_READY("NOT_READY"),

	@SerializedName("READY")
	READY("READY"),

	@SerializedName("BLOCKED")
	BLOCKED("BLOCKED");

	private final String code;

	AccountsReadinessStatus(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static Optional<AccountsReadinessStatus> fromValue(String value) {
		for (AccountsReadinessStatus status : values()) {
			if (status.code.equals(value)) {
				return Optional.of(status);
			}
		}
		return Optional.empty();
	}
}
