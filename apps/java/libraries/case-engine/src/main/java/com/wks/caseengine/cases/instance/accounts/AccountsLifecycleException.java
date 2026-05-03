package com.wks.caseengine.cases.instance.accounts;

import java.util.List;

public class AccountsLifecycleException extends RuntimeException {

	private final List<String> reasonCodes;

	public AccountsLifecycleException(String message) {
		super(message);
		this.reasonCodes = List.of();
	}

	public AccountsLifecycleException(String message, Throwable cause) {
		super(message, cause);
		this.reasonCodes = List.of();
	}

	public AccountsLifecycleException(String message, List<String> reasonCodes) {
		super(message);
		this.reasonCodes = reasonCodes != null ? List.copyOf(reasonCodes) : List.of();
	}

	public List<String> getReasonCodes() {
		return reasonCodes;
	}
}
