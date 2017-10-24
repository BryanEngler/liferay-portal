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

package com.liferay.asset.search.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.kernel.service.persistence.AssetEntryQuery;
import com.liferay.dynamic.data.mapping.test.util.DDMStructureTestUtil;
import com.liferay.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.UserGroupRole;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserGroupRoleLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.service.persistence.UserGroupRolePK;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.service.test.ServiceTestUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portlet.asset.util.AssetSearcher;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

/**
 * @author André de Oliveira
 */
@RunWith(Arquillian.class)
@Sync
public class AssetSearcherTest {

	@ClassRule
	@Rule
	public static final TestRule testRule = new AggregateTestRule(
		new LiferayIntegrationTestRule(),
		SynchronousDestinationTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();
	}

	@Test
	public void testGuestUser() throws Exception {
		setGuestUser();

		AssetEntryQuery assetEntryQuery = getAssetEntryQuery();

		assetEntryQuery.setClassNameIds(
			getClassNameIds("com.liferay.blogs.kernel.model.BlogsEntry"));

		search(assetEntryQuery, getSearchContext());
	}

	@Test
	public void testGuestUserBorrowedPermissions() throws Exception {
		setGuestUser();

		AssetEntryQuery assetEntryQuery = getAssetEntryQuery();

		assetEntryQuery.setClassNameIds(
			getClassNameIds(
				"com.liferay.calendar.model.CalendarBooking",
				"com.liferay.dynamic.data.lists.model.DDLRecord"));

		search(assetEntryQuery, getSearchContext());
	}

	@Test
	public void testSiteRolePermissions() throws Exception {
		Role role = RoleTestUtil.addRole(RoleConstants.TYPE_SITE);

		RoleTestUtil.addResourcePermission(
			role, "com.liferay.journal.model.JournalArticle",
			ResourceConstants.SCOPE_GROUP_TEMPLATE, "0", ActionKeys.VIEW);

		Group group = GroupTestUtil.addGroup();

		User user = UserTestUtil.addUser(group.getGroupId());

		ServiceTestUtil.setUser(user);

		UserGroupRolePK userGroupRolePK = new UserGroupRolePK(
			user.getUserId(), group.getGroupId(), role.getRoleId());

		UserGroupRole userGroupRole =
			UserGroupRoleLocalServiceUtil.createUserGroupRole(userGroupRolePK);

		UserGroupRoleLocalServiceUtil.addUserGroupRole(userGroupRole);

		addJournalArticle(group);

		GroupTestUtil.enableLocalStaging(group);

		Group stagingGroup = group.getStagingGroup();

		SearchContext searchContext = new SearchContext();

		searchContext.setCompanyId(group.getCompanyId());
		searchContext.setGroupIds(new long[] {stagingGroup.getGroupId()});
		searchContext.setUserId(user.getUserId());

		AssetEntryQuery assetEntryQuery = new AssetEntryQuery();

		assetEntryQuery.setGroupIds(new long[] {group.getGroupId()});

		assetEntryQuery.setClassNameIds(
			getClassNameIds("com.liferay.journal.model.JournalArticle"));

		Hits hits = search(assetEntryQuery, searchContext);

		GroupLocalServiceUtil.deleteGroup(group);
		RoleLocalServiceUtil.deleteRole(role);
		UserLocalServiceUtil.deleteUser(user);

		Assert.assertEquals(hits.toString(), 1, hits.getLength());
	}

	protected void addJournalArticle(Group group) throws Exception {
		Map<Locale, String> titleMap = new HashMap<>();

		titleMap.put(Locale.US, RandomTestUtil.randomString());

		Map<Locale, String> descriptionMap = new HashMap<>();

		descriptionMap.put(Locale.US, RandomTestUtil.randomString());

		String ddmStructureKey = "BASIC-WEB-CONTENT";
		String ddmTemplateKey = "BASIC-WEB-CONTENT";

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				group.getGroupId(), TestPropsValues.getUserId());

		serviceContext.setAddGuestPermissions(false);
		serviceContext.setAddGroupPermissions(false);

		JournalArticleLocalServiceUtil.addArticle(
			TestPropsValues.getUserId(), group.getGroupId(), 0, titleMap,
			descriptionMap, DDMStructureTestUtil.getSampleStructuredContent(),
			ddmStructureKey, ddmTemplateKey, serviceContext);
	}

	protected AssetEntryQuery getAssetEntryQuery() {
		AssetEntryQuery assetEntryQuery = new AssetEntryQuery();

		assetEntryQuery.setGroupIds(new long[] {_group.getGroupId()});

		return assetEntryQuery;
	}

	protected long[] getClassNameIds(String... classNames) {
		Stream<String> stream = Stream.of(classNames);

		return stream.mapToLong(
			PortalUtil::getClassNameId
		).toArray();
	}

	protected SearchContext getSearchContext() {
		SearchContext searchContext = new SearchContext();

		searchContext.setCompanyId(_group.getCompanyId());
		searchContext.setGroupIds(new long[] {_group.getGroupId()});

		return searchContext;
	}

	protected Hits search(
			AssetEntryQuery assetEntryQuery, SearchContext searchContext)
		throws Exception {

		AssetSearcher assetSearcher = new AssetSearcher();

		assetSearcher.setAssetEntryQuery(assetEntryQuery);

		return assetSearcher.search(searchContext);
	}

	protected void setGuestUser() throws Exception {
		ServiceTestUtil.setUser(
			_userLocalService.getDefaultUser(_group.getCompanyId()));
	}

	@Inject
	private static UserLocalService _userLocalService;

	@DeleteAfterTestRun
	private Group _group;

}