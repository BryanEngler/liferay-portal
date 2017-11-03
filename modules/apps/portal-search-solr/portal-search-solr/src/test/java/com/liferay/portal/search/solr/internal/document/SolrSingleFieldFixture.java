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
import com.liferay.portal.search.solr.internal.query.SearchAssert;
import com.liferay.portal.search.test.util.document.BaseSingleFieldFixture;
import com.liferay.portal.search.test.util.document.SingleFieldQueryFactory;

import java.util.List;

import org.apache.lucene.search.Query;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;

/**
 * @author Bryan Engler
 */
public class SolrSingleFieldFixture extends BaseSingleFieldFixture {

	public SolrSingleFieldFixture(SolrClient client) {
		_client = client;
	}

	@Override
	public void assertHighlights(String text, String... expected)
		throws Exception {

		SearchAssert.assertHighlights(
			_client, getField(), _createQuery(text), expected);
	}

	@Override
	public void assertNoHits(String text) throws Exception {
		SearchAssert.assertNoHits(_client, getField(), _createQuery(text));
	}

	@Override
	public void assertSearch(String text, String... expected) throws Exception {
		SearchAssert.assertSearch(
			_client, getField(), _createQuery(text), expected);
	}

	@Override
	public void deleteDocuments(List<String> uids) throws Exception {
		_client.deleteById(uids);

		_client.commit();
	}

	@Override
	public String indexDocument(String value) throws Exception {
		SolrInputDocument solrInputDocument = new SolrInputDocument();

		String uid = RandomTestUtil.randomString();

		solrInputDocument.addField(Field.UID, uid);

		solrInputDocument.addField(getField(), value);

		_client.add(solrInputDocument);

		_client.commit();

		return uid;
	}

	@Override
	public void setSingleFieldQueryFactory(
		SingleFieldQueryFactory singleFieldQueryFactory) {

		_singleFieldQueryFactory = singleFieldQueryFactory;
	}

	private Query _createQuery(String text) {
		return _singleFieldQueryFactory.create(getField(), text);
	}

	private final SolrClient _client;
	private SingleFieldQueryFactory<Query> _singleFieldQueryFactory;

}