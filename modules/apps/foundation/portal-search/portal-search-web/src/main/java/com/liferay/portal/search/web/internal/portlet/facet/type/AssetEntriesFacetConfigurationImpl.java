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

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.facet.config.FacetConfiguration;

/**
 * @author Lino Alves
 */
public class AssetEntriesFacetConfigurationImpl
	implements AssetEntriesFacetConfiguration {

	public AssetEntriesFacetConfigurationImpl(
		FacetConfiguration facetConfiguration) {

		_jsonObject = facetConfiguration.getData();
	}

	@Override
	public String[] getClassNames() {
		String[] classNames = new String[0];

		if (_jsonObject.has("assetTypes")) {
			JSONArray assetTypes = _jsonObject.getJSONArray("assetTypes");

			classNames = new String[assetTypes.length()];

			for (int i = 0; i < assetTypes.length(); i++) {
				classNames[i] = assetTypes.getString(i);
			}
		}

		return classNames;
	}

	@Override
	public int getFrequencyThreshold() {
		return _jsonObject.getInt("frequencyThreshold");
	}

	@Override
	public void setClassNames(String[] classNames) {
		JSONArray assetTypes = JSONFactoryUtil.createJSONArray();

		for (String assetType : classNames) {
			assetTypes.put(assetType);
		}

		_jsonObject.put("assetTypes", assetTypes);
	}

	@Override
	public void setFrequencyThreshold(int frequencyThreshold) {
		_jsonObject.put("frequencyThreshold", frequencyThreshold);
	}

	private final JSONObject _jsonObject;

}