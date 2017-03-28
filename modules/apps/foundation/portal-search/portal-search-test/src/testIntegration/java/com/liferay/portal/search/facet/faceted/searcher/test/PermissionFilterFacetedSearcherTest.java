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
import com.liferay.journal.configuration.JournalServiceConfiguration;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.model.JournalFolder;
import com.liferay.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.journal.service.JournalFolderLocalServiceUtil;
import com.liferay.osgi.util.ServiceTrackerFactory;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.AssetEntriesFacetFactory;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.service.permission.ModelPermissions;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.SearchContextTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.registry.Registry;
import com.liferay.registry.RegistryUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Bryan Engler
 */
@RunWith(Arquillian.class)
@Sync
public class PermissionFilterFacetedSearcherTest
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

		_assetEntriesFacetFactory = registry.getService(
			AssetEntriesFacetFactory.class);

		_originalPermissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		Bundle bundle = FrameworkUtil.getBundle(
			PermissionFilterFacetedSearcherTest.class);

		_configurationAdminServiceTracker = ServiceTrackerFactory.open(
			bundle, ConfigurationAdmin.class);

		_configurationAdmin = _configurationAdminServiceTracker.waitForService(
			5000);
	}

	@After
	public void tearDown() throws Exception {
		_configurationAdminServiceTracker.close();

		PermissionThreadLocal.setPermissionChecker(_originalPermissionChecker);

		for (JournalArticle article : _articles) {
			JournalArticleLocalServiceUtil.deleteArticle(article);
		}

		_articles.clear();

		for (JournalFolder folder : _folders) {
			JournalFolderLocalServiceUtil.deleteFolder(folder);
		}

		_folders.clear();

		for (User user : _users) {
			UserLocalServiceUtil.deleteUser(user);
		}

		_users.clear();
	}

	@Test
	public void testDecrementFrequencyCount() throws Exception {
		Configuration configuration = _configurationAdmin.getConfiguration(
			JournalServiceConfiguration.class.getName(), StringPool.QUESTION);

		Dictionary<String, Object> properties = new Hashtable<>();

		properties.put("articleViewPermissionsCheckEnabled", true);

		configuration.update(properties);

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				TestPropsValues.getGroupId(), TestPropsValues.getUserId());

		ModelPermissions modelPermissions = new ModelPermissions();

		modelPermissions.addRolePermissions(
			RoleConstants.OWNER, ActionKeys.VIEW);

		serviceContext.setModelPermissions(modelPermissions);

		JournalFolder folder = JournalFolderLocalServiceUtil.addFolder(
			TestPropsValues.getUserId(), TestPropsValues.getGroupId(), 0,
			RandomTestUtil.randomString(), StringPool.BLANK, serviceContext);

		_folders.add(folder);

		serviceContext.setAddGroupPermissions(true);
		serviceContext.setAddGuestPermissions(false);

		String keyword = addArticles(folder, serviceContext);

		User user = UserTestUtil.addUser(TestPropsValues.getGroupId());

		_users.add(user);

		PermissionChecker permissionChecker =
			PermissionCheckerFactoryUtil.create(user);

		PermissionThreadLocal.setPermissionChecker(permissionChecker);

		SearchContext searchContext = SearchContextTestUtil.getSearchContext();

		Facet facet = _assetEntriesFacetFactory.newInstance(searchContext);

		searchContext.addFacet(facet);

		searchContext.setEnd(2);
		searchContext.setEntryClassNames(
			new String[] {JournalArticle.class.getName()});
		searchContext.setKeywords(keyword);
		searchContext.setStart(0);
		searchContext.setUserId(user.getUserId());

		search(searchContext);

		configuration.delete();

		Map<String, Integer> expected = Collections.singletonMap(
			JournalArticle.class.getName(), 1);

		assertFrequencies(facet.getFieldName(), searchContext, expected);
	}

	protected String addArticles(
			JournalFolder journalFolder, ServiceContext serviceContext)
		throws Exception {

		Map<Locale, String> titleMap = new HashMap<>();

		String title = RandomTestUtil.randomString();

		titleMap.put(LocaleUtil.US, title);

		String content = DDMStructureTestUtil.getSampleStructuredContent();

		JournalArticle article1 = JournalArticleLocalServiceUtil.addArticle(
			TestPropsValues.getUserId(), TestPropsValues.getGroupId(),
			journalFolder.getFolderId(), titleMap, null, content,
			"BASIC-WEB-CONTENT", "BASIC-WEB-CONTENT", serviceContext);

		_articles.add(article1);

		JournalArticle article2 = JournalArticleLocalServiceUtil.addArticle(
			TestPropsValues.getUserId(), TestPropsValues.getGroupId(), 0,
			titleMap, null, content, "BASIC-WEB-CONTENT", "BASIC-WEB-CONTENT",
			serviceContext);

		_articles.add(article2);

		return title;
	}

	private final List<JournalArticle> _articles = new ArrayList<>();
	private AssetEntriesFacetFactory _assetEntriesFacetFactory;
	private ConfigurationAdmin _configurationAdmin;
	private ServiceTracker<ConfigurationAdmin, ConfigurationAdmin>
		_configurationAdminServiceTracker;
	private final List<JournalFolder> _folders = new ArrayList<>();
	private PermissionChecker _originalPermissionChecker;
	private final List<User> _users = new ArrayList<>();

}