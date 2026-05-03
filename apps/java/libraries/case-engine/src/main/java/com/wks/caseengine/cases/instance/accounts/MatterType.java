package com.wks.caseengine.cases.instance.accounts;

import java.io.Serializable;
import java.util.Optional;

import com.google.gson.annotations.SerializedName;

public enum MatterType implements Serializable {

	@SerializedName("FLAT_FEE")
	FLAT_FEE("FLAT_FEE"),

	@SerializedName("HOURLY_CREDIT_CARD")
	HOURLY_CREDIT_CARD("HOURLY_CREDIT_CARD"),

	@SerializedName("HOURLY_RETAINER")
	HOURLY_RETAINER("HOURLY_RETAINER"),

	@SerializedName("SUBSCRIPTION_CREDIT_CARD")
	SUBSCRIPTION_CREDIT_CARD("SUBSCRIPTION_CREDIT_CARD"),

	@SerializedName("SUBSCRIPTION_RETAINER")
	SUBSCRIPTION_RETAINER("SUBSCRIPTION_RETAINER"),

	@SerializedName("COUNSEL")
	COUNSEL("COUNSEL");

	private final String code;

	MatterType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static Optional<MatterType> fromValue(String value) {
		for (MatterType matterType : values()) {
			if (matterType.code.equals(value)) {
				return Optional.of(matterType);
			}
		}
		return Optional.empty();
	}
}
