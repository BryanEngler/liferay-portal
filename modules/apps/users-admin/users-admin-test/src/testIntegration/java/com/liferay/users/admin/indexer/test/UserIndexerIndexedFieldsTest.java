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
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.test.util.FieldValuesAssert;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerTestRule;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Luan Maoski
 */
@RunWith(Arquillian.class)
public class UserIndexerIndexedFieldsTest extends BaseUserIndexerTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerTestRule.INSTANCE,
			SynchronousDestinationTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@Test
	public void testIndexedFields() throws Exception {
		String firstName = "richard";
		String lastName = "bower";
		String screenName = "richard bower";

		User user = userFixture.createAnUser(firstName, lastName, screenName);

		String searchTerm = "richard";

		Document document = userIndexerFixture.searchOnlyOne(searchTerm);

		indexedFieldsFixture.postProcessDocument(document);

		Map<String, String> expected = _expectedFieldValues(user);

		FieldValuesAssert.assertFieldValues(expected, document, searchTerm);
	}

	private Map<String, String> _expectedFieldValues(User user)
		throws Exception {

		Map<String, String> map = new HashMap<>();

		map.put(Field.ENTRY_CLASS_PK, String.valueOf(user.getUserId()));
		map.put(Field.ENTRY_CLASS_NAME, User.class.getName());
		map.put(Field.COMPANY_ID, String.valueOf(user.getCompanyId()));

		map.put("firstName", StringUtil.toLowerCase(user.getFirstName()));
		map.put(
			"firstName_sortable", StringUtil.toLowerCase(user.getFirstName()));

		map.put("lastName", StringUtil.toLowerCase(user.getLastName()));
		map.put(
			"lastName_sortable", StringUtil.toLowerCase(user.getLastName()));

		map.put("fullName", StringUtil.toLowerCase(user.getFullName()));
		map.put("userName", StringUtil.toLowerCase(user.getFullName()));

		map.put("screenName", StringUtil.toLowerCase(user.getScreenName()));
		map.put(
			"screenName_sortable",
			StringUtil.toLowerCase(user.getScreenName()));

		map.put("emailAddress", user.getEmailAddress());

		map.put(
			"organizationCount",
			String.valueOf(user.getOrganizationIds().length));

		map.put("groupId", String.valueOf(user.getGroupIds()[0]));

		for (int i = 0; i < user.getGroupIds().length; i++) {
			map.put("groupIds", String.valueOf(user.getGroupIds()[i]));
			map.put("scopeGroupId", String.valueOf(user.getGroupIds()[i]));
		}

		for (int i = 0; i < user.getRoleIds().length; i++) {
			map.put("roleIds", String.valueOf(user.getRoleIds()[i]));
		}

		map.put("status", String.valueOf(user.getStatus()));

		map.put("userId", String.valueOf(user.getUserId()));

		indexedFieldsFixture.populateUID(
			User.class.getName(), user.getUserId(), map);

		_populateDates(user, map);
		_populateRoles(user, map);

		return map;
	}

	private void _populateDates(User user, Map<String, String> map) {
		indexedFieldsFixture.populateDate(
			Field.MODIFIED_DATE, user.getModifiedDate(), map);
	}

	private void _populateRoles(User user, Map<String, String> map)
		throws Exception {

		indexedFieldsFixture.populateRoleIdFields(
			user.getCompanyId(), User.class.getName(), user.getUserId(),
			user.getGroupId(), null, map);
	}

}