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

import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.search.web.internal.display.context.SearchScopePreferenceStringSupplier;

import javax.portlet.PortletPreferences;

/**
 * @author André de Oliveira
 */
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