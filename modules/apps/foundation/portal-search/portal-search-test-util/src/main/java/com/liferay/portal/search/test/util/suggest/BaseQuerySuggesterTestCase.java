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
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.Localization;
import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.kernel.util.StringPool;

import java.util.Locale;

import org.junit.Before;

import org.mockito.Mockito;

/**
 * @author André de Oliveira
 * @author Wade Cao
 * @author Bryan Engler
 */
public abstract class BaseQuerySuggesterTestCase {

	@Before
	public void setUp() throws Exception {
		_companyId = RandomTestUtil.randomLong();

		_querySuggester = createQuerySuggester();

		LocalizationUtil localizationUtil = new LocalizationUtil();

		localizationUtil.setLocalization(createLocalization());
	}

	public void testSpellCheckJapaneseIdeographicSpace() throws Exception {
		indexSpellCheckWord(RandomTestUtil.randomString());

		String ideographicSpace = "\u3000";

		spellCheckKeywords("あ" + ideographicSpace + "い");
		spellCheckKeywords("あ" + ideographicSpace + ideographicSpace + "い");
		spellCheckKeywords("A" + ideographicSpace + "B");
	}

	public void testSpellCheckLuceneUnfriendlyTerms() throws Exception {
		indexSpellCheckWord(RandomTestUtil.randomString());

		spellCheckKeywords("+alpha AND -bravo");
	}

	public void testSpellCheckShortTerms() throws Exception {
		indexSpellCheckWord(RandomTestUtil.randomString());

		spellCheckKeywords("1 2");
		spellCheckKeywords("A B");
		spellCheckKeywords("A  B");
	}

	public void testSpellCheckWhitespace() throws Exception {
		indexSpellCheckWord(RandomTestUtil.randomString());

		spellCheckKeywords("Liferay Search");
		spellCheckKeywords(" Liferay Search   ");
		spellCheckKeywords("Liferay    Search");
		spellCheckKeywords("L ife  ray    Searc h");
	}

	protected Localization createLocalization() {
		Localization localization = Mockito.mock(Localization.class);

		Mockito.doReturn(
			StringPool.BLANK
		).when(
			localization
		).getLocalizedName(
			Mockito.anyString(), Mockito.anyString()
		);

		return localization;
	}

	protected abstract QuerySuggester createQuerySuggester() throws Exception;

	protected long getCompanyId() {
		return _companyId;
	}

	protected abstract void indexKeywordSearch(String value);

	protected abstract void indexSpellCheckWord(String value);

	protected String spellCheckKeywords(String keywords) throws Exception {
		return _querySuggester.spellCheckKeywords(
			_createSearchContext(keywords));
	}

	protected String[] suggestKeywordQueries(String keywords) throws Exception {
		return _querySuggester.suggestKeywordQueries(
			_createSearchContext(keywords), 1);
	}

	private SearchContext _createSearchContext(String keywords) {
		return new SearchContext() {
			{
				setCompanyId(_companyId);
				setKeywords(keywords);
				setLocale(Locale.US);
			}
		};
	}

	private long _companyId;
	private QuerySuggester _querySuggester;

}