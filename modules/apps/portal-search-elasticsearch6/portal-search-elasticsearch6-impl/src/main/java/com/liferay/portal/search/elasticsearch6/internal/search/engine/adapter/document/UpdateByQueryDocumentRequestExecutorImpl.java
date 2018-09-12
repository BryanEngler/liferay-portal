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

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.search.elasticsearch6.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.search.engine.adapter.document.UpdateByQueryDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.UpdateByQueryDocumentResponse;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.script.Script;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Dylan Rebelak
 */
@Component(
	immediate = true, service = UpdateByQueryDocumentRequestExecutor.class
)
public class UpdateByQueryDocumentRequestExecutorImpl
	implements UpdateByQueryDocumentRequestExecutor {

	@Override
	public UpdateByQueryDocumentResponse execute(
		UpdateByQueryDocumentRequest updateByQueryDocumentRequest) {

		UpdateByQueryRequest updateByQueryRequest =
			createUpdateByQueryRequest(updateByQueryDocumentRequest);

		RestHighLevelClient restHighLevelClient =
			elasticsearchConnectionManager.getRestHighLevelClient();

		//no high level REST api yet. coming soon ~6.5.0
		BulkByScrollResponse bulkByScrollResponse =
			//restHighLevelClient.updateByQuery(
			//	updateByQueryRequest, RequestOptions.DEFAULT);
			new BulkByScrollResponse();

		TimeValue timeValue = bulkByScrollResponse.getTook();

		UpdateByQueryDocumentResponse updateByQueryDocumentResponse =
			new UpdateByQueryDocumentResponse(
				bulkByScrollResponse.getUpdated(), timeValue.getMillis());

		return updateByQueryDocumentResponse;
	}

	protected UpdateByQueryRequest createUpdateByQueryRequest(
		UpdateByQueryDocumentRequest updateByQueryDocumentRequest) {

		UpdateByQueryRequest updateByQueryRequest = new UpdateByQueryRequest();

		Query query = updateByQueryDocumentRequest.getQuery();

		QueryBuilder queryBuilder = new QueryStringQueryBuilder(
			query.toString());

		//no api yet. coming soon ~6.5.0
//		updateByQueryRequest.setQuery(queryBuilder); //was filter

		updateByQueryRequest.setRefresh(
			updateByQueryDocumentRequest.isRefresh());

		JSONObject jsonObject =
			updateByQueryDocumentRequest.getScriptJSONObject();

		if (jsonObject != null) {
			Script script = new Script(jsonObject.toString());

			updateByQueryRequest.setScript(script);
		}

		updateByQueryRequest.indices(
			updateByQueryDocumentRequest.getIndexNames()); //was source

		return updateByQueryRequest;
	}

	@Reference
	protected ElasticsearchConnectionManager elasticsearchConnectionManager;

}