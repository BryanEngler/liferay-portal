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

package com.liferay.portal.search.ranking.web.internal.index;

import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.module.framework.ModuleServiceLifecycle;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.document.DocumentBuilder;
import com.liferay.portal.search.document.DocumentBuilderFactory;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.DeleteDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.GetDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.GetDocumentResponse;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentResponse;
import com.liferay.portal.search.engine.adapter.document.UpdateDocumentRequest;
import com.liferay.portal.search.engine.adapter.index.CreateIndexRequest;
import com.liferay.portal.search.engine.adapter.index.IndicesExistsIndexRequest;
import com.liferay.portal.search.engine.adapter.index.IndicesExistsIndexResponse;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.MatchQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.TermQuery;
import com.liferay.portal.search.query.TermsQuery;

import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bryan Engler
 */
@Component(immediate = true, service = ResultsRankingIndexer.class)
public class ResultsRankingIndexer {

	public String addResultsRanking(ResultsRanking resultsRanking) {
		Document document = _createResultsRankingDocument(resultsRanking);

		return _addDocument(document);
	}

	public void deleteResultsRanking(String resultsRankingUid) {
		_deleteDocument(resultsRankingUid);
	}

	public boolean exists(
		String index, String uid, String keywords, List<String> aliases) {

		return _findDocumentsByKeywordsAndAliases(
			index, uid, keywords, aliases);
	}

	public ResultsRanking getResultsRanking(String uid) {
		Document document = _getDocument(uid);

		ResultsRanking resultsRanking = new ResultsRanking();

		resultsRanking.setAliases(
			ArrayUtil.toStringArray(document.getStrings("aliases")));
		//resultsRanking.setDisplayDate();
		resultsRanking.setHiddenDocuments((List<String>)
			document.getValue("hidden_documents"));
		resultsRanking.setIndex(document.getString("index"));
		resultsRanking.setKeywords(document.getString("keywords"));
		//resultsRanking.setModifiedDate();
		resultsRanking.setPinnedDocuments((List<Map<String,String>>)
			document.getValue("pinned_documents"));
		resultsRanking.setStatus(
			GetterUtil.getInteger(document.getInteger("status")));
		resultsRanking.setUid(uid);

		return resultsRanking;
	}

	public void updateResultsRanking(ResultsRanking resultsRanking) {
		Document document = _createResultsRankingDocument(resultsRanking);

		_updateDocument(document);
	}

	@Activate
	protected void activate() throws Exception {
		createIndex();
	}

