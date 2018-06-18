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

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.config.FacetConfiguration;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.solr.facet.FacetProcessor;

import org.apache.solr.client.solrj.SolrQuery;

import org.osgi.service.component.annotations.Component;

/**
 * @author Michael C. Han
 * @author Tibor Lipusz
 */
@Component(
	immediate = true,
	property = "class.name=com.liferay.portal.kernel.search.facet.RangeFacet"
)
public class RangeFacetProcessor implements FacetProcessor<SolrQuery> {

	@Override
	public void processFacet(JSONObject jsonFacetProperties, Facet facet) {
		FacetConfiguration facetConfiguration = facet.getFacetConfiguration();

		addConfigurationRanges(jsonFacetProperties, facetConfiguration);

		addCustomRange(jsonFacetProperties, facet);
	}

	/**
	 * @deprecated As of 2.0.0, replaced by {@link #processFacet(JSONObject,
	 *             Facet)}
	 */
	@Deprecated
	@Override
	public void processFacet(SolrQuery solrQuery, Facet facet) {
	}

	protected void addConfigurationRanges(
		JSONObject jsonFacetProperties, FacetConfiguration facetConfiguration) {

		JSONObject jsonObject = facetConfiguration.getData();

		JSONArray jsonArray = jsonObject.getJSONArray("ranges");

		if (jsonArray == null) {
			return;
		}

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject rangeJSONObject = jsonArray.getJSONObject(i);

			String label = rangeJSONObject.getString("label");
			String range = rangeJSONObject.getString("range");

			JSONObject facetJSONObject = JSONFactoryUtil.createJSONObject();

			_addParameters(facetJSONObject, facetConfiguration, range);

			String facetName =
				facetConfiguration.getFieldName() + StringPool.UNDERLINE +
					label;

			jsonFacetProperties.put(facetName, facetJSONObject);
		}
	}

	protected void addCustomRange(JSONObject jsonFacetProperties, Facet facet) {
		SearchContext searchContext = facet.getSearchContext();

		String range = GetterUtil.getString(
			searchContext.getAttribute(facet.getFieldId()));

		if (Validator.isNull(range)) {
			return;
		}

		FacetConfiguration facetConfiguration = facet.getFacetConfiguration();

		JSONObject facetJSONObject = JSONFactoryUtil.createJSONObject();

		_addParameters(facetJSONObject, facetConfiguration, range);

		String facetName =
			facetConfiguration.getFieldName() + StringPool.UNDERLINE +
				"custom-range";

		jsonFacetProperties.put(facetName, facetJSONObject);
	}

	private void _addParameters(
		JSONObject facetJSONObject, FacetConfiguration facetConfiguration,
		String range) {

		facetJSONObject.put("excludeTags", facetConfiguration.getFieldName());

		String facetQuery =
			facetConfiguration.getFieldName() + StringPool.COLON + range;

		facetJSONObject.put("q", StringUtil.quote(facetQuery, StringPool.AT));

		facetJSONObject.put("type", "query");
	}

}