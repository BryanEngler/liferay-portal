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

package com.liferay.portal.search.facet.faceted.searcher.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.kernel.model.AssetTag;
import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.search.facet.Facet;
import com.liferay.portal.search.facet.tag.AssetTagNamesFacetFactory;
import com.liferay.portal.search.test.journal.util.JournalArticleBlueprint;
import com.liferay.portal.search.test.journal.util.JournalArticleContent;
import com.liferay.portal.search.test.journal.util.JournalArticleTitle;
import com.liferay.portal.search.test.util.DocumentsAssert;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Andrew Betts
 * @author André de Oliveira
 */
@RunWith(Arquillian.class)
public class AssetTagNamesFacetedSearcherTest
	extends BaseFacetedSearcherTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testAggregation() throws Exception {
		String keyword = RandomTestUtil.randomString();

		Group group = userSearchFixture.addGroup();
		String title = keyword;

		addJournalArticle(group, title);

		String tag = keyword;

		addUser(group, tag);

		SearchContext searchContext = getSearchContext(keyword);

		Facet facet = _assetTagNamesFacetFactory.newInstance(searchContext);

		searchContext.addFacet(facet);

		Hits hits = search(searchContext);

		assertEntryClassNames(
			Arrays.asList(JournalArticle.class.getName(), User.class.getName()),
			hits, keyword);

		List<AssetTag> tags = userSearchFixture.getAssetTags();

		AssetTag assetTag = tags.get(0);

		Map<String, Integer> frequencies = Collections.singletonMap(
			String.valueOf(assetTag.getTagId()), 1);

		assertFrequencies(facet.getFieldName(), searchContext, frequencies);
	}

	@Test
	public void testSearchByFacet() throws Exception {
		String tag = "enterprise. open-source for life";

		addUser(tag);

		SearchContext searchContext = getSearchContext(tag);

		Facet facet = _assetTagNamesFacetFactory.newInstance(searchContext);

		searchContext.addFacet(facet);

		search(searchContext);

		List<AssetTag> tags = userSearchFixture.getAssetTags();

		AssetTag assetTag = tags.get(0);

		Map<String, Integer> frequencies = Collections.singletonMap(
			String.valueOf(assetTag.getTagId()), 1);

		assertFrequencies(facet.getFieldName(), searchContext, frequencies);
	}

	@Test
	public void testSearchQuoted() throws Exception {
		String[] assetTagNames = {"Enterprise", "Open Source", "For   Life"};

		User user = addUser(assetTagNames);

		Map<String, String> expected = userSearchFixture.toMap(
			user, assetTagNames);

		assertTags("\"Enterprise\"", expected);
		assertTags("\"Open\"", expected);
		assertTags("\"Source\"", expected);
		assertTags("\"Open Source\"", expected);
		assertTags("\"For   Life\"", expected);
	}

	@Test
	public void testSelection() throws Exception {
		String keyword = RandomTestUtil.randomString();

		Group group = userSearchFixture.addGroup();
		String title = keyword;

		addJournalArticle(group, title);

		String tag = keyword;

		addUser(group, tag);

		SearchContext searchContext = getSearchContext(keyword);

		Facet facet = _assetTagNamesFacetFactory.newInstance(searchContext);

		List<AssetTag> tags = userSearchFixture.getAssetTags();

		AssetTag assetTag = tags.get(0);

		String tagId = String.valueOf(assetTag.getTagId());

		facet.select(tagId);

		searchContext.addFacet(facet);

		Hits hits = search(searchContext);

		assertEntryClassNames(
			Arrays.asList(User.class.getName()), hits, keyword);

		Map<String, Integer> frequencies = Collections.singletonMap(tagId, 1);

		assertFrequencies(facet.getFieldName(), searchContext, frequencies);
	}

	protected void addJournalArticle(Group group, String title)
		throws Exception {

		journalArticleSearchFixture.addArticle(
			new JournalArticleBlueprint() {
				{
					groupId = group.getGroupId();
					journalArticleContent = new JournalArticleContent() {
						{
							defaultLocale = LocaleUtil.US;
							name = "content";

							put(LocaleUtil.US, RandomTestUtil.randomString());
						}
					};
					journalArticleTitle = new JournalArticleTitle() {
						{
							put(LocaleUtil.US, title);
						}
					};
				}
			});
	}

	protected User addUser(String... assetTagNames) throws Exception {
		Group group = userSearchFixture.addGroup();

		return addUser(group, assetTagNames);
	}

	protected void assertEntryClassNames(
		Collection<String> entryClassNames, Hits hits, String keyword) {

		DocumentsAssert.assertValuesIgnoreRelevance(
			keyword, hits.getDocs(), Field.ENTRY_CLASS_NAME, entryClassNames);
	}

	protected void assertTags(String keywords, Map<String, String> expected)
		throws Exception {

		SearchContext searchContext = getSearchContext(keywords);

		Hits hits = search(searchContext);

		assertTags(keywords, hits, expected);
	}

	@Inject
	private static AssetTagNamesFacetFactory _assetTagNamesFacetFactory;

}