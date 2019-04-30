/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.search.ranking.web.internal.portlet.action;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.portlet.LiferayPortletURL;
import com.liferay.portal.kernel.portlet.PortletURLFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.search.ranking.web.internal.constants.ResultsRankingPortletKeys;
import com.liferay.portal.search.ranking.web.internal.index.ResultsRanking;
import com.liferay.portal.search.ranking.web.internal.index.ResultsRankingIndexer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Kevin Tan
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + ResultsRankingPortletKeys.RESULTS_RANKING,
		"mvc.command.name=/results_ranking/edit"
	},
	service = MVCActionCommand.class
)
public class EditResultsRankingMVCActionCommand extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		String cmd = ParamUtil.getString(actionRequest, Constants.CMD);

		String redirect = ParamUtil.getString(actionRequest, "redirect");

		String index = ParamUtil.getString(actionRequest, "index-name");

		if (Validator.isBlank(index)) {
			long companyId = portal.getCompanyId(actionRequest);

			index = "liferay-" + companyId;
		}

		if (cmd.equals(Constants.ADD)) {
			if (rankingExistsForKeyword(actionRequest)) {
				SessionErrors.add(actionRequest, Exception.class);

				actionResponse.setRenderParameter("mvcPath", "/error.jsp");

				return;
			}

			String keywords = ParamUtil.getString(actionRequest, "keywords");
			Date displayDate = null;
			Date modifiedDate = new Date();

			ResultsRanking resultsRanking = new ResultsRanking();

			resultsRanking.setDisplayDate(displayDate);
			resultsRanking.setIndex(index);
			resultsRanking.setKeywords(keywords);
			resultsRanking.setModifiedDate(modifiedDate);
			resultsRanking.setStatus(WorkflowConstants.STATUS_DRAFT);

			resultsRankingIndexer.addResultsRanking(resultsRanking);

			redirect = getSaveAndContinueRedirect(
				actionRequest, resultsRanking, redirect);
		}
		else if (cmd.equals(Constants.UPDATE)) {
			if (rankingExistsForAliases(actionRequest)) {
				SessionErrors.add(actionRequest, Exception.class);

				actionResponse.setRenderParameter("mvcPath", "/error.jsp");

				return;
			}

			String resultsRankingUid =
				ParamUtil.getString(actionRequest, "uid");

			ResultsRanking resultsRanking =
				resultsRankingIndexer.getResultsRanking(resultsRankingUid);

			String[] aliases =
				ParamUtil.getStringValues(actionRequest, "aliases");

			resultsRanking.setAliases(aliases);

			String[] hiddenAdded = ParamUtil.getStringValues(
				actionRequest, "hiddenAdded");
			String[] hiddenRemoved = ParamUtil.getStringValues(
				actionRequest, "hiddenRemoved");

			List<String> hiddenDocuments =
				resultsRanking.getHiddenDocuments();

			if (hiddenDocuments == null) {
				hiddenDocuments = new ArrayList<>();
			}

			hiddenDocuments.addAll(ListUtil.fromArray(hiddenAdded));
			hiddenDocuments.removeAll(ListUtil.fromArray(hiddenRemoved));

			resultsRanking.setHiddenDocuments(hiddenDocuments);

			String[] pinnedAdded = ParamUtil.getStringValues(
				actionRequest, "pinnedAdded");
			String[] pinnedRemoved = ParamUtil.getStringValues(
				actionRequest, "pinnedRemoved");

			List<Map<String, String>> originalPinnedDocuments =
				resultsRanking.getPinnedDocuments();

			List<Map<String, String>> newPinnedDocuments = new ArrayList<>();

			//process pinned documents

			if (ListUtil.isNotEmpty(newPinnedDocuments)) {
				resultsRanking.setPinnedDocuments(newPinnedDocuments);
			}
			else {
				resultsRanking.setPinnedDocuments(null);
			}

			int workflowAction = ParamUtil.getInteger(
				actionRequest, "workflowAction",
				WorkflowConstants.ACTION_PUBLISH);

			if (workflowAction == WorkflowConstants.ACTION_SAVE_DRAFT) {
				// @TODO Save draft action

				resultsRanking.setStatus(WorkflowConstants.STATUS_DRAFT);
			}
			else {
				// @TODO Publish action

				resultsRanking.setStatus(WorkflowConstants.STATUS_APPROVED);
			}

			resultsRankingIndexer.updateResultsRanking(resultsRanking);
		}
		else if (cmd.equals(Constants.DELETE)) {
			String resultsRankingUid =
				ParamUtil.getString(actionRequest, "uid");

			resultsRankingIndexer.deleteResultsRanking(resultsRankingUid);
		}

		sendRedirect(actionRequest, actionResponse, redirect);
	}

	protected String getSaveAndContinueRedirect(
		ActionRequest actionRequest, ResultsRanking resultsRanking,
		String redirect)
		throws Exception {

		PortletConfig portletConfig = (PortletConfig)actionRequest.getAttribute(
			JavaConstants.JAVAX_PORTLET_CONFIG);

		LiferayPortletURL portletURL = PortletURLFactoryUtil.create(
			actionRequest, portletConfig.getPortletName(),
			PortletRequest.RENDER_PHASE);

		portletURL.setParameter(
			"mvcRenderCommandName", "editResultsRankingEntry");
		portletURL.setParameter(Constants.CMD, Constants.UPDATE, false);
		portletURL.setParameter("redirect", redirect, false);
		portletURL.setParameter(
			"aliases",
			StringUtil.merge(
				resultsRanking.getAliases(), StringPool.COMMA), false);
		portletURL.setParameter(
			"keywords", resultsRanking.getKeywords(), false);
		portletURL.setWindowState(actionRequest.getWindowState());

		return portletURL.toString();
	}

	protected boolean rankingExistsForAliases(ActionRequest actionRequest) {
		String resultsRankingUid = ParamUtil.getString(actionRequest, "uid");

		String index = ParamUtil.getString(actionRequest, "index-name");

		if (Validator.isBlank(index)) {
			long companyId = portal.getCompanyId(actionRequest);

			index = "liferay-" + companyId;
		}

		String[] aliases = ParamUtil.getStringValues(actionRequest, "aliases");

		if (ArrayUtil.isEmpty(aliases)) {
			return false;
		}

		return resultsRankingIndexer.exists(
			index, resultsRankingUid, null, ListUtil.fromArray(aliases));
	}

	protected boolean rankingExistsForKeyword(ActionRequest actionRequest) {
		String index = ParamUtil.getString(actionRequest, "index-name");

		if (Validator.isBlank(index)) {
			long companyId = portal.getCompanyId(actionRequest);

			index = "liferay-" + companyId;
		}

		String keywords = ParamUtil.getString(actionRequest, "keywords");

		return resultsRankingIndexer.exists(
			index, null, keywords, null);
	}

	@Reference
	protected Portal portal;

	@Reference
	protected ResultsRankingIndexer resultsRankingIndexer;

}