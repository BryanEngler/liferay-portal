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

package com.liferay.portal.search.web.internal.portlet;

import com.liferay.portal.kernel.search.QueryConfig;
import com.liferay.portal.search.web.internal.display.context.QueryConfigPreferences;
import com.liferay.portal.search.web.internal.display.context.QueryConfigSupplier;

/**
 * @author André de Oliveira
 */
public class SearchPortletQueryConfigSupplier implements QueryConfigSupplier {

	public SearchPortletQueryConfigSupplier(
		QueryConfigPreferences queryConfigPreferences) {

		_queryConfigPreferences = queryConfigPreferences;
	}

	@Override
	public QueryConfig getQueryConfig() {
		if (_queryConfig != null) {
			return _queryConfig;
		}

		_queryConfig = new QueryConfig();

		_queryConfig.setCollatedSpellCheckResultEnabled(
			_queryConfigPreferences.isCollatedSpellCheckResultEnabled());
		_queryConfig.setCollatedSpellCheckResultScoresThreshold(
			_queryConfigPreferences.
				getCollatedSpellCheckResultDisplayThreshold());
		_queryConfig.setQueryIndexingEnabled(
			_queryConfigPreferences.isQueryIndexingEnabled());
		_queryConfig.setQueryIndexingThreshold(
			_queryConfigPreferences.getQueryIndexingThreshold());
		_queryConfig.setQuerySuggestionEnabled(
			_queryConfigPreferences.isQuerySuggestionsEnabled());
		_queryConfig.setQuerySuggestionScoresThreshold(
			_queryConfigPreferences.getQuerySuggestionsDisplayThreshold());
		_queryConfig.setQuerySuggestionsMax(
			_queryConfigPreferences.getQuerySuggestionsMax());

		return _queryConfig;
	}

	private QueryConfig _queryConfig;
	private final QueryConfigPreferences _queryConfigPreferences;

}