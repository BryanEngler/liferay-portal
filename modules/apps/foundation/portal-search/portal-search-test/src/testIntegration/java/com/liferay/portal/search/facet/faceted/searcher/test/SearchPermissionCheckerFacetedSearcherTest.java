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
import com.liferay.dynamic.data.mapping.test.util.DDMStructureTestUtil;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.AssetEntriesFacetFactory;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactory;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.permission.ModelPermissions;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.SearchContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.registry.Registry;
import com.liferay.registry.RegistryUtil;

import java.util.ArrayList;
import java.util.Collections;
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
public class SearchPermissionCheckerFacetedSearcherTest
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

		Registry registry = RegistryUtil.getRegistry();

		assetEntriesFacetFactory = registry.getService(
			AssetEntriesFacetFactory.class);

		journalArticleLocalService = registry.getService(
			JournalArticleLocalService.class);

		permissionCheckerFactory = registry.getService(
			PermissionCheckerFactory.class);

		_originalPermissionChecker =
			PermissionThreadLocal.getPermissionChecker();
	}

	@After
	@Override
	public void tearDown() throws Exception {
		super.tearDown();

		PermissionThreadLocal.setPermissionChecker(_originalPermissionChecker);
	}

	@Test
	public void testDecrementFrequencyCount() throws Exception {
		Group group = userSearchFixture.addGroup();

		User user1 = userSearchFixture.addUser(group);

		ServiceContext serviceContext = createServiceContext(group, user1);

		String title = RandomTestUtil.randomString();

		addArticle(title, user1, group, 0, serviceContext);

		User user2 = UserTestUtil.addUser(group.getGroupId());

		PermissionThreadLocal.setPermissionChecker(
			permissionCheckerFactory.create(user2));

		SearchContext searchContext = getSearchContext(title);

		Facet facet = assetEntriesFacetFactory.newInstance(searchContext);

		searchContext.addFacet(facet);

		searchContext.setUserId(user2.getUserId());

		search(searchContext);

		Map<String, Integer> expected = Collections.emptyMap();

		assertFrequencies(facet.getFieldName(), searchContext, expected);
	}

	protected static SearchContext getSearchContext(String keywords)
		throws Exception {

		SearchContext searchContext = SearchContextTestUtil.getSearchContext();

		searchContext.setKeywords(keywords);

		return searchContext;
	}

	protected void addArticle(
			String title, User user, Group group, long folderId,
			ServiceContext serviceContext)
		throws Exception {

		String content = DDMStructureTestUtil.getSampleStructuredContent();

		JournalArticle article = journalArticleLocalService.addArticle(
			user.getUserId(), group.getGroupId(), folderId,
			Collections.singletonMap(LocaleUtil.US, title), null, content,
			"BASIC-WEB-CONTENT", "BASIC-WEB-CONTENT", serviceContext);

		_articles.add(article);
	}

	protected ServiceContext createServiceContext(Group group, User user)
		throws Exception {

		ServiceContext serviceContext = new ServiceContext();

		ModelPermissions modelPermissions = new ModelPermissions();

		modelPermissions.addRolePermissions(
			RoleConstants.OWNER, ActionKeys.VIEW);

		serviceContext.setModelPermissions(modelPermissions);

		serviceContext.setAddGroupPermissions(false);
		serviceContext.setAddGuestPermissions(false);
		serviceContext.setCompanyId(group.getCompanyId());
		serviceContext.setScopeGroupId(group.getGroupId());
		serviceContext.setUserId(user.getUserId());

		return serviceContext;
	}

	protected AssetEntriesFacetFactory assetEntriesFacetFactory;
	protected JournalArticleLocalService journalArticleLocalService;
	protected PermissionCheckerFactory permissionCheckerFactory;

	@DeleteAfterTestRun
	private final List<JournalArticle> _articles = new ArrayList<>();

	private PermissionChecker _originalPermissionChecker;

}