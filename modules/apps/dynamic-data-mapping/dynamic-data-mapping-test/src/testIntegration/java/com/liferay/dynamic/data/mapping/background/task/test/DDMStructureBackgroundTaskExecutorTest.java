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

package com.liferay.dynamic.data.mapping.background.task.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.test.util.RandomTestUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Lucas Marques de Paula
 */
@RunWith(Arquillian.class)
public final class DDMStructureBackgroundTaskExecutorTest
	extends BaseDDMStructureBackgroundTaskExecutor {

	@Before
	@Override
	public void setUp() {
		super.setUp();
	}

	@Test
	public void testReindexJournalArticle() throws Exception, PortalException {
		Group group = addGroup();

		DDMStructure ddmStructure = addDDMStructure(group);

		DDMTemplate template = addDDMTemplate(group, ddmStructure);

		String keywords = "私はプログラミングが大好きです";

		for (int i = 1; i <= NUMBER_OF_JOURNAL_ARTICLES; i++) {
			String title = String.format(
				"%s - %s - %02d", keywords, RandomTestUtil.randomString(), i);

			addJournalArticle(group, ddmStructure, template, title);
		}

		IndexerFixture indexerFixture = new IndexerFixture(
			JournalArticle.class);

		Hits hits = indexerFixture.search(keywords);

		assertHitsLength(NUMBER_OF_JOURNAL_ARTICLES, hits);

		indexerFixture.deleteDocuments(hits.getDocs());

		assertHitsLength(0, indexerFixture.search(keywords));

		reindex(ddmStructure);

		assertHitsLength(
			NUMBER_OF_JOURNAL_ARTICLES, indexerFixture.search(keywords));
	}

}