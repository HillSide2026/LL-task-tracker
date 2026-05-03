package com.wks.caseengine.cases.instance.accounts;

import java.io.Serializable;
import java.util.Optional;

import com.google.gson.annotations.SerializedName;

public enum AccountsPolicyProfile implements Serializable {

	@SerializedName("FLAT_FEE_PROFILE")
	FLAT_FEE_PROFILE("FLAT_FEE_PROFILE"),

	@SerializedName("HOURLY_CREDIT_CARD_PROFILE")
	HOURLY_CREDIT_CARD_PROFILE("HOURLY_CREDIT_CARD_PROFILE"),

	@SerializedName("HOURLY_RETAINER_PROFILE")
	HOURLY_RETAINER_PROFILE("HOURLY_RETAINER_PROFILE"),

	@SerializedName("SUBSCRIPTION_CREDIT_CARD_PROFILE")
	SUBSCRIPTION_CREDIT_CARD_PROFILE("SUBSCRIPTION_CREDIT_CARD_PROFILE"),

	@SerializedName("SUBSCRIPTION_RETAINER_PROFILE")
	SUBSCRIPTION_RETAINER_PROFILE("SUBSCRIPTION_RETAINER_PROFILE"),

	@SerializedName("COUNSEL_PROFILE")
	COUNSEL_PROFILE("COUNSEL_PROFILE");

	private final String code;

	AccountsPolicyProfile(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static Optional<AccountsPolicyProfile> fromValue(String value) {
		for (AccountsPolicyProfile profile : values()) {
			if (profile.code.equals(value)) {
				return Optional.of(profile);
			}
		}
		return Optional.empty();
	}
}
