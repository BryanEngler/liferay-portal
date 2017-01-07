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

package com.liferay.portal.search.web.internal.portlet.facet.site;

import com.liferay.portal.search.web.internal.util.PortletPreferencesHelper;

import java.util.Optional;

import javax.portlet.PortletPreferences;

/**
 * @author André de Oliveira
 */
public class SiteFacetPortletPreferencesImpl
	implements SiteFacetPortletPreferences {

	public SiteFacetPortletPreferencesImpl(
		Optional<PortletPreferences> portletPreferences) {

		_portletPreferencesHelper = new PortletPreferencesHelper(
			portletPreferences);
	}

	@Override
	public int getFrequencyThreshold() {
		return _portletPreferencesHelper.getInteger(
			SiteFacetPortletPreferences.PREFERENCE_FREQUENCY_THRESHOLD,
			ScopeFacetConfiguration.DEFAULT_FREQUENCY_THRESHOLD);
	}

	@Override
	public int getMaxTerms() {
		return _portletPreferencesHelper.getInteger(
			SiteFacetPortletPreferences.PREFERENCE_MAX_TERMS,
			ScopeFacetConfiguration.DEFAULT_MAX_TERMS);
	}

	@Override
	public boolean isFrequenciesVisible() {
		return _portletPreferencesHelper.getBoolean(
			SiteFacetPortletPreferences.PREFERENCE_FREQUENCIES_VISIBLE,
			SiteFacetPortletPreferences.DEFAULT_FREQUENCIES_VISIBLE);
	}

	@Override
	public boolean isMultipleSelection() {
		return _portletPreferencesHelper.getBoolean(
			SiteFacetPortletPreferences.PREFERENCE_MULTIPLE_SELECTION,
			SiteFacetPortletPreferences.DEFAULT_MULTIPLE_SELECTION);
	}

	private final PortletPreferencesHelper _portletPreferencesHelper;

}