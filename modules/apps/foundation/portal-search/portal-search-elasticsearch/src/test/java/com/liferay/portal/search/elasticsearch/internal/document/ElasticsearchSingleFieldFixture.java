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

package com.liferay.portal.search.elasticsearch.internal.document;

import com.liferay.portal.search.elasticsearch.internal.connection.IndexName;
import com.liferay.portal.search.elasticsearch.internal.query.SearchAssert;
import com.liferay.portal.search.test.util.document.BaseSingleFieldFixture;
import com.liferay.portal.search.test.util.document.SingleFieldQueryFactory;

import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;

/**
 * @author André de Oliveira
 */
public class ElasticsearchSingleFieldFixture extends BaseSingleFieldFixture {

	public ElasticsearchSingleFieldFixture(
		Client client, IndexName indexName, String type) {

		_client = client;
		_index = indexName.getName();
		_type = type;
	}

	@Override
	public void assertHighlights(String text, String... expected)
		throws Exception {

		SearchAssert.assertHighlights(
			_client, getField(), _createQueryBuilder(text), expected);
	}

	@Override
	public void assertNoHits(String text) throws Exception {
		SearchAssert.assertNoHits(
			_client, getField(), _createQueryBuilder(text));
	}

	@Override
	public void assertSearch(String text, String... expected) throws Exception {
		SearchAssert.assertSearch(
			_client, getField(), _createQueryBuilder(text), expected);
	}

	@Override
	public String indexDocument(String value) {
		IndexRequestBuilder indexRequestBuilder = _client.prepareIndex(
			_index, _type);

		indexRequestBuilder.setSource(getField(), value);

		IndexResponse indexResponse = indexRequestBuilder.get();

		return indexResponse.getId();
	}

	@Override
	public void setSingleFieldQueryFactory(
		SingleFieldQueryFactory singleFieldQueryFactory) {

		_singleFieldQueryFactory = singleFieldQueryFactory;
	}

	private QueryBuilder _createQueryBuilder(String text) {
		return _singleFieldQueryFactory.create(getField(), text);
	}

	private final Client _client;
	private final String _index;
	private SingleFieldQueryFactory<QueryBuilder> _singleFieldQueryFactory;
	private final String _type;

}