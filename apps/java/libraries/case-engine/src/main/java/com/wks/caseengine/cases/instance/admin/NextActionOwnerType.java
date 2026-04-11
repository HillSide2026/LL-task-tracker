package com.wks.caseengine.cases.instance.admin;

import java.io.Serializable;
import java.util.Optional;

import com.google.gson.annotations.SerializedName;

public enum NextActionOwnerType implements Serializable {

	@SerializedName("Admin")
	ADMIN("Admin"),

	@SerializedName("Lawyer")
	LAWYER("Lawyer"),

	@SerializedName("Client")
	CLIENT("Client"),

	@SerializedName("External")
	EXTERNAL("External"),

	@SerializedName("System")
	SYSTEM("System");

	private final String code;

	NextActionOwnerType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static Optional<NextActionOwnerType> fromValue(String value) {
		for (NextActionOwnerType ownerType : values()) {
			if (ownerType.code.equals(value)) {
				return Optional.of(ownerType);
			}
		}
		return Optional.empty();
	}
}
