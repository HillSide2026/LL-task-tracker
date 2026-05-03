package com.wks.caseengine.cases.instance.accounts;

import java.io.Serializable;
import java.util.Optional;

public enum AccountsTransition implements Serializable {

	COMPLETE_BILLING_SETUP("completeBillingSetup"),
	AUTHORIZE_PAYMENT_METHOD("authorizePaymentMethod"),
	RECORD_RETAINER_FUNDED("recordRetainerFunded"),
	RECORD_ENGAGEMENT_TERMS_COMPLETE("recordEngagementTermsComplete"),
	ACTIVATE_ACCOUNTS("activateAccounts"),
	ISSUE_INVOICE("issueInvoice"),
	RECORD_PAYMENT_RECEIVED("recordPaymentReceived"),
	REQUEST_TRUST_DRAWDOWN("requestTrustDrawdown"),
	APPROVE_TRUST_DRAWDOWN("approveTrustDrawdown"),
	FLAG_PAST_DUE("flagPastDue"),
	ESCALATE_TO_COLLECTIONS("escalateToCollections"),
	RECORD_PAYMENT_ARRANGEMENT("recordPaymentArrangement"),
	RETURN_TO_CURRENT("returnToCurrent"),
	ISSUE_FINAL_ACCOUNT_STATEMENT("issueFinalAccountStatement"),
	RECONCILE_FINAL_BALANCE("reconcileFinalBalance"),
	CLOSE_ACCOUNT("closeAccount");

	private final String code;

	AccountsTransition(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static Optional<AccountsTransition> fromValue(String value) {
		for (AccountsTransition transition : values()) {
			if (transition.code.equals(value)) {
				return Optional.of(transition);
			}
		}
		return Optional.empty();
	}
}
