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

package com.liferay.users.admin.indexer.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.security.permission.PermissionCheckerUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
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
public class OrganizationIndexerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			SynchronousDestinationTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_indexer = IndexerRegistryUtil.getIndexer(Organization.class);

		_permissionChecker = PermissionThreadLocal.getPermissionChecker();

		_principal = PrincipalThreadLocal.getName();

		_user = UserLocalServiceUtil.getUser(TestPropsValues.getUserId());

		PermissionCheckerUtil.setThreadValues(_user);
	}

	@After
	public void tearDown() throws Exception {
		PermissionThreadLocal.setPermissionChecker(_permissionChecker);

		PrincipalThreadLocal.setName(_principal);
	}

	@Test
	public void testOrganizationMultiwordName() throws Exception {
		addOrganization("abcd efgh ijkl mnop");
		addOrganization("qrst uvwx yz");

		assertHits("", 2);

		assertHits("abcd", 1);
		assertHits("efgh", 1);
		assertHits("ij", 1);
		assertHits("kl", 0);

		assertHits("abcd efgh", 1);
		assertHits("abcd ef", 1);
		assertHits("abcd ijkl", 1);
		assertHits("efgh ijkl", 1);

		assertHits("\"abcd\"", 1);
		assertHits("\"Abcd\"", 1);
		assertHits("\"efgh\"", 1);
		assertHits("\"eFgh\"", 1);
		assertHits("\"abcd efgh\"", 1);
		assertHits("\"abcd ef\"", 0);
		assertHits("\"abcd ijkl\"", 0);
		assertHits("\"efgh ijkl\"", 1);
		assertHits("\"efgh ij\"", 0);
		assertHits("\"gh ij\"", 0);

		assertHits("name", "abcd", 1);
		assertHits("name", "efgh", 1);
		assertHits("name", "ij", 1);
		assertHits("name", "kl", 0);

		assertHits("name", "abcd efgh", 1);
		assertHits("name", "abcd ef", 1);
		assertHits("name", "abcd ijkl", 1);
		assertHits("name", "efgh ijkl", 1);

		assertHits("name", "\"abcd\"", 1);
		assertHits("name", "\"Abcd\"", 1);
		assertHits("name", "\"efgh\"", 1);
		assertHits("name", "\"eFgh\"", 1);
		assertHits("name", "\"abcd efgh\"", 1);
		assertHits("name", "\"abcd ef\"", 0);
		assertHits("name", "\"abcd ijkl\"", 0);
		assertHits("name", "\"efgh ijkl\"", 1);
		assertHits("name", "\"efgh ij\"", 0);
		assertHits("name", "\"gh ij\"", 0);
	}

	@Test
	public void testOrganizationName() throws Exception {
		addOrganization("Abcd");
		addOrganization("cdef");

		assertHits("", 2);

		assertHits("Abcd", 1);
		assertHits("abcd", 1);
		assertHits("Ab", 1);
		assertHits("ab", 1);
		assertHits("bc", 0);
		assertHits("cd", 1);
		assertHits("Cd", 1);
		assertHits("cD", 1);
		assertHits("Abcde", 0);
		assertHits("bcde", 0);
		assertHits("cde", 1);

		assertHits("name", "Abcd", 1);
		assertHits("name", "abcd", 1);
		assertHits("name", "Ab", 1);
		assertHits("name", "ab", 1);
		assertHits("name", "bc", 0);
		assertHits("name", "cd", 1);
		assertHits("name", "Cd", 1);
		assertHits("name", "cD", 1);
		assertHits("name", "Abcde", 0);
		assertHits("name", "bcde", 0);
		assertHits("name", "cde", 1);
	}

	protected Organization addOrganization(String name) throws Exception {
		Organization organization =
			OrganizationLocalServiceUtil.addOrganization(
				_user.getUserId(), 0, name, false);

		_organizations.add(organization);

		return organization;
	}

	protected Hits assertHits(SearchContext searchContext, int length)
		throws Exception {

		Hits hits = _indexer.search(searchContext);

		Assert.assertEquals(length, hits.getLength());

		return hits;
	}

	protected Hits assertHits(String keywords, int length) throws Exception {
		SearchContext searchContext = new SearchContext();

		searchContext.setCompanyId(TestPropsValues.getCompanyId());

		searchContext.setKeywords(keywords);

		return assertHits(searchContext, length);
	}

	protected Hits assertHits(String field, String value, int length)
		throws Exception {

		SearchContext searchContext = new SearchContext();

		searchContext.setCompanyId(TestPropsValues.getCompanyId());

		searchContext.setAttribute(field, value);

		return assertHits(searchContext, length);
	}

	private static Indexer<Organization> _indexer;

	@DeleteAfterTestRun
	private final List<Organization> _organizations = new ArrayList<>();

	private PermissionChecker _permissionChecker;
	private String _principal;
	private User _user;

}