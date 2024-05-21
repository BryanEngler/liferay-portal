/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch7.internal;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.search.BaseIndexWriter;
import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.IndexWriter;
import com.liferay.portal.kernel.search.ParseException;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.search.filter.BooleanFilter;
import com.liferay.portal.kernel.search.filter.TermFilter;
import com.liferay.portal.kernel.search.generic.BooleanQueryImpl;
import com.liferay.portal.kernel.search.generic.MatchAllQuery;
import com.liferay.portal.kernel.search.suggest.SpellCheckIndexWriter;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.util.PortalRunMode;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.elasticsearch7.internal.configuration.ElasticsearchConfigurationWrapper;
import com.liferay.portal.search.elasticsearch7.internal.logging.ElasticsearchExceptionHandler;
import com.liferay.portal.search.elasticsearch7.internal.util.DocumentTypes;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.BulkDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.BulkDocumentResponse;
import com.liferay.portal.search.engine.adapter.document.DeleteByQueryDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.DeleteDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.UpdateDocumentRequest;
import com.liferay.portal.search.engine.adapter.index.RefreshIndexRequest;
import com.liferay.portal.search.index.IndexNameBuilder;
import com.liferay.portal.search.script.Script;
import com.liferay.portal.search.script.ScriptType;
import com.liferay.portal.search.script.Scripts;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael C. Han
 * @author Milen Dyankov
 */
