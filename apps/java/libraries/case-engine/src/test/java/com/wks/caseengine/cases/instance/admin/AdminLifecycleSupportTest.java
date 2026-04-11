package com.wks.caseengine.cases.instance.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.wks.caseengine.cases.instance.CaseInstance;

class AdminLifecycleSupportTest {

	@Test
	void shouldNormalizeLegacyOpenStateAndResumeTarget() {
		CaseInstance caseInstance = CaseInstance.builder().caseDefinitionId(AdminLifecycleSupport.CASE_DEFINITION_ID)
				.adminState("Open").resumeToState("Open").build();

		boolean changed = AdminLifecycleSupport.normalizeLegacyState(caseInstance);

		assertTrue(changed);
		assertEquals(AdminState.OPENED.getCode(), caseInstance.getAdminState());
		assertEquals(AdminState.OPENED.getCode(), caseInstance.getResumeToState());
	}

	@Test
	void shouldFlagInvalidMaintenanceResumeTargetAsMalformed() {
		CaseInstance caseInstance = CaseInstance.builder().caseDefinitionId(AdminLifecycleSupport.CASE_DEFINITION_ID)
				.adminState(AdminState.MAINTENANCE_CLIENT_WAIT.getCode()).stage("Open").nextActionSummary("Await response")
				.nextActionOwnerType(NextActionOwnerType.CLIENT.getCode()).waitingSince("2026-04-01")
				.expectedResponseAt("2026-04-15").resumeToState(AdminState.READY_TO_OPEN.getCode()).build();

		AdminControlEvaluation evaluation = AdminLifecycleSupport.evaluate(caseInstance);

		assertEquals(AdminHealth.RED.getCode(), evaluation.getAdminHealth());
		assertTrue(evaluation.getMalformedCase());
		assertTrue(evaluation.getHealthReasonCodes().contains(AdminHealthReasonCode.INVALID_RESUME_TARGET.getCode()));
		assertTrue(evaluation.getHealthReasonCodes().contains(AdminHealthReasonCode.CONTROL_INCOMPLETE.getCode()));
	}

	@Test
	void shouldFlagMissingExternalPartyRefForExternalWait() {
		CaseInstance caseInstance = CaseInstance.builder().caseDefinitionId(AdminLifecycleSupport.CASE_DEFINITION_ID)
				.adminState(AdminState.WAITING_ON_EXTERNAL.getCode()).stage("Open").nextActionSummary("Await external response")
				.nextActionOwnerType(NextActionOwnerType.EXTERNAL.getCode()).waitingSince("2026-04-01")
				.expectedResponseAt("2026-04-15").resumeToState(AdminState.ACTIVE.getCode()).build();

		AdminControlEvaluation evaluation = AdminLifecycleSupport.evaluate(caseInstance);

		assertEquals(AdminHealth.RED.getCode(), evaluation.getAdminHealth());
		assertTrue(evaluation.getMalformedCase());
		assertTrue(evaluation.getHealthReasonCodes().contains(AdminHealthReasonCode.MISSING_EXTERNAL_PARTY_REF.getCode()));
	}
}
