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

package com.liferay.portal.search.web.internal.search.bar.classic.portlet;

import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.search.web.internal.request.params.SearchParametersConfiguration;

import javax.portlet.PortletPreferences;

/**
 * @author André de Oliveira
 */
public class SearchBarClassicConfigurationImpl
	implements SearchBarClassicConfiguration, SearchParametersConfiguration {

	public SearchBarClassicConfigurationImpl(
		PortletPreferences portletPreferences) {

		_portletPreferences = portletPreferences;
	}

	@Override
	public String getDestination() {
		return _portletPreferences.getValue(
			SearchBarClassicPortletKeys.PREFERENCE_DESTINATION,
			StringPool.BLANK);
	}

	@Override
	public String getQParameterName() {
		return "q";
	}

	private final PortletPreferences _portletPreferences;

}