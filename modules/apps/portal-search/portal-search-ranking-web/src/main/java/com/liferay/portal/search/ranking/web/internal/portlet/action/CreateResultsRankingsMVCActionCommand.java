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
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.ranking.web.internal.constants.ResultsRankingPortletKeys;
import com.liferay.portal.search.ranking.web.internal.index.ResultsRanking;
import com.liferay.portal.search.ranking.web.internal.index.ResultsRankingIndexer;

import java.util.Date;

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
		"mvc.command.name=createResultsRankingsEntry"
	},
	service = MVCActionCommand.class
)
public class CreateResultsRankingsMVCActionCommand
	extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		String cmd = ParamUtil.getString(actionRequest, Constants.CMD);

		String[] aliases = StringUtil.split(
			ParamUtil.getString(actionRequest, "aliases"), StringPool.SPACE);
		Date displayDate = null;
		//ParamUtil.getDate(actionRequest, "displayDate", null);
		String[] hiddenDocuments = ParamUtil.getStringValues(
			actionRequest, "hiddenDocuments");

		String index = ParamUtil.getString(actionRequest, "index-name");

		long companyId = portal.getCompanyId(actionRequest);

		if (Validator.isBlank(index)) {
			index = "liferay-" + companyId;
		}

		String keywords = ParamUtil.getString(actionRequest, "search-term");
		Date modifiedDate = new Date();
		String[] pinnedDocuments = ParamUtil.getStringValues(
			actionRequest, "pinnedDocuments");
		int status = ParamUtil.getInteger(actionRequest, "status");
		String uid = ParamUtil.getString(actionRequest, "uid");

		ResultsRanking resultsRanking = new ResultsRanking();

		resultsRanking.setAliases(aliases);
		resultsRanking.setDisplayDate(displayDate);
		resultsRanking.setHiddenDocuments(hiddenDocuments);
		resultsRanking.setIndex(index);
		resultsRanking.setKeywords(keywords);
		resultsRanking.setModifiedDate(modifiedDate);
		resultsRanking.setPinnedDocuments(pinnedDocuments);
		resultsRanking.setStatus(status);
		resultsRanking.setUid(uid);

		if (cmd.equals("add")) {
			boolean exists = resultsRankingIndexer.exists(resultsRanking);

			if (!exists) {
				resultsRankingIndexer.addResultsRanking(resultsRanking);
			}
			else {
				throw new Exception(
					"ranking already exists for keywords/aliases");
			}
		}
		else if (cmd.equals("update")) {
			resultsRankingIndexer.updateResultsRanking(resultsRanking);
		}
		else if (cmd.equals("delete")) {
			resultsRankingIndexer.deleteResultsRanking(resultsRanking);
		}

		String redirect = ParamUtil.getString(actionRequest, "redirect");

		PortletConfig portletConfig = (PortletConfig)actionRequest.getAttribute(
			JavaConstants.JAVAX_PORTLET_CONFIG);

		LiferayPortletURL portletURL = PortletURLFactoryUtil.create(
			actionRequest, portletConfig.getPortletName(),
			PortletRequest.RENDER_PHASE);

		portletURL.setParameter(
			"mvcRenderCommandName", "editResultsRankingsEntry");
		portletURL.setParameter(Constants.CMD, Constants.UPDATE, false);
		portletURL.setParameter("redirect", redirect, false);
		portletURL.setWindowState(actionRequest.getWindowState());

		sendRedirect(actionRequest, actionResponse, portletURL.toString());
	}

	@Reference
	protected Portal portal;

	@Reference
	protected ResultsRankingIndexer resultsRankingIndexer;

}