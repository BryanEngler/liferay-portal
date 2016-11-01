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

import com.liferay.portal.search.elasticsearch.internal.connection.ElasticsearchFixture.IndexName;

import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;

/**
 * @author André de Oliveira
 */
public class SingleFieldFixture {

	public SingleFieldFixture(Client client, IndexName indexName, String type) {
		_client = client;
		_index = indexName.getName();
		_type = type;
	}

	public void indexDocument(String value) {
		IndexRequestBuilder indexRequestBuilder = _client.prepareIndex(
			_index, _type);

		indexRequestBuilder.setSource(_field, value);

		indexRequestBuilder.get();
	}

	public void setField(String field) {
		_field = field;
	}

	private final Client _client;
	private String _field;
	private final String _index;
	private final String _type;

}