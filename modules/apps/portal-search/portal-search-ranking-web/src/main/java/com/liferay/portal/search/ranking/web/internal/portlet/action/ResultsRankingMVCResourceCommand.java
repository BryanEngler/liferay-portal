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

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.GetDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.GetDocumentResponse;
import com.liferay.portal.search.legacy.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.ranking.web.internal.constants.ResultsRankingPortletKeys;
import com.liferay.portal.search.searcher.SearchRequest;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.portlet.PortletException;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bryan Engler
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + ResultsRankingPortletKeys.RESULTS_RANKING,
		"mvc.command.name=/results_ranking/get_results"
	},
	service = MVCResourceCommand.class
)
public class ResultsRankingMVCResourceCommand implements MVCResourceCommand {

	@Override
	public boolean serveResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws PortletException {

		String cmd = ParamUtil.getString(resourceRequest, Constants.CMD);

		if (cmd.equals("getVisibleResults")) {
			_writeVisibleDocumentsJSON(resourceRequest, resourceResponse);
		}
		else if (cmd.equals("getHiddenResults")) {
			_writeHiddenDocumentsJSON(resourceRequest, resourceResponse);
		}

		return false;
	}

	protected Document getDocument(
		String index, String uid, String type, String[] storedFields) {

		GetDocumentRequest getDocumentRequest = new GetDocumentRequest(
			index, uid);

		getDocumentRequest.setType(type);
		getDocumentRequest.setStoredFields(storedFields);

		GetDocumentResponse getDocumentResponse =
			searchEngineAdapter.execute(getDocumentRequest);

		if (!getDocumentResponse.isExists()) {
			return null;
		}

		return getDocumentResponse.getDocument();
	}

	protected String getResultsRankingIndexType() {
		return "ResultsRankingType";
	}

	protected String getResultsRankingIndexName() {
		return "results-ranking";
	}

	@Reference
	protected SearchEngineAdapter searchEngineAdapter;

	@Reference
	protected Searcher searcher;

	@Reference
	protected SearchRequestBuilderFactory searchRequestBuilderFactory;

	private JSONObject _translate(Document document) {
		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		String title = document.getString(Field.TITLE + "_en_US");

		if (Validator.isBlank(title)) {
			title = document.getString(Field.TITLE);
		}

		jsonObject.put("author", document.getString(Field.USER_NAME));
		jsonObject.put("clicks", document.getString("clicks"));
		jsonObject.put("description", document.getString(Field.DESCRIPTION));
		jsonObject.put("hidden", document.getString(Field.HIDDEN));
		jsonObject.put("id", document.getString(Field.UID));
		jsonObject.put("pinned", true);
		jsonObject.put("title", title);
		jsonObject.put("type", document.getString(Field.ENTRY_CLASS_NAME));

		return jsonObject;
	}

	private void _writeHiddenDocumentsJSON(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws PortletException {

		String resultsRankingUid = ParamUtil.getString(
			resourceRequest, "resultsRankingUid");

		Document resultsRankingDocument = getDocument(
			getResultsRankingIndexName(), resultsRankingUid,
			getResultsRankingIndexType(), _STORED_FIELDS);

		List<String> hiddenDocumentUids =
			resultsRankingDocument.getStrings("hidden_documents");

		String index = resultsRankingDocument.getString("index");

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		for (String hiddenDocumentUid : hiddenDocumentUids) {
			Document hiddenDocument = getDocument(
				index, hiddenDocumentUid, "LiferayDocumentType",
					new String[] {"title", "title_en_US", Field.HIDDEN,
						Field.ENTRY_CLASS_NAME, Field.UID, Field.USER_NAME});

			if (hiddenDocument != null) {
				jsonArray.put(_translate(hiddenDocument));
			}
		}

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		jsonObject.put("documents", jsonArray);
		jsonObject.put("total", jsonArray.length());

		try {
			JSONPortletResponseUtil.writeJSON(
				resourceRequest, resourceResponse, jsonObject);
		}
		catch (Exception e) {
			throw new PortletException(e);
		}
	}

	private void _writeVisibleDocumentsJSON(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws PortletException {

		String resultsRankingUid = ParamUtil.getString(
			resourceRequest, "resultsRankingUid");

		Document resultsRankingDocument = getDocument(
			getResultsRankingIndexName(), resultsRankingUid,
			getResultsRankingIndexType(), _STORED_FIELDS);

		List<String> hiddenUids =
			resultsRankingDocument.getStrings("hidden_documents");

		SearchContext searchContext = new SearchContext();

		long companyId = ParamUtil.getLong(resourceRequest, "companyId");
		String keywords = ParamUtil.getString(resourceRequest, "keywords");
		int from = ParamUtil.getInteger(resourceRequest, "from");
		int size = ParamUtil.getInteger(resourceRequest, "size", 10);

		searchContext.setCompanyId(companyId);
		searchContext.setKeywords(keywords);
		searchContext.setStart(from);
		searchContext.setEnd(from + size);

		SearchRequestBuilder searchRequestBuilder =
			searchRequestBuilderFactory.builder(searchContext);

		SearchRequest searchRequest = searchRequestBuilder.build();

		SearchResponse searchResponse = searcher.search(searchRequest);

		Stream<Document> docs = searchResponse.getDocumentsStream();

		List<Document> filteredDocs = new ArrayList<>();

		docs.forEach(
			document -> {
				String uid = document.getString(Field.UID);

				if (!hiddenUids.contains(uid)) {
					filteredDocs.add(document);
				}
			}
		);

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		for (Document document : filteredDocs) {
			jsonArray.put(_translate(document));
		}

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		jsonObject.put("documents", jsonArray);
		jsonObject.put("total", jsonArray.length());

		try {
			JSONPortletResponseUtil.writeJSON(
				resourceRequest, resourceResponse, jsonObject);
		}
		catch (Exception e) {
			throw new PortletException(e);
		}
	}

	private static final String[] _STORED_FIELDS = {
		"aliases", "displayDate", "hidden_documents", "index", "keywords",
		"modified", "pinned_documents", "status", "uid"
	};

}