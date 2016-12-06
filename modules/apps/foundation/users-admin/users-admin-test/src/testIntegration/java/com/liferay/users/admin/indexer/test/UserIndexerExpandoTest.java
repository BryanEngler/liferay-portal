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
import com.liferay.expando.kernel.model.ExpandoColumn;
import com.liferay.expando.kernel.model.ExpandoColumnConstants;
import com.liferay.expando.kernel.model.ExpandoTable;
import com.liferay.expando.kernel.service.ExpandoColumnLocalServiceUtil;
import com.liferay.expando.kernel.service.ExpandoTableLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.ClassNameLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.SearchContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.security.permission.PermissionCheckerUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portlet.expando.util.test.ExpandoTestUtil;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author André de Oliveira
 */
@RunWith(Arquillian.class)
@Sync
public class UserIndexerExpandoTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			SynchronousDestinationTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_indexer = IndexerRegistryUtil.getIndexer(User.class);

		_permissionChecker = PermissionThreadLocal.getPermissionChecker();

		_principal = PrincipalThreadLocal.getName();

		User user = UserLocalServiceUtil.getUser(TestPropsValues.getUserId());

		PermissionCheckerUtil.setThreadValues(user);
	}

	@After
	public void tearDown() throws Exception {
		PermissionThreadLocal.setPermissionChecker(_permissionChecker);

		PrincipalThreadLocal.setName(_principal);
	}

	@Test
	public void testKeywordCaseSensitivity() throws Exception {
		long classNameId = ClassNameLocalServiceUtil.getClassNameId(User.class);

		ExpandoTable table = ExpandoTableLocalServiceUtil.fetchTable(
			TestPropsValues.getCompanyId(), classNameId, "CUSTOM_FIELDS");

		ExpandoColumn expandoColumn = ExpandoTestUtil.addColumn(
			table, "keywordField", ExpandoColumnConstants.STRING);

		_columns.add(expandoColumn);

		UnicodeProperties typeSettingsProperties =
			expandoColumn.getTypeSettingsProperties();

		typeSettingsProperties.setProperty(
			ExpandoColumnConstants.INDEX_TYPE, String.valueOf(
				ExpandoColumnConstants.INDEX_TYPE_KEYWORD));

		expandoColumn.setTypeSettingsProperties(typeSettingsProperties);

		ExpandoColumnLocalServiceUtil.updateExpandoColumn(expandoColumn);

		User user = addUser();
		addUser();

		ServiceContext serviceContext = new ServiceContext();

		Map<String, Serializable> expandoBridgeAttributes = new HashMap<>();

		expandoBridgeAttributes.put("keywordField", "Software");

		assertSearchCase(user, serviceContext, expandoBridgeAttributes);

		expandoBridgeAttributes.put("keywordField", "SoftWare");

		assertSearchCase(user, serviceContext, expandoBridgeAttributes);

		expandoBridgeAttributes.put("keywordField", "softWare");

		assertSearchCase(user, serviceContext, expandoBridgeAttributes);

		expandoBridgeAttributes.put("keywordField", "software");

		assertSearchCase(user, serviceContext, expandoBridgeAttributes);
	}

	@Test
	public void testKeywordField() throws Exception {
		long classNameId = ClassNameLocalServiceUtil.getClassNameId(User.class);

		ExpandoTable table = ExpandoTableLocalServiceUtil.fetchTable(
			TestPropsValues.getCompanyId(), classNameId, "CUSTOM_FIELDS");

		ExpandoColumn expandoColumn = ExpandoTestUtil.addColumn(
			table, "keywordField", ExpandoColumnConstants.STRING);

		_columns.add(expandoColumn);

		UnicodeProperties typeSettingsProperties =
			expandoColumn.getTypeSettingsProperties();

		typeSettingsProperties.setProperty(
			ExpandoColumnConstants.INDEX_TYPE, String.valueOf(
				ExpandoColumnConstants.INDEX_TYPE_KEYWORD));

		expandoColumn.setTypeSettingsProperties(typeSettingsProperties);

		ExpandoColumnLocalServiceUtil.updateExpandoColumn(expandoColumn);

		User user = addUser();
		addUser();

		ServiceContext serviceContext = new ServiceContext();

		Map<String, Serializable> expandoBridgeAttributes = new HashMap<>();

		expandoBridgeAttributes.put("keywordField", "Software Engineer");

		serviceContext.setExpandoBridgeAttributes(expandoBridgeAttributes);

		UserTestUtil.updateUser(user, serviceContext);

		assertSearch("Software Engineer", 1, user);
		assertSearch("\"Software Engineer\"", 1, user);
		assertSearch("software engineer", 1, user);
		assertSearch("\"software engineer\"", 1, user);
		assertSearch("Software", 1, user);
		assertSearch("\"Software\"", 1, user);
		assertSearch("software", 1, user);
		assertSearch("\"software\"", 1, user);
		assertSearch("Engineer", 1, user);
		assertSearch("\"Engineer\"", 1, user);
		assertSearch("engineer", 1, user);
		assertSearch("\"engineer\"", 1, user);
		assertSearch("oftware Engineer", 1, user);
		assertSearch("\"oftware Engineer\"", 1, user);
		assertSearch("oftware engineer", 1, user);
		assertSearch("\"oftware engineer\"", 1, user);
		assertSearch("Software Enginee", 1, user);
		assertSearch("\"Software Enginee\"", 1, user);
		assertSearch("software Enginee", 1, user);
		assertSearch("\"software Enginee\"", 1, user);
		assertSearch("Software enginee", 1, user);
		assertSearch("\"Software enginee\"", 1, user);
		assertSearch("software enginee", 1, user);
		assertSearch("\"software enginee\"", 1, user);
		assertSearch("oftware", 1, user);
		assertSearch("\"oftware\"", 1, user);
		assertSearch("ngineer", 1, user);
		assertSearch("\"ngineer\"", 1, user);
	}

	@Test
	public void testNotSearchableField() throws Exception {
		long classNameId = ClassNameLocalServiceUtil.getClassNameId(User.class);

		ExpandoTable table = ExpandoTableLocalServiceUtil.fetchTable(
			TestPropsValues.getCompanyId(), classNameId, "CUSTOM_FIELDS");

		ExpandoColumn expandoColumn = ExpandoTestUtil.addColumn(
			table, "notSearchableField", ExpandoColumnConstants.STRING);

		_columns.add(expandoColumn);

		UnicodeProperties typeSettingsProperties =
			expandoColumn.getTypeSettingsProperties();

		typeSettingsProperties.setProperty(
			ExpandoColumnConstants.INDEX_TYPE, String.valueOf(
				ExpandoColumnConstants.INDEX_TYPE_NONE));

		expandoColumn.setTypeSettingsProperties(typeSettingsProperties);

		ExpandoColumnLocalServiceUtil.updateExpandoColumn(expandoColumn);

		User user = addUser();
		addUser();

		ServiceContext serviceContext = new ServiceContext();

		Map<String, Serializable> expandoBridgeAttributes = new HashMap<>();

		expandoBridgeAttributes.put("notSearchableField", "Software Engineer");

		serviceContext.setExpandoBridgeAttributes(expandoBridgeAttributes);

		UserTestUtil.updateUser(user, serviceContext);

		assertSearch("Software Engineer", 0, user);
		assertSearch("\"Software Engineer\"", 0, user);
		assertSearch("software engineer", 0, user);
		assertSearch("\"software engineer\"", 0, user);
		assertSearch("Software", 0, user);
		assertSearch("\"Software\"", 0, user);
		assertSearch("software", 0, user);
		assertSearch("\"software\"", 0, user);
		assertSearch("Engineer", 0, user);
		assertSearch("\"Engineer\"", 0, user);
		assertSearch("engineer", 0, user);
		assertSearch("\"engineer\"", 0, user);
		assertSearch("oftware Engineer", 0, user);
		assertSearch("\"oftware Engineer\"", 0, user);
		assertSearch("oftware engineer", 0, user);
		assertSearch("\"oftware engineer\"", 0, user);
		assertSearch("Software Enginee", 0, user);
		assertSearch("\"Software Enginee\"", 0, user);
		assertSearch("software Enginee", 0, user);
		assertSearch("\"software Enginee\"", 0, user);
		assertSearch("Software enginee", 0, user);
		assertSearch("\"Software enginee\"", 0, user);
		assertSearch("software enginee", 0, user);
		assertSearch("\"software enginee\"", 0, user);
		assertSearch("oftware", 0, user);
		assertSearch("\"oftware\"", 0, user);
		assertSearch("ngineer", 0, user);
		assertSearch("\"ngineer\"", 0, user);
	}

	@Test
	public void testTextField() throws Exception {
		long classNameId = ClassNameLocalServiceUtil.getClassNameId(User.class);

		ExpandoTable table = ExpandoTableLocalServiceUtil.fetchTable(
			TestPropsValues.getCompanyId(), classNameId, "CUSTOM_FIELDS");

		ExpandoColumn expandoColumn = ExpandoTestUtil.addColumn(
			table, "textField", ExpandoColumnConstants.STRING);

		_columns.add(expandoColumn);

		UnicodeProperties typeSettingsProperties =
			expandoColumn.getTypeSettingsProperties();

		typeSettingsProperties.setProperty(
			ExpandoColumnConstants.INDEX_TYPE, String.valueOf(
				ExpandoColumnConstants.INDEX_TYPE_TEXT));

		expandoColumn.setTypeSettingsProperties(typeSettingsProperties);

		ExpandoColumnLocalServiceUtil.updateExpandoColumn(expandoColumn);

		User user = addUser();
		addUser();

		ServiceContext serviceContext = new ServiceContext();

		Map<String, Serializable> expandoBridgeAttributes = new HashMap<>();

		expandoBridgeAttributes.put("textField", "Software Engineer");

		serviceContext.setExpandoBridgeAttributes(expandoBridgeAttributes);

		UserTestUtil.updateUser(user, serviceContext);

		assertSearch("Software Engineer", 1, user);
		assertSearch("\"Software Engineer\"", 1, user);
		assertSearch("software engineer", 1, user);
		assertSearch("\"software engineer\"", 1, user);
		assertSearch("Software", 1, user);
		assertSearch("\"Software\"", 1, user);
		assertSearch("software", 1, user);
		assertSearch("\"software\"", 1, user);
		assertSearch("Engineer", 1, user);
		assertSearch("\"Engineer\"", 1, user);
		assertSearch("engineer", 1, user);
		assertSearch("\"engineer\"", 1, user);
		assertSearch("oftware Engineer", 1, user);
		assertSearch("\"oftware Engineer\"", 0, user);
		assertSearch("oftware engineer", 1, user);
		assertSearch("\"oftware engineer\"", 0, user);
		assertSearch("Software Enginee", 1, user);
		assertSearch("\"Software Enginee\"", 0, user);
		assertSearch("software Enginee", 1, user);
		assertSearch("\"software Enginee\"", 0, user);
		assertSearch("Software enginee", 1, user);
		assertSearch("\"Software enginee\"", 0, user);
		assertSearch("software enginee", 1, user);
		assertSearch("\"software enginee\"", 0, user);
		assertSearch("oftware", 0, user);
		assertSearch("\"oftware\"", 0, user);
		assertSearch("ngineer", 0, user);
		assertSearch("\"ngineer\"", 0, user);
	}

	protected User addUser() throws Exception {
		User user = UserTestUtil.addUser();

		_users.add(user);

		return user;
	}

	protected Hits assertHits(
			final SearchContext searchContext, final int length)
		throws Exception {

		Hits hits = _indexer.search(searchContext);

		Assert.assertEquals(hits.toString(), length, hits.getLength());

		return hits;
	}

	protected Hits assertHits(String keywords, int length) throws Exception {
		SearchContext searchContext = SearchContextTestUtil.getSearchContext();

		searchContext.setKeywords(keywords);

		return assertHits(searchContext, length);
	}

	protected void assertSearch(String value, int length, User user)
		throws Exception {

		Hits hits = assertHits(value, length);

		if (length > 0) {
			User result = getUser(hits);

			Assert.assertEquals(user.getUserId(), result.getUserId());
		}
	}

	protected void assertSearchCase(
			User user, ServiceContext serviceContext,
			Map<String, Serializable> expandoBridgeAttributes)
		throws Exception {

		serviceContext.setExpandoBridgeAttributes(expandoBridgeAttributes);

		UserTestUtil.updateUser(user, serviceContext);

		assertSearch("Software", 1, user);
		assertSearch("\"Software\"", 1, user);
		assertSearch("SoftWare", 1, user);
		assertSearch("\"SoftWare\"", 1, user);
		assertSearch("softWare", 1, user);
		assertSearch("\"softWare\"", 1, user);
		assertSearch("software", 1, user);
		assertSearch("\"software\"", 1, user);
		assertSearch("oftware ", 1, user);
		assertSearch("\"oftware\"", 1, user);
		assertSearch("oftWare ", 1, user);
		assertSearch("\"oftWare\"", 1, user);
	}

	protected User getUser(Hits hits) throws PortalException {
		Document document = hits.doc(0);

		long userId = GetterUtil.getLong(document.get(Field.USER_ID));

		return UserLocalServiceUtil.getUser(userId);
	}

	private static Indexer<User> _indexer;

	@DeleteAfterTestRun
	private final List<ExpandoColumn> _columns = new ArrayList<>();

	private PermissionChecker _permissionChecker;
	private String _principal;

	@DeleteAfterTestRun
	private final List<User> _users = new ArrayList<>();

}