package com.wks.caseengine.cases.instance.accounts;

import java.io.Serializable;
import java.util.Optional;

import com.google.gson.annotations.SerializedName;

public enum AccountsWorkPriority implements Serializable {

	@SerializedName("LOW")
	LOW("LOW"),
	@SerializedName("NORMAL")
	NORMAL("NORMAL"),
	@SerializedName("HIGH")
	HIGH("HIGH"),
	@SerializedName("URGENT")
	URGENT("URGENT");

	private final String code;

	AccountsWorkPriority(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static Optional<AccountsWorkPriority> fromValue(String value) {
		for (AccountsWorkPriority priority : values()) {
			if (priority.code.equals(value)) {
				return Optional.of(priority);
			}
		}
		return Optional.empty();
	}
}
