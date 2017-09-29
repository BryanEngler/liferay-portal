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
import com.liferay.blogs.model.BlogsEntry;
import com.liferay.blogs.service.BlogsEntryLocalServiceUtil;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.facet.Facet;
import com.liferay.portal.search.facet.scope.ScopeFacetFactory;
import com.liferay.portal.search.facet.type.AssetEntriesFacetFactory;
import com.liferay.portal.search.facet.user.UserFacetFactory;
import com.liferay.portal.search.test.journal.util.JournalArticleBuilder;
import com.liferay.portal.search.test.journal.util.JournalArticleContent;
import com.liferay.portal.search.test.journal.util.JournalArticleTitle;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portlet.documentlibrary.util.test.DLAppTestUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
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
public class AggregationFilterFacetedSearcherTest
	extends BaseFacetedSearcherTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			SynchronousDestinationTestRule.INSTANCE);

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_user1 = addUser();
		_user2 = addUser();
		_user3 = addUser();

		_group1 = addGroup();
		_group2 = addGroup();

		_keyword = RandomTestUtil.randomString();

		addJournalArticle(_user1, _group1);
		addJournalArticle(_user1, _group2);
		addJournalArticle(_user3, _group1);

		addFileEntry(_user1, _group1);
		addFileEntry(_user2, _group2);
		addFileEntry(_user3, _group2);

		addBlogsEntry(_user1, _group1);
		addBlogsEntry(_user2, _group1);
		addBlogsEntry(_user2, _group2);
	}

	@After
	@Override
	public void tearDown() throws Exception {
		super.tearDown();

		for (FileEntry fileEntry : _fileEntries) {
			DLAppLocalServiceUtil.deleteFileEntry(fileEntry.getFileEntryId());
		}

		_fileEntries.clear();
	}

	@Test
	public void testSelectNone() throws Exception {
		assertAggregationFrequencies(null, null, null, 4, 3, 2, 3, 3, 3, 5, 4);
	}

	@Test
	public void testSelectOneGroupOneUser() throws Exception {
		String[] groups = selectGroups(_group2);
		String[] users = selectUsers(_user1);

		assertAggregationFrequencies(
			groups, users, null, 1, 2, 1, 0, 0, 1, 3, 1);
	}

	@Test
	public void testSelectOneGroupOneUserOneType() throws Exception {
		String[] groups = selectGroups(_group2);
		String[] users = selectUsers(_user1);
		String[] types = selectTypes(JournalArticle.class);

		assertAggregationFrequencies(
			groups, users, types, 1, 0, 0, 0, 0, 1, 1, 1);
	}

	@Test
	public void testSelectOneUser() throws Exception {
		String[] users = selectUsers(_user1);

		assertAggregationFrequencies(null, users, null, 4, 3, 2, 1, 1, 2, 3, 1);
	}

	@Test
	public void testSelectOneUserOneType() throws Exception {
		String[] users = selectUsers(_user1);
		String[] types = selectTypes(JournalArticle.class);

		assertAggregationFrequencies(
			null, users, types, 2, 0, 1, 1, 1, 2, 1, 1);
	}

	@Test
	public void testSelectOneUserTwoTypes() throws Exception {
		String[] users = selectUsers(_user1);
		String[] types = selectTypes(JournalArticle.class, DLFileEntry.class);

		assertAggregationFrequencies(
			null, users, types, 3, 1, 2, 1, 1, 2, 2, 1);
	}

	protected BlogsEntry addBlogsEntry(User user, Group group)
		throws Exception {

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(group.getGroupId());

		BlogsEntry blogsEntry = BlogsEntryLocalServiceUtil.addEntry(
			user.getUserId(), _keyword, RandomTestUtil.randomString(),
			serviceContext);

		_blogsEntries.add(blogsEntry);

		return blogsEntry;
	}

	protected FileEntry addFileEntry(User user, Group group) throws Exception {
		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(group.getGroupId());

		String title =
			_keyword + StringPool.SPACE + RandomTestUtil.randomString();

		FileEntry fileEntry = DLAppTestUtil.addFileEntryWithWorkflow(
			user.getUserId(), group.getGroupId(), 0, StringPool.BLANK, title,
			true, serviceContext);

		_fileEntries.add(fileEntry);

		return fileEntry;
	}

	protected Group addGroup() throws Exception {
		Group group = GroupTestUtil.addGroup();

		_groups.add(group);

		return group;
	}

	protected JournalArticle addJournalArticle(User user, Group group)
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
					put(LocaleUtil.US, _keyword);
				}
			});
		journalArticleBuilder.setUserId(user.getUserId());

		JournalArticle journalArticle = journalArticleSearchFixture.addArticle(
			journalArticleBuilder);

		return journalArticle;
	}

	protected User addUser() throws Exception {
		User user = UserTestUtil.addUser();

		_users.add(user);

		return user;
	}

	protected void assertAggregationFrequencies(
			String[] groupSelections, String[] userSelections,
			String[] typeSelections, int user1Count, int user2Count,
			int user3Count, int blogsEntryCount, int fileEntryCount,
			int journalArticleCount, int group1Count, int group2Count)
		throws Exception {

		SearchContext searchContext = getSearchContext(_keyword);

		Facet facet = userFacetFactory.newInstance(searchContext);

		facet.select(userSelections);

		searchContext.addFacet(facet);

		facet = scopeFacetFactory.newInstance(searchContext);

		facet.select(groupSelections);

		searchContext.addFacet(facet);

		facet = assetEntriesFacetFactory.newInstance(searchContext);

		facet.select(typeSelections);

		searchContext.addFacet(facet);

		search(searchContext);

		Map<String, Integer> userFrequencies = new HashMap<>();

		if (user1Count > 0) {
			userFrequencies.put(
				StringUtil.toLowerCase(_user1.getFullName()), user1Count);
		}

		if (user2Count > 0) {
			userFrequencies.put(
				StringUtil.toLowerCase(_user2.getFullName()), user2Count);
		}

		if (user3Count > 0) {
			userFrequencies.put(
				StringUtil.toLowerCase(_user3.getFullName()), user3Count);
		}

		assertFrequencies(Field.USER_NAME, searchContext, userFrequencies);

		Map<String, Integer> typeFrequencies = new HashMap<>();

		if (blogsEntryCount > 0) {
			typeFrequencies.put(BlogsEntry.class.getName(), blogsEntryCount);
		}

		if (fileEntryCount > 0) {
			typeFrequencies.put(DLFileEntry.class.getName(), fileEntryCount);
		}

		if (journalArticleCount > 0) {
			typeFrequencies.put(
				JournalArticle.class.getName(), journalArticleCount);
		}

		assertFrequencies(
			Field.ENTRY_CLASS_NAME, searchContext, typeFrequencies);

		Map<String, Integer> groupFrequencies = new HashMap<>();

		if (group1Count > 0) {
			groupFrequencies.put(
				String.valueOf(_group1.getGroupId()), group1Count);
		}

		if (group2Count > 0) {
			groupFrequencies.put(
				String.valueOf(_group2.getGroupId()), group2Count);
		}

		assertFrequencies(Field.GROUP_ID, searchContext, groupFrequencies);
	}

	protected String[] selectGroups(Group... groups) {
		String[] selectedGroups = new String[groups.length];

		for (int i = 0; i < groups.length; i++) {
			selectedGroups[i] = String.valueOf(groups[i].getGroupId());
		}

		return selectedGroups;
	}

	protected String[] selectTypes(Class... clazzes) {
		String[] selectedTypes = new String[clazzes.length];

		for (int i = 0; i < clazzes.length; i++) {
			selectedTypes[i] = clazzes[i].getName();
		}

		return selectedTypes;
	}

	protected String[] selectUsers(User... users) {
		String[] selectedUsers = new String[users.length];

		for (int i = 0; i < users.length; i++) {
			selectedUsers[i] = StringUtil.toLowerCase(users[i].getFullName());
		}

		return selectedUsers;
	}

	@Inject
	protected AssetEntriesFacetFactory assetEntriesFacetFactory;

	@Inject
	protected ScopeFacetFactory scopeFacetFactory;

	@Inject
	protected UserFacetFactory userFacetFactory;

	@DeleteAfterTestRun
	private final List<BlogsEntry> _blogsEntries = new ArrayList<>();

	private final List<FileEntry> _fileEntries = new ArrayList<>();
	private Group _group1;
	private Group _group2;

	@DeleteAfterTestRun
	private final List<Group> _groups = new ArrayList<>();

	private String _keyword;
	private User _user1;
	private User _user2;
	private User _user3;

	@DeleteAfterTestRun
	private final List<User> _users = new ArrayList<>();

}