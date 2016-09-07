package com.liferay.portal.search.web.internal.portlet;

import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.search.web.internal.display.context.SearchScopePreferenceStringSupplier;

import javax.portlet.PortletPreferences;
public class SearchPortletSearchScopePreferenceStringSupplier
	implements SearchScopePreferenceStringSupplier {

	public SearchPortletSearchScopePreferenceStringSupplier(
		PortletPreferences portletPreferences) {

		_portletPreferences = portletPreferences;
	}

	@Override
	public String getSearchScopePreferenceString() {
		if (_searchScopePreferenceString != null) {
			return _searchScopePreferenceString;
		}

		_searchScopePreferenceString = _portletPreferences.getValue(
			"searchScope", StringPool.BLANK);

		return _searchScopePreferenceString;
	}

	private final PortletPreferences _portletPreferences;
	private String _searchScopePreferenceString;

}