/*
 * WKS Platform - Open-Source Project
 * 
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 * 
 * WKS Platform is licensed under the MIT License.
 * 
 * © 2021 WKS Power. All rights reserved.
 * 
 * For licensing information, see the LICENSE file in the root directory of the project.
 */
package com.wks.caseengine.cases.instance.service;

import java.util.List;

import com.wks.caseengine.cases.instance.CaseComment;
import com.wks.caseengine.cases.instance.CaseDocument;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceFilter;
import com.wks.caseengine.cases.instance.accounts.AccountsEvent;
import com.wks.caseengine.cases.instance.accounts.AccountsReadinessEvaluation;
import com.wks.caseengine.cases.instance.accounts.AccountsTransition;
import com.wks.caseengine.cases.instance.accounts.AccountsTransitionRequest;
import com.wks.caseengine.cases.instance.accounts.AccountsWorkSummary;
import com.wks.caseengine.cases.instance.admin.AdminTransition;
import com.wks.caseengine.cases.instance.admin.AdminTransitionRequest;
import com.wks.caseengine.pagination.PageResult;

public interface CaseInstanceService {

	PageResult<CaseInstance> find(CaseInstanceFilter filters);

	CaseInstance get(final String businessKey);

	CaseInstance startWithValues(final CaseInstance caseInstance);

	void saveWithValues(final CaseInstance caseInstance);

	CaseInstance patch(final String businessKey, final CaseInstance caseInstance);

	CaseInstance transition(final String businessKey, final AdminTransition transition, final AdminTransitionRequest request);

	CaseInstance getAccounts(final String businessKey);

	List<AccountsEvent> getAccountsHistory(final String businessKey);

	AccountsReadinessEvaluation getAccountsReadiness(final String businessKey);

	AccountsReadinessEvaluation evaluateAccountsReadiness(final String businessKey);

	PageResult<CaseInstance> findAccountsWork(CaseInstanceFilter filters);

	AccountsWorkSummary getAccountsWorkSummary(CaseInstanceFilter filters);

	CaseInstance transitionAccounts(final String businessKey, final AccountsTransition transition,
			final AccountsTransitionRequest request);

	void delete(final String businessKey);

	void saveDocument(final String businessKey, final CaseDocument document);

	void saveComment(final String businessKey, final CaseComment comment);

	void updateComment(final String businessKey, final String commentId, final String body);

	void deleteComment(final String businessKey, final String commentId);

}
