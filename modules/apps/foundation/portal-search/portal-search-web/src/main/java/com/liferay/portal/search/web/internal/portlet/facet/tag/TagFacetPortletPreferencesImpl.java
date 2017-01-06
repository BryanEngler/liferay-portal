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

package com.liferay.portal.search.web.internal.portlet.facet.tag;

import com.liferay.portal.search.web.internal.util.PortletPreferencesHelper;

import java.util.Optional;

import javax.portlet.PortletPreferences;

/**
 * @author Lino Alves
 */
public class TagFacetPortletPreferencesImpl
	implements TagFacetPortletPreferences {

	public TagFacetPortletPreferencesImpl(
		Optional<PortletPreferences> portletPreferences) {

		_portletPreferencesHelper = new PortletPreferencesHelper(
			portletPreferences);
	}

	@Override
	public String getDisplayStyle() {
		return _portletPreferencesHelper.getString(
			TagFacetPortletPreferences.PREFERENCE_DISPLAY_STYLE).orElse(
				AssetTagsFacetConfiguration.DEFAULT_DISPLAY_STYLE);
	}

	@Override
	public int getFrequencyThreshold() {
		return _portletPreferencesHelper.getInteger(
			TagFacetPortletPreferences.PREFERENCE_FREQUENCY_THRESHOLD,
			AssetTagsFacetConfiguration.DEFAULT_FREQUENCY_THRESHOLD);
	}

	@Override
	public int getMaxTerms() {
		return _portletPreferencesHelper.getInteger(
			TagFacetPortletPreferences.PREFERENCE_MAX_TERMS,
			AssetTagsFacetConfiguration.DEFAULT_MAX_TERMS);
	}

	@Override
	public boolean isFrequenciesVisible() {
		return _portletPreferencesHelper.getBoolean(
			TagFacetPortletPreferences.PREFERENCE_FREQUENCIES_VISIBLE,
			TagFacetPortletPreferences.DEFAULT_FREQUENCIES_VISIBLE);
	}

	private final PortletPreferencesHelper _portletPreferencesHelper;

}