	protected void createIndex() {
		IndicesExistsIndexRequest indicesExistsIndexRequest =
			new IndicesExistsIndexRequest(getIndexName());

		IndicesExistsIndexResponse indicesExistsIndexResponse =
			_searchEngineAdapter.execute(indicesExistsIndexRequest);

		if (indicesExistsIndexResponse.isExists()) {
			return;
		}

		try {
			CreateIndexRequest createIndexRequest = new CreateIndexRequest(
				getIndexName());

			JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
				StringUtil.read(getClass(), "/META-INF/search/mappings.json"));

			createIndexRequest.setSource(
				JSONUtil.put(
					"mappings",
					JSONUtil.put(getIndexType(), jsonObject.get(getIndexType()))
				).toString());

			_searchEngineAdapter.execute(createIndexRequest);
		}
		catch (Exception e) {
			System.out.println("unable to create index");
		}
	}

	protected String getIndexName() {
		return "results-ranking";
	}

	protected String getIndexType() {
		return "ResultsRankingType";
	}

	@Reference(unbind = "-")
	protected void setDocumentBuilderFactory(
		DocumentBuilderFactory documentBuilderFactory) {

		_documentBuilderFactory = documentBuilderFactory;
	}

	@Reference(target = ModuleServiceLifecycle.PORTAL_INITIALIZED, unbind = "-")
	protected void setModuleServiceLifecycle(
		ModuleServiceLifecycle moduleServiceLifecycle) {
	}

	@Reference(unbind = "-")
	protected void setQueries(Queries queries) {
		_queries = queries;
	}

	@Reference(unbind = "-")
	protected void setSearchEngineAdapter(
		SearchEngineAdapter searchEngineAdapter) {

		_searchEngineAdapter = searchEngineAdapter;
	}

	private String _addDocument(Document document) {
		IndexDocumentRequest indexDocumentRequest = new IndexDocumentRequest(
			getIndexName(), document);

		indexDocumentRequest.setType(getIndexType());

		IndexDocumentResponse indexDocumentResponse =
			_searchEngineAdapter.execute(indexDocumentRequest);

		return indexDocumentResponse.getUid();
	}

	private Document _createResultsRankingDocument(
		ResultsRanking resultsRanking) {

		DocumentBuilder documentBuilder = _documentBuilderFactory.builder();

		String resultsRankingUid = resultsRanking.getUid();

		if (!Validator.isBlank(resultsRankingUid)) {
			documentBuilder.setString("uid", resultsRankingUid);
		}

		if (resultsRanking.getAliases() != null &&
			ArrayUtil.isNotEmpty(resultsRanking.getAliases())) {

			documentBuilder.setStrings("aliases", resultsRanking.getAliases());
		}

		//documentBuilder.setDate(
		//	Field.DISPLAY_DATE, resultsRanking.getDisplayDate().toString());

		if (resultsRanking.getHiddenDocuments() != null &&
			ListUtil.isNotEmpty(resultsRanking.getHiddenDocuments())) {

			documentBuilder.setValue(
				"hidden_documents", resultsRanking.getHiddenDocuments());
		}

		if (resultsRanking.getIndex() != null) {
			documentBuilder.setString("index", resultsRanking.getIndex());
		}

		if (resultsRanking.getKeywords() != null) {
			documentBuilder.setString("keywords", resultsRanking.getKeywords());
		}

		//documentBuilder.setDate(
		//	Field.MODIFIED_DATE, resultsRanking.getModifiedDate().toString());

		if (resultsRanking.getPinnedDocuments() != null &&
			ListUtil.isNotEmpty(resultsRanking.getPinnedDocuments())) {

			documentBuilder.setValue(
				"pinned_documents", resultsRanking.getPinnedDocuments());
		}

		if (resultsRanking.getPinnedDocuments() != null &&
			resultsRanking.getStatus() >= 0) {

			documentBuilder.setInteger("status", resultsRanking.getStatus());
		}

		return documentBuilder.build();
	}

	private void _deleteDocument(String resultsRankingUid) {
		DeleteDocumentRequest deleteDocumentRequest = new DeleteDocumentRequest(
			getIndexName(), resultsRankingUid);

		deleteDocumentRequest.setType(getIndexType());

		_searchEngineAdapter.execute(deleteDocumentRequest);
	}

	private boolean _findDocumentsByKeywordsAndAliases(
		String index, String uid, String keywords, List<String> aliases) {

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setTypes(getIndexType());
		searchSearchRequest.setIndexNames(getIndexName());

		BooleanQuery booleanQuery = _queries.booleanQuery();

		BooleanQuery keywordsBooleanQuery = _queries.booleanQuery();

		if (!Validator.isBlank(keywords)) {
			TermQuery aliasesKeywordsTermQuery = _queries.term(
				"aliases", keywords);

			TermQuery keywordsKeywordsTermQuery = _queries.term(
				"keywords", keywords);

			keywordsBooleanQuery.addShouldQueryClauses(
				aliasesKeywordsTermQuery, keywordsKeywordsTermQuery);
		}

		if (ListUtil.isNotEmpty(aliases)) {
			TermsQuery aliasesAliasesTermsQuery = _queries.terms("aliases");

			aliasesAliasesTermsQuery.addValues(aliases.toArray());

			TermsQuery keywordsAliasesTermsQuery = _queries.terms("keywords");

			keywordsAliasesTermsQuery.addValues(aliases.toArray());

			keywordsBooleanQuery.addShouldQueryClauses(
				aliasesAliasesTermsQuery, keywordsAliasesTermsQuery);
		}

		MatchQuery indexMatchQuery = _queries.match("index", index);

		booleanQuery.addMustQueryClauses(indexMatchQuery, keywordsBooleanQuery);

		if (uid != null) {
			MatchQuery uidMatchQuery = _queries.match("_id", uid);

			booleanQuery.addMustNotQueryClauses(uidMatchQuery);
		}

		searchSearchRequest.setQuery(booleanQuery);

		SearchSearchResponse searchSearchResponse =
			_searchEngineAdapter.execute(searchSearchRequest);

		if (searchSearchResponse.getCount() > 0) {
			return true;
		}

		return false;
	}

	private Document _getDocument(String uid) {
		GetDocumentRequest getDocumentRequest = new GetDocumentRequest(
			getIndexName(), uid);

		getDocumentRequest.setType(getIndexType());
		getDocumentRequest.setFetchSourceInclude("*");

		GetDocumentResponse getDocumentResponse = _searchEngineAdapter.execute(
			getDocumentRequest);

		if (getDocumentResponse.isExists()) {
			return getDocumentResponse.getDocument();
		}

		return null;
	}

	private void _updateDocument(Document document) {
		IndexDocumentRequest indexDocumentRequest = new IndexDocumentRequest(
			getIndexName(), document.getString("uid"), document);

		indexDocumentRequest.setType(getIndexType());

		_searchEngineAdapter.execute(indexDocumentRequest);
	}

	private DocumentBuilderFactory _documentBuilderFactory;
	private Queries _queries;
	private SearchEngineAdapter _searchEngineAdapter;

}