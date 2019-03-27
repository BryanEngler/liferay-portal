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
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.ArrayUtil;
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

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bryan Engler
 */
@Component(immediate = true, service = ResultsRankingIndexer.class)
public class ResultsRankingIndexer {

	public String addResultsRanking(ResultsRanking resultsRanking) {
		Document document = _createCustomRankingDocument(resultsRanking);

		return _addDocument(document);
	}

	public void deleteResultsRanking(ResultsRanking resultsRanking) {
		Document document = _createCustomRankingDocument(resultsRanking);

		_deleteDocument(document);
	}

	public boolean exists(ResultsRanking resultsRanking) {
		Document document = _createCustomRankingDocument(resultsRanking);

		return _findDocumentsByKeywordsAndAliases(document);
	}

	public Document getResultsRanking(ResultsRanking resultsRanking) {
		Document document = _createCustomRankingDocument(resultsRanking);

		return _getDocument(document);
	}

	public void updateResultsRanking(ResultsRanking resultsRanking) {
		Document document = _createCustomRankingDocument(resultsRanking);

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

	private Document _createCustomRankingDocument(
		ResultsRanking resultsRanking) {

		DocumentBuilder documentBuilder = _documentBuilderFactory.builder();

		String uid = resultsRanking.getUid();

		if (!Validator.isBlank(uid)) {
			documentBuilder.setString(Field.UID, uid);
		}

		if (ArrayUtil.isNotEmpty(resultsRanking.getAliases())) {
			documentBuilder.setStrings("aliases", resultsRanking.getAliases());
		}

		//documentBuilder.setDate(
		//	Field.DISPLAY_DATE, resultsRanking.getDisplayDate().toString());

		if (ArrayUtil.isNotEmpty(resultsRanking.getHiddenDocuments())) {
			documentBuilder.setStrings(
				"hidden_documents", resultsRanking.getHiddenDocuments());
		}

		if (!Validator.isBlank(resultsRanking.getIndex())) {
			documentBuilder.setString("index", resultsRanking.getIndex());
		}

		if (!Validator.isBlank(resultsRanking.getKeywords())) {
			documentBuilder.setString("keywords", resultsRanking.getKeywords());
		}

		//documentBuilder.setDate(
		//	Field.MODIFIED_DATE, resultsRanking.getModifiedDate().toString());

		if (ArrayUtil.isNotEmpty(resultsRanking.getPinnedDocuments())) {
			documentBuilder.setStrings(
				"pinned_documents", resultsRanking.getPinnedDocuments());
		}

		if (Validator.isNotNull(resultsRanking.getStatus())) {
			documentBuilder.setInteger(
				Field.STATUS, resultsRanking.getStatus());
		}

		return documentBuilder.build();
	}

	private void _deleteDocument(Document document) {
		DeleteDocumentRequest deleteDocumentRequest = new DeleteDocumentRequest(
			getIndexName(), document.getString(Field.UID));

		deleteDocumentRequest.setType(getIndexType());

		_searchEngineAdapter.execute(deleteDocumentRequest);
	}

	private boolean _findDocumentsByKeywordsAndAliases(Document document) {
		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setTypes(getIndexType());
		searchSearchRequest.setIndexNames(getIndexName());

		BooleanQuery booleanQuery = _queries.booleanQuery();

		BooleanQuery keywordsBooleanQuery = _queries.booleanQuery();

		String keywords = document.getString("keywords");

		if (!Validator.isBlank(keywords)) {
			TermQuery aliasesKeywordsTermQuery = _queries.term(
				"aliases", keywords);

			TermQuery keywordsKeywordsTermQuery = _queries.term(
				"keywords", keywords);

			keywordsBooleanQuery.addShouldQueryClauses(
				aliasesKeywordsTermQuery, keywordsKeywordsTermQuery);
		}

		List<String> aliases = document.getStrings("aliases");

		if (ListUtil.isNotEmpty(aliases)) {
			TermsQuery aliasesAliasesTermsQuery = _queries.terms("aliases");

			aliasesAliasesTermsQuery.addValues(aliases.toArray());

			TermsQuery keywordsAliasesTermsQuery = _queries.terms("keywords");

			keywordsAliasesTermsQuery.addValues(aliases.toArray());

			keywordsBooleanQuery.addShouldQueryClauses(
				aliasesAliasesTermsQuery, keywordsAliasesTermsQuery);
		}

		MatchQuery indexMatchQuery = _queries.match(
			"index", document.getString("index"));

		booleanQuery.addMustQueryClauses(indexMatchQuery, keywordsBooleanQuery);

		searchSearchRequest.setQuery(booleanQuery);

		SearchSearchResponse searchSearchResponse =
			_searchEngineAdapter.execute(searchSearchRequest);

		if (searchSearchResponse.getCount() > 0) {
			return true;
		}

		return false;
	}

	private Document _getDocument(Document document) {
		GetDocumentRequest getDocumentRequest = new GetDocumentRequest(
			getIndexName(), document.getString(Field.UID));

		getDocumentRequest.setType(getIndexType());
		getDocumentRequest.setStoredFields(_STORED_FIELDS);

		GetDocumentResponse getDocumentResponse = _searchEngineAdapter.execute(
			getDocumentRequest);

		if (getDocumentResponse.isExists()) {
			return getDocumentResponse.getDocument();
		}

		return null;
	}

	private void _updateDocument(Document document) {
		UpdateDocumentRequest updateDocumentRequest = new UpdateDocumentRequest(
			getIndexName(), document.getString(Field.UID), document);

		updateDocumentRequest.setType(getIndexType());

		_searchEngineAdapter.execute(updateDocumentRequest);
	}

	private static final String[] _STORED_FIELDS = {
		"aliases", "displayDate", "hidden_documents", "index", "keywords",
		"modified", "pinned_documents", "status", "uid"
	};

	private DocumentBuilderFactory _documentBuilderFactory;
	private Queries _queries;
	private SearchEngineAdapter _searchEngineAdapter;

}