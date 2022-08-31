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

package com.liferay.portal.search.elasticsearch7.internal.index.field.initializer;

import com.liferay.asset.entry.rel.service.AssetEntryAssetCategoryRelLocalService;
import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.background.task.constants.BackgroundTaskContextMapConstants;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManager;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskResult;
import com.liferay.portal.kernel.backgroundtask.BaseBackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.display.BackgroundTaskDisplay;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.model.UserConstants;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.aggregation.Aggregations;
import com.liferay.portal.search.aggregation.bucket.Bucket;
import com.liferay.portal.search.aggregation.bucket.TermsAggregationResult;
import com.liferay.portal.search.document.DocumentBuilder;
import com.liferay.portal.search.document.DocumentBuilderFactory;
import com.liferay.portal.search.elasticsearch7.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.search.elasticsearch7.internal.index.constants.LiferayTypeMappingsConstants;
import com.liferay.portal.search.elasticsearch7.internal.util.DocumentTypes;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.BulkDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.UpdateDocumentRequest;
import com.liferay.portal.search.engine.adapter.index.GetFieldMappingIndexRequest;
import com.liferay.portal.search.engine.adapter.index.GetFieldMappingIndexResponse;
import com.liferay.portal.search.engine.adapter.index.PutMappingIndexRequest;
import com.liferay.portal.search.engine.adapter.index.PutMappingIndexResponse;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.query.Queries;

import java.io.IOException;
import java.io.Serializable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bryan Engler
 */
