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

package com.liferay.portal.search.web.internal.portlet.facet.type;

import com.liferay.portal.kernel.util.KeyValuePair;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * @author Lino Alves
 */
public interface TypeFacetPortletPreferences {

	public static final boolean DEFAULT_FREQUENCIES_VISIBLE = true;

	public static final String DEFAULT_PARAM_NAME = "type";

	public static final String PREFERENCE_ASSET_TYPES = "assetTypes";

	public static final String PREFERENCE_FREQUENCIES_VISIBLE =
		"frequenciesVisible";

	public static final String PREFERENCE_FREQUENCY_THRESHOLD =
		"frequencyThreshold";

	public static final String PREFERENCE_PARAM_NAME = "paramName";

	public String getAssetTypes();

	public Optional<String[]> getAssetTypesArray();

	public List<KeyValuePair> getAvailableAssetTypes(
		long companyId, Locale locale);

	public List<KeyValuePair> getCurrentAssetTypes(
		long companyId, Locale locale);

	public int getFrequencyThreshold();

	public String getParamName();

	public boolean isFrequenciesVisible();

}