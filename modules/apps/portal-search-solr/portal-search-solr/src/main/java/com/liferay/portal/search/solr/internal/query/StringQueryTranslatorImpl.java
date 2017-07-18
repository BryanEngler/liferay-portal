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

package com.liferay.portal.search.solr.internal.query;

import com.liferay.portal.kernel.search.generic.StringQuery;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.solr.query.StringQueryTranslator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.service.component.annotations.Component;

/**
 * @author Michael C. Han
 */
@Component(immediate = true, service = StringQueryTranslator.class)
public class StringQueryTranslatorImpl implements StringQueryTranslator {

	@Override
	public org.apache.lucene.search.Query translate(StringQuery stringQuery) {
		return new org.apache.lucene.search.Query() {

			@Override
			public String toString(String field) {
				String query = stringQuery.getQuery();

				Matcher matcher = _negatedWord.matcher(query);

				while (matcher.find()) {
					StringBundler sb = new StringBundler(5);

					sb.append(StringPool.OPEN_PARENTHESIS);
					sb.append("*:*");
					sb.append(StringPool.SPACE);
					sb.append(matcher.group());
					sb.append(StringPool.CLOSE_PARENTHESIS);

					query = StringUtil.replace(
						query, matcher.group(), sb.toString());
				}

				return query;
			}

		};
	}

	private final Pattern _negatedWord = Pattern.compile("(!|NOT[\\s]+)[\\w]+");

}