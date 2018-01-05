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

package com.liferay.portal.search.solr.internal.suggest;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.IndexWriter;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.suggest.SuggestionConstants;
import com.liferay.portal.kernel.util.DigesterUtil;
import com.liferay.portal.kernel.util.Props;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.search.internal.DefaultCollatorImpl;
import com.liferay.portal.search.solr.connection.TestSolrClientManager;
import com.liferay.portal.search.solr.internal.SolrIndexingFixture;
import com.liferay.portal.search.solr.internal.SolrQuerySuggester;
import com.liferay.portal.search.solr.internal.SolrUnitTestRequirements;
import com.liferay.portal.search.test.util.suggest.BaseQuerySuggesterTestCase;
import com.liferay.portal.util.DigesterImpl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author André de Oliveira
 * @author Wade Cao
 */
public class SolrQuerySuggesterTest extends BaseQuerySuggesterTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		Assume.assumeTrue(
			SolrUnitTestRequirements.isSolrExternallyStartedByDeveloper());

		PropsUtil.setProps(Mockito.mock(Props.class));

		DigesterUtil digesterUtil = new DigesterUtil();

		digesterUtil.setDigester(new DigesterImpl());

		super.setUp();
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
	public void testSpellCheckResultsMisspelledWord() throws Exception {
		super.testSpellCheckResultsMisspelledWord();
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
	public void testSuggestKeywordResults() throws Exception {
		super.testSuggestKeywordResults();
	}

	@Override
	protected SolrQuerySuggester createQuerySuggester() throws Exception {
		return new SolrQuerySuggester() {
			{
				collator = new DefaultCollatorImpl();

				setNGramQueryBuilder(_createNGramQueryBuilder());

				setSolrClientManager(
					new TestSolrClientManager(_getProperties()));
			}
		};
	}

	@Override
	protected void indexKeywordSearch(String value) {
		_index(value, SuggestionConstants.TYPE_QUERY_SUGGESTION);
	}

	@Override
	protected void indexSpellCheckWord(String value) {
		_index(value, SuggestionConstants.TYPE_SPELL_CHECKER);
	}

	private NGramQueryBuilderImpl _createNGramQueryBuilder() {
		return new NGramQueryBuilderImpl() {
			{
				setNGramHolderBuilder(new NGramHolderBuilderImpl());
			}
		};
	}

	private Map<String, Object> _getProperties() {
		Map<String, Object> properties = new HashMap<>();

		properties.put("logExceptionsOnly", false);
		properties.put("readURL", "http://localhost:8983/solr/liferay");
		properties.put("writeURL", "http://localhost:8983/solr/liferay");

		return properties;
	}

	private void _index(String value, String type) {
		try {
			SolrIndexingFixture solrIndexingFixture = new SolrIndexingFixture();

			solrIndexingFixture.setUp();

			IndexWriter indexWriter = solrIndexingFixture.getIndexWriter();

			SearchContext searchContext = new SearchContext();

			searchContext.setCompanyId(getCompanyId());
			searchContext.setKeywords(value);
			searchContext.setLocale(Locale.US);

			indexWriter.indexKeyword(searchContext, 0.0F, type);
		}
		catch (Exception e) {
			_log.error("Unable to index value: " + value);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SolrQuerySuggesterTest.class);

}