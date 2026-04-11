package com.wks.caseengine.cases.instance.admin;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminControlEvaluation {

	private String adminHealth;

	@Builder.Default
	private List<String> healthReasonCodes = new ArrayList<>();

	private String healthEvaluatedAt;

	private String staleSince;

	private Boolean malformedCase;
}
