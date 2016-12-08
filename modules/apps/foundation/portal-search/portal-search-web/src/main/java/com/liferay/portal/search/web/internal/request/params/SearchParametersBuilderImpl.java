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

import com.liferay.portal.search.web.portlet.SearchParametersBuilder;
import com.liferay.portal.search.web.portlet.SearchParametersConfiguration;

import java.util.Optional;

/**
 * @author André de Oliveira
 */
public class SearchParametersBuilderImpl implements SearchParametersBuilder {

	@Override
	public SearchParametersConfiguration build() {
		SearchParametersConfigurationImpl searchParametersConfigurationImpl =
			new SearchParametersConfigurationImpl();

		_fromParameterName.ifPresent(
			searchParametersConfigurationImpl::setFromParameterName);

		_keywordsParameterName.ifPresent(
			searchParametersConfigurationImpl::setKeywordsParameterName);

		return searchParametersConfigurationImpl;
	}

	@Override
	public void setFromParameterName(String fromParameterName) {
		_fromParameterName = Optional.of(fromParameterName);
	}

	@Override
	public void setKeywordsParameterName(String keywordsParameterName) {
		_keywordsParameterName = Optional.of(keywordsParameterName);
	}

	private Optional<String> _fromParameterName = Optional.empty();
	private Optional<String> _keywordsParameterName = Optional.empty();

}