@Component(
	immediate = true,
	property = "background.task.executor.class.name=com.liferay.portal.search.elasticsearch7.internal.index.field.initializer.AssetVocabularyCategoryIdsInitializationBackgroundTaskExecutor",
	service = {
		AssetVocabularyCategoryIdsInitializationBackgroundTaskExecutor.class,
		BackgroundTaskExecutor.class
	}
)
public class AssetVocabularyCategoryIdsInitializationBackgroundTaskExecutor
	extends BaseBackgroundTaskExecutor {

	@Override
	public BackgroundTaskExecutor clone() {
		return this;
	}

	@Override
	public BackgroundTaskResult execute(BackgroundTask backgroundTask)
		throws Exception {

		Map<String, Serializable> taskContextMap =
			backgroundTask.getTaskContextMap();

		String indexName = (String)taskContextMap.get("indexName");

		try {
			_indexAssetVocabularyCategoryIds(indexName);
		}
		catch (IOException ioException) {
			_log.error(
				StringBundler.concat(
					"Unable to index assetVocabularyCategoryIds values in ",
					"index ", indexName, ". A full reindex may be necessary."));
		}

		return BackgroundTaskResult.SUCCESS;
	}

	@Override
	public BackgroundTaskDisplay getBackgroundTaskDisplay(
		BackgroundTask backgroundTask) {

		return null;
	}

	public void initialize(
		long companyId, String indexName,
		ElasticsearchConnectionManager elasticsearchConnectionManager) {

		if (!_indexHasValidMapping(indexName)) {
			return;
		}

		long assetCategoryIdsDocCount = _getDocCount(
			companyId, indexName, "assetCategoryIds");

		long assetVocabularyCategoryIdsDocCount = _getDocCount(
			companyId, indexName, "assetVocabularyCategoryIds");

		if (assetCategoryIdsDocCount == assetVocabularyCategoryIdsDocCount) {
			return;
		}

		if (_elasticsearchConnectionManager == null) {
			_elasticsearchConnectionManager = elasticsearchConnectionManager;
		}

		Map<String, Serializable> taskContextMap =
			HashMapBuilder.<String, Serializable>put(
				BackgroundTaskContextMapConstants.DELETE_ON_SUCCESS, true
			).put(
				"indexName", indexName
			).build();

		try {
			_backgroundTaskManager.addBackgroundTask(
				UserConstants.USER_ID_DEFAULT, CompanyConstants.SYSTEM,
				"indexAssetVocabularyCategoryIds-" + indexName,
				AssetVocabularyCategoryIdsInitializationBackgroundTaskExecutor.
					class.getName(),
				taskContextMap, new ServiceContext());
		}
		catch (PortalException portalException) {
			_log.error(
				StringBundler.concat(
					"Unable to index assetVocabularyCategoryIds values in ",
					"index ", indexName, ". A full reindex may be necessary."),
				portalException);
		}
	}

	private long _getDocCount(
		long companyId, String indexName, String fieldName) {

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.addAggregation(
			_aggregations.terms(fieldName, "companyId"));
		searchSearchRequest.setIndexNames(indexName);
		searchSearchRequest.setPreferLocalCluster(false);
		searchSearchRequest.setQuery(_queries.exists(fieldName));

		SearchSearchResponse searchSearchResponse =
			_searchEngineAdapter.execute(searchSearchRequest);

		Map<String, AggregationResult> aggregationResultsMap =
			searchSearchResponse.getAggregationResultsMap();

		TermsAggregationResult termsAggregationResult =
			(TermsAggregationResult)aggregationResultsMap.get(fieldName);

		Bucket bucket = termsAggregationResult.getBucket(
			String.valueOf(companyId));

		if (bucket != null) {
			return bucket.getDocCount();
		}

		return -1;
	}

	private void _indexAssetVocabularyCategoryIds(String indexName)
		throws IOException {

		if (_log.isInfoEnabled()) {
			_log.info(
				"Started indexing of the assetVocabularyCategoryIds field " +
					"for index " + indexName);
		}

		SearchRequest searchRequest = new SearchRequest(indexName);

		Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));

		searchRequest.scroll(scroll);

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		searchSourceBuilder.query(new ExistsQueryBuilder("assetCategoryIds"));
		searchSourceBuilder.size(1000);
		searchSourceBuilder.storedFields(
			Arrays.asList(
				Field.UID, Field.ENTRY_CLASS_NAME, Field.ENTRY_CLASS_PK));

		searchRequest.source(searchSourceBuilder);

		RestHighLevelClient restHighLevelClient =
			_elasticsearchConnectionManager.getRestHighLevelClient();

		SearchResponse searchResponse = restHighLevelClient.search(
			searchRequest, RequestOptions.DEFAULT);

		String scrollId = searchResponse.getScrollId();

		SearchHits searchHits = searchResponse.getHits();

		SearchHit[] hits = searchHits.getHits();

		while (ArrayUtil.isNotEmpty(hits)) {
			_updateDocuments(hits, indexName);

			SearchScrollRequest scrollRequest = new SearchScrollRequest(
				scrollId);

			scrollRequest.scroll(scroll);

			searchResponse = restHighLevelClient.scroll(
				scrollRequest, RequestOptions.DEFAULT);

			scrollId = searchResponse.getScrollId();

			searchHits = searchResponse.getHits();

			hits = searchHits.getHits();
		}

		if (_log.isInfoEnabled()) {
			_log.info(
				"Finished indexing of the assetVocabularyCategoryIds field " +
					"for index " + indexName);
		}

		ClearScrollRequest clearScrollRequest = new ClearScrollRequest();

		clearScrollRequest.addScrollId(scrollId);

		ClearScrollResponse clearScrollResponse =
			restHighLevelClient.clearScroll(
				clearScrollRequest, RequestOptions.DEFAULT);

		if (!clearScrollResponse.isSucceeded()) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to clear scroll successfully for index " +
						indexName);
			}
		}
	}

	private boolean _indexHasValidMapping(String indexName) {
		GetFieldMappingIndexRequest getFieldMappingIndexRequest =
			new GetFieldMappingIndexRequest(
				new String[] {indexName},
				LiferayTypeMappingsConstants.LIFERAY_DOCUMENT_TYPE,
				new String[] {"assetVocabularyCategoryIds"});

		getFieldMappingIndexRequest.setPreferLocalCluster(false);

		GetFieldMappingIndexResponse getFieldMappingIndexResponse =
			_searchEngineAdapter.execute(getFieldMappingIndexRequest);

		Map<String, String> fieldMappings =
			getFieldMappingIndexResponse.getFieldMappings();

		String fieldMapping = fieldMappings.get(indexName);

		try {
			JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
				fieldMapping);

			JSONObject assetVocabularyCategoryIdsJSONObject1 =
				jsonObject.getJSONObject("assetVocabularyCategoryIds");

			if (assetVocabularyCategoryIdsJSONObject1 == null) {
				return _putMapping(indexName);
			}

			JSONObject assetVocabularyCategoryIdsJSONObject2 =
				assetVocabularyCategoryIdsJSONObject1.getJSONObject(
					"assetVocabularyCategoryIds");

			String type = assetVocabularyCategoryIdsJSONObject2.getString(
				"type");

			if (!type.equals("keyword")) {
				_log.error(
					StringBundler.concat(
						"Mapping for assetVocabularyCategoryIds is not of ",
						"keyword type in index ", indexName, ". A full ",
						"reindex may be necessary."));

				return false;
			}
		}
		catch (JSONException jsonException) {
			_log.error(jsonException);

			return false;
		}

		return true;
	}

	private boolean _putMapping(String indexName) {
		PutMappingIndexRequest putMappingIndexRequest =
			new PutMappingIndexRequest(
				new String[] {indexName},
				LiferayTypeMappingsConstants.LIFERAY_DOCUMENT_TYPE,
				"{\"properties\":{\"assetVocabularyCategoryIds\":{\"store\":" +
					"true,\"type\":\"keyword\"}}}");

		PutMappingIndexResponse putMappingIndexResponse =
			_searchEngineAdapter.execute(putMappingIndexRequest);

		if (!putMappingIndexResponse.isAcknowledged()) {
			_log.error(
				StringBundler.concat(
					"Unable to put mapping for assetVocabularyCategoryIds ",
					"field in index ", indexName, ". A full reindex may be ",
					"necessary."));

			return false;
		}

		return true;
	}

	private void _updateDocuments(SearchHit[] hits, String indexName) {
		BulkDocumentRequest bulkDocumentRequest = new BulkDocumentRequest();

		for (SearchHit hit : hits) {
			Map<String, DocumentField> fields = hit.getFields();

			DocumentField entryClassNameDocumentField = fields.get(
				Field.ENTRY_CLASS_NAME);
			DocumentField entryClassPKDocumentField = fields.get(
				Field.ENTRY_CLASS_PK);
			DocumentField uidDocumentField = fields.get(Field.UID);

			List<AssetCategory> assetCategories =
				_assetCategoryLocalService.getCategories(
					entryClassNameDocumentField.getValue(),
					GetterUtil.getLong(entryClassPKDocumentField.getValue()));

			String[] assetVocabularyCategoryIds =
				new String[assetCategories.size()];

			for (int i = 0; i < assetCategories.size(); i++) {
				AssetCategory assetCategory = assetCategories.get(i);

				assetVocabularyCategoryIds[i] =
					assetCategory.getVocabularyId() + StringPool.DASH +
						assetCategory.getCategoryId();
			}

			DocumentBuilder documentBuilder = _documentBuilderFactory.builder();

			documentBuilder.setStrings(
				"assetVocabularyCategoryIds", assetVocabularyCategoryIds);

			UpdateDocumentRequest updateDocumentRequest =
				new UpdateDocumentRequest(
					indexName, uidDocumentField.getValue(),
					documentBuilder.build());

			updateDocumentRequest.setType(DocumentTypes.LIFERAY);

			bulkDocumentRequest.addBulkableDocumentRequest(
				updateDocumentRequest);
		}

		_searchEngineAdapter.execute(bulkDocumentRequest);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AssetVocabularyCategoryIdsInitializationBackgroundTaskExecutor.class);

	@Reference
	private Aggregations _aggregations;

	@Reference
	private AssetCategoryLocalService _assetCategoryLocalService;

	@Reference
	private AssetEntryAssetCategoryRelLocalService
		_assetEntryAssetCategoryRelLocalService;

	@Reference
	private BackgroundTaskManager _backgroundTaskManager;

	@Reference
	private DocumentBuilderFactory _documentBuilderFactory;

	@Reference
	private Queries _queries;

	private ElasticsearchConnectionManager _elasticsearchConnectionManager;

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

}