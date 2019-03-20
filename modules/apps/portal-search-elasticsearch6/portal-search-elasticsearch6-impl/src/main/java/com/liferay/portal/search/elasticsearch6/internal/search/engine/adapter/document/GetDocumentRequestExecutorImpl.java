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

package com.liferay.portal.search.elasticsearch6.internal.search.engine.adapter.document;

import com.liferay.portal.search.elasticsearch6.internal.connection.ElasticsearchClientResolver;
import com.liferay.portal.search.engine.adapter.document.GetDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.GetDocumentResponse;

import org.elasticsearch.action.get.GetAction;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bryan Engler
 */
@Component(immediate = true, service = GetDocumentRequestExecutor.class)
public class GetDocumentRequestExecutorImpl
	implements GetDocumentRequestExecutor {

	@Override
	public GetDocumentResponse execute(GetDocumentRequest getDocumentRequest) {
		GetRequestBuilder getRequestBuilder = _translate(getDocumentRequest);

		GetResponse getResponse = getRequestBuilder.get();

		GetDocumentResponse getDocumentResponse = new GetDocumentResponse(
			getResponse.isExists(), getResponse.getSourceAsString());

		return getDocumentResponse;
	}

	@Reference(unbind = "-")
	protected void setElasticsearchClientResolver(
		ElasticsearchClientResolver elasticsearchClientResolver) {

		_elasticsearchClientResolver = elasticsearchClientResolver;
	}

	private GetRequestBuilder _translate(
		GetDocumentRequest getDocumentRequest) {

		Client client = _elasticsearchClientResolver.getClient();

		GetRequestBuilder getRequestBuilder =
			GetAction.INSTANCE.newRequestBuilder(client);

		getRequestBuilder.setId(getDocumentRequest.getUid());
		getRequestBuilder.setIndex(getDocumentRequest.getIndexName());
		getRequestBuilder.setRefresh(getDocumentRequest.isRefresh());
		getRequestBuilder.setType(getDocumentRequest.getType());

		return getRequestBuilder;
	}

	private ElasticsearchClientResolver _elasticsearchClientResolver;

}