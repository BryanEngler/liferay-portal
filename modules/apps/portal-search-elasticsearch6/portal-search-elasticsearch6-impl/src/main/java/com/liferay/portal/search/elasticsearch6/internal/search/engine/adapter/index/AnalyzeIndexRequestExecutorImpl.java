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

package com.liferay.portal.search.elasticsearch6.internal.search.engine.adapter.index;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.elasticsearch6.internal.connection.ElasticsearchConnectionManager;
import com.liferay.portal.search.elasticsearch6.internal.io.StringOutputStream;
import com.liferay.portal.search.engine.adapter.index.AnalysisIndexResponseToken;
import com.liferay.portal.search.engine.adapter.index.AnalyzeIndexRequest;
import com.liferay.portal.search.engine.adapter.index.AnalyzeIndexResponse;

import java.io.IOException;

import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.admin.indices.analyze.DetailAnalyzeResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.io.stream.OutputStreamStreamOutput;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael C. Han
 */
@Component(service = AnalyzeIndexRequestExecutor.class)
public class AnalyzeIndexRequestExecutorImpl
	implements AnalyzeIndexRequestExecutor {

	@Override
	public AnalyzeIndexResponse execute(
		AnalyzeIndexRequest analyzeIndexRequest) {

		AnalyzeRequest analyzeRequest = createAnalyzeRequest(
			analyzeIndexRequest);

		AnalyzeResponse analyzeResponse = getAnalyzeResponse(analyzeRequest);

		AnalyzeIndexResponse analyzeIndexResponse = new AnalyzeIndexResponse();

		for (AnalyzeResponse.AnalyzeToken analyzeToken :
				analyzeResponse.getTokens()) {

			AnalysisIndexResponseToken analysisIndexResponseToken =
				new AnalysisIndexResponseToken(analyzeToken.getTerm());

			analysisIndexResponseToken.setAttributes(
				analyzeToken.getAttributes());
			analysisIndexResponseToken.setEndOffset(
				analyzeToken.getEndOffset());
			analysisIndexResponseToken.setPosition(analyzeToken.getPosition());
			analysisIndexResponseToken.setPositionLength(
				analyzeToken.getPositionLength());
			analysisIndexResponseToken.setStartOffset(
				analyzeToken.getStartOffset());
			analysisIndexResponseToken.setType(analyzeToken.getType());

			analyzeIndexResponse.addAnalysisIndexResponseTokens(
				analysisIndexResponseToken);
		}

		processDetailAnalyzeResponse(
			analyzeIndexResponse, analyzeResponse.detail());

		return analyzeIndexResponse;
	}

	protected AnalyzeRequest createAnalyzeRequest(
		AnalyzeIndexRequest analyzeIndexRequest) {

		AnalyzeRequest analyzeRequest = new AnalyzeRequest();

		if (Validator.isNotNull(analyzeIndexRequest.getAnalyzer())) {
			analyzeRequest.analyzer(analyzeIndexRequest.getAnalyzer());
		}

		analyzeRequest.attributes(analyzeIndexRequest.getAttributesArray());
		analyzeRequest.explain(analyzeIndexRequest.isExplain());

		if (Validator.isNotNull(analyzeIndexRequest.getFieldName())) {
			analyzeRequest.field(analyzeIndexRequest.getFieldName());
		}

		analyzeRequest.index(analyzeIndexRequest.getIndexName());

		if (Validator.isNotNull(analyzeIndexRequest.getNormalizer())) {
			analyzeRequest.normalizer(analyzeIndexRequest.getNormalizer());
		}

		analyzeRequest.text(analyzeIndexRequest.getTexts());

		if (Validator.isNotNull(analyzeIndexRequest.getTokenizer())) {
			analyzeRequest.tokenizer(analyzeIndexRequest.getTokenizer());
		}

		for (String charFilter : analyzeIndexRequest.getCharFilters()) {
			analyzeRequest.addCharFilter(charFilter);
		}

		for (String tokenFilter : analyzeIndexRequest.getTokenFilters()) {
			analyzeRequest.addTokenFilter(tokenFilter);
		}

		return analyzeRequest;
	}

	protected AnalyzeResponse getAnalyzeResponse(
		AnalyzeRequest analyzeRequest) {

		IndicesClient indicesClient =
			elasticsearchConnectionManager.getIndicesClient();

		try {
			return indicesClient.analyze(
				analyzeRequest, RequestOptions.DEFAULT);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	protected void processDetailAnalyzeResponse(
		AnalyzeIndexResponse analyzeIndexResponse,
		DetailAnalyzeResponse detailAnalyzeResponse) {

		if (detailAnalyzeResponse != null) {
			StringOutputStream stringOutputStream = new StringOutputStream();

			OutputStreamStreamOutput outputStreamStreamOutput =
				new OutputStreamStreamOutput(stringOutputStream);

			try {
				detailAnalyzeResponse.writeTo(outputStreamStreamOutput);

				outputStreamStreamOutput.flush();
			}
			catch (IOException ioe) {
				if (_log.isDebugEnabled()) {
					_log.debug(ioe, ioe);
				}
			}
			finally {
				try {
					outputStreamStreamOutput.close();
				}
				catch (IOException ioe) {
					if (_log.isDebugEnabled()) {
						_log.debug(ioe, ioe);
					}
				}
			}

			analyzeIndexResponse.setAnalysisDetails(
				stringOutputStream.toString());
		}
	}

	@Reference
	protected ElasticsearchConnectionManager elasticsearchConnectionManager;

	private static final Log _log = LogFactoryUtil.getLog(
		AnalyzeIndexRequestExecutorImpl.class);

}