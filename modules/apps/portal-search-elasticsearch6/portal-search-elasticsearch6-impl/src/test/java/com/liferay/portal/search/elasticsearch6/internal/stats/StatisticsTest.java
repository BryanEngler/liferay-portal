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

package com.liferay.portal.search.elasticsearch6.internal.stats;

import com.liferay.portal.search.elasticsearch6.internal.ElasticsearchIndexingFixture;
import com.liferay.portal.search.elasticsearch6.internal.connection.ElasticsearchFixture;
import com.liferay.portal.search.elasticsearch6.internal.connection.IndexCreator;
import com.liferay.portal.search.elasticsearch6.internal.connection.IndicesAdminClientSupplier;
import com.liferay.portal.search.elasticsearch6.internal.connection.LiferayIndexCreationHelper;
import com.liferay.portal.search.elasticsearch6.internal.index.LiferayDocumentTypeFactory;
import com.liferay.portal.search.test.util.indexing.BaseIndexingTestCase;
import com.liferay.portal.search.test.util.indexing.IndexingFixture;
import com.liferay.portal.search.test.util.stats.BaseStatisticsTestCase;

import org.junit.Test;

/**
 * @author André de Oliveira
 */
public class StatisticsTest extends BaseStatisticsTestCase {

	@Override
	@Test
	public void testGetStats() throws Exception {
		super.testGetStats();
	}

	@Override
	protected IndexingFixture createIndexingFixture() {
		ElasticsearchFixture elasticsearchFixture = new ElasticsearchFixture(
			StatisticsTest.class.getSimpleName());

		IndexCreator indexCreator = new IndexCreator(elasticsearchFixture);

		indexCreator.setIndexCreationHelper(
			new PriorityFieldLiferayIndexCreationHelper(elasticsearchFixture));

		return new ElasticsearchIndexingFixture(
			elasticsearchFixture, BaseIndexingTestCase.COMPANY_ID,
			indexCreator);
	}

	private static class PriorityFieldLiferayIndexCreationHelper
		extends LiferayIndexCreationHelper {

		public PriorityFieldLiferayIndexCreationHelper(
			IndicesAdminClientSupplier indicesAdminClientSupplier) {

			super(indicesAdminClientSupplier);
		}

		@Override
		public void whenIndexCreated(String indexName) {
			super.whenIndexCreated(indexName);

			LiferayDocumentTypeFactory liferayDocumentTypeFactory =
				getLiferayDocumentTypeFactory();

			String source =
				"{ \"properties\": { \"priority\": { \"store\": true, " +
				"\"type\": \"double\" } } }";

			liferayDocumentTypeFactory.addTypeMappings(indexName, source);
		}

	}

}