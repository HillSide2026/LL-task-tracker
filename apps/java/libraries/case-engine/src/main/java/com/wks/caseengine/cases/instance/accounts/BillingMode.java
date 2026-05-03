package com.wks.caseengine.cases.instance.accounts;

import java.io.Serializable;
import java.util.Optional;

import com.google.gson.annotations.SerializedName;

public enum BillingMode implements Serializable {

	@SerializedName("FLAT_FEE")
	FLAT_FEE("FLAT_FEE"),

	@SerializedName("HOURLY_CARD")
	HOURLY_CARD("HOURLY_CARD"),

	@SerializedName("HOURLY_RETAINER")
	HOURLY_RETAINER("HOURLY_RETAINER"),

	@SerializedName("SUBSCRIPTION_CARD")
	SUBSCRIPTION_CARD("SUBSCRIPTION_CARD"),

	@SerializedName("SUBSCRIPTION_RETAINER")
	SUBSCRIPTION_RETAINER("SUBSCRIPTION_RETAINER"),

	@SerializedName("COUNSEL_HOURLY")
	COUNSEL_HOURLY("COUNSEL_HOURLY"),

	@SerializedName("COUNSEL_RETAINER")
	COUNSEL_RETAINER("COUNSEL_RETAINER"),

	@SerializedName("COUNSEL_FLAT_FEE")
	COUNSEL_FLAT_FEE("COUNSEL_FLAT_FEE"),

	@SerializedName("COUNSEL_RECURRING")
	COUNSEL_RECURRING("COUNSEL_RECURRING");

	private final String code;

	BillingMode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static Optional<BillingMode> fromValue(String value) {
		for (BillingMode billingMode : values()) {
			if (billingMode.code.equals(value)) {
				return Optional.of(billingMode);
			}
		}
		return Optional.empty();
	}
}
