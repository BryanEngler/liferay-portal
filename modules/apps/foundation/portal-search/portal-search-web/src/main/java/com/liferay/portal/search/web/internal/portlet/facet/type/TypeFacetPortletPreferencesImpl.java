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

import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.portal.kernel.security.permission.ResourceActionsUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.KeyValuePair;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.web.internal.util.PortletPreferencesHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.portlet.PortletPreferences;

/**
 * @author Lino Alves
 */
public class TypeFacetPortletPreferencesImpl
	implements TypeFacetPortletPreferences {

	public TypeFacetPortletPreferencesImpl(
		Optional<PortletPreferences> portletPreferences) {

		_portletPreferencesHelper = new PortletPreferencesHelper(
			portletPreferences);
	}

	public String[] getAllAssetTypes(long companyId) {
		ArrayList<String> classNames = new ArrayList<>();

		List<AssetRendererFactory<?>> assetRendererFactories =
			AssetRendererFactoryRegistryUtil.getAssetRendererFactories(
				companyId);

		for (AssetRendererFactory<?> assetRendererFactory :
				assetRendererFactories) {

			String className = assetRendererFactory.getClassName();

			classNames.add(className);
		}

		return ArrayUtil.toStringArray(classNames);
	}

	@Override
	public String getAssetTypes() {
		return _portletPreferencesHelper.getString(
			TypeFacetPortletPreferences.PREFERENCE_ASSET_TYPES,
			StringPool.BLANK);
	}

	@Override
	public Optional<String[]> getAssetTypesArray() {
		Optional<String> assetTypes = _portletPreferencesHelper.getString(
			TypeFacetPortletPreferences.PREFERENCE_ASSET_TYPES);

		return assetTypes.map(s -> StringUtil.split(s));
	}

	@Override
	public List<KeyValuePair> getAvailableAssetTypes(
		long companyId, Locale locale) {

		List<KeyValuePair> availableAssetTypes = new ArrayList<>();

		String[] assetTypes =
			getAssetTypesArray().orElse(getAllAssetTypes(companyId));

		for (String className : getAllAssetTypes(companyId)) {
			if (!ArrayUtil.contains(assetTypes, className)) {
				availableAssetTypes.add(
					new KeyValuePair(
						className,
						ResourceActionsUtil.getModelResource(
							locale, className)));
			}
		}

		return availableAssetTypes;
	}

	@Override
	public List<KeyValuePair> getCurrentAssetTypes(
		long companyId, Locale locale) {

		List<KeyValuePair> currentAssetTypes = new ArrayList<>();

		String[] assetTypes =
			getAssetTypesArray().orElse(getAllAssetTypes(companyId));

		for (String assetType : assetTypes) {
			currentAssetTypes.add(
				new KeyValuePair(
					assetType,
					ResourceActionsUtil.getModelResource(locale, assetType)));
		}

		return currentAssetTypes;
	}

	@Override
	public int getFrequencyThreshold() {
		return _portletPreferencesHelper.getInteger(
			TypeFacetPortletPreferences.PREFERENCE_FREQUENCY_THRESHOLD,
			AssetEntriesFacetConfiguration.DEFAULT_FREQUENCY_THRESHOLD);
	}

	@Override
	public String getParamName() {
		return _portletPreferencesHelper.getString(
			TypeFacetPortletPreferences.PREFERENCE_PARAM_NAME,
			TypeFacetPortletPreferences.DEFAULT_PARAM_NAME);
	}

	@Override
	public boolean isFrequenciesVisible() {
		return _portletPreferencesHelper.getBoolean(
			TypeFacetPortletPreferences.PREFERENCE_FREQUENCIES_VISIBLE,
			TypeFacetPortletPreferences.DEFAULT_FREQUENCIES_VISIBLE);
	}

	private final PortletPreferencesHelper _portletPreferencesHelper;

}