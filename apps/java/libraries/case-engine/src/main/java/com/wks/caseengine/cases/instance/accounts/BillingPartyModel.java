package com.wks.caseengine.cases.instance.accounts;

import java.io.Serializable;
import java.util.Optional;

import com.google.gson.annotations.SerializedName;

public enum BillingPartyModel implements Serializable {

	@SerializedName("DIRECT_CLIENT")
	DIRECT_CLIENT("DIRECT_CLIENT"),

	@SerializedName("INSTRUCTING_FIRM")
	INSTRUCTING_FIRM("INSTRUCTING_FIRM"),

	@SerializedName("THIRD_PARTY_PAYOR")
	THIRD_PARTY_PAYOR("THIRD_PARTY_PAYOR");

	private final String code;

	BillingPartyModel(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static Optional<BillingPartyModel> fromValue(String value) {
		for (BillingPartyModel model : values()) {
			if (model.code.equals(value)) {
				return Optional.of(model);
			}
		}
		return Optional.empty();
	}
}
