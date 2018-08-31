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

import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistry;
import com.liferay.portal.kernel.search.SearchEngineHelper;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.search.test.util.IndexedFieldsFixture;
import com.liferay.portal.test.rule.Inject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Luan Maoski
 */
public abstract class BaseUserIndexerTestCase {

	public void setUp() throws Exception {
		userFixture = createUserFixture();

		userFixture.setUp();

		setGroup(userFixture.addGroup());

		userIndexerFixture = createUserIndexerFixture();
		indexedFieldsFixture = createIndexedFieldsFixture();
	}

	protected IndexedFieldsFixture createIndexedFieldsFixture() {
		return new IndexedFieldsFixture(
			resourcePermissionLocalService, searchEngineHelper);
	}

	protected UserFixture createUserFixture() {
		return new UserFixture(userLocalService, _groups, _users);
	}

	protected UserIndexerFixture createUserIndexerFixture() {
		Indexer<User> indexer = indexerRegistry.getIndexer(User.class);

		return new UserIndexerFixture(indexer);
	}

	protected void setGroup(Group group) {
		userFixture.setGroup(group);
	}

	protected IndexedFieldsFixture indexedFieldsFixture;

	@Inject
	protected IndexerRegistry indexerRegistry;

	@Inject
	protected ResourcePermissionLocalService resourcePermissionLocalService;

	@Inject
	protected SearchEngineHelper searchEngineHelper;

	protected UserFixture userFixture;
	protected UserIndexerFixture userIndexerFixture;

	@Inject
	protected UserLocalService userLocalService;

	@DeleteAfterTestRun
	private final List<Group> _groups = new ArrayList<>(1);

	@DeleteAfterTestRun
	private final List<User> _users = new ArrayList<>(1);

}