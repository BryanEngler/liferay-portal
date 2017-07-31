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
import com.liferay.journal.test.util.JournalArticleBuilder;
import com.liferay.journal.test.util.JournalArticleContent;
import com.liferay.journal.test.util.JournalArticleTitle;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.Summary;
import com.liferay.portal.kernel.search.highlight.HighlightUtil;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.service.test.ServiceTestUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

/**
 * @author André de Oliveira
 * @author Bryan Engler
 */
@RunWith(Arquillian.class)
@Sync
public class JournalArticleSummaryTest {

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

		_indexer = IndexerRegistryUtil.getIndexer(JournalArticle.class);
	}

	@Test
	public void testGetSummary() throws Exception {
		String title = "test title";

		setTitle(
			new JournalArticleTitle() {
				{
					put(LocaleUtil.US, title);
				}
			});

		String content = "test content";

		setContent(
			new JournalArticleContent() {
				{
					name = "content";
					defaultLocale = LocaleUtil.US;

					put(LocaleUtil.US, content);
				}
			});

		JournalArticle journalArticle = addArticle();

		Document document = _indexer.getDocument(journalArticle);

		String snippet = null;

		PortletRequest portletRequest = Mockito.mock(PortletRequest.class);

		PortletResponse portletResponse = Mockito.mock(PortletResponse.class);

		Summary summary = _indexer.getSummary(
			document, snippet, portletRequest, portletResponse);

		String summaryTitle = summary.getTitle();
		String summaryContent = summary.getContent();

		Assert.assertEquals(title, summaryTitle);
		Assert.assertEquals(content, summaryContent);
	}

	@Test
	public void testGetSummaryHighlighted() throws  Exception {
		String title = "test title";

		setTitle(
			new JournalArticleTitle() {
				{
					put(LocaleUtil.US, title);
				}
			});

		String content = "test content";

		setContent(
			new JournalArticleContent() {
				{
					name = "content";
					defaultLocale = LocaleUtil.US;

					put(LocaleUtil.US, content);
				}
			});

		JournalArticle journalArticle = addArticle();

		Document document = _indexer.getDocument(journalArticle);

		StringBundler sb = new StringBundler(4);

		sb.append(HighlightUtil.HIGHLIGHT_TAG_OPEN);
		sb.append("test");
		sb.append(HighlightUtil.HIGHLIGHT_TAG_CLOSE);
		sb.append(" title");

		String highlight1 = sb.toString();

		document.addText(
			Field.SNIPPET + StringPool.UNDERLINE + Field.TITLE, highlight1);

		sb = new StringBundler(4);

		sb.append(HighlightUtil.HIGHLIGHT_TAG_OPEN);
		sb.append("test");
		sb.append(HighlightUtil.HIGHLIGHT_TAG_CLOSE);
		sb.append(" content");

		String highlight2 = sb.toString();

		document.addText(
			Field.SNIPPET + StringPool.UNDERLINE + Field.CONTENT, highlight2);

		String snippet = null;

		PortletRequest portletRequest = Mockito.mock(PortletRequest.class);

		PortletResponse portletResponse = Mockito.mock(PortletResponse.class);

		Summary summary = _indexer.getSummary(
			document, snippet, portletRequest, portletResponse);

		String summaryTitle = summary.getTitle();
		String summaryContent = summary.getContent();

		Assert.assertEquals(highlight1, summaryTitle);
		Assert.assertEquals(highlight2, summaryContent);
	}

	protected void setContent(JournalArticleContent journalArticleContent) {
		_journalArticleBuilder.setContent(journalArticleContent);
	}

	protected void setTitle(JournalArticleTitle journalArticleTitle) {
		_journalArticleBuilder.setTitle(journalArticleTitle);
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

	@DeleteAfterTestRun
	private Group _group;

	private Indexer<JournalArticle> _indexer;

	private JournalArticleBuilder _journalArticleBuilder;

}