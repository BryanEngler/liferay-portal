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

package com.liferay.organizations.search.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.OrganizationConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.UserConstants;
import com.liferay.portal.kernel.search.BaseModelSearchResult;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.QueryConfig;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchEngineHelper;
import com.liferay.portal.kernel.service.CountryService;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.OrganizationService;
import com.liferay.portal.kernel.service.RegionService;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.OrganizationTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.test.util.IndexerFixture;
import com.liferay.portal.search.test.util.SearchContextTestUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.users.admin.test.util.search.UserSearchFixture;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Igor Samuel Trong Tran
 */
@RunWith(Arquillian.class)
public class OrganizationIndexerOrganizationsTreeTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE,
			SynchronousDestinationTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		setUpOrganizationIndexerFixture();

		setUpUserSearchFixture();

		setUpOrganizationFixture();
	}

	@Test
	public void testOrganizationsTree() throws Exception {
		User user = userSearchFixture.addUser(
			RandomTestUtil.randomString(), userSearchFixture.addGroup());

		Organization organization =
			organizationFixture.createOrganization(
				RandomTestUtil.randomString());

		UserLocalServiceUtil.addOrganizationUser(
			organization.getOrganizationId(), user.getUserId());

		List<Organization> organizations = new ArrayList<>(1);

		organizations.add(organization);

		LinkedHashMap<String, Object> params = new LinkedHashMap<>();

		params.put("organizationsTree", organizations);

		SearchContext searchContext = new SearchContext();

		searchContext.setAndSearch(true);

		Map<String, Serializable> attributes = new HashMap<>();

		attributes.put("params", params);

		searchContext.setAttributes(attributes);

		searchContext.setCompanyId(TestPropsValues.getCompanyId());

		QueryConfig queryConfig = searchContext.getQueryConfig();

		queryConfig.setHighlightEnabled(false);
		queryConfig.setScoreEnabled(false);

		Document[] documents = organizationIndexerFixture.search(searchContext);

		Assert.assertEquals(Arrays.toString(documents), 1, documents.length);
	}

	protected void setUpOrganizationFixture() throws Exception {
		organizationFixture = new OrganizationFixture(
			organizationService, countryService, regionService);

		organizationFixture.setUp();

		organizationFixture.setGroup(group);

		_organizations = organizationFixture.getOrganizations();
	}

	protected void setUpOrganizationIndexerFixture() {
		organizationIndexerFixture = new IndexerFixture<>(Organization.class);
	}

	protected void setUpUserSearchFixture() throws Exception {
		userSearchFixture = new UserSearchFixture();

		userSearchFixture.setUp();

		_groups = userSearchFixture.getGroups();
		_users = userSearchFixture.getUsers();

		group = userSearchFixture.addGroup();
	}

	@Inject
	protected CountryService countryService;

	protected Group group;
	protected OrganizationFixture organizationFixture;
	protected IndexerFixture<Organization> organizationIndexerFixture;

	@Inject
	protected OrganizationService organizationService;

	@Inject
	protected RegionService regionService;

	@Inject
	protected ResourcePermissionLocalService resourcePermissionLocalService;

	@Inject
	protected SearchEngineHelper searchEngineHelper;

	protected UserSearchFixture userSearchFixture;

	@DeleteAfterTestRun
	private List<Group> _groups;

	@DeleteAfterTestRun
	private List<Organization> _organizations;

	@DeleteAfterTestRun
	private List<User> _users;

}