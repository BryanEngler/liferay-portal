/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch7.internal.count;

import com.liferay.portal.search.elasticsearch7.internal.connection.ElasticsearchFixture;
import com.liferay.portal.search.elasticsearch7.internal.indexing.LiferayElasticsearchIndexingFixtureFactory;
import com.liferay.portal.search.test.util.count.BaseCountTestCase;
import com.liferay.portal.search.test.util.indexing.IndexingFixture;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.IOException;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.StoredFieldsContext;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.FieldAndFormat;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author André de Oliveira
 */
public class ElasticsearchCountTest extends BaseCountTestCase {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Override
	protected IndexingFixture createIndexingFixture() {
		return LiferayElasticsearchIndexingFixtureFactory.getInstance();
	}

	@Test
	public void testSource() throws Exception {
		SearchRequest searchRequest = new SearchRequest();

		searchRequest.source(
			getSearchSourceBuilder(
				searchSourceBuilder ->
				{
					searchSourceBuilder.fetchSource(FetchSourceContext.FETCH_SOURCE);
					searchSourceBuilder.fetchSource(true);
					searchSourceBuilder.fetchSource(fields[0], null);
					searchSourceBuilder.fetchSource(fields, null);
				}));

		SearchHit searchHit = getSearchHit(searchRequest);

		searchHit.getSourceAsString();
		searchHit.getSourceAsMap();
	}

	@Test
	public void testFetchFields() throws Exception {
		SearchRequest searchRequest = new SearchRequest();

		searchRequest.source(
			getSearchSourceBuilder(
				searchSourceBuilder -> {
					for (String field : fields) {
						searchSourceBuilder.fetchField(field);
						searchSourceBuilder.fetchField(new FieldAndFormat("",""));
					}
				}));

		SearchHit searchHit = getSearchHit(searchRequest);

		searchHit.getFields(); //includes document and metadata fields
		searchHit.getDocumentFields();
		searchHit.getMetadataFields();

	}

	@Test
	public void testStoredField() throws Exception  {
		SearchRequest searchRequest = new SearchRequest();

		searchRequest.source(
			getSearchSourceBuilder(
				searchSourceBuilder -> {
					for (String field : fields) {
						searchSourceBuilder.storedField(field);
						searchSourceBuilder.storedFields(new ArrayList<>());
						searchSourceBuilder.storedFields(StoredFieldsContext.fromList(new ArrayList<>()));
					}
				}));

		SearchHit searchHit = getSearchHit(searchRequest);

		searchHit.getFields(); //includes document and metadata fields
		searchHit.getDocumentFields();
		searchHit.getMetadataFields();
	}

	private SearchSourceBuilder getSearchSourceBuilder(
		Consumer<SearchSourceBuilder> searchSourceBuilderConsumer) {

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		searchSourceBuilder.query(QueryBuilders.matchAllQuery());

		searchSourceBuilderConsumer.accept(searchSourceBuilder);

		return searchSourceBuilder;
	}

	private SearchHit getSearchHit(SearchRequest searchRequest)
		throws IOException {

		SearchResponse searchResponse =
			restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

		SearchHits searchHits = searchResponse.getHits();

		SearchHit[] searchHitsArray = searchHits.getHits();

		SearchHit searchHit = searchHitsArray[0];

		return searchHit;
	}

	ElasticsearchFixture elasticsearchFixture = new ElasticsearchFixture();

	RestHighLevelClient restHighLevelClient =
		elasticsearchFixture.getRestHighLevelClient();

	String[] fields = {"companyId", "entryClassName", "entryClassPK"};

}