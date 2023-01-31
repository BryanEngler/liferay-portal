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

package com.liferay.search.experiences.internal.ml.text.embedding;

import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskResult;
import com.liferay.portal.kernel.backgroundtask.BaseBackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.display.BackgroundTaskDisplay;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentImpl;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.search.document.DocumentBuilder;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.BulkDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.BulkableDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.UpdateDocumentRequest;
import com.liferay.portal.search.engine.adapter.search.ClosePointInTimeRequest;
import com.liferay.portal.search.engine.adapter.search.OpenPointInTimeRequest;
import com.liferay.portal.search.engine.adapter.search.OpenPointInTimeResponse;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.index.IndexNameBuilder;
import com.liferay.portal.search.legacy.document.DocumentBuilderFactory;
import com.liferay.portal.search.pit.PointInTime;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.sort.Sorts;
import com.liferay.search.experiences.configuration.SemanticSearchConfiguration;

import java.io.IOException;
import java.io.Serializable;

import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Gustavo Lima
 */
@Component(
	enabled = false, immediate = true,
	property = "background.task.executor.class.name=com.liferay.search.experiences.internal.ml.text.embedding.TextEmbeddingBackgroundTaskExecutor",
	service = BackgroundTaskExecutor.class
)
public class TextEmbeddingBackgroundTaskExecutor
	extends BaseBackgroundTaskExecutor {

	@Override
	public BackgroundTaskExecutor clone() {
		return this;
	}

	@Override
	public BackgroundTaskResult execute(BackgroundTask backgroundTask) {
		Map<String, Serializable> taskContextMap =
			backgroundTask.getTaskContextMap();

		long[] companyIds = (long[])taskContextMap.get("companyIds");

		_companyLocalService.forEachCompanyId(
			companyId -> _reindexTextEmbeddings(companyId), companyIds);

		return BackgroundTaskResult.SUCCESS;
	}

	@Override
	public BackgroundTaskDisplay getBackgroundTaskDisplay(
		BackgroundTask backgroundTask) {

		return null;
	}

	private void _contribute(
			String entryClassName, Document document)
		throws PortalException {

		document.add(new Field("testField", "test text " + entryClassName));
	}

	private BooleanQuery _createQuery() {
		BooleanQuery booleanQuery = _queries.booleanQuery();

		for (String name :
			_semanticSearchConfiguration.assetEntryClassNames()) {

			booleanQuery.addShouldQueryClauses(
				_queries.term("entryClassName", name));
		}

		return booleanQuery;
	}

	private SearchSearchRequest _createSearchRequest(PointInTime pointInTime) {
		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.addSorts(_sorts.field("_shard_doc"));
		searchSearchRequest.setPointInTime(pointInTime);
		searchSearchRequest.setQuery(_createQuery());
		searchSearchRequest.setSelectedFieldNames(Field.ENTRY_CLASS_NAME);
		searchSearchRequest.setSize(10);

		return searchSearchRequest;
	}

	private UpdateDocumentRequest _createUpdateDocumentRequest(
		String indexName, String uid, DocumentBuilder documentBuilder) {

		return new UpdateDocumentRequest(
			indexName, uid, documentBuilder.build());
	}

	private SemanticSearchConfiguration _getSemanticSearchConfiguration(
		long companyId) {

		try {
			return _configurationProvider.getCompanyConfiguration(
				SemanticSearchConfiguration.class, companyId);
		}
		catch (ConfigurationException configurationException) {
			return ReflectionUtil.throwException(configurationException);
		}
	}

	private void _indexTextEmbeddings(String indexName)
		throws IOException {

		if (_log.isInfoEnabled()) {
			_log.info(
				"Started indexing of the text embedding fields for index " +
					indexName);
		}

		OpenPointInTimeRequest openPointInTimeRequest =
			new OpenPointInTimeRequest();

		openPointInTimeRequest.setIndices(indexName);
		openPointInTimeRequest.setKeepAliveMinutes(1);

		OpenPointInTimeResponse openPointInTimeResponse =
			_searchEngineAdapter.execute(openPointInTimeRequest);

		PointInTime pointInTime = new PointInTime(
			openPointInTimeResponse.pitId());

		pointInTime.setKeepAlive("1m");

		SearchSearchRequest searchSearchRequest = _createSearchRequest(
			pointInTime);

		SearchSearchResponse searchSearchResponse =
			_searchEngineAdapter.execute(searchSearchRequest);

		SearchHits searchHits = searchSearchResponse.getSearchHits();

		List<SearchHit> searchHitsList = searchHits.getSearchHits();

		while (!searchHitsList.isEmpty()) {
			_updateDocuments(indexName, searchHitsList);

			SearchHit lastSearchHit = searchHitsList.get(
				searchHitsList.size() - 1);

			searchSearchRequest.setSearchAfter(lastSearchHit.getSortValues());

			searchSearchResponse = _searchEngineAdapter.execute(
				searchSearchRequest);

			searchHits = searchSearchResponse.getSearchHits();

			searchHitsList = searchHits.getSearchHits();
		}

		if (_log.isInfoEnabled()) {
			_log.info(
				"Finished indexing of the text embedding fields for index " +
					indexName);
		}

		ClosePointInTimeRequest closePointInTimeRequest =
			new ClosePointInTimeRequest(openPointInTimeResponse.pitId());

		_searchEngineAdapter.execute(closePointInTimeRequest);
	}

	private void _reindexTextEmbeddings(long companyId) {
		String indexName = _indexNameBuilder.getIndexName(companyId);

		try {
			_semanticSearchConfiguration = _getSemanticSearchConfiguration(
				companyId);

			if (_semanticSearchConfiguration.textEmbeddingsEnabled()) {
				if (_log.isInfoEnabled()) {
					_log.info(
						StringBundler.concat(
							"Start reindexing company ", companyId,
							" for text embedding"));
				}

				_indexTextEmbeddings(indexName);
			}
			else {
				if (_log.isInfoEnabled()) {
					_log.info(
						"text embedding is disabled for company " + companyId);
				}
			}
		}
		catch (IOException ioException) {
			_log.error(
				StringBundler.concat(
					"Unable to index textEmbedding values in index ", indexName,
					". A full reindex may be necessary."),
				ioException);
		}
		finally {
			if (_log.isInfoEnabled()) {
				_log.info("Finished reindexing company " + companyId);
			}
		}
	}

	private void _updateDocuments(
		String indexName, List<SearchHit> searchHitsList) {

		BulkDocumentRequest bulkDocumentRequest = new BulkDocumentRequest();

		for (SearchHit hit : searchHitsList) {
			com.liferay.portal.search.document.Document document =
				hit.getDocument();

			DocumentImpl documentImpl = new DocumentImpl();

			try {
				_contribute(
					document.getString(Field.ENTRY_CLASS_NAME),	documentImpl);

				bulkDocumentRequest.addBulkableDocumentRequest(
					_createUpdateDocumentRequest(
						indexName, document.getString(Field.UID),
						_documentBuilderFactory.builder(documentImpl)));
			}
			catch (PortalException portalException) {
				_log.error(portalException);
			}
		}

		List<BulkableDocumentRequest<?>> bulkableDocumentRequests =
			bulkDocumentRequest.getBulkableDocumentRequests();

		if (!bulkableDocumentRequests.isEmpty()) {
			_searchEngineAdapter.execute(bulkDocumentRequest);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		TextEmbeddingBackgroundTaskExecutor.class);

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private ConfigurationProvider _configurationProvider;

	@Reference
	private DocumentBuilderFactory _documentBuilderFactory;

	@Reference
	private IndexNameBuilder _indexNameBuilder;

	@Reference
	private Queries _queries;

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

	private volatile SemanticSearchConfiguration _semanticSearchConfiguration;

	@Reference
	private Sorts _sorts;

}