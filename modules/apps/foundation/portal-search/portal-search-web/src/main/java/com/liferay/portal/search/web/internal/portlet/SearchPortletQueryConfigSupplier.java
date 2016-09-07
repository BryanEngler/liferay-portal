package com.liferay.portal.search.web.internal.portlet;

import com.liferay.portal.kernel.search.QueryConfig;
import com.liferay.portal.search.web.internal.display.context.QueryConfigPreferences;
import com.liferay.portal.search.web.internal.display.context.QueryConfigSupplier;
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
			_queryConfigPreferences.getCollatedSpellCheckResultDisplayThreshold());
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