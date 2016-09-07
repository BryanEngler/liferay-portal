
package com.liferay.portal.search.web.internal.portlet;

import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.search.web.internal.display.context.SearchScope;
import com.liferay.portal.search.web.internal.display.context.SearchScopeGroupIdSupplier;
import com.liferay.portal.search.web.internal.display.context.SearchScopeSupplier;
import com.liferay.portal.search.web.internal.display.context.ThemeDisplaySupplier;
public class SearchPortletSearchScopeGroupIdSupplier
	implements SearchScopeGroupIdSupplier {

	public SearchPortletSearchScopeGroupIdSupplier(
		SearchScopeSupplier searchScopeSupplier,
		ThemeDisplaySupplier themeDisplaySupplier) {

		_searchScopeSupplier = searchScopeSupplier;
		_themeDisplay = themeDisplaySupplier.getThemeDisplay();
	}

	@Override
	public long getSearchScopeGroupId() {
		SearchScope searchScope = _searchScopeSupplier.getSearchScope();

		if (searchScope == SearchScope.EVERYTHING) {
			return 0;
		}

		return _themeDisplay.getScopeGroupId();
	}

	private final SearchScopeSupplier _searchScopeSupplier;
	private final ThemeDisplay _themeDisplay;

}