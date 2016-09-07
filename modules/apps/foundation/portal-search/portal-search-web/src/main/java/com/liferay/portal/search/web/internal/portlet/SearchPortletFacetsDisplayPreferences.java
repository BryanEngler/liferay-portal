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

import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.liferay.portal.search.web.internal.display.context.FacetsDisplayPreferences;

import javax.portlet.PortletPreferences;

/**
 * @author André de Oliveira
 */
public class SearchPortletFacetsDisplayPreferences
	implements FacetsDisplayPreferences {

	public SearchPortletFacetsDisplayPreferences(
		PortletPreferences portletPreferences) {

			this._portletPreferences = portletPreferences;
	}

	@Override
	public boolean isDisplay(SearchFacet searchFacet) {
		return GetterUtil.getBoolean(
			_portletPreferences.getValue(searchFacet.getClassName(), null),
			true);
	}

	private final PortletPreferences _portletPreferences;

}