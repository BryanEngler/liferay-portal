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

package com.liferay.portal.kernel.search.facet;

import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerPostProcessor;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.filter.BooleanFilter;
import com.liferay.portal.kernel.search.filter.QueryFilter;
import com.liferay.portal.kernel.search.generic.BooleanQueryImpl;

/**
 * @author Raymond Augé
 */
public class AssetEntriesFacet extends MultiValueFacet {

	public AssetEntriesFacet(SearchContext searchContext) {
		super(searchContext);

		setFieldName(Field.ENTRY_CLASS_NAME);
	}

	/**
	 * @deprecated As of 7.0.0, with no direct replacement
	 */
	@Deprecated
	protected void initFacetClause() {
	}

	/**
	 * @deprecated As of 7.0.0, added strictly to support backwards
	 *             compatibility of {@link
	 *             Indexer#postProcessContextQuery(BooleanQuery, SearchContext)}
	 */
	@Deprecated
	protected void postProcessContextQuery(
			BooleanFilter entityFilter, SearchContext searchContext,
			Indexer<?> indexer)
		throws Exception {

		BooleanQuery entityBooleanQuery = new BooleanQueryImpl();

		indexer.postProcessContextQuery(entityBooleanQuery, searchContext);

		for (IndexerPostProcessor indexerPostProcessor :
				indexer.getIndexerPostProcessors()) {

			indexerPostProcessor.postProcessContextQuery(
				entityBooleanQuery, searchContext);
		}

		if (entityBooleanQuery.hasClauses()) {
			QueryFilter queryFilter = new QueryFilter(entityBooleanQuery);

			entityFilter.add(queryFilter, BooleanClauseOccur.MUST);
		}
	}

}