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

import com.liferay.blogs.model.BlogsEntry;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.knowledge.base.model.KBArticle;
import com.liferay.message.boards.model.MBMessage;
import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerList;
import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerListFactory;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskResult;
import com.liferay.portal.kernel.backgroundtask.BaseBackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.display.BackgroundTaskDisplay;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentImpl;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.search.document.DocumentBuilder;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.BulkDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.UpdateDocumentRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.index.TextEmbeddigHelper;
import com.liferay.portal.search.legacy.document.DocumentBuilderFactory;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;
import com.liferay.search.experiences.configuration.SemanticSearchConfiguration;
import com.liferay.wiki.model.WikiPage;

import java.io.IOException;
import java.io.Serializable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author André de Oliveira
 */
@Component(
	enabled = false, immediate = true,
	property = "background.task.executor.class.name=com.liferay.search.experiences.internal.ml.text.embedding.TextEmbeddingBackgroundTaskExecutor",
	service = {BackgroundTaskExecutor.class, TextEmbeddigHelper.class}
)
public class TextEmbeddingBackgroundTaskExecutor
	extends BaseBackgroundTaskExecutor implements TextEmbeddigHelper {

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
			_indexTextEmbbeding(indexName);
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

	@Activate
	protected void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;
	}

	private void _indexTextEmbbeding(String indexName) throws IOException {
		if (_log.isInfoEnabled()) {
			_log.info(
				"Started indexing of the Text Embbeding field for index " +
					indexName);
		}

		BooleanQuery booleanQueryLang = _queries.booleanQuery();

		List<String> languageIds = Arrays.asList(
			_semanticSearchConfiguration.languageIds());

		for (String lang : languageIds) {
			booleanQueryLang.addShouldQueryClauses(
				_queries.exists("text_embedding_256_" + lang));
			booleanQueryLang.addShouldQueryClauses(
				_queries.exists("text_embedding_512_" + lang));
			booleanQueryLang.addShouldQueryClauses(
				_queries.exists("text_embedding_768_" + lang));
		}

		String[] classNames =
			_semanticSearchConfiguration.assetEntryClassNames();

		BooleanQuery booleanQueryClassName = _queries.booleanQuery();

		for (String name : classNames) {
			booleanQueryClassName.addShouldQueryClauses(
				_queries.term("entryClassName", name));
		}

		BooleanQuery finalBooleanQuery = _queries.booleanQuery();

		finalBooleanQuery.addMustQueryClauses(booleanQueryLang);

		finalBooleanQuery.addFilterQueryClauses(booleanQueryClassName);

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setIndexNames(indexName);

		searchSearchRequest.setQuery(finalBooleanQuery);

		searchSearchRequest.setSize(10000);

		searchSearchRequest.setSelectedFieldNames(
			Field.UID, Field.ENTRY_CLASS_NAME, Field.ENTRY_CLASS_PK);

		SearchSearchResponse searchSearchResponse =
			_searchEngineAdapter.execute(searchSearchRequest);

		SearchHits searchHits = searchSearchResponse.getSearchHits();

		List<SearchHit> hits = searchHits.getSearchHits();

		while (!hits.isEmpty()) {
			// Maybe it works but not sure, need to test
			// I would test the SolrIndexSearcher way to do it
			_updateDocuments(hits, indexName);

			searchSearchRequest.setStart(searchSearchRequest.getStart() + 10000);

			searchSearchResponse =
				_searchEngineAdapter.execute(searchSearchRequest);

			searchHits = searchSearchResponse.getSearchHits();

			hits = searchHits.getSearchHits();

			if (_log.isInfoEnabled()) {
				_log.info(
					"Indexed " + searchSearchRequest.getStart() +
						" documents");
			}
		}

		if (_log.isInfoEnabled()) {
			_log.info(
				"Finished indexing of the Text Embbeded field for index " +
					indexName);
		}
	}

	private void _updateDocuments(List<SearchHit> hits, String indexName) {
		BulkDocumentRequest bulkDocumentRequest = new BulkDocumentRequest();

		for (SearchHit hit : hits) {
			com.liferay.portal.search.document.Document portalSearchDocument = // that type change will cause problems?
				hit.getDocument();

			String entryClassName = portalSearchDocument.getString(
				Field.ENTRY_CLASS_NAME);
			long entryClassPK = portalSearchDocument.getLong(
				Field.ENTRY_CLASS_PK);
			String uid = portalSearchDocument.getString(Field.UID);

			Document portalKernelDocument = new DocumentImpl();

			ServiceTrackerList<ModelDocumentContributor>
				modelDocumentContributors =
					ServiceTrackerListFactory.open(
						_bundleContext, ModelDocumentContributor.class,
						"(&(indexer.class.name=" + entryClassName +
							")(text.embedding.contributor=true)");

			if (entryClassName.equals(BlogsEntry.class.getName())) {
//				List<JournalArticle> journalArticles =
//					_journalArticleLocalService.getArticlesByResourcePrimKey(
//						GetterUtil.getLong(entryClassPKField.getValue()));
//
//				modelDocumentContributors.forEach(
//					modelDocumentContributor -> {
//						for (JournalArticle journalArticle : journalArticles) {
//							String articleId = journalArticle.getArticleId();
//
//							if (articleId.equals(entryClassPKField.getValue())) {
//								modelDocumentContributor.contribute(
//									portalKernelDocument, journalArticle);
//							}
//						}
//					}
//				);
			}
			else if (entryClassName.equals(JournalArticle.class.getName())) {
				List<JournalArticle> journalArticles =
					_journalArticleLocalService.getArticlesByResourcePrimKey(
						entryClassPK);

				modelDocumentContributors.forEach(
					modelDocumentContributor -> {
						for (JournalArticle journalArticle : journalArticles) {
							String articleId = journalArticle.getArticleId();

							if (articleId.equals(String.valueOf(entryClassPK))) {
								modelDocumentContributor.contribute(
									portalKernelDocument, journalArticle);
							}
						}
					}
				);
			}
			else if (entryClassName.equals(KBArticle.class.getName())) {
//				List<JournalArticle> journalArticles =
//					_journalArticleLocalService.getArticlesByResourcePrimKey(
//						GetterUtil.getLong(entryClassPKField.getValue()));
//
//				modelDocumentContributors.forEach(
//					modelDocumentContributor -> {
//						for (JournalArticle journalArticle : journalArticles) {
//							String articleId = journalArticle.getArticleId();
//
//							if (articleId.equals(entryClassPKField.getValue())) {
//								modelDocumentContributor.contribute(
//									portalKernelDocument, journalArticle);
//							}
//						}
//					}
//				);
			}
			else if (entryClassName.equals(MBMessage.class.getName())) {
//				List<JournalArticle> journalArticles =
//					_journalArticleLocalService.getArticlesByResourcePrimKey(
//						GetterUtil.getLong(entryClassPKField.getValue()));
//
//				modelDocumentContributors.forEach(
//					modelDocumentContributor -> {
//						for (JournalArticle journalArticle : journalArticles) {
//							String articleId = journalArticle.getArticleId();
//
//							if (articleId.equals(entryClassPKField.getValue())) {
//								modelDocumentContributor.contribute(
//									portalKernelDocument, journalArticle);
//							}
//						}
//					}
//				);
			}
			else if (entryClassName.equals(WikiPage.class.getName())) {
//				List<JournalArticle> journalArticles =
//					_journalArticleLocalService.getArticlesByResourcePrimKey(
//						GetterUtil.getLong(entryClassPKField.getValue()));
//
//				modelDocumentContributors.forEach(
//					modelDocumentContributor -> {
//						for (JournalArticle journalArticle : journalArticles) {
//							String articleId = journalArticle.getArticleId();
//
//							if (articleId.equals(entryClassPKField.getValue())) {
//								modelDocumentContributor.contribute(
//									portalKernelDocument, journalArticle);
//							}
//						}
//					}
//				);
			}

			DocumentBuilder documentBuilder = _documentBuilderFactory.builder(
				portalKernelDocument);

			UpdateDocumentRequest updateDocumentRequest =
				new UpdateDocumentRequest(
					indexName, uid, documentBuilder.build());

			updateDocumentRequest.setType("LiferayDocumentType");

			bulkDocumentRequest.addBulkableDocumentRequest(
				updateDocumentRequest);
		}

		_searchEngineAdapter.execute(bulkDocumentRequest);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		TextEmbeddingBackgroundTaskExecutor.class);

	private BundleContext _bundleContext;

	@Reference
	private DocumentBuilderFactory _documentBuilderFactory;

	@Reference
	private JournalArticleLocalService _journalArticleLocalService;

	@Reference
	private Queries _queries;

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

	@Reference
	private Searcher _searcher;

	@Reference
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

	private volatile SemanticSearchConfiguration _semanticSearchConfiguration;

}