package com.wks.caseengine.cases.instance.admin;

import java.io.Serializable;
import java.util.Optional;

import com.google.gson.annotations.SerializedName;

public enum AdminHealth implements Serializable {

	@SerializedName("Green")
	GREEN("Green"),

	@SerializedName("Amber")
	AMBER("Amber"),

	@SerializedName("Red")
	RED("Red");

	private final String code;

	AdminHealth(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static Optional<AdminHealth> fromValue(String value) {
		for (AdminHealth health : values()) {
			if (health.code.equals(value)) {
				return Optional.of(health);
			}
		}
		return Optional.empty();
	}
}
