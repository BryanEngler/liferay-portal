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

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.bookmarks.model.BookmarksFolder;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerTestRule;

import java.util.Locale;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Luan Maoski
 * @author Luca Marques
 */
@RunWith(Arquillian.class)
public class BookmarksFolderIndexerReindexTest
	extends BaseBookmarksFolderIndexerTestCase {

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

		super.setGroup(bookmarksFolderFixture.addGroup());
	}

	@Test
	public void testReindexingFolders() throws Exception {
		Locale locale = LocaleUtil.US;

		BookmarksFolder bookmarksFolder =
			bookmarksFolderFixture.createBookmarksFolder();

		String searchTerm = bookmarksFolder.getName();

		bookmarksFolderFixture.updateDisplaySettings(locale);

		bookmarksFolderIndexerFixture.searchOnlyOne(searchTerm);

		Document document = bookmarksFolderIndexerFixture.searchOnlyOne(
			searchTerm, locale);

		bookmarksFolderIndexerFixture.deleteDocument(document);

		bookmarksFolderIndexerFixture.searchNoOne(searchTerm, locale);

		bookmarksFolderIndexerFixture.reindex(bookmarksFolder.getCompanyId());

		bookmarksFolderIndexerFixture.searchOnlyOne(searchTerm);
	}

}