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

import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.collector.FacetCollector;
import com.liferay.portal.kernel.search.facet.collector.TermCollector;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.comparator.ModelResourceComparator;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.SortedArrayList;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.web.internal.facet.display.context.AssetEntriesSearchFacetDisplayContext;
import com.liferay.portal.search.web.internal.facet.display.context.AssetEntriesSearchFacetTermDisplayContext;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author Lino Alves
 */
public class AssetEntriesSearchFacetDisplayBuilder implements Serializable {

	public AssetEntriesSearchFacetDisplayContext build() {
		AssetEntriesSearchFacetDisplayContext
			assetEntriesSearchFacetDisplayContext =
				new AssetEntriesSearchFacetDisplayContext();

		assetEntriesSearchFacetDisplayContext.setParamName(_paramName);
		assetEntriesSearchFacetDisplayContext.setParamValue(_paramValue);

		assetEntriesSearchFacetDisplayContext.setTermDisplayContexts(
			buildTermDisplayContexts());

		boolean renderNothing = ListUtil.isEmpty(
			assetEntriesSearchFacetDisplayContext.getTermDisplayContexts());

		assetEntriesSearchFacetDisplayContext.setRenderNothing(renderNothing);

		assetEntriesSearchFacetDisplayContext.setNothingSelected(
			isNothingSelected());

		return assetEntriesSearchFacetDisplayContext;
	}

	public AssetEntriesSearchFacetTermDisplayContext buildTermDisplay(
		String typeName, boolean selected, String assetType, int frequency) {

		AssetEntriesSearchFacetTermDisplayContext
			assetEntriesSearchFacetFieldDisplayContext =
				new AssetEntriesSearchFacetTermDisplayContext();

		assetEntriesSearchFacetFieldDisplayContext.setFrequency(frequency);
		assetEntriesSearchFacetFieldDisplayContext.setFrequencyVisible(
			_frequenciesVisible);
		assetEntriesSearchFacetFieldDisplayContext.setSelected(selected);
		assetEntriesSearchFacetFieldDisplayContext.setTypeName(typeName);
		assetEntriesSearchFacetFieldDisplayContext.setAssetType(assetType);

		return assetEntriesSearchFacetFieldDisplayContext;
	}

	public List<AssetEntriesSearchFacetTermDisplayContext>
		buildTermDisplayContexts() {

		FacetCollector facetCollector = _facet.getFacetCollector();

		if (facetCollector == null) {
			return Collections.emptyList();
		}

		List<AssetEntriesSearchFacetTermDisplayContext>
			assetEntriesSearchFacetFieldDisplayContexts = new ArrayList<>();

		List<String> assetTypes = new SortedArrayList<>(
			new ModelResourceComparator(_locale));

		for (String className : _classNames) {
			if (assetTypes.contains(className) ||
				!ArrayUtil.contains(_classNames, className)) {

				continue;
			}

			assetTypes.add(className);
		}

		for (String assetType : assetTypes) {
			TermCollector termCollector = facetCollector.getTermCollector(
				assetType);

			int frequency = 0;

			if (termCollector != null) {
				frequency = termCollector.getFrequency();
			}

			if (_frequencyThreshold > frequency) {
				continue;
			}

			AssetRendererFactory<?> assetRendererFactory =
				AssetRendererFactoryRegistryUtil.
					getAssetRendererFactoryByClassName(assetType);

			boolean selected = _paramValue.equals(termCollector.getTerm());

			AssetEntriesSearchFacetTermDisplayContext
				assetEntriesSearchFacetFieldDisplayContext = buildTermDisplay(
					assetRendererFactory.getTypeName(_locale), selected,
					assetType, termCollector.getFrequency());

			assetEntriesSearchFacetFieldDisplayContexts.add(
				assetEntriesSearchFacetFieldDisplayContext);
		}

		return assetEntriesSearchFacetFieldDisplayContexts;
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

	public String getParamName() {
		return _paramName;
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

	public boolean isFrequenciesVisible() {
		return _frequenciesVisible;
	}

	public boolean isNothingSelected() {
		return Validator.isNull(_paramValue);
	}

	public void setClassNames(String[] classNames) {
		_classNames = classNames;
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

	private String[] _classNames;
	private Facet _facet;
	private boolean _frequenciesVisible;
	private int _frequencyThreshold;
	private Locale _locale;
	private int _maxTerms;
	private String _paramName;
	private String _paramValue;
	private PermissionChecker _permissionChecker;

}