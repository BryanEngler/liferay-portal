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

package com.liferay.trash.internal.search;

import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.IndexerPostProcessor;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.Summary;
import com.liferay.portal.kernel.search.filter.BooleanFilter;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.search.filter.QueryFilter;
import com.liferay.portal.kernel.search.filter.TermsFilter;
import com.liferay.portal.kernel.trash.TrashHandler;
import com.liferay.portal.kernel.trash.TrashHandlerRegistryUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.util.List;
import java.util.Locale;

import org.osgi.service.component.annotations.Component;

/**
 * @author Luan Maoski
 */
@Component(
	immediate = true,
	property = "indexer.class.name=com.liferay.trash.model.TrashEntry",
	service = IndexerPostProcessor.class
)
public class TrashIndexerPostProcessor implements IndexerPostProcessor {

	@Override
	public void postProcessContextBooleanFilter(
			BooleanFilter booleanFilter, SearchContext searchContext)
		throws Exception {
	}

	@Override
	public void postProcessContextQuery(
			BooleanQuery contextQuery, SearchContext searchContext)
		throws Exception {
	}

	@Override
	public void postProcessDocument(Document document, Object obj)
		throws Exception {
	}

	@Override
	public void postProcessFullQuery(
			BooleanQuery fullQuery, SearchContext searchContext)
		throws Exception {

		BooleanFilter booleanFilter = new BooleanFilter();

		fullQuery.setPreBooleanFilter(booleanFilter);

		booleanFilter.addRequiredTerm(
			Field.COMPANY_ID, searchContext.getCompanyId());

		List<TrashHandler> trashHandlers =
			TrashHandlerRegistryUtil.getTrashHandlers();

		for (TrashHandler trashHandler : trashHandlers) {
			Filter filter = trashHandler.getExcludeFilter(searchContext);

			if (filter != null) {
				booleanFilter.add(filter, BooleanClauseOccur.MUST_NOT);
			}

			processTrashHandlerExcludeQuery(
				searchContext, booleanFilter, trashHandler);
		}

		long[] groupIds = searchContext.getGroupIds();

		if (ArrayUtil.isNotEmpty(groupIds)) {
			TermsFilter groupTermsFilter = new TermsFilter(Field.GROUP_ID);

			groupTermsFilter.addValues(ArrayUtil.toStringArray(groupIds));

			booleanFilter.add(groupTermsFilter, BooleanClauseOccur.MUST);
		}

		booleanFilter.addRequiredTerm(
			Field.STATUS, WorkflowConstants.STATUS_IN_TRASH);
	}

	@Override
	public void postProcessSearchQuery(
			BooleanQuery searchQuery, BooleanFilter booleanFilter,
			SearchContext searchContext)
		throws Exception {
	}

	@Override
	public void postProcessSearchQuery(
			BooleanQuery searchQuery, SearchContext searchContext)
		throws Exception {
	}

	@Override
	public void postProcessSummary(
		Summary summary, Document document, Locale locale, String snippet) {
	}

	protected void processTrashHandlerExcludeQuery(
		SearchContext searchContext, BooleanFilter fullQueryBooleanFilter,
		TrashHandler trashHandler) {

		Query query = trashHandler.getExcludeQuery(searchContext);

		if (query != null) {
			fullQueryBooleanFilter.add(
				new QueryFilter(query), BooleanClauseOccur.MUST_NOT);
		}
	}

}