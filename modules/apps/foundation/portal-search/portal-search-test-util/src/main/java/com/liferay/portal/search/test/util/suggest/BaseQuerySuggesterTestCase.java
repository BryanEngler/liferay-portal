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

package com.liferay.portal.search.test.util.suggest;

import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.suggest.QuerySuggester;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.search.test.util.IdempotentRetryAssert;
import org.junit.Assert;
import org.junit.Before;

/**
 * @author André de Oliveira
 * @author Wade Cao
 * @author Bryan Engler
 */
public abstract class BaseQuerySuggesterTestCase {

	@Before
	public void setUp() throws Exception {
		_querySuggester = createQuerySuggester();
	}

	public void testSpellCheckJapaneseIdeographicSpace() throws Exception {
		indexSpellCheckWord("indexed");

		String ideographicSpace = "\u3000";

		spellCheckKeywords("あ" + ideographicSpace + "い");
		spellCheckKeywords("あ" + ideographicSpace + ideographicSpace + "い");
		spellCheckKeywords("A" + ideographicSpace + "B");
	}

	public void testSpellCheckLuceneUnfriendlyTerms() throws Exception {
		indexSpellCheckWord("indexed");

		spellCheckKeywords("+alpha AND -bravo");
	}

	public void testSpellCheckShortTerms() throws Exception {
		indexSpellCheckWord("indexed");

		spellCheckKeywords("1 2");
		spellCheckKeywords("A B");
		spellCheckKeywords("A  B");
	}

	public void testSpellCheckWhitespace() throws Exception {
		indexSpellCheckWord("indexed");

		spellCheckKeywords("Liferay Search");
		spellCheckKeywords(" Liferay Search   ");
		spellCheckKeywords("Liferay    Search");
		spellCheckKeywords("L ife  ray    Searc h");
	}

	public void testSpellCheckResults() throws Exception {
		indexSpellCheckWord("indexed");
		indexSpellCheckWord("this");
		indexSpellCheckWord("phrase");

		IdempotentRetryAssert.retryAssert(
			3, TimeUnit.SECONDS,
			() -> {
				Map<String, List<String>> results =
					spellCheckKeywords("indexef");

				List<String> suggestions = results.get("indexef");

				String suggestion = StringPool.BLANK;

				if (ListUtil.isNotEmpty(suggestions)) {
					suggestion = suggestions.get(0);
				}

				Assert.assertEquals("indexed", suggestion);

				return null;
			});

		IdempotentRetryAssert.retryAssert(
			3, TimeUnit.SECONDS,
			() -> {
				Map<String, List<String>> results =
					spellCheckKeywords("this");

				List<String> suggestions = results.get("this");

				Assert.assertEquals(0, suggestions.size());

				return null;
			});

		IdempotentRetryAssert.retryAssert(
			3, TimeUnit.SECONDS,
			() -> {
				Map<String, List<String>> results =
					spellCheckKeywords("phrasd");

				List<String> suggestions = results.get("phrasd");

				String suggestion = StringPool.BLANK;

				if (ListUtil.isNotEmpty(suggestions)) {
					suggestion = suggestions.get(0);
				}

				Assert.assertEquals("phrase", suggestion);

				return null;
			});
	}

	public void testSuggestKeywordResults() throws Exception {
		indexKeywordSearch("indexed this phrase");

		IdempotentRetryAssert.retryAssert(
			3, TimeUnit.SECONDS,
			() -> {
				String[] suggestions =
					suggestKeywordQueries("indexef this phrasd");

				String suggestion = StringPool.BLANK;

				if (suggestions.length > 0) {
					suggestion = suggestions[0];
				}

				Assert.assertEquals("indexed this phrase", suggestion);

				return null;
			});
	}

	protected abstract QuerySuggester createQuerySuggester() throws Exception;

	protected abstract void indexKeywordSearch(String value);

	protected abstract void indexSpellCheckWord(String value);

	protected Map<String, List<String>> spellCheckKeywords(String keywords)
		throws Exception {

		return _querySuggester.spellCheckKeywords(
			_createSearchContext(keywords), 1);
	}

	protected String[] suggestKeywordQueries(String keywords)
		throws Exception {

		return _querySuggester.suggestKeywordQueries(
			_createSearchContext(keywords), 1);
	}

	private SearchContext _createSearchContext(String keywords) {
		return new SearchContext() {
			{
				setCompanyId(0);
				setKeywords(keywords);
				setLocale(Locale.US);
			}
		};
	}

	private QuerySuggester _querySuggester;

}