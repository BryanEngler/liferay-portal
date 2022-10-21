/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.search.experiences.internal.blueprint.search.request.body.contributor;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.index.IndexInformation;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.search.experiences.blueprint.exception.InvalidIndexNameException;
import com.liferay.search.experiences.internal.blueprint.parameter.SXPParameterData;
import com.liferay.search.experiences.rest.dto.v1_0.Configuration;
import com.liferay.search.experiences.rest.dto.v1_0.IndexConfiguration;

import java.beans.ExceptionListener;

import java.util.Arrays;
import java.util.List;

/**
 * @author Wade Cao
 */
public class IndexSXPSearchRequestBodyContributor
	implements SXPSearchRequestBodyContributor {

	public IndexSXPSearchRequestBodyContributor(
		IndexInformation indexInformation) {

		_indexInformation = indexInformation;
	}

	@Override
	public void contribute(
		Configuration configuration, SearchRequestBuilder searchRequestBuilder,
		SXPParameterData sxpParameterData) {

		IndexConfiguration indexConfiguration =
			configuration.getIndexConfiguration();

		if ((indexConfiguration == null) ||
			Validator.isBlank(indexConfiguration.getIndexName())) {

			return;
		}

		RuntimeException runtimeException = new RuntimeException();

		_processIndexName(
			runtimeException::addSuppressed, indexConfiguration.getExternal(),
			indexConfiguration.getIndexName(), searchRequestBuilder,
			searchRequestBuilder.withSearchContextGet(
				searchContext -> searchContext));

		if (ArrayUtil.isNotEmpty(runtimeException.getSuppressed())) {
			throw runtimeException;
		}
	}

	@Override
	public String getName() {
		return "indexConfiguration";
	}

	private void _processIndexName(
		ExceptionListener exceptionListener, boolean external, String indexName,
		SearchRequestBuilder searchRequestBuilder,
		SearchContext searchContext) {

		String fullIndexName = null;

		if (external) {
			fullIndexName = "external:" + indexName;
		}
		else {
			String companyIndexName = _indexInformation.getCompanyIndexName(
				searchContext.getCompanyId());

			fullIndexName = companyIndexName + StringPool.DASH + indexName;
		}

		List<String> indexNames = Arrays.asList(
			_indexInformation.getIndexNames());

		if (!indexNames.contains(fullIndexName)) {
			exceptionListener.exceptionThrown(
				new InvalidIndexNameException(
					"Unable to resolve index name " + fullIndexName));

			return;
		}

		searchRequestBuilder.addIndex(fullIndexName);
	}

	private final IndexInformation _indexInformation;

}