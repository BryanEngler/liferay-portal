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

package com.liferay.portal.search.solr.internal.facet;

import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.config.FacetConfiguration;
import com.liferay.portal.search.solr.facet.FacetProcessor;

import org.apache.solr.client.solrj.SolrQuery;

import org.osgi.service.component.annotations.Component;

/**
 * @author Michael C. Han
 */
@Component(immediate = true, property = "class.name=DEFAULT")
public class DefaultFacetProcessor implements FacetProcessor<SolrQuery> {

	@Override
	public void processFacet(JSONObject jsonFacetProperties, Facet facet) {
		JSONObject facetJSONObject = JSONFactoryUtil.createJSONObject();

		FacetConfiguration facetConfiguration = facet.getFacetConfiguration();

		String fieldName = facetConfiguration.getFieldName();

		facetJSONObject.put("excludeTags", fieldName);
		facetJSONObject.put("field", fieldName);

		facetJSONObject.put("type", "terms");

		JSONObject dataJSONObject = facetConfiguration.getData();

		applyFrequencyThreshold(facetJSONObject, dataJSONObject);
		applyMaxTerms(facetJSONObject, dataJSONObject);

		applySort(facetJSONObject, facetConfiguration);

		jsonFacetProperties.put(fieldName, facetJSONObject);
	}

	/**
	 * @deprecated As of 2.0.0, replaced by {@link #processFacet(JSONObject,
	 *             Facet)}
	 */
	@Deprecated
	@Override
	public void processFacet(SolrQuery solrQuery, Facet facet) {
	}

	protected void applyFrequencyThreshold(
		JSONObject facetJSONObject, JSONObject dataJSONObject) {

		int minCount = dataJSONObject.getInt("frequencyThreshold");

		if (minCount > 0) {
			facetJSONObject.put("mincount", minCount);
		}
	}

	protected void applyMaxTerms(
		JSONObject facetJSONObject, JSONObject dataJSONObject) {

		int limit = dataJSONObject.getInt("maxTerms");

		if (limit > 0) {
			facetJSONObject.put("limit", limit);
		}
	}

	protected void applySort(
		JSONObject facetJSONObject, FacetConfiguration facetConfiguration) {

		String sortParam = "count";
		String sortValue = "desc";

		String order = facetConfiguration.getOrder();

		if (order.equals("OrderValueAsc")) {
			sortParam = "index";
			sortValue = "asc";
		}

		JSONObject sortJSONObject = JSONFactoryUtil.createJSONObject();

		sortJSONObject.put(sortParam, sortValue);

		facetJSONObject.put("sort", sortJSONObject);
	}

}