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

import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.search.web.internal.display.context.SearchScope;
import com.liferay.portal.search.web.internal.display.context.SearchScopeGroupIdSupplier;
import com.liferay.portal.search.web.internal.display.context.SearchScopeSupplier;
import com.liferay.portal.search.web.internal.display.context.ThemeDisplaySupplier;

/**
 * @author André de Oliveira
 */
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