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

package com.liferay.portal.search.elasticsearch.internal.suggest;

import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.search.suggest.QuerySuggester;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.elasticsearch.connection.TestElasticsearchConnectionManager;
import com.liferay.portal.search.elasticsearch.index.IndexNameBuilder;
import com.liferay.portal.search.elasticsearch.internal.ElasticsearchQuerySuggester;
import com.liferay.portal.search.elasticsearch.internal.connection.ElasticsearchFixture;
import com.liferay.portal.search.elasticsearch.internal.index.CompanyIndexFactory;
import com.liferay.portal.search.elasticsearch.internal.index.LiferayTypeMappingsConstants;
import com.liferay.portal.search.test.util.suggest.BaseQuerySuggesterTestCase;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * @author Bryan Engler
 */
public class ElasticsearchQuerySuggesterTest extends
	BaseQuerySuggesterTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		_elasticsearchFixture = new ElasticsearchFixture(
			ElasticsearchQuerySuggesterTest.class.getSimpleName());

		_elasticsearchFixture.setUp();

		super.setUp();

		JSONFactoryUtil jsonFactoryUtil = new JSONFactoryUtil();

		jsonFactoryUtil.setJSONFactory(new JSONFactoryImpl());

		createIndices();
	}

	@After
	public void tearDown() throws Exception {
		_elasticsearchFixture.tearDown();
	}

	@Override
	@Test
	public void testSpellCheckJapaneseIdeographicSpace() throws Exception {
		super.testSpellCheckJapaneseIdeographicSpace();
	}

	@Override
	@Test
	public void testSpellCheckLuceneUnfriendlyTerms() throws Exception {
		super.testSpellCheckLuceneUnfriendlyTerms();
	}

	@Override
	@Test
	public void testSpellCheckShortTerms() throws Exception {
		super.testSpellCheckShortTerms();
	}

	@Override
	@Test
	public void testSpellCheckWhitespace() throws Exception {
		super.testSpellCheckWhitespace();
	}

	@Override
	@Test
	public void testSpellCheckResults() throws Exception {
		super.testSpellCheckResults();
	}

	@Override
	@Test
	public void testSuggestKeywordResults() throws Exception {
		super.testSuggestKeywordResults();
	}

	@Override
	protected QuerySuggester createQuerySuggester() throws Exception {

		return new ElasticsearchQuerySuggester() {
			{
				elasticsearchConnectionManager =
					new TestElasticsearchConnectionManager(
						_elasticsearchFixture);

				indexNameBuilder = new TestIndexNameBuilder();

				suggesterTranslator = new ElasticsearchSuggesterTranslator() {{
					aggregateSuggesteTranslator =
						new AggregateSuggesterTranslatorImpl();

					completionSuggesterTranslator =
						new CompletionSuggesterTranslatorImpl();

					phraseSuggesterTranslator=
						new PhraseSuggesterTranslatorImpl();

					termSuggesterTranslator =
						new TermSuggesterTranslatorImpl();
				}};
			}
		};
	}

	protected class TestIndexNameBuilder implements IndexNameBuilder {

		@Override
		public String getIndexName(long companyId) {
			return StringUtil.toLowerCase(testName.getMethodName());
		}

	}

	protected void createIndices()
		throws Exception {

		CompanyIndexFactory companyIndexFactory = new CompanyIndexFactory() {
			{
				indexNameBuilder = new TestIndexNameBuilder();
			}
		};

		AdminClient adminClient = _elasticsearchFixture.getAdminClient();

		companyIndexFactory.createIndices(adminClient, 0);
	}

	@Rule
	public TestName testName = new TestName();

	private ElasticsearchFixture _elasticsearchFixture;

	protected IndexRequestBuilder getIndexRequestBuilder() {
		Client client = _elasticsearchFixture.getClient();

		IndexNameBuilder indexNameBuilder = new TestIndexNameBuilder();

		IndexRequestBuilder indexRequestBuilder =
			client.prepareIndex(
				indexNameBuilder.getIndexName(0),
				LiferayTypeMappingsConstants.LIFERAY_DOCUMENT_TYPE);

		return indexRequestBuilder;
	}

	@Override
	protected void indexKeywordSearch(String value) {
		IndexRequestBuilder indexRequestBuilder = getIndexRequestBuilder();

		indexRequestBuilder.setSource("keywordSearch_en_US", value);

		indexRequestBuilder.get();
	}

	@Override
	protected void indexSpellCheckWord(String value) {
		IndexRequestBuilder indexRequestBuilder = getIndexRequestBuilder();

		indexRequestBuilder.setSource("spellCheckWord_en_US", value);

		indexRequestBuilder.get();
	}

}
