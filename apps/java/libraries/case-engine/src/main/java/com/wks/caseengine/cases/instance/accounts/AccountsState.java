package com.wks.caseengine.cases.instance.accounts;

import java.io.Serializable;
import java.util.Optional;

import com.google.gson.annotations.SerializedName;

public enum AccountsState implements Serializable {

	@SerializedName("AWAITING_BILLING_SETUP")
	AWAITING_BILLING_SETUP("AWAITING_BILLING_SETUP"),
	@SerializedName("AWAITING_PAYMENT_METHOD")
	AWAITING_PAYMENT_METHOD("AWAITING_PAYMENT_METHOD"),
	@SerializedName("AWAITING_RETAINER_FUNDING")
	AWAITING_RETAINER_FUNDING("AWAITING_RETAINER_FUNDING"),
	@SerializedName("AWAITING_ENGAGEMENT_TERMS")
	AWAITING_ENGAGEMENT_TERMS("AWAITING_ENGAGEMENT_TERMS"),
	@SerializedName("READY_FOR_ACTIVATION")
	READY_FOR_ACTIVATION("READY_FOR_ACTIVATION"),
	@SerializedName("ACTIVE_CURRENT")
	ACTIVE_CURRENT("ACTIVE_CURRENT"),
	@SerializedName("INVOICE_ISSUED")
	INVOICE_ISSUED("INVOICE_ISSUED"),
	@SerializedName("PAYMENT_PENDING")
	PAYMENT_PENDING("PAYMENT_PENDING"),
	@SerializedName("TRUST_DRAWDOWN_PENDING")
	TRUST_DRAWDOWN_PENDING("TRUST_DRAWDOWN_PENDING"),
	@SerializedName("PAST_DUE")
	PAST_DUE("PAST_DUE"),
	@SerializedName("IN_COLLECTIONS")
	IN_COLLECTIONS("IN_COLLECTIONS"),
	@SerializedName("PAYMENT_ARRANGEMENT_ACTIVE")
	PAYMENT_ARRANGEMENT_ACTIVE("PAYMENT_ARRANGEMENT_ACTIVE"),
	@SerializedName("FINAL_RECONCILIATION_PENDING")
	FINAL_RECONCILIATION_PENDING("FINAL_RECONCILIATION_PENDING"),
	@SerializedName("CLOSED")
	CLOSED("CLOSED");

	private final String code;

	AccountsState(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static Optional<AccountsState> fromValue(String value) {
		for (AccountsState state : values()) {
			if (state.code.equals(value)) {
				return Optional.of(state);
			}
		}
		return Optional.empty();
	}
}
