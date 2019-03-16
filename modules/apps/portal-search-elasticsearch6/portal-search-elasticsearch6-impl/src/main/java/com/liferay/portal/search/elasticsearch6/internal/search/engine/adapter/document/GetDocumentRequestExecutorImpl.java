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

import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.document.DocumentBuilderFactory;
import com.liferay.portal.search.elasticsearch6.internal.document.DocumentFieldsProcessor;
import com.liferay.portal.search.engine.adapter.document.BulkableDocumentRequestTranslator;
import com.liferay.portal.search.engine.adapter.document.GetDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.GetDocumentResponse;
import com.liferay.portal.search.geolocation.GeoBuilders;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.update.UpdateRequestBuilder;

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
		GetRequestBuilder getRequestBuilder =
			_bulkableDocumentRequestTranslator.translate(
				getDocumentRequest, null);

		GetResponse getResponse = getRequestBuilder.get();

		GetDocumentResponse getDocumentResponse = new GetDocumentResponse(
			getResponse.isExists());

		if (!getResponse.isExists()) {
			return getDocumentResponse;
		}

		getDocumentResponse.setSource(getResponse.getSourceAsString());
		getDocumentResponse.setVersion(getResponse.getVersion());

		DocumentFieldsProcessor documentFieldsProcessor =
			new DocumentFieldsProcessor(_documentBuilderFactory, _geoBuilders);

		Document document = documentFieldsProcessor.process(
			getResponse.getFields(), null);

		getDocumentResponse.setDocument(document);

		return getDocumentResponse;
	}

	@Reference(target = "(search.engine.impl=Elasticsearch)", unbind = "-")
	protected void setBulkableDocumentRequestTranslator(
		BulkableDocumentRequestTranslator
			<DeleteRequestBuilder, GetRequestBuilder, IndexRequestBuilder,
			 UpdateRequestBuilder, BulkRequestBuilder>
				bulkableDocumentRequestTranslator) {

		_bulkableDocumentRequestTranslator = bulkableDocumentRequestTranslator;
	}

	@Reference(unbind = "-")
	protected void setDocumentBuilderFactory(
		DocumentBuilderFactory documentBuilderFactory) {

		_documentBuilderFactory = documentBuilderFactory;
	}

	@Reference(unbind = "-")
	protected void setGeoBuilders(GeoBuilders geoBuilders) {
		_geoBuilders = geoBuilders;
	}

	private BulkableDocumentRequestTranslator
		<DeleteRequestBuilder, GetRequestBuilder, IndexRequestBuilder,
		 UpdateRequestBuilder, BulkRequestBuilder>
			_bulkableDocumentRequestTranslator;
	private DocumentBuilderFactory _documentBuilderFactory;
	private GeoBuilders _geoBuilders;

}