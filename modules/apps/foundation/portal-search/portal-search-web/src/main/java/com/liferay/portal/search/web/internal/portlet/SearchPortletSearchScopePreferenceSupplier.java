package com.liferay.portal.search.web.internal.portlet;

import com.liferay.portal.search.web.internal.display.context.SearchScopePreference;
import com.liferay.portal.search.web.internal.display.context.SearchScopePreferenceStringSupplier;
import com.liferay.portal.search.web.internal.display.context.SearchScopePreferenceSupplier;
public class SearchPortletSearchScopePreferenceSupplier
	implements SearchScopePreferenceSupplier {

	public SearchPortletSearchScopePreferenceSupplier(
		SearchScopePreferenceStringSupplier searchScopePreferenceStringSupplier) {

		_searchScopePreferenceStringSupplier =
			searchScopePreferenceStringSupplier;
	}

	@Override
	public SearchScopePreference getSearchScopePreference() {
		return SearchScopePreference.getSearchScopePreference(
			_searchScopePreferenceStringSupplier.getSearchScopePreferenceString());
	}

	private final SearchScopePreferenceStringSupplier
		_searchScopePreferenceStringSupplier;

}