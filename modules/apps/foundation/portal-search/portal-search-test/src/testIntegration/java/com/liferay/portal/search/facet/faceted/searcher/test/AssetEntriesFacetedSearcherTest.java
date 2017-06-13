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
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.dynamic.data.mapping.test.util.DDMStructureTestUtil;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.AssetEntriesFacetFactory;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.config.FacetConfiguration;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactory;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
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
public class AssetEntriesFacetedSearcherTest
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
	public void testClassNameFacetFilter() throws Exception {
		Group group = userSearchFixture.addGroup();

		User user1 = userSearchFixture.addUser(group);

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext();

		String title = RandomTestUtil.randomString();

		addArticle(title, user1, group, 0, serviceContext);

		PermissionThreadLocal.setPermissionChecker(
			permissionCheckerFactory.create(user1));

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		jsonArray.put(JournalArticle.class.getName());
		jsonArray.put(DLFileEntry.class.getName());

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		jsonObject.put("values", jsonArray);

		FacetConfiguration facetConfiguration = new FacetConfiguration();

		facetConfiguration.setDataJSONObject(jsonObject);
		facetConfiguration.setFieldName(Field.ENTRY_CLASS_NAME);
		facetConfiguration.setStatic(false);

		SearchContext searchContext = getSearchContext(title);

		searchContext.setAttribute(
			facetConfiguration.getFieldName(), DLFileEntry.class.getName());

		Facet facet = assetEntriesFacetFactory.newInstance(searchContext);

		facet.setFacetConfiguration(facetConfiguration);

		searchContext.addFacet(facet);

		search(searchContext);

		Map<String, Integer> expected = Collections.singletonMap(
			JournalArticle.class.getName(), 1);

		assertFrequencies(facet.getFieldName(), searchContext, expected);
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

	protected AssetEntriesFacetFactory assetEntriesFacetFactory;
	protected JournalArticleLocalService journalArticleLocalService;
	protected PermissionCheckerFactory permissionCheckerFactory;

	@DeleteAfterTestRun
	private final List<JournalArticle> _articles = new ArrayList<>();

	private PermissionChecker _originalPermissionChecker;

}