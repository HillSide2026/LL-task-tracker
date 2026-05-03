package com.wks.caseengine.cases.instance.accounts;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountsEvent {

	private String eventType;

	private Date occurredAt;

	private String actorType;

	private String actorId;

	private String actorName;

	private String fromState;

	private String toState;

	private String fromStage;

	private String toStage;

	private String reasonCode;

	private String note;
}
