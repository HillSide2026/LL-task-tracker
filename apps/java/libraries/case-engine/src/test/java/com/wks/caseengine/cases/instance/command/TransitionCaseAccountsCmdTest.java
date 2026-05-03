package com.wks.caseengine.cases.instance.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import com.google.gson.GsonBuilder;
import com.wks.api.security.context.SecurityContextTenantHolderImpl;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.accounts.AccountsEventType;
import com.wks.caseengine.cases.instance.accounts.AccountsHealthReasonCode;
import com.wks.caseengine.cases.instance.accounts.AccountsLifecycleException;
import com.wks.caseengine.cases.instance.accounts.AccountsLifecycleStage;
import com.wks.caseengine.cases.instance.accounts.AccountsPolicyProfile;
import com.wks.caseengine.cases.instance.accounts.AccountsState;
import com.wks.caseengine.cases.instance.accounts.AccountsTransition;
import com.wks.caseengine.cases.instance.accounts.AccountsTransitionRequest;
import com.wks.caseengine.cases.instance.accounts.BillingMode;
import com.wks.caseengine.cases.instance.accounts.MatterType;
import com.wks.caseengine.cases.instance.repository.CaseInstanceRepository;
import com.wks.caseengine.command.CommandContext;

@ExtendWith(MockitoExtension.class)
class TransitionCaseAccountsCmdTest {

	@Mock
	private CaseInstanceRepository caseInstanceRepository;

	@Captor
	private ArgumentCaptor<CaseInstance> caseInstanceCaptor;

	private CommandContext commandContext;

