/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch7.internal;

import com.liferay.petra.string.StringBundler;
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
import com.liferay.portal.search.elasticsearch7.internal.util.DocumentTypes;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.BulkDocumentItemResponse;
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
	 *             #indexDocument(long, boolean, Document)}
	 */
	@Deprecated
	@Override
	public void addDocument(SearchContext searchContext, Document document) {
		indexDocument(
			searchContext.getCompanyId(), searchContext.isCommitImmediately(),
			document);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link
	 *             #indexDocuments(long, boolean, Collection)}
	 */
	@Deprecated
	@Override
	public void addDocuments(
		SearchContext searchContext, Collection<Document> documents) {

		indexDocuments(
			searchContext.getCompanyId(), searchContext.isCommitImmediately(),
			documents);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link #commit(long)}
	 */
	@Deprecated
	@Override
	public void commit(SearchContext searchContext) {
		commit(searchContext.getCompanyId());
	}

	@Override
	public void commit(long companyId) {
		for (String indexName : _getIndexNames(companyId)) {
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

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link
	 *             #deleteDocument(long, boolean, String)}
	 */
	@Deprecated
	@Override
	public void deleteDocument(SearchContext searchContext, String uid) {
		deleteDocument(
			searchContext.getCompanyId(), searchContext.isCommitImmediately(),
			uid);
	}

	@Override
	public void deleteDocument(
		long companyId, boolean commitImmediately, String uid) {

		_deleteDocuments(
			companyId, commitImmediately, Collections.singleton(uid), true);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link
	 *             #deleteDocuments(long, boolean, Collection)}
	 */
	@Deprecated
	@Override
	public void deleteDocuments(
		SearchContext searchContext, Collection<String> uids) {

		deleteDocuments(
			searchContext.getCompanyId(), searchContext.isCommitImmediately(),
			uids);
	}

	@Override
	public void deleteDocuments(
		long companyId, boolean commitImmediately, Collection<String> uids) {

		_deleteDocuments(companyId, commitImmediately, uids, false);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link
	 *             #deleteEntityDocuments(long, boolean, String)}
	 */
	@Override
	public void deleteEntityDocuments(
		SearchContext searchContext, String className) {

		deleteEntityDocuments(
			searchContext.getCompanyId(), searchContext.isCommitImmediately(),
			className);
	}

	@Override
	public void deleteEntityDocuments(
		long companyId, boolean commitImmediately, String className) {

		for (String indexName : _getIndexNames(companyId)) {
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

				if (PortalRunMode.isTestMode() || commitImmediately) {
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
	public void indexDocument(
		long companyId, boolean commitImmediately, Document document) {

		indexDocuments(
			companyId, commitImmediately, Collections.singleton(document));
	}

	@Override
	public void indexDocuments(
		long companyId, boolean commitImmediately,
		Collection<Document> documents) {

		BulkDocumentRequest bulkDocumentRequest = _getBulkDocumentRequest(
			commitImmediately);

		for (String indexName : _getIndexNames(companyId)) {
			documents.forEach(
				document -> {
					IndexDocumentRequest indexDocumentRequest =
						new IndexDocumentRequest(indexName, document);

					indexDocumentRequest.setType(DocumentTypes.LIFERAY);

					bulkDocumentRequest.addBulkableDocumentRequest(
						indexDocumentRequest);
				});
		}

		_execute(bulkDocumentRequest);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link
	 *             #partiallyUpdateDocument(long, boolean, Document)}
	 */
	@Deprecated
	@Override
	public void partiallyUpdateDocument(
		SearchContext searchContext, Document document) {

		partiallyUpdateDocument(
			searchContext.getCompanyId(), searchContext.isCommitImmediately(),
			document);
	}

	@Override
	public void partiallyUpdateDocument(
		long companyId, boolean commitImmediately, Document document) {

		partiallyUpdateDocuments(
			companyId, commitImmediately, Collections.singleton(document));
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link
	 *             #partiallyUpdateDocuments(long, boolean, Collection)}
	 */
	@Deprecated
	@Override
	public void partiallyUpdateDocuments(
		SearchContext searchContext, Collection<Document> documents) {

		partiallyUpdateDocuments(
			searchContext.getCompanyId(), searchContext.isCommitImmediately(),
			documents);
	}

	@Override
	public void partiallyUpdateDocuments(
		long companyId, boolean commitImmediately,
		Collection<Document> documents) {

		BulkDocumentRequest bulkDocumentRequest = _getBulkDocumentRequest(
			commitImmediately);

		for (String indexName : _getIndexNames(companyId)) {
			documents.forEach(
				document -> {
					UpdateDocumentRequest updateDocumentRequest =
						new UpdateDocumentRequest(
							indexName, document.getUID(), document);

					updateDocumentRequest.setType(DocumentTypes.LIFERAY);
					updateDocumentRequest.setUpsert(true);

					bulkDocumentRequest.addBulkableDocumentRequest(
						updateDocumentRequest);
				});
		}

		_execute(bulkDocumentRequest);
	}

	@Override
	public void removeFieldsFromDocument(
			long companyId, boolean commitImmediately, Document document,
			String... fields)
		throws SearchException {

		removeFieldsFromDocuments(
			companyId, commitImmediately, Collections.singleton(document),
			fields);
	}

	@Override
	public void removeFieldsFromDocuments(
			long companyId, boolean commitImmediately,
			Collection<Document> documents, String... fields)
		throws SearchException {

		BulkDocumentRequest bulkDocumentRequest = _getBulkDocumentRequest(
			commitImmediately);

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

		for (String indexName : _getIndexNames(companyId)) {
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

		_execute(bulkDocumentRequest);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link
	 *             #indexDocument(long, boolean, Document)}
	 */
	@Deprecated
	@Override
	public void updateDocument(SearchContext searchContext, Document document) {
		indexDocument(
			searchContext.getCompanyId(), searchContext.isCommitImmediately(),
			document);
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link
	 *             #indexDocuments(long, boolean, Collection)}
	 */
	@Deprecated
	@Override
	public void updateDocuments(
		SearchContext searchContext, Collection<Document> documents) {

		indexDocuments(
			searchContext.getCompanyId(), searchContext.isCommitImmediately(),
			documents);
	}

	@Override
	protected SpellCheckIndexWriter getSpellCheckIndexWriter() {
		return _spellCheckIndexWriter;
	}

	private void _deleteDocuments(
		long companyId, boolean commitImmediately, Collection<String> uids,
		boolean singleDeleteRequest) {

		BulkDocumentRequest bulkDocumentRequest = _getBulkDocumentRequest(
			commitImmediately);

		for (String indexName : _getIndexNames(companyId)) {
			uids.forEach(
				uid -> {
					DeleteDocumentRequest deleteDocumentRequest =
						new DeleteDocumentRequest(indexName, uid);

					deleteDocumentRequest.setType(DocumentTypes.LIFERAY);

					bulkDocumentRequest.addBulkableDocumentRequest(
						deleteDocumentRequest);
				});
		}

		_execute(bulkDocumentRequest, singleDeleteRequest);
	}

	private void _execute(BulkDocumentRequest bulkDocumentRequest) {
		_execute(bulkDocumentRequest, false);
	}

	private void _execute(
		BulkDocumentRequest bulkDocumentRequest, boolean singleDeleteRequest) {

		BulkDocumentResponse bulkDocumentResponse =
			_searchEngineAdapter.execute(bulkDocumentRequest);

		if (bulkDocumentResponse.hasErrors()) {
			String failureMessages = _getBulkDocumentResponseFailureMessages(
				bulkDocumentResponse);

			if (singleDeleteRequest &&
				failureMessages.contains(_INDEX_NOT_FOUND_EXCEPTION_MESSAGE)) {

				if (_log.isInfoEnabled()) {
					_log.info(failureMessages);
				}
			}
			else {
				String errorMessage = "Bulk request failed: " + failureMessages;

				if (_elasticsearchConfigurationWrapper.logExceptionsOnly()) {
					_log.error(errorMessage);
				}
				else {
					throw new SystemException(errorMessage);
				}
			}
		}
	}

	private BulkDocumentRequest _getBulkDocumentRequest(
		boolean isCommitImmediately) {

		BulkDocumentRequest bulkDocumentRequest = new BulkDocumentRequest();

		if (PortalRunMode.isTestMode() || isCommitImmediately) {
			bulkDocumentRequest.setRefresh(true);
		}

		return bulkDocumentRequest;
	}

	private String _getBulkDocumentResponseFailureMessages(
		BulkDocumentResponse bulkDocumentResponse) {

		StringBundler sb = new StringBundler();

		for (BulkDocumentItemResponse bulkDocumentItemResponse :
				bulkDocumentResponse.getBulkDocumentItemResponses()) {

			if (bulkDocumentItemResponse.getFailureMessage() != null) {
				if (sb.length() > 0) {
					sb.append(", ");
				}

				sb.append(bulkDocumentItemResponse.getFailureMessage());
			}
		}

		return sb.toString();
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

	private Set<String> _getIndexNames(long companyId) {
		Set<String> indexNames = new HashSet<>();

		String indexNameCurrent = _indexNameBuilder.getIndexName(companyId);

		indexNames.add(indexNameCurrent);

		String indexNameNext = _getIndexNameNext(companyId);

		if (indexNameNext != null) {
			indexNames.add(indexNameNext);
		}

		return indexNames;
	}

	private static final String _INDEX_NOT_FOUND_EXCEPTION_MESSAGE =
		"type=index_not_found_exception";

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