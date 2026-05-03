package com.wks.caseengine.cases.instance.accounts;

public final class AccountsWorkQueue {

	public static final String ACCOUNTS_READY = "ACCOUNTS_READY";
	public static final String ACCOUNTS_NOT_READY = "ACCOUNTS_NOT_READY";
	public static final String ACCOUNTS_BLOCKED = "ACCOUNTS_BLOCKED";
	public static final String MISSING_PAYMENT_METHOD = "MISSING_PAYMENT_METHOD";
	public static final String MISSING_RETAINER = "MISSING_RETAINER";
	public static final String MISSING_SUBSCRIPTION_SETUP = "MISSING_SUBSCRIPTION_SETUP";
	public static final String COUNSEL_BILLING_SETUP = "COUNSEL_BILLING_SETUP";
	public static final String MALFORMED_ACCOUNTS_CONFIGURATION = "MALFORMED_ACCOUNTS_CONFIGURATION";

	private AccountsWorkQueue() {
	}
}
