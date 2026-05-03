package com.wks.caseengine.cases.instance.admin;

import java.util.List;

public class AdminLifecycleException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final List<String> reasonCodes;

	public AdminLifecycleException(String message) {
		super(message);
		this.reasonCodes = List.of();
	}

	public AdminLifecycleException(String message, List<String> reasonCodes) {
		super(message);
		this.reasonCodes = reasonCodes != null ? List.copyOf(reasonCodes) : List.of();
	}

	public List<String> getReasonCodes() {
		return reasonCodes;
	}
}
