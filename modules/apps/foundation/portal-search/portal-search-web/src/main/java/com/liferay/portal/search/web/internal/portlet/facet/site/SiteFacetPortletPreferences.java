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

/**
 * @author André de Oliveira
 */
public interface SiteFacetPortletPreferences {

	public static final boolean DEFAULT_FREQUENCIES_VISIBLE = true;

	public static final String PREFERENCE_FREQUENCIES_VISIBLE =
		"frequenciesVisible";

	public static final String PREFERENCE_FREQUENCY_THRESHOLD =
		"frequencyThreshold";

	public static final String PREFERENCE_MAX_TERMS = "maxTerms";

	public int getFrequencyThreshold();

	public int getMaxTerms();

	public boolean isFrequenciesVisible();

}