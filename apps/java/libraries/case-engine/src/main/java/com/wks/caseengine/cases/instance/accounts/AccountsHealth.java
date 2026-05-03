package com.wks.caseengine.cases.instance.accounts;

import java.io.Serializable;
import java.util.Optional;

import com.google.gson.annotations.SerializedName;

public enum AccountsHealth implements Serializable {

	@SerializedName("GREEN")
	GREEN("GREEN"),
	@SerializedName("AMBER")
	AMBER("AMBER"),
	@SerializedName("RED")
	RED("RED");

	private final String code;

	AccountsHealth(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static Optional<AccountsHealth> fromValue(String value) {
		for (AccountsHealth health : values()) {
			if (health.code.equals(value)) {
				return Optional.of(health);
			}
		}
		return Optional.empty();
	}
}
