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

package com.liferay.headless.search.internal.resource.v1_0;

import com.liferay.headless.search.dto.v1_0.Results;
import com.liferay.headless.search.resource.v1_0.ResultsResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Bryan Engler
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/results.properties",
	scope = ServiceScope.PROTOTYPE, service = ResultsResource.class
)
public class ResultsResourceImpl extends BaseResultsResourceImpl {

	@Override
	public Results getSearchIndexKeywordsHiddenStartDelta(
			String index,
			String keywords,
			String hidden, Long start,
			Long delta)
		throws Exception {

		return new Results();
	}

}