package com.wks.caseengine.cases.instance.accounts;

import java.io.Serializable;
import java.util.Optional;

import com.google.gson.annotations.SerializedName;

public enum AccountsWorkOwnerType implements Serializable {

	@SerializedName("ACCOUNTS")
	ACCOUNTS("ACCOUNTS"),
	@SerializedName("ADMIN")
	ADMIN("ADMIN"),
	@SerializedName("LAWYER")
	LAWYER("LAWYER"),
	@SerializedName("CLIENT")
	CLIENT("CLIENT"),
	@SerializedName("INSTRUCTING_FIRM")
	INSTRUCTING_FIRM("INSTRUCTING_FIRM"),
	@SerializedName("SYSTEM")
	SYSTEM("SYSTEM");

	private final String code;

	AccountsWorkOwnerType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static Optional<AccountsWorkOwnerType> fromValue(String value) {
		for (AccountsWorkOwnerType ownerType : values()) {
			if (ownerType.code.equals(value)) {
				return Optional.of(ownerType);
			}
		}
		return Optional.empty();
	}
}
