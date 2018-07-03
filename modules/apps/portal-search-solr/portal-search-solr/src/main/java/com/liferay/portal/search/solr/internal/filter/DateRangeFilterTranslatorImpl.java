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

package com.liferay.portal.search.solr.internal.filter;

import com.liferay.portal.search.filter.DateRangeFilter;
import com.liferay.portal.search.solr.internal.util.DateFormatUtil;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;

import org.osgi.service.component.annotations.Component;

/**
 * @author André de Oliveira
 */
@Component(immediate = true, service = DateRangeFilterTranslator.class)
public class DateRangeFilterTranslatorImpl
	implements DateRangeFilterTranslator {

	@Override
	public Query translate(DateRangeFilter dateRangeFilter) {
		String from = DateFormatUtil.getFormattedDateString(
			dateRangeFilter.getFormat(), dateRangeFilter.getFrom());

		String to = DateFormatUtil.getFormattedDateString(
			dateRangeFilter.getFormat(), dateRangeFilter.getTo());

		return TermRangeQuery.newStringRange(
			dateRangeFilter.getFieldName(), from, to,
			dateRangeFilter.isIncludeLower(),
			dateRangeFilter.isIncludeUpper());
	}

}