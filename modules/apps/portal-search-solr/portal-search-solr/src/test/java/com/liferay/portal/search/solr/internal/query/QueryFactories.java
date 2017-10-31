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

import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.StringPool;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * @author Bryan Engler
 */
public class QueryFactories {

	public static final QueryFactory MATCH = new QueryFactory() {

		@Override
		public Query create(String name, String text) {
			text = _encloseMultiword(
				text, StringPool.OPEN_PARENTHESIS,
				StringPool.CLOSE_PARENTHESIS);

			return new TermQuery(new Term(name, text));
		}

	};

	public static final QueryFactory MATCH_PHRASE_PREFIX = new QueryFactory() {

		@Override
		public Query create(String name, String text) {
			text = text.concat(StringPool.STAR);

			text = _encloseMultiword(
				text, StringPool.OPEN_PARENTHESIS + StringPool.PLUS,
				StringPool.CLOSE_PARENTHESIS);

			return new TermQuery(new Term(name, text));
		}

	};

	private static String _encloseMultiword(
		String value, String open, String close) {

		if (value.indexOf(CharPool.SPACE) == -1) {
			return value;
		}

		return open + value + close;
	}

}