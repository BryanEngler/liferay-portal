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

package com.liferay.portal.search.elasticsearch.internal.query;

import com.liferay.portal.kernel.search.generic.StringQuery;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.elasticsearch.query.StringQueryTranslator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;

import org.osgi.service.component.annotations.Component;

/**
 * @author Michael C. Han
 */
@Component(immediate = true, service = StringQueryTranslator.class)
public class StringQueryTranslatorImpl implements StringQueryTranslator {

	@Override
	public QueryBuilder translate(StringQuery stringQuery) {
		String query = stringQuery.getQuery();

		Matcher matcher = _negatedWord.matcher(query);

		while (matcher.find()) {
			query = StringUtil.replace(
				query, matcher.group(),
				StringPool.OPEN_PARENTHESIS + matcher.group() +
					StringPool.CLOSE_PARENTHESIS);
		}

		QueryStringQueryBuilder queryStringQueryBuilder =
			QueryBuilders.queryStringQuery(query);

		if (!stringQuery.isDefaultBoost()) {
			queryStringQueryBuilder.boost(stringQuery.getBoost());
		}

		return queryStringQueryBuilder;
	}

	private final Pattern _negatedWord = Pattern.compile("(!|NOT[\\s]+)[\\w]+");

}