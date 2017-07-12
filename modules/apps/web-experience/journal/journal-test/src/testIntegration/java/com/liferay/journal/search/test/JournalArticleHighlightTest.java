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

package com.liferay.journal.search.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.search.JournalArticleIndexer;
import com.liferay.journal.test.util.FieldValuesAssert;
import com.liferay.journal.test.util.JournalArticleBuilder;
import com.liferay.journal.test.util.JournalArticleContent;
import com.liferay.journal.test.util.JournalArticleTitle;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.QueryConfig;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.highlight.HighlightUtil;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.SearchContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.service.test.ServiceTestUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Bryan Engler
 */
@RunWith(Arquillian.class)
@Sync
public class JournalArticleHighlightTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			SynchronousDestinationTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_journalArticleBuilder = new JournalArticleBuilder();

		_journalArticleBuilder.setGroupId(_group.getGroupId());

		ServiceTestUtil.setUser(TestPropsValues.getUser());

		CompanyThreadLocal.setCompanyId(TestPropsValues.getCompanyId());

		_indexer = new JournalArticleIndexer();
	}

	@Test
	public void testJapaneseSnippetFields1() throws Exception {
		String title = "新規作成";

		setTitle(
			new JournalArticleTitle() {
				{
					put(LocaleUtil.JAPAN, title);
				}
			});

		String content = title;

		setContent(
			new JournalArticleContent() {
				{
					name = "content";
					defaultLocale = LocaleUtil.JAPAN;

					put(LocaleUtil.JAPAN, content);
				}
			});

		addArticle();

		assertSnippetFields(
			"新規作成", LocaleUtil.JAPAN, "<liferay-hl>新規作成</liferay-hl>",
			"<liferay-hl>新規作成</liferay-hl>");

		assertSnippetFields(
			"新規", LocaleUtil.JAPAN, "<liferay-hl>新規</liferay-hl>作成",
			"<liferay-hl>新規</liferay-hl>作成");

		assertSnippetFields(
			"作成", LocaleUtil.JAPAN, "新規<liferay-hl>作成</liferay-hl>",
			"新規<liferay-hl>作成</liferay-hl>");

		assertSnippetFields(
			"新", LocaleUtil.JAPAN, "<liferay-hl>新規</liferay-hl>作成",
			"<liferay-hl>新規</liferay-hl>作成");

		assertSnippetFields(
			"作", LocaleUtil.JAPAN, "新規<liferay-hl>作成</liferay-hl>",
			"新規<liferay-hl>作成</liferay-hl>");
	}

	@Test
	public void testJapaneseSnippetFields2() throws Exception {
		String title = "あいうえお 日本語";

		setTitle(
			new JournalArticleTitle() {
				{
					put(LocaleUtil.JAPAN, title);
				}
			});

		String content = title;

		setContent(
			new JournalArticleContent() {
				{
					name = "content";
					defaultLocale = LocaleUtil.JAPAN;

					put(LocaleUtil.JAPAN, content);
				}
			});

		addArticle();

		StringBundler sb = new StringBundler(10);

		sb.append(HighlightUtil.HIGHLIGHT_TAG_OPEN);
		sb.append("あい");
		sb.append(HighlightUtil.HIGHLIGHT_TAG_CLOSE);
		sb.append("うえお ");
		sb.append(HighlightUtil.HIGHLIGHT_TAG_OPEN);
		sb.append("日本");
		sb.append(HighlightUtil.HIGHLIGHT_TAG_CLOSE);
		sb.append(HighlightUtil.HIGHLIGHT_TAG_OPEN);
		sb.append("語");
		sb.append(HighlightUtil.HIGHLIGHT_TAG_CLOSE);

		assertSnippetFields(
			"あいうえお 日本語", LocaleUtil.JAPAN, sb.toString(), sb.toString());
	}

	@Test
	public void testSnippetFields() throws Exception {
		String title = "entity title";

		setTitle(
			new JournalArticleTitle() {
				{
					put(LocaleUtil.US, title);
				}
			});

		String content = "entity content";

		setContent(
			new JournalArticleContent() {
				{
					name = "content";
					defaultLocale = LocaleUtil.US;

					put(LocaleUtil.US, content);
				}
			});

		addArticle();

		assertSnippetFields(
			"entity", LocaleUtil.US, "<liferay-hl>entity</liferay-hl> title",
			"<liferay-hl>entity</liferay-hl> content");

		assertSnippetFields(
			"entity title", LocaleUtil.US,
			"<liferay-hl>entity title</liferay-hl>",
			"<liferay-hl>entity</liferay-hl> content");
	}

	protected JournalArticle addArticle() {
		try {
			return _journalArticleBuilder.addArticle();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void assertSnippetFields(
		String searchTerm, Locale locale, String expectedTitle,
		String expectedContent) {

		HashMap<String, String> highlights = new HashMap<String, String>() {
			{
				put("snippet_title_" + locale.toString(), expectedTitle);
				put("snippet_content_" + locale.toString(), expectedContent);
			}
		};

		Document document = _search(searchTerm, locale);

		FieldValuesAssert.assertFieldValues(
			highlights, "snippet", document, searchTerm);
	}

	protected void setContent(JournalArticleContent journalArticleContent) {
		_journalArticleBuilder.setContent(journalArticleContent);
	}

	protected void setTitle(JournalArticleTitle journalArticleTitle) {
		_journalArticleBuilder.setTitle(journalArticleTitle);
	}

	private SearchContext _getSearchContext(String searchTerm, Locale locale)
		throws Exception {

		SearchContext searchContext = SearchContextTestUtil.getSearchContext(
			_group.getGroupId());

		searchContext.setKeywords(searchTerm);

		searchContext.setLocale(locale);

		QueryConfig queryConfig = searchContext.getQueryConfig();

		queryConfig.setHighlightEnabled(true);
		queryConfig.setLocale(locale);
		queryConfig.setSelectedFieldNames(StringPool.STAR);

		return searchContext;
	}

	private Document _getSingleDocument(String searchTerm, Hits hits) {
		List<Document> documents = hits.toList();

		if (documents.size() == 1) {
			return documents.get(0);
		}

		throw new AssertionError(searchTerm + "->" + documents);
	}

	private Document _search(String searchTerm, Locale locale) {
		try {
			SearchContext searchContext = _getSearchContext(searchTerm, locale);

			Hits hits = _indexer.search(searchContext);

			return _getSingleDocument(searchTerm, hits);
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@DeleteAfterTestRun
	private Group _group;

	private Indexer<?> _indexer;
	private JournalArticleBuilder _journalArticleBuilder;

}