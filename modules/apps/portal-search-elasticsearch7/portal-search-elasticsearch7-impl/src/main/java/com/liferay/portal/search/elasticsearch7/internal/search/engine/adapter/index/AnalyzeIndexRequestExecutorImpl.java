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

package com.liferay.portal.search.elasticsearch7.internal.search.engine.adapter.index;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.elasticsearch7.internal.connection.ElasticsearchClientResolver;
import com.liferay.portal.search.elasticsearch7.internal.io.StringOutputStream;
import com.liferay.portal.search.engine.adapter.index.AnalysisIndexResponseToken;
import com.liferay.portal.search.engine.adapter.index.AnalyzeIndexRequest;
import com.liferay.portal.search.engine.adapter.index.AnalyzeIndexResponse;

import java.io.IOException;

import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.AnalyzeRequest;
import org.elasticsearch.client.indices.AnalyzeResponse;
import org.elasticsearch.client.indices.DetailAnalyzeResponse;
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

		AnalyzeRequest analyzeRequest;

		if (Validator.isNotNull(analyzeIndexRequest.getAnalyzer())) {
			analyzeRequest = AnalyzeRequest.withIndexAnalyzer(
				analyzeIndexRequest.getIndexName(),
				analyzeIndexRequest.getAnalyzer(),
				analyzeIndexRequest.getTexts());
		}
		else if (Validator.isNotNull(analyzeIndexRequest.getFieldName())) {
			analyzeRequest = AnalyzeRequest.withField(
				analyzeIndexRequest.getIndexName(),
				analyzeIndexRequest.getFieldName(),
				analyzeIndexRequest.getTexts());
		}
		else if (Validator.isNotNull(analyzeIndexRequest.getNormalizer())) {
			analyzeRequest = AnalyzeRequest.withNormalizer(
				analyzeIndexRequest.getIndexName(),
				analyzeIndexRequest.getNormalizer(),
				analyzeIndexRequest.getTexts());
		}
		else {
			AnalyzeRequest.CustomAnalyzerBuilder customAnalyzerBuilder;

			if (Validator.isNotNull(analyzeIndexRequest.getTokenizer())) {
				customAnalyzerBuilder = AnalyzeRequest.buildCustomAnalyzer(
					analyzeIndexRequest.getIndexName(),
					analyzeIndexRequest.getTokenizer());
			}
			else {
				customAnalyzerBuilder = AnalyzeRequest.buildCustomNormalizer(
					analyzeIndexRequest.getIndexName());
			}

			analyzeRequest = createAnalyzeRequest(
				customAnalyzerBuilder, analyzeIndexRequest);
		}

		analyzeRequest.attributes(analyzeIndexRequest.getAttributesArray());
		analyzeRequest.explain(analyzeIndexRequest.isExplain());

		return analyzeRequest;
	}

	protected AnalyzeRequest createAnalyzeRequest(
		AnalyzeRequest.CustomAnalyzerBuilder customAnalyzerBuilder,
		AnalyzeIndexRequest analyzeIndexRequest) {

		for (String charFilter : analyzeIndexRequest.getCharFilters()) {
			customAnalyzerBuilder.addCharFilter(charFilter);
		}

		for (String tokenFilter : analyzeIndexRequest.getTokenFilters()) {
			customAnalyzerBuilder.addTokenFilter(tokenFilter);
		}

		return customAnalyzerBuilder.build(analyzeIndexRequest.getTexts());
	}

	protected AnalyzeResponse getAnalyzeResponse(
		AnalyzeRequest analyzeRequest) {

		RestHighLevelClient restHighLevelClient =
			_elasticsearchClientResolver.getRestHighLevelClient();

		IndicesClient indicesClient = restHighLevelClient.indices();

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
				//detailAnalyzeResponse.writeTo(outputStreamStreamOutput);

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

	@Reference(unbind = "-")
	protected void setElasticsearchClientResolver(
		ElasticsearchClientResolver elasticsearchClientResolver) {

		_elasticsearchClientResolver = elasticsearchClientResolver;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AnalyzeIndexRequestExecutorImpl.class);

	private ElasticsearchClientResolver _elasticsearchClientResolver;

}