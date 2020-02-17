/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.portal.search.tuning.rankings.web.internal.index;

import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManager;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.model.UserConstants;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.BulkDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.BulkDocumentResponse;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentRequest;
import com.liferay.portal.search.engine.adapter.index.CreateIndexRequest;
import com.liferay.portal.search.engine.adapter.index.CreateIndexResponse;
import com.liferay.portal.search.engine.adapter.index.DeleteIndexRequest;
import com.liferay.portal.search.engine.adapter.index.DeleteIndexResponse;
import com.liferay.portal.search.engine.adapter.index.IndicesExistsIndexRequest;
import com.liferay.portal.search.engine.adapter.index.IndicesExistsIndexResponse;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.index.IndexNameBuilder;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.tuning.rankings.web.internal.background.task.RankingIndexRenameBackgroundTaskExecutor;

import java.io.Serializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Wade Cao
 */
@Component(immediate = true, service = {})
public class RankingIndexUtil {

	public static String getRankingIndexName() {
		return _rankingIndexUtil.getRankingIndexName(
			RankingIndexDefinition.INDEX_NAME);
	}

	public static void renameRankingIndexName(String indexName) {
		if (_rankingIndexUtil.isIndicesExists(indexName)) {
			List<Document> documents = _rankingIndexUtil.getDocuments(
				indexName);

			if (documents.isEmpty()) {
				_rankingIndexUtil.deleteIndex(indexName);

				return;
			}

			if (_rankingIndexUtil.addDocuments(
					_rankingIndexUtil.getRankingIndexName(indexName),
					documents)) {

				_rankingIndexUtil.deleteIndex(indexName);
			}
		}
	}

	@Activate
	protected void activate() {
		_rankingIndexUtil = this;

		_addBackgroundTask();
	}

	protected boolean addDocuments(String indexName, List<Document> documents) {
		boolean successed = true;

		BulkDocumentRequest bulkDocumentRequest = new BulkDocumentRequest();

		documents.forEach(
			document -> {
				IndexDocumentRequest indexDocumentRequest =
					new IndexDocumentRequest(indexName, document);

				bulkDocumentRequest.addBulkableDocumentRequest(
					indexDocumentRequest);
			});

		BulkDocumentResponse bulkDocumentResponse =
			_searchEngineAdapter.execute(bulkDocumentRequest);

		if (bulkDocumentResponse.hasErrors()) {
			successed = false;
		}

		return successed;
	}

	protected boolean createIndex(String indexName) {
		String mappingSource = StringUtil.read(
			getClass(), RankingIndexDefinition.INDEX_SETTINGS_RESOURCE_NAME);

		CreateIndexRequest createIndexRequest = new CreateIndexRequest(
			indexName);

		createIndexRequest.setSource(mappingSource);

		CreateIndexResponse createIndexResponse = _searchEngineAdapter.execute(
			createIndexRequest);

		return createIndexResponse.isAcknowledged();
	}

	protected boolean deleteIndex(String... indexNames) {
		DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(
			indexNames);

		DeleteIndexResponse deleteIndexResponse = _searchEngineAdapter.execute(
			deleteIndexRequest);

		return deleteIndexResponse.isAcknowledged();
	}

	protected List<Document> getDocuments(String indexName) {
		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setIndexNames(indexName);
		searchSearchRequest.setQuery(_queries.matchAll());
		searchSearchRequest.setFetchSource(true);

		SearchSearchResponse searchSearchResponse =
			_searchEngineAdapter.execute(searchSearchRequest);

		SearchHits searchHits = searchSearchResponse.getSearchHits();

		List<SearchHit> searchHitsList = searchHits.getSearchHits();

		Stream<SearchHit> documentStream = searchHitsList.stream();

		return documentStream.map(
			SearchHit::getDocument
		).collect(
			Collectors.toList()
		);
	}

	protected String getRankingIndexName(String rankingIndexDefinitionName) {
		String rankingIndexName =
			_indexNameBuilder.getIndexName(0) + "-" +
				rankingIndexDefinitionName;

		if (isIndicesExists(rankingIndexName)) {
			return rankingIndexName;
		}
		
		if (!createIndex(rankingIndexName)) {
			rankingIndexName = rankingIndexDefinitionName;
		}

		return rankingIndexName;
	}

	protected boolean isIndicesExists(String... indexNames) {
		IndicesExistsIndexRequest indicesExistsIndexRequest =
			new IndicesExistsIndexRequest(indexNames);

		IndicesExistsIndexResponse indicesExistsIndexResponse =
			_searchEngineAdapter.execute(indicesExistsIndexRequest);

		return indicesExistsIndexResponse.isExists();
	}

	@Reference(unbind = "-")
	protected void setBackgroundTaskManager(
		BackgroundTaskManager backgroundTaskManager) {

		_backgroundTaskManager = backgroundTaskManager;
	}

	@Reference(unbind = "-")
	protected void setIndexNameBuilder(IndexNameBuilder indexNameBuilder) {
		_indexNameBuilder = indexNameBuilder;
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

	private boolean _addBackgroundTask() {
		boolean successed = true;

		Map<String, Serializable> taskContextMap = new HashMap<>();

		taskContextMap.put("indexName", RankingIndexDefinition.INDEX_NAME);

		String jobName = "RankingIndexRename";

		try {
			_backgroundTaskManager.addBackgroundTask(
				UserConstants.USER_ID_DEFAULT, CompanyConstants.SYSTEM, jobName,
				RankingIndexRenameBackgroundTaskExecutor.class.getName(),
				taskContextMap, new ServiceContext());
		}
		catch (PortalException pe) {
			_log.error("Unable to schedule the job for " + jobName, pe);
			successed = false;
		}

		return successed;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		RankingIndexUtil.class);

	private static RankingIndexUtil _rankingIndexUtil;

	private BackgroundTaskManager _backgroundTaskManager;
	private IndexNameBuilder _indexNameBuilder;
	private Queries _queries;
	private SearchEngineAdapter _searchEngineAdapter;

}