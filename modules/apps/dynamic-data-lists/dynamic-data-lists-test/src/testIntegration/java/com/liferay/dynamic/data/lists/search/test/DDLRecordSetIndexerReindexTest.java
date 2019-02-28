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

package com.liferay.dynamic.data.lists.search.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.dynamic.data.lists.model.DDLRecord;
import com.liferay.dynamic.data.lists.model.DDLRecordSet;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.search.test.util.IndexerFixture;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerTestRule;
import com.liferay.users.admin.test.util.search.UserSearchFixture;

import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Luan Maoski
 * @author Lucas Marques
 */
@RunWith(Arquillian.class)
public class DDLRecordSetIndexerReindexTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerTestRule.INSTANCE,
			SynchronousDestinationTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		setUpUserSearchFixture();
		setUpDdlRecordIndexerFixture();
		setUpDdlRecordSetIndexerFixture();
		setUpDdlRecordSetFixture();
	}

	@Test
	public void testReindexing() throws Exception {
		Locale locale = LocaleUtil.US;

		String searchTerm = TestPropsValues.getUser().getFullName();

		ddlRecordSetFixture.updateDisplaySettings(locale);

		ddlRecordSetIndexerFixture.searchOnlyOne(searchTerm);

		Document document = ddlRecordSetIndexerFixture.searchOnlyOne(
			searchTerm, locale);

		ddlRecordSetIndexerFixture.deleteDocument(document);

		ddlRecordSetIndexerFixture.searchNoOne(searchTerm, locale);

		ddlRecordSetIndexerFixture.reindex(TestPropsValues.getCompanyId());

		ddlRecordSetIndexerFixture.searchOnlyOne(searchTerm);
	}

	@Test
	public void testReindexingChildren() throws Exception {
		Locale locale = LocaleUtil.US;

		String searchTerm = TestPropsValues.getUser().getFullName();

		ddlRecordSetFixture.updateDisplaySettings(locale);

		DDLRecord ddlRecord = createAnDDLRecord();

		ddlRecordIndexerFixture.searchOnlyOne(searchTerm);

		Document document = ddlRecordIndexerFixture.searchOnlyOne(
			searchTerm, locale);

		ddlRecordIndexerFixture.deleteDocument(document);

		ddlRecordIndexerFixture.searchNoOne(searchTerm, locale);

		ddlRecordSetIndexerFixture.reindex(ddlRecord.getCompanyId());

		ddlRecordSetIndexerFixture.searchOnlyOne(searchTerm);
	}

	protected DDLRecord createAnDDLRecord() throws Exception {
		try {
			DDLRecord ddmFormInstanceRecord = ddlRecordSetFixture.addRecord(
				"Joe Bloggs", "Simple description");

			return ddmFormInstanceRecord;
		}
		catch (PortalException pe) {
			throw new RuntimeException(pe);
		}
	}

	protected void setUpDdlRecordIndexerFixture() {
		ddlRecordIndexerFixture = new IndexerFixture(DDLRecord.class);
	}

	protected void setUpDdlRecordSetFixture() throws Exception {
		ddlRecordSetFixture = new DDLRecordSetFixture();

		ddlRecordSetFixture.setGroup(group);

		ddlRecordSetFixture.setUp();

		_ddlRecordSets = ddlRecordSetFixture.getDdlRecordSets();
	}

	protected void setUpDdlRecordSetIndexerFixture() {
		ddlRecordSetIndexerFixture = new IndexerFixture(DDLRecordSet.class);
	}

	protected void setUpUserSearchFixture() throws Exception {
		userSearchFixture = new UserSearchFixture();

		userSearchFixture.setUp();

		_groups = userSearchFixture.getGroups();

		_users = userSearchFixture.getUsers();

		group = userSearchFixture.addGroup();
	}

	protected IndexerFixture<DDLRecord> ddlRecordIndexerFixture;
	protected DDLRecordSetFixture ddlRecordSetFixture;
	protected IndexerFixture<DDLRecordSet> ddlRecordSetIndexerFixture;
	protected Group group;
	protected UserSearchFixture userSearchFixture;

	@DeleteAfterTestRun
	private List<DDLRecordSet> _ddlRecordSets;

	@DeleteAfterTestRun
	private List<Group> _groups;

	@DeleteAfterTestRun
	private List<User> _users;

}