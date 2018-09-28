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

package com.liferay.bookmarks.search.test;

import com.liferay.bookmarks.model.BookmarksFolder;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistry;
import com.liferay.portal.kernel.search.SearchEngineHelper;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.search.test.util.IndexedFieldsFixture;
import com.liferay.portal.test.rule.Inject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Luan Maoski
 * @author Luca Marques
 */
public abstract class BaseBookmarksFolderIndexerTestCase {

	public void setUp() throws Exception {
		bookmarksFolderFixture = createBookmarksFolderFixture();

		bookmarksFolderFixture.setUp();

		setGroup(bookmarksFolderFixture.addGroup());

		bookmarksFolderIndexerFixture = createBookmarksFolderIndexerFixture();

		indexedFieldsFixture = createIndexedFieldsFixture();
	}

	protected BookmarksFolderFixture createBookmarksFolderFixture() {
		return new BookmarksFolderFixture(_groups, _bookmarksFolders);
	}

	protected BookmarksFolderIndexerFixture
		createBookmarksFolderIndexerFixture() {

		Indexer<BookmarksFolder> indexer = indexerRegistry.getIndexer(
			BookmarksFolder.class);

		return new BookmarksFolderIndexerFixture(indexer);
	}

	protected IndexedFieldsFixture createIndexedFieldsFixture() {
		return new IndexedFieldsFixture(
			resourcePermissionLocalService, searchEngineHelper);
	}

	protected void setGroup(Group group) {
		bookmarksFolderFixture.setGroup(group);
	}

	protected BookmarksFolderFixture bookmarksFolderFixture;
	protected BookmarksFolderIndexerFixture bookmarksFolderIndexerFixture;
	protected IndexedFieldsFixture indexedFieldsFixture;

	@Inject
	protected IndexerRegistry indexerRegistry;

	@Inject
	protected ResourcePermissionLocalService resourcePermissionLocalService;

	@Inject
	protected SearchEngineHelper searchEngineHelper;

	@DeleteAfterTestRun
	private final List<BookmarksFolder> _bookmarksFolders = new ArrayList<>(1);

	@DeleteAfterTestRun
	private final List<Group> _groups = new ArrayList<>(1);

}