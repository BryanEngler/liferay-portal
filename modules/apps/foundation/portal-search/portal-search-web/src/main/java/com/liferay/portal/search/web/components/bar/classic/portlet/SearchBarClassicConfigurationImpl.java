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

package com.liferay.portal.search.web.components.bar.classic.portlet;

import com.liferay.portal.search.web.internal.request.params.SearchParametersConfiguration;
import com.liferay.portal.search.web.internal.util.PortletPreferencesHelper;

import java.util.Optional;

import javax.portlet.PortletPreferences;

/**
 * @author André de Oliveira
 */
public class SearchBarClassicConfigurationImpl
	implements SearchBarClassicConfiguration, SearchParametersConfiguration {

	public SearchBarClassicConfigurationImpl(
		PortletPreferences portletPreferences) {

		_portletPreferencesHelper = new PortletPreferencesHelper(
			portletPreferences);
	}

	@Override
	public Optional<String> getDestination() {
		return _portletPreferencesHelper.getString(
			SearchBarClassicPortletKeys.PREFERENCE_DESTINATION);
	}

	@Override
	public String getQParameterName() {
		return "q";
	}

	private final PortletPreferencesHelper _portletPreferencesHelper;

}