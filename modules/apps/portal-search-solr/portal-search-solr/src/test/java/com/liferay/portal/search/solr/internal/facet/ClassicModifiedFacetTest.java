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

package com.liferay.portal.search.solr.internal.facet;

import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.search.solr.internal.SolrIndexingFixture;
import com.liferay.portal.search.solr.internal.util.DateFormatUtil;
import com.liferay.portal.search.test.util.facet.BaseClassicModifiedFacetTestCase;
import com.liferay.portal.search.test.util.indexing.IndexingFixture;

import java.util.ArrayList;
import java.util.List;

/**
 * @author André de Oliveira
 */
public class ClassicModifiedFacetTest extends BaseClassicModifiedFacetTestCase {

	@Override
	protected IndexingFixture createIndexingFixture() {
		SolrIndexingFixture solrIndexingFixture = new SolrIndexingFixture();

		solrIndexingFixture.setFacetProcessor(
			new ModifiedFacetProcessor() {
				{
					jsonFactory = new JSONFactoryImpl();
				}
			});

		return solrIndexingFixture;
	}

	@Override
	protected void addDocument(String... values) throws Exception {
		List<String> formattedStrings = new ArrayList<>();

		for (String value : values) {
			formattedStrings.add(
				DateFormatUtil.getFormattedDateString("yyyyMMddHHmmss", value));
		}

		super.addDocument(ArrayUtil.toStringArray(formattedStrings));
	}

}