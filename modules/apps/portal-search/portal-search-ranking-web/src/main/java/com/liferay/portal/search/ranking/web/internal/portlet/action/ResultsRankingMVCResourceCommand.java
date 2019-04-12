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
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.GetDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.GetDocumentResponse;
import com.liferay.portal.search.ranking.web.internal.constants.ResultsRankingPortletKeys;
import com.liferay.portal.search.searcher.SearchRequest;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.legacy.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.portlet.PortletException;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import java.util.ArrayList;
import java.util.List;

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

	private void _writeVisibleDocumentsJSON(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws PortletException {

		String resultsRankingUid =
			ParamUtil.getString(resourceRequest, "resultsRankingUid");

		JSONObject resultsRankingDocumentJSONObject = getDocumentJSONObject(
			getResultsRankingIndexName(), resultsRankingUid);

		JSONArray hiddenDocumentUids =
			(JSONArray)resultsRankingDocumentJSONObject.get("hidden_documents");

		List<String> hiddenUids = new ArrayList<>();

		if (hiddenDocumentUids != null) {
			hiddenDocumentUids.forEach(
				string -> hiddenUids.add((String) string)
			);
		}

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

		List<Document> docs = searchResponse.getDocuments71();

		List<Document> filteredDocs = new ArrayList<>();

		for (Document document1 : docs) {
			String uid = document1.getUID();

			if (!hiddenUids.contains(uid)) {
				filteredDocs.add(document1);
			}
		}

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

	private void _writeHiddenDocumentsJSON(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws PortletException {

		String resultsRankingUid =
			ParamUtil.getString(resourceRequest, "resultsRankingUid");

		JSONObject resultsRankingDocumentJSONObject = getDocumentJSONObject(
			getResultsRankingIndexName(), resultsRankingUid);

		JSONArray hiddenDocumentUids =
			(JSONArray)resultsRankingDocumentJSONObject.get("hidden_documents");

		String index = resultsRankingDocumentJSONObject.getString("index");

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		int from = ParamUtil.getInteger(resourceRequest, "from", 0);
		int size = ParamUtil.getInteger(
			resourceRequest, "size", hiddenDocumentUids.length());

		for (int i = from; i < size; i++) {
			String hiddenDocumentUid = hiddenDocumentUids.getString(i);

			JSONObject hiddenDocumentJSONObject = getDocumentJSONObject(
				index, hiddenDocumentUid);

			if (hiddenDocumentJSONObject != null) {
				jsonArray.put(_translate(hiddenDocumentJSONObject));
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

	private JSONObject _translate(JSONObject documentJSONObject) {
		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		String title = documentJSONObject.getString(
			Field.TITLE + "_en_US");

		if (Validator.isBlank(title)) {
			title = documentJSONObject.getString(Field.TITLE);
		}

		jsonObject.put("author", documentJSONObject.get(Field.USER_NAME));
		jsonObject.put("clicks", documentJSONObject.get("clicks"));
		jsonObject.put(
			"description", documentJSONObject.get(Field.DESCRIPTION));
		jsonObject.put("hidden", documentJSONObject.get(Field.HIDDEN));
		jsonObject.put("id", documentJSONObject.get(Field.UID));
		jsonObject.put("pinned", true);
		jsonObject.put("title", title);
		jsonObject.put("type", documentJSONObject.get(Field.ENTRY_CLASS_NAME));

		return jsonObject;
	}

	private JSONObject _translate(Document document) {
		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		String title = document.get(Field.TITLE + "_en_US");

		if (Validator.isBlank(title)) {
			title = document.get(Field.TITLE);
		}

		jsonObject.put("author", document.get(Field.USER_NAME));
		jsonObject.put("clicks", document.get("clicks"));
		jsonObject.put(
			"description", document.get(Field.DESCRIPTION));
		jsonObject.put("hidden", document.get(Field.HIDDEN));
		jsonObject.put("id", document.get(Field.UID));
		jsonObject.put("pinned", true);
		jsonObject.put("title", title);
		jsonObject.put("type", document.get(Field.ENTRY_CLASS_NAME));

		return jsonObject;
	}

	protected JSONObject getDocumentJSONObject(String index, String uid) {
		try {
			GetDocumentRequest getDocumentRequest = new GetDocumentRequest(
				index, uid);

			GetDocumentResponse getDocumentResponse =
				searchEngineAdapter.execute(getDocumentRequest);

			if (!getDocumentResponse.isExists()) {
				return null;
			}

			JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
				getDocumentResponse.getSource());

			return jsonObject;
		}
		catch (JSONException jsone) {
			return null;
		}
	}

	@Reference
	protected SearchEngineAdapter searchEngineAdapter;

	@Reference
	protected SearchRequestBuilderFactory searchRequestBuilderFactory;

	@Reference
	protected Searcher searcher;


	protected String getResultsRankingIndexName() {
		return "results-ranking";
	}
}