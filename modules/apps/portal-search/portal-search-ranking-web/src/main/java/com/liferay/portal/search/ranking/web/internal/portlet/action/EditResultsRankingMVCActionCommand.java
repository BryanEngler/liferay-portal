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
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.document.DocumentBuilder;
import com.liferay.portal.search.document.DocumentBuilderFactory;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.BulkDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.ranking.web.internal.constants.ResultsRankingPortletKeys;
import com.liferay.portal.search.ranking.web.internal.index.ResultsRanking;
import com.liferay.portal.search.ranking.web.internal.index.ResultsRankingIndexer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

		String keywords = ParamUtil.getString(actionRequest, "keywords");
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

		if (cmd.equals(Constants.ADD)) {
			boolean exists = resultsRankingIndexer.exists(resultsRanking);

			if (!exists) {
				resultsRankingIndexer.addResultsRanking(resultsRanking);

				updateIndexDocuments(resultsRanking, false);
			}
			else {
				SessionErrors.add(actionRequest, Exception.class);

				actionResponse.setRenderParameter("mvcPath", "/error.jsp");

				return;
			}
		}
		else if (cmd.equals(Constants.UPDATE)) {
			resultsRankingIndexer.updateResultsRanking(resultsRanking);

			updateIndexDocuments(resultsRanking, false);
		}
		else if (cmd.equals(Constants.DELETE)) {
			resultsRankingIndexer.deleteResultsRanking(resultsRanking);

			updateIndexDocuments(resultsRanking, true);
		}

		String redirect = ParamUtil.getString(actionRequest, "redirect");

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
			"aliases", StringUtil.merge(aliases, StringPool.COMMA), false);
		portletURL.setParameter("keywords", keywords, false);
		portletURL.setWindowState(actionRequest.getWindowState());

		sendRedirect(actionRequest, actionResponse, portletURL.toString());
	}

	protected Document getDocument(String index, String uid) {
		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setIndexNames(index);
		searchSearchRequest.setQuery(queries.term("uid", uid));

		SearchSearchResponse searchSearchResponse = searchEngineAdapter.execute(
			searchSearchRequest);

		SearchHits searchHits = searchSearchResponse.getSearchHits();

		List<SearchHit> searchHitList = searchHits.getSearchHits();

		SearchHit searchHit = searchHitList.get(0);

		return searchHit.getDocument();
	}

	protected List<Document> getDocuments(
			ResultsRanking resultsRanking, boolean remove)
		throws Exception {

		List<Document> documents = new ArrayList<>();

		for (String pinnedDocument : resultsRanking.getPinnedDocuments()) {
			String[] pinnedDocumentParts = StringUtil.split(
				pinnedDocument, StringPool.DASH);

			String position = pinnedDocumentParts[0];

			String uid = pinnedDocumentParts[1];

			Document document = getDocument(resultsRanking.getIndex(), uid);

			String keywords = resultsRanking.getKeywords();

			List<Object> pinnedKeywords = document.getValues(
				"custom_ranking_pinned_keywords");

			DocumentBuilder builder = documentBuilderFactory.builder(document);

			if (remove && pinnedKeywords.contains(keywords)) {
				pinnedKeywords.remove(keywords);
			}
			else if (!pinnedKeywords.contains(keywords)) {
				pinnedKeywords.add(keywords);
			}

			if (ListUtil.isEmpty(pinnedKeywords)) {
				builder.unsetValue("custom_ranking_pinned_keywords");
			}
			else {
				builder.setValues(
					"custom_ranking_pinned_keywords", pinnedKeywords);
			}

			List<Object> pinnedPositions = document.getValues(
				"custom_ranking_pinned_positions");

			if (remove && pinnedPositions.contains(keywords + "_" + position)) {
				pinnedPositions.remove(keywords + "_" + position);
			}
			else {
				pinnedPositions.add(keywords + "_" + position);
			}

			if (ListUtil.isEmpty(pinnedPositions)) {
				builder.unsetValue("custom_ranking_pinned_positions");
			}
			else {
				builder.setValues(
					"custom_ranking_pinned_positions", pinnedPositions);
			}

			documents.add(builder.build());
		}

		for (String hiddenDocumentUid : resultsRanking.getHiddenDocuments()) {
			Document document = getDocument(
				resultsRanking.getIndex(), hiddenDocumentUid);

			DocumentBuilder builder = documentBuilderFactory.builder(document);

			String keywords = resultsRanking.getKeywords();

			List<Object> hiddenKeywords = document.getValues(
				"custom_ranking_hidden_keywords");

			if (remove && hiddenKeywords.contains(keywords)) {
				hiddenKeywords.remove(keywords);
			}
			else {
				hiddenKeywords.add(keywords);
			}

			if (ListUtil.isEmpty(hiddenKeywords)) {
				builder.unsetValue("custom_ranking_hidden_keywords");
			}
			else {
				builder.setValues(
					"custom_ranking_hidden_keywords", hiddenKeywords);
			}

			documents.add(builder.build());
		}

		return documents;
	}

	protected void updateIndexDocuments(
			ResultsRanking resultsRanking, boolean remove)
		throws Exception {

		BulkDocumentRequest bulkDocumentRequest = new BulkDocumentRequest();

		List<Document> documents = getDocuments(resultsRanking, remove);

		if (!documents.isEmpty()) {
			for (Document document : documents) {
				IndexDocumentRequest indexDocumentRequest =
					new IndexDocumentRequest(
						resultsRanking.getIndex(),
						document.getString(Field.UID), document);

				indexDocumentRequest.setType("LiferayDocumentType");

				bulkDocumentRequest.addBulkableDocumentRequest(
					indexDocumentRequest);
			}

			bulkDocumentRequest.setRefresh(true);

			searchEngineAdapter.execute(bulkDocumentRequest);
		}
	}

	@Reference
	protected DocumentBuilderFactory documentBuilderFactory;

	@Reference
	protected Portal portal;

	@Reference
	protected Queries queries;

	@Reference
	protected ResultsRankingIndexer resultsRankingIndexer;

	@Reference
	protected SearchEngineAdapter searchEngineAdapter;

}