	@BeforeEach
	void setup() {
		commandContext = new CommandContext();
		SecurityContextTenantHolderImpl tenantHolder = new SecurityContextTenantHolderImpl();
		tenantHolder.setTenantId("wks");
		tenantHolder.setUserId("accounts-sub");
		commandContext.setSecurityContextTenantHolder(tenantHolder);
		commandContext.setCaseInstanceRepository(caseInstanceRepository);
		commandContext.setGsonBuilder(new GsonBuilder());
		setJwtSecurityContext("accounts-sub", List.of("accounts_manager"));
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void shouldInitializeAccountsShellAndRejectUnsupportedTransition() throws Exception {
		when(caseInstanceRepository.get("BK-1")).thenReturn(completeFlatFeeCase().build());

		AccountsLifecycleException exception = assertThrows(AccountsLifecycleException.class,
				() -> new TransitionCaseAccountsCmd("BK-1", AccountsTransition.COMPLETE_BILLING_SETUP,
						AccountsTransitionRequest.builder().note("Phase 3 policy").build()).execute(commandContext));

		verify(caseInstanceRepository).update(eq("BK-1"), caseInstanceCaptor.capture());
		CaseInstance persistedCase = caseInstanceCaptor.getValue();
		assertEquals("Accounts transition is not valid for this matter type", exception.getMessage());
		assertTrue(exception.getReasonCodes().contains(AccountsHealthReasonCode.INVALID_ACCOUNTS_TRANSITION.getCode()));
		assertEquals(AccountsLifecycleStage.SETUP.getCode(), persistedCase.getAccountsStage());
		assertEquals(AccountsState.AWAITING_BILLING_SETUP.getCode(), persistedCase.getAccountsState());
		assertEquals(false, persistedCase.getAccountsMalformedCase());
		assertEquals(1, persistedCase.getAccountsEvents().size());
		assertEquals(AccountsEventType.ACCOUNTS_INITIALIZED.getCode(),
				persistedCase.getAccountsEvents().get(0).getEventType());
	}

	@Test
	void shouldRejectActivateAccountsWhenMatterTypeIsMissing() throws Exception {
		when(caseInstanceRepository.get("BK-1")).thenReturn(CaseInstance.builder().businessKey("BK-1").build());

		AccountsLifecycleException exception = assertThrows(AccountsLifecycleException.class,
				() -> new TransitionCaseAccountsCmd("BK-1", AccountsTransition.ACTIVATE_ACCOUNTS,
						AccountsTransitionRequest.builder().build()).execute(commandContext));

		verify(caseInstanceRepository).update(eq("BK-1"), caseInstanceCaptor.capture());
		assertEquals("Accounts transition is not valid for this matter type", exception.getMessage());
		assertTrue(exception.getReasonCodes().contains(AccountsHealthReasonCode.MISSING_MATTER_TYPE.getCode()));
		assertEquals(AccountsLifecycleStage.SETUP.getCode(), caseInstanceCaptor.getValue().getAccountsStage());
		assertEquals(AccountsState.AWAITING_BILLING_SETUP.getCode(), caseInstanceCaptor.getValue().getAccountsState());
		assertFalse(AccountsState.ACTIVE_CURRENT.getCode().equals(caseInstanceCaptor.getValue().getAccountsState()));
	}

	@Test
	void shouldRejectActivateAccountsWhenPolicyRequirementsAreMissing() throws Exception {
		when(caseInstanceRepository.get("BK-1")).thenReturn(CaseInstance.builder().businessKey("BK-1")
				.matterType(MatterType.FLAT_FEE.getCode()).billingMode(BillingMode.FLAT_FEE.getCode()).build());

		AccountsLifecycleException exception = assertThrows(AccountsLifecycleException.class,
				() -> new TransitionCaseAccountsCmd("BK-1", AccountsTransition.ACTIVATE_ACCOUNTS,
						AccountsTransitionRequest.builder().build()).execute(commandContext));

		verify(caseInstanceRepository).update(eq("BK-1"), caseInstanceCaptor.capture());
		assertEquals("Accounts activation blocked by policy", exception.getMessage());
		assertTrue(exception.getReasonCodes().contains(AccountsHealthReasonCode.MISSING_BILLING_SETUP.getCode()));
		assertTrue(exception.getReasonCodes().contains(AccountsHealthReasonCode.MISSING_FLAT_FEE_SETUP.getCode()));
		assertEquals(AccountsPolicyProfile.FLAT_FEE_PROFILE.getCode(), caseInstanceCaptor.getValue().getAccountsProfile());
		assertFalse(AccountsState.ACTIVE_CURRENT.getCode().equals(caseInstanceCaptor.getValue().getAccountsState()));
	}

	@Test
	void shouldActivateAccountsWhenPolicyPasses() throws Exception {
		when(caseInstanceRepository.get("BK-1")).thenReturn(completeFlatFeeCase().build());

		CaseInstance result = new TransitionCaseAccountsCmd("BK-1", AccountsTransition.ACTIVATE_ACCOUNTS,
				AccountsTransitionRequest.builder().note("Ready").build()).execute(commandContext);

		verify(caseInstanceRepository).update(eq("BK-1"), caseInstanceCaptor.capture());
		assertEquals(AccountsLifecycleStage.ACTIVE.getCode(), result.getAccountsStage());
		assertEquals(AccountsState.ACTIVE_CURRENT.getCode(), result.getAccountsState());
		assertEquals(AccountsPolicyProfile.FLAT_FEE_PROFILE.getCode(), result.getAccountsProfile());
		assertTrue(result.getAccountsEvents().stream()
				.anyMatch(event -> AccountsEventType.ACCOUNTS_ACTIVATED.getCode().equals(event.getEventType())));
	}

	@Test
	void shouldRejectActivateAccountsWhenExistingProfileConflictsWithMatterType() throws Exception {
		when(caseInstanceRepository.get("BK-1")).thenReturn(completeFlatFeeCase()
				.accountsProfile(AccountsPolicyProfile.COUNSEL_PROFILE.getCode()).build());

		AccountsLifecycleException exception = assertThrows(AccountsLifecycleException.class,
				() -> new TransitionCaseAccountsCmd("BK-1", AccountsTransition.ACTIVATE_ACCOUNTS,
						AccountsTransitionRequest.builder().build()).execute(commandContext));

		verify(caseInstanceRepository).update(eq("BK-1"), caseInstanceCaptor.capture());
		assertTrue(exception.getReasonCodes().contains(AccountsHealthReasonCode.MALFORMED_CONFIGURATION.getCode()));
		assertEquals(AccountsPolicyProfile.COUNSEL_PROFILE.getCode(), caseInstanceCaptor.getValue().getAccountsProfile());
		assertFalse(AccountsState.ACTIVE_CURRENT.getCode().equals(caseInstanceCaptor.getValue().getAccountsState()));
	}

	@Test
	void shouldRejectRepeatedActivationWithoutAppendingDuplicateEvent() throws Exception {
		when(caseInstanceRepository.get("BK-1")).thenReturn(completeFlatFeeCase()
				.accountsStage(AccountsLifecycleStage.ACTIVE.getCode())
				.accountsState(AccountsState.ACTIVE_CURRENT.getCode()).build());

		AccountsLifecycleException exception = assertThrows(AccountsLifecycleException.class,
				() -> new TransitionCaseAccountsCmd("BK-1", AccountsTransition.ACTIVATE_ACCOUNTS,
						AccountsTransitionRequest.builder().build()).execute(commandContext));

		verify(caseInstanceRepository).update(eq("BK-1"), caseInstanceCaptor.capture());
		assertTrue(exception.getReasonCodes().contains(AccountsHealthReasonCode.INVALID_ACCOUNTS_TRANSITION.getCode()));
		assertEquals(0, caseInstanceCaptor.getValue().getAccountsEvents().size());
	}

	@Test
	void shouldRejectUnauthorizedTransitionWithoutPersisting() throws Exception {
		setJwtSecurityContext("client-sub", List.of("client_case"));

		assertThrows(AccountsLifecycleException.class,
				() -> new TransitionCaseAccountsCmd("BK-1", AccountsTransition.COMPLETE_BILLING_SETUP,
						AccountsTransitionRequest.builder().build()).execute(commandContext));

		verify(caseInstanceRepository, never()).get("BK-1");
		verify(caseInstanceRepository, never()).update(eq("BK-1"), org.mockito.Mockito.any());
	}

	private void setJwtSecurityContext(String sub, List<String> roles) {
		Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(300), Map.of("alg", "none"),
				Map.of("sub", sub, "realm_access", Map.of("roles", roles)));
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken("user", jwt, List.of()));
	}

	private CaseInstance.CaseInstanceBuilder completeFlatFeeCase() {
		return CaseInstance.builder().businessKey("BK-1").matterType(MatterType.FLAT_FEE.getCode())
				.billingSetupComplete(true).flatFeeAmount("2500").billingMode(BillingMode.FLAT_FEE.getCode());
	}
}
