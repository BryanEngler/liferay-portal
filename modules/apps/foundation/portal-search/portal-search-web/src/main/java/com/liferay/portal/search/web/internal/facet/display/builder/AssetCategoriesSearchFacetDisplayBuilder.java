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

package com.liferay.portal.search.web.internal.facet.display.builder;

import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.service.AssetCategoryLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.collector.FacetCollector;
import com.liferay.portal.kernel.search.facet.collector.TermCollector;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.web.internal.facet.display.context.AssetCategoriesSearchFacetDisplayContext;
import com.liferay.portal.search.web.internal.facet.display.context.AssetCategoriesSearchFacetTermDisplayContext;
import com.liferay.portlet.asset.service.permission.AssetCategoryPermission;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author Lino Alves
 */
public class AssetCategoriesSearchFacetDisplayBuilder implements Serializable {

	public AssetCategoriesSearchFacetDisplayContext build()
		throws PortalException {

		AssetCategoriesSearchFacetDisplayContext
			assetCategoriesSearchFacetDisplayContext =
				new AssetCategoriesSearchFacetDisplayContext();

		assetCategoriesSearchFacetDisplayContext.setDisplayStyle(_displayStyle);
		assetCategoriesSearchFacetDisplayContext.setParamName(_paramName);
		assetCategoriesSearchFacetDisplayContext.setParamValue(_paramValue);

		assetCategoriesSearchFacetDisplayContext.setTermDisplayContexts(
			buildTermDisplayContexts());

		boolean renderNothing = ListUtil.isEmpty(
			assetCategoriesSearchFacetDisplayContext.getTermDisplayContexts());

		assetCategoriesSearchFacetDisplayContext.setRenderNothing(
			renderNothing);

		boolean nothingSelected = Validator.isNull(_paramValue);

		assetCategoriesSearchFacetDisplayContext.setNothingSelected(
			nothingSelected);

		return assetCategoriesSearchFacetDisplayContext;
	}

	public AssetCategoriesSearchFacetTermDisplayContext buildTermDisplay(
		int popularity, boolean selected, AssetCategory assetCategory,
		int frequency) {

		AssetCategoriesSearchFacetTermDisplayContext
			assetCategoriesSearchFacetTermDisplayContext =
				new AssetCategoriesSearchFacetTermDisplayContext();

		assetCategoriesSearchFacetTermDisplayContext.setFrequency(frequency);
		assetCategoriesSearchFacetTermDisplayContext.setFrequencyVisible(
			_frequenciesVisible);
		assetCategoriesSearchFacetTermDisplayContext.setSelected(selected);
		assetCategoriesSearchFacetTermDisplayContext.setTitle(
			assetCategory.getTitle(_locale));
		assetCategoriesSearchFacetTermDisplayContext.setAssetCategoryId(
			assetCategory.getCategoryId());
		assetCategoriesSearchFacetTermDisplayContext.setPopularity(popularity);

		return assetCategoriesSearchFacetTermDisplayContext;
	}

	public List<AssetCategoriesSearchFacetTermDisplayContext>
			buildTermDisplayContexts()
		throws PortalException {

		List<AssetCategoriesSearchFacetTermDisplayContext>
			assetCategoriesSearchFacetTermDisplayContexts = new ArrayList<>();

		FacetCollector facetCollector = _facet.getFacetCollector();

		List<TermCollector> termCollectors =
			Collections.<TermCollector>emptyList();

		if (facetCollector != null) {
			termCollectors = facetCollector.getTermCollectors();
		}

		int maxCount = 1;
		int minCount = 1;

		if (_frequenciesVisible && _displayStyle.equals("cloud")) {

			// The cloud style may not list tags in the order of frequency,
			// so keep looking through the results until we reach the maximum
			// number of terms or we run out of terms.

			for (int i = 0, j = 0; i < termCollectors.size(); i++, j++) {
				if (j >= _maxTerms) {
					break;
				}

				TermCollector termCollector = termCollectors.get(i);

				int frequency = termCollector.getFrequency();

				if (_frequencyThreshold > frequency) {
					j--;

					continue;
				}

				maxCount = Math.max(maxCount, frequency);
				minCount = Math.min(minCount, frequency);
			}
		}

		double multiplier = 1;

		if (maxCount != minCount) {
			multiplier = (double)5 / (maxCount - minCount);
		}

		for (int i = 0, j = 0; i < termCollectors.size(); i++, j++) {
			if (j >= _maxTerms) {
				break;
			}

			TermCollector termCollector = termCollectors.get(i);

			long assetCategoryId = GetterUtil.getLong(termCollector.getTerm());

			if (assetCategoryId == 0) {
				continue;
			}

			AssetCategory curAssetCategory =
				AssetCategoryLocalServiceUtil.fetchAssetCategory(
					assetCategoryId);

			if ((curAssetCategory != null) &&
				AssetCategoryPermission.contains(
					_permissionChecker, curAssetCategory, ActionKeys.VIEW)) {

				int popularity = getPopularity(
					termCollector.getFrequency(), maxCount, minCount,
					multiplier);

				if (_frequencyThreshold > termCollector.getFrequency()) {
					j--;

					continue;
				}

				boolean selected = _paramValue.equals(termCollector.getTerm());

				AssetCategoriesSearchFacetTermDisplayContext
					assetCategoriesSearchFacetTermDisplayContext =
						buildTermDisplay(
							popularity, selected, curAssetCategory,
							termCollector.getFrequency());

				assetCategoriesSearchFacetTermDisplayContexts.add(
					assetCategoriesSearchFacetTermDisplayContext);
			}
		}

		return assetCategoriesSearchFacetTermDisplayContexts;
	}

	public String getDisplayStyle() {
		return _displayStyle;
	}

	public Facet getFacet() {
		return _facet;
	}

	public int getFrequencyThreshold() {
		return _frequencyThreshold;
	}

	public Locale getLocale() {
		return _locale;
	}

	public int getMaxTerms() {
		return _maxTerms;
	}

	public String getParamValue() {
		return _paramValue;
	}

	public PermissionChecker getPermissionChecker() {
		return _permissionChecker;
	}

	public int getPopularity(
		int frequency, int maxCount, int minCount, double multiplier) {

		int popularity = maxCount - (maxCount - (frequency - minCount));

		return (int)(1 + (popularity * multiplier));
	}

	public boolean isShowAssetCount() {
		return _frequenciesVisible;
	}

	public void setDisplayStyle(String displayStyle) {
		_displayStyle = displayStyle;
	}

	public void setFacet(Facet facet) {
		_facet = facet;
	}

	public void setFrequenciesVisible(boolean frequenciesVisible) {
		_frequenciesVisible = frequenciesVisible;
	}

	public void setFrequencyThreshold(int frequencyThreshold) {
		_frequencyThreshold = frequencyThreshold;
	}

	public void setLocale(Locale locale) {
		_locale = locale;
	}

	public void setMaxTerms(int maxTerms) {
		_maxTerms = maxTerms;
	}

	public void setParamName(String paramName) {
		_paramName = paramName;
	}

	public void setParamValue(String paramValue) {
		_paramValue = paramValue;
	}

	public void setPermissionChecker(PermissionChecker permissionChecker) {
		_permissionChecker = permissionChecker;
	}

	private String _displayStyle;
	private Facet _facet;
	private boolean _frequenciesVisible;
	private int _frequencyThreshold;
	private Locale _locale;
	private int _maxTerms;
	private String _paramName;
	private String _paramValue;
	private PermissionChecker _permissionChecker;

}