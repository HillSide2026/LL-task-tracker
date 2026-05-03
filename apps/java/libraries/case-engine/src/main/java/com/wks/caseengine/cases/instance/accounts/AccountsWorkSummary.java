package com.wks.caseengine.cases.instance.accounts;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AccountsWorkSummary {

	private long total;

	private long blocked;

	private long dueOrOverdue;

	private long upcoming;

	@Builder.Default
	private Map<String, Long> byQueue = new HashMap<>();

	@Builder.Default
	private Map<String, Long> byOwner = new HashMap<>();

	@Builder.Default
	private Map<String, Long> byReadinessStatus = new HashMap<>();
}
