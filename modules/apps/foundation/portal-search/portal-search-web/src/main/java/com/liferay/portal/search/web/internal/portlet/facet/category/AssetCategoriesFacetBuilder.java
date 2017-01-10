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

package com.liferay.portal.search.web.internal.portlet.facet.category;

import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.MultiValueFacet;
import com.liferay.portal.kernel.search.facet.config.FacetConfiguration;

/**
 * @author Lino Alves
 */
public class AssetCategoriesFacetBuilder {

	public MultiValueFacet build() {
		MultiValueFacet facet = new MultiValueFacet(_searchContext);

		facet.setFacetConfiguration(buildFacetConfiguration());

		return facet;
	}

	public void setFrequencyThreshold(int frequencyThreshold) {
		_frequencyThreshold = frequencyThreshold;
	}

	public void setMaxTerms(int maxTerms) {
		_maxTerms = maxTerms;
	}

	public void setSearchContext(SearchContext searchContext) {
		_searchContext = searchContext;
	}

	protected FacetConfiguration buildFacetConfiguration() {
		FacetConfiguration facetConfiguration = new FacetConfiguration();

		facetConfiguration.setClassName(MultiValueFacet.class.getName());
		facetConfiguration.setFieldName(
			AssetCategoriesFacetConstants.FIELD_NAME);
		facetConfiguration.setLabel("any-category");
		facetConfiguration.setOrder("OrderHitsDesc");
		facetConfiguration.setStatic(false);
		facetConfiguration.setWeight(1.6);

		AssetCategoriesFacetConfiguration assetCategoriesFacetConfiguration =
			new AssetCategoriesFacetConfigurationImpl(facetConfiguration);

		assetCategoriesFacetConfiguration.setFrequencyThreshold(
			_frequencyThreshold);
		assetCategoriesFacetConfiguration.setMaxTerms(_maxTerms);

		return facetConfiguration;
	}

	private int _frequencyThreshold =
		AssetCategoriesFacetConfiguration.DEFAULT_FREQUENCY_THRESHOLD;
	private int _maxTerms = AssetCategoriesFacetConfiguration.DEFAULT_MAX_TERMS;
	private SearchContext _searchContext;

}