@Component(
	property = "search.engine.impl=Elasticsearch", service = IndexWriter.class
)
public class ElasticsearchIndexWriter extends BaseIndexWriter {

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link
	 *             #indexDocument(SearchContext, Document)}
	 */
	@Deprecated
	@Override
	public void addDocument(SearchContext searchContext, Document document) {
		indexDocument(searchContext, document);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link
	 *             #indexDocuments(SearchContext, Collection)}
	 */
	@Deprecated
	@Override
	public void addDocuments(
		SearchContext searchContext, Collection<Document> documents) {

		indexDocuments(searchContext, documents);
	}

	@Override
	public void commit(SearchContext searchContext) {
		for (String indexName : _getIndexNames(searchContext)) {
			RefreshIndexRequest refreshIndexRequest = new RefreshIndexRequest(
				indexName);

			try {
				_searchEngineAdapter.execute(refreshIndexRequest);
			}
			catch (RuntimeException runtimeException) {
				if (_elasticsearchConfigurationWrapper.logExceptionsOnly()) {
					_log.error(runtimeException);
				}
				else {
					throw runtimeException;
				}
			}
		}
	}

	@Override
	public void deleteDocument(SearchContext searchContext, String uid) {
		for (String indexName : _getIndexNames(searchContext)) {
			DeleteDocumentRequest deleteDocumentRequest =
				new DeleteDocumentRequest(indexName, uid);

			if (PortalRunMode.isTestMode() ||
				searchContext.isCommitImmediately()) {

				deleteDocumentRequest.setRefresh(true);
			}

			deleteDocumentRequest.setType(DocumentTypes.LIFERAY);

			try {
				_searchEngineAdapter.execute(deleteDocumentRequest);
			}
			catch (RuntimeException runtimeException) {
				ElasticsearchExceptionHandler elasticsearchExceptionHandler =
					new ElasticsearchExceptionHandler(
						_log,
						_elasticsearchConfigurationWrapper.logExceptionsOnly());

				elasticsearchExceptionHandler.handleDeleteDocumentException(
					runtimeException);
			}
		}
	}

	@Override
	public void deleteDocuments(
		SearchContext searchContext, Collection<String> uids) {

		BulkDocumentRequest bulkDocumentRequest = new BulkDocumentRequest();

		if (PortalRunMode.isTestMode() || searchContext.isCommitImmediately()) {
			bulkDocumentRequest.setRefresh(true);
		}

		for (String indexName : _getIndexNames(searchContext)) {
			uids.forEach(
				uid -> {
					DeleteDocumentRequest deleteDocumentRequest =
						new DeleteDocumentRequest(indexName, uid);

					deleteDocumentRequest.setType(DocumentTypes.LIFERAY);

					bulkDocumentRequest.addBulkableDocumentRequest(
						deleteDocumentRequest);
				});
		}

		BulkDocumentResponse bulkDocumentResponse =
			_searchEngineAdapter.execute(bulkDocumentRequest);

		if (bulkDocumentResponse.hasErrors()) {
			if (_elasticsearchConfigurationWrapper.logExceptionsOnly()) {
				_log.error("Bulk delete failed");
			}
			else {
				throw new SystemException("Bulk delete failed");
			}
		}
	}

	@Override
	public void deleteEntityDocuments(
		SearchContext searchContext, String className) {

		for (String indexName : _getIndexNames(searchContext)) {
			try {
				BooleanQuery booleanQuery = new BooleanQueryImpl();

				booleanQuery.add(new MatchAllQuery(), BooleanClauseOccur.MUST);

				BooleanFilter booleanFilter = new BooleanFilter();

				booleanFilter.add(
					new TermFilter(Field.ENTRY_CLASS_NAME, className),
					BooleanClauseOccur.MUST);

				booleanQuery.setPreBooleanFilter(booleanFilter);

				DeleteByQueryDocumentRequest deleteByQueryDocumentRequest =
					new DeleteByQueryDocumentRequest(booleanQuery, indexName);

				if (PortalRunMode.isTestMode() ||
					searchContext.isCommitImmediately()) {

					deleteByQueryDocumentRequest.setRefresh(true);
				}

				_searchEngineAdapter.execute(deleteByQueryDocumentRequest);
			}
			catch (ParseException parseException) {
				throw new SystemException(parseException);
			}
			catch (RuntimeException runtimeException) {
				if (_elasticsearchConfigurationWrapper.logExceptionsOnly()) {
					_log.error(runtimeException);
				}
				else {
					throw runtimeException;
				}
			}
		}
	}

	@Override
	public void indexDocument(SearchContext searchContext, Document document) {
		indexDocuments(searchContext, Collections.singleton(document));
	}

	@Override
	public void indexDocuments(
		SearchContext searchContext, Collection<Document> documents) {

		BulkDocumentRequest bulkDocumentRequest = new BulkDocumentRequest();

		if (PortalRunMode.isTestMode() || searchContext.isCommitImmediately()) {
			bulkDocumentRequest.setRefresh(true);
		}

		for (String indexName : _getIndexNames(searchContext)) {
			documents.forEach(
				document -> {
					IndexDocumentRequest indexDocumentRequest =
						new IndexDocumentRequest(indexName, document);

					indexDocumentRequest.setType(DocumentTypes.LIFERAY);

					bulkDocumentRequest.addBulkableDocumentRequest(
						indexDocumentRequest);
				});
		}

		BulkDocumentResponse bulkDocumentResponse =
			_searchEngineAdapter.execute(bulkDocumentRequest);

		if (bulkDocumentResponse.hasErrors()) {
			if (_elasticsearchConfigurationWrapper.logExceptionsOnly()) {
				_log.error("Bulk add failed");
			}
			else {
				throw new SystemException("Bulk add failed");
			}
		}
	}

	@Override
	public void partiallyUpdateDocument(
		SearchContext searchContext, Document document) {

		for (String indexName : _getIndexNames(searchContext)) {
			UpdateDocumentRequest updateDocumentRequest =
				new UpdateDocumentRequest(
					indexName, document.getUID(), document);

			updateDocumentRequest.setType(DocumentTypes.LIFERAY);

			if (PortalRunMode.isTestMode() ||
				searchContext.isCommitImmediately()) {

				updateDocumentRequest.setRefresh(true);
			}

			updateDocumentRequest.setUpsert(true);

			try {
				_searchEngineAdapter.execute(updateDocumentRequest);
			}
			catch (RuntimeException runtimeException) {
				if (_elasticsearchConfigurationWrapper.logExceptionsOnly()) {
					_log.error(runtimeException);
				}
				else {
					throw runtimeException;
				}
			}
		}
	}

	@Override
	public void partiallyUpdateDocuments(
		SearchContext searchContext, Collection<Document> documents) {

		BulkDocumentRequest bulkDocumentRequest = new BulkDocumentRequest();

		if (PortalRunMode.isTestMode() || searchContext.isCommitImmediately()) {
			bulkDocumentRequest.setRefresh(true);
		}

		for (String indexName : _getIndexNames(searchContext)) {
			documents.forEach(
				document -> {
					UpdateDocumentRequest updateDocumentRequest =
						new UpdateDocumentRequest(
							indexName, document.getUID(), document);

					updateDocumentRequest.setType(DocumentTypes.LIFERAY);

					bulkDocumentRequest.addBulkableDocumentRequest(
						updateDocumentRequest);
				});
		}

		BulkDocumentResponse bulkDocumentResponse =
			_searchEngineAdapter.execute(bulkDocumentRequest);

		if (bulkDocumentResponse.hasErrors()) {
			if (_elasticsearchConfigurationWrapper.logExceptionsOnly()) {
				_log.error("Bulk partial update failed");
			}
			else {
				throw new SystemException("Bulk partial update failed");
			}
		}
	}

	@Override
	public void removeFieldsFromDocument(
			SearchContext searchContext, Document document, String... fields)
		throws SearchException {

		removeFieldsFromDocuments(
			searchContext, Collections.singleton(document), fields);
	}

	@Override
	public void removeFieldsFromDocuments(
			SearchContext searchContext, Collection<Document> documents,
			String... fields)
		throws SearchException {

		BulkDocumentRequest bulkDocumentRequest = new BulkDocumentRequest();

		if (PortalRunMode.isTestMode() || searchContext.isCommitImmediately()) {
			bulkDocumentRequest.setRefresh(true);
		}

		Script script = _scripts.builder(
		).idOrCode(
			"for (field in params.fields) { ctx._source.remove(field) }"
		).language(
			"painless"
		).putParameter(
			"fields", fields
		).scriptType(
			ScriptType.INLINE
		).build();

		for (String indexName : _getIndexNames(searchContext)) {
			documents.forEach(
				document -> {
					UpdateDocumentRequest updateDocumentRequest =
						new UpdateDocumentRequest(
							indexName, document.getUID(), script);

					updateDocumentRequest.setType(DocumentTypes.LIFERAY);

					bulkDocumentRequest.addBulkableDocumentRequest(
						updateDocumentRequest);
				});
		}

		BulkDocumentResponse bulkDocumentResponse =
			_searchEngineAdapter.execute(bulkDocumentRequest);

		if (bulkDocumentResponse.hasErrors()) {
			if (_elasticsearchConfigurationWrapper.logExceptionsOnly()) {
				_log.error("Bulk partial update failed");
			}
			else {
				throw new SystemException("Bulk partial update failed");
			}
		}
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link
	 *             #indexDocument(SearchContext, Document)}
	 */
	@Deprecated
	@Override
	public void updateDocument(SearchContext searchContext, Document document) {
		indexDocument(searchContext, document);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link
	 *             #indexDocuments(SearchContext, Collection)}
	 */
	@Deprecated
	@Override
	public void updateDocuments(
		SearchContext searchContext, Collection<Document> documents) {

		indexDocuments(searchContext, documents);
	}

	@Override
	protected SpellCheckIndexWriter getSpellCheckIndexWriter() {
		return _spellCheckIndexWriter;
	}

	private String _getIndexNameNext(long companyId) {
		Company company = _companyLocalService.fetchCompany(companyId);

		if (company == null) {
			return null;
		}

		String indexNameNext = company.getIndexNameNext();

		if (Validator.isBlank(indexNameNext)) {
			return null;
		}

		return indexNameNext;
	}

	private Set<String> _getIndexNames(SearchContext searchContext) {
		Set<String> indexNames = new HashSet<>();

		String indexNameCurrent = _indexNameBuilder.getIndexName(
			searchContext.getCompanyId());

		indexNames.add(indexNameCurrent);

		String indexNameNext = _getIndexNameNext(searchContext.getCompanyId());

		if (indexNameNext != null) {
			indexNames.add(indexNameNext);
		}

		return indexNames;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ElasticsearchIndexWriter.class);

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private ElasticsearchConfigurationWrapper
		_elasticsearchConfigurationWrapper;

	@Reference
	private IndexNameBuilder _indexNameBuilder;

	@Reference
	private Scripts _scripts;

	@Reference(target = "(search.engine.impl=Elasticsearch)")
	private SearchEngineAdapter _searchEngineAdapter;

	@Reference(target = "(search.engine.impl=Elasticsearch)")
	private SpellCheckIndexWriter _spellCheckIndexWriter;

}