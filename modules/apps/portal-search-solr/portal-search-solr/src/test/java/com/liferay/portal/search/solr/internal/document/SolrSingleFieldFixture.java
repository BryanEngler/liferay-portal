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

package com.liferay.portal.search.solr.internal.document;

import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.search.solr.internal.query.QueryFactory;
import com.liferay.portal.search.solr.internal.query.SearchAssert;

import java.util.List;

import org.apache.lucene.search.Query;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;

/**
 * @author Bryan Engler
 */
public class SingleFieldFixture {

	public SingleFieldFixture(SolrClient client) {
		_client = client;
	}

	public void assertHighlights(String text, String... expected)
		throws Exception {

		SearchAssert.assertHighlights(
			_client, _field, _createQuery(text), expected);
	}

	public void assertNoHits(String text) throws Exception {
		SearchAssert.assertNoHits(_client, _field, _createQuery(text));
	}

	public void assertSearch(String text, String... expected) throws Exception {
		SearchAssert.assertSearch(
			_client, _field, _createQuery(text), expected);
	}

	public void deleteDocuments(List<String> uids) throws Exception {
		_client.deleteById(uids);

		_client.commit();
	}

	public String indexDocument(String prefix, String value) throws Exception {
		SolrInputDocument solrInputDocument = new SolrInputDocument();

		String uid =
			prefix + StringPool.UNDERLINE + RandomTestUtil.randomString();

		solrInputDocument.addField(Field.UID, uid);

		solrInputDocument.addField(_field, value);

		_client.add(solrInputDocument);

		_client.commit();

		return uid;
	}

	public void setField(String field) {
		_field = field;
	}

	public void setQueryFactory(QueryFactory queryFactory) {
		_queryFactory = queryFactory;
	}

	private Query _createQuery(String text) {
		return _queryFactory.create(_field, text);
	}

	private final SolrClient _client;
	private String _field;
	private QueryFactory _queryFactory;

}