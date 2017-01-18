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

package com.liferay.portal.search.web.internal.modified.facet.builder;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.ModifiedFacetFactory;
import com.liferay.portal.kernel.search.facet.config.FacetConfiguration;

import java.util.Collection;

/**
 * @author Lino Alves
 */
public class ModifiedFacetBuilder {

	public ModifiedFacetBuilder(ModifiedFacetFactory modifiedFacetFactory) {
		_modifiedFacetFactory = modifiedFacetFactory;
	}

	public Facet build() {
		Facet facet = _modifiedFacetFactory.newInstance(_searchContext);

		facet.setFacetConfiguration(buildFacetConfiguration(facet));

		_applySelectedRanges(facet);

		return facet;
	}

	public void setSearchContext(SearchContext searchContext) {
		_searchContext = searchContext;
	}

	public void setSelectedRanges(String... selectedRanges) {
		_selectedRanges = selectedRanges;
	}

	protected FacetConfiguration buildFacetConfiguration(Facet facet) {
		FacetConfiguration facetConfiguration = new FacetConfiguration();

		facetConfiguration.setFieldName(facet.getFieldName());
		facetConfiguration.setLabel("any-time");
		facetConfiguration.setOrder("OrderHitsDesc");
		facetConfiguration.setStatic(false);
		facetConfiguration.setWeight(1.0);

		ModifiedFacetConfiguration modifiedFacetConfiguration =
			new ModifiedFacetConfigurationImpl(facetConfiguration);

		modifiedFacetConfiguration.setRangesJSONArray(getRangesJSONArray());

		return facetConfiguration;
	}

	protected JSONArray getRangesJSONArray() {
		JSONArray rangesJSONArray = JSONFactoryUtil.createJSONArray();

		Collection<String> labels = _dateRangeFactory.getLabels();

		for (String label : labels) {
			JSONObject range = JSONFactoryUtil.createJSONObject();

			range.put("label", label);
			range.put("range", _dateRangeFactory.getRangeString(label));

			rangesJSONArray.put(range);
		}

		return rangesJSONArray;
	}

	private void _applySelectedRanges(Facet facet) {
		if (_selectedRanges == null) {
			return;
		}

		String label = _selectedRanges[_selectedRanges.length - 1];

		String rangeString = _dateRangeFactory.getRangeString(label);

		if (rangeString != null) {
			_searchContext.setAttribute(facet.getFieldName(), rangeString);
		}
	}

	private final DateRangeFactory _dateRangeFactory = new DateRangeFactory();
	private final ModifiedFacetFactory _modifiedFacetFactory;
	private SearchContext _searchContext;
	private String[] _selectedRanges;

}