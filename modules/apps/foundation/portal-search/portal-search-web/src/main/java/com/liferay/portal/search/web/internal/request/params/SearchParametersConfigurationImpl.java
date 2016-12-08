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

package com.liferay.portal.search.web.internal.request.params;

import com.liferay.portal.search.web.portlet.SearchParametersConfiguration;

/**
 * @author André de Oliveira
 */
public class SearchParametersConfigurationImpl
	implements SearchParametersConfiguration {

	@Override
	public String getFromParameterName() {
		return _fromParameterName;
	}

	@Override
	public String getKeywordsParameterName() {
		return _keywordsParameterName;
	}

	public void setFromParameterName(String fromParameterName) {
		_fromParameterName = fromParameterName;
	}

	public void setKeywordsParameterName(String keywordsParameterName) {
		_keywordsParameterName = keywordsParameterName;
	}

	private String _fromParameterName =
		SearchParametersConfiguration.DEFAULT_FROM_PARAMETER_NAME;
	private String _keywordsParameterName =
		SearchParametersConfiguration.DEFAULT_KEYWORDS_PARAMETER_NAME;

}