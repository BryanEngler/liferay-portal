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

import com.liferay.asset.kernel.model.AssetTag;
import com.liferay.blogs.model.BlogsEntry;
import com.liferay.blogs.service.BlogsEntryLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.journal.model.JournalArticle;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.collector.FacetCollector;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcher;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcherManager;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowThreadLocal;
import com.liferay.portal.search.test.internal.util.UserSearchFixture;
import com.liferay.portal.search.test.journal.util.JournalArticleBuilder;
import com.liferay.portal.search.test.journal.util.JournalArticleContent;
import com.liferay.portal.search.test.journal.util.JournalArticleSearchFixture;
import com.liferay.portal.search.test.journal.util.JournalArticleTitle;
import com.liferay.portal.search.test.util.AssertUtils;
import com.liferay.portal.search.test.util.TermCollectorUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portlet.documentlibrary.util.test.DLAppTestUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * @author André de Oliveira
 */
public abstract class BaseFacetedSearcherTestCase {

	@Before
	public void setUp() throws Exception {
		WorkflowThreadLocal.setEnabled(false);

		setUpJournalArticleSearchFixture();
		setUpUserSearchFixture();
	}

	@After
	public void tearDown() throws Exception {
		journalArticleSearchFixture.tearDown();
		userSearchFixture.tearDown();

		for (FileEntry fileEntry : _fileEntries) {
			DLAppLocalServiceUtil.deleteFileEntry(fileEntry.getFileEntryId());
		}

		_fileEntries.clear();
	}

	@Rule
	public TestName testName = new TestName();

	protected BlogsEntry addBlogsEntry(Group group, User user, String keyword)
		throws Exception {

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(group.getGroupId());

		BlogsEntry blogsEntry = BlogsEntryLocalServiceUtil.addEntry(
			user.getUserId(), keyword, RandomTestUtil.randomString(),
			serviceContext);

		_blogsEntries.add(blogsEntry);

		return blogsEntry;
	}

	protected FileEntry addFileEntry(Group group, User user, String keyword)
		throws Exception {

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(group.getGroupId());

		String title =
			keyword + StringPool.SPACE + RandomTestUtil.randomString();

		FileEntry fileEntry = DLAppTestUtil.addFileEntryWithWorkflow(
			user.getUserId(), group.getGroupId(), 0, StringPool.BLANK, title,
			true, serviceContext);

		_fileEntries.add(fileEntry);

		return fileEntry;
	}

	protected JournalArticle addJournalArticle(
			Group group, User user, String keyword)
		throws Exception {

		JournalArticleBuilder journalArticleBuilder =
			new JournalArticleBuilder();

		journalArticleBuilder.setContent(
			new JournalArticleContent() {
				{
					name = "content";
					defaultLocale = LocaleUtil.US;

					put(LocaleUtil.US, RandomTestUtil.randomString());
				}
			});
		journalArticleBuilder.setGroupId(group.getGroupId());
		journalArticleBuilder.setTitle(
			new JournalArticleTitle() {
				{
					put(LocaleUtil.US, keyword);
				}
			});
		journalArticleBuilder.setUserId(user.getUserId());

		JournalArticle journalArticle = journalArticleSearchFixture.addArticle(
			journalArticleBuilder);

		return journalArticle;
	}

	protected User addUser(Group group, String... assetTagNames)
		throws Exception {

		String screenName = testName.getMethodName();

		int size = _users.size();

		if (size > 0) {
			screenName = screenName.concat(String.valueOf(size));
		}

		return userSearchFixture.addUser(screenName, group, assetTagNames);
	}

	protected void assertAllHitsAreUsers(String keywords, Hits hits) {
		List<Document> documents = Stream.of(
			hits.getDocs()
		).filter(
			this::isMissingScreenName
		).collect(
			Collectors.toList()
		);

		Assert.assertTrue(
			keywords + "->" + documents.toString(), documents.isEmpty());
	}

	protected void assertFrequencies(
		String fieldName, SearchContext searchContext,
		Map<String, Integer> expected) {

		Map<String, Facet> facets = searchContext.getFacets();

		Facet facet = facets.get(fieldName);

		FacetCollector facetCollector = facet.getFacetCollector();

		AssertUtils.assertEquals(
			searchContext.getKeywords(), expected,
			TermCollectorUtil.toMap(facetCollector.getTermCollectors()));
	}

	protected void assertTags(
		String keywords, Hits hits, Map<String, String> expected) {

		assertAllHitsAreUsers(keywords, hits);

		AssertUtils.assertEquals(
			keywords, expected, userSearchFixture.toMap(hits.toList()));
	}

	protected FacetedSearcher createFacetedSearcher() {
		return _facetedSearcherManager.createFacetedSearcher();
	}

	protected SearchContext getSearchContext(String keywords) throws Exception {
		return userSearchFixture.getSearchContext(keywords);
	}

	protected SearchContext getSearchContext(String keywords, long[] groupIds)
		throws Exception {

		return userSearchFixture.getSearchContext(keywords, groupIds);
	}

	protected boolean isMissingScreenName(Document document) {
		return Validator.isNull(document.get("screenName"));
	}

	protected Hits search(SearchContext searchContext) throws Exception {
		FacetedSearcher facetedSearcher = createFacetedSearcher();

		return facetedSearcher.search(searchContext);
	}

	protected void setUpJournalArticleSearchFixture() throws Exception {
		journalArticleSearchFixture.setUp();

		_journalArticles = journalArticleSearchFixture.getJournalArticles();
	}

	protected void setUpUserSearchFixture() throws Exception {
		userSearchFixture.setUp();

		_assetTags = userSearchFixture.getAssetTags();
		_groups = userSearchFixture.getGroups();
		_users = userSearchFixture.getUsers();
	}

	protected Map<String, String> toMap(User user, String... tags) {
		return userSearchFixture.toMap(user, tags);
	}

	protected final JournalArticleSearchFixture journalArticleSearchFixture =
		new JournalArticleSearchFixture();
	protected final UserSearchFixture userSearchFixture =
		new UserSearchFixture();

	@Inject
	private static FacetedSearcherManager _facetedSearcherManager;

	@DeleteAfterTestRun
	private List<AssetTag> _assetTags;

	@DeleteAfterTestRun
	private final List<BlogsEntry> _blogsEntries = new ArrayList<>();

	private final List<FileEntry> _fileEntries = new ArrayList<>();

	@DeleteAfterTestRun
	private List<Group> _groups;

	@DeleteAfterTestRun
	private List<JournalArticle> _journalArticles;

	@DeleteAfterTestRun
	private List<User> _users;

}