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

package com.liferay.portal.search.web.internal.facet;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.facet.config.FacetConfiguration;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.web.facet.BaseJSPSearchFacet;
import com.liferay.portal.search.web.facet.SearchFacet;

import javax.portlet.ActionRequest;

import javax.servlet.ServletContext;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author André de Oliveira
 */
@Component(immediate = true, service = SearchFacet.class)
public class GeoDistanceSearchFacet extends BaseJSPSearchFacet {

	@Override
	public String getConfigurationJspPath() {
		return "/facets/configuration/geodistance.jsp";
	}

	@Override
	public FacetConfiguration getDefaultConfiguration(long companyId) {
		FacetConfiguration facetConfiguration = new FacetConfiguration();

		facetConfiguration.setClassName(getFacetClassName());

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		jsonObject.put("frequencyThreshold", 0);

		 // Boston City Hall

		jsonObject.put("GEODISTANCE-CENTER-POINT", "42.3594,-71.0587");
		jsonObject.put("GEOLOCATION-FIELD-NAME", Field.GEO_LOCATION);

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		for (int i = 0; i < _LABELS.length; i++) {
			JSONObject range = JSONFactoryUtil.createJSONObject();

			range.put("label", _LABELS[i]);
			range.put("range", _RANGES[i]);

			jsonArray.put(range);
		}

		jsonObject.put("ranges", jsonArray);

		facetConfiguration.setDataJSONObject(jsonObject);
		facetConfiguration.setFieldName(getFieldName());
		facetConfiguration.setLabel(getLabel());
		facetConfiguration.setOrder(getOrder());
		facetConfiguration.setStatic(false);
		facetConfiguration.setWeight(1.0);

		return facetConfiguration;
	}

	@Override
	public String getDisplayJspPath() {
		return "/facets/view/geodistance-lexicon.jsp";
	}

	@Override
	public String getFacetClassName() {
		return "com.liferay.portal.search.facet.internal.geolocation." +
			"GeoDistanceFacet";
	}

	@Override
	public String getFieldName() {
		return Field.GEO_LOCATION;
	}

	@Override
	public JSONObject getJSONData(ActionRequest actionRequest) {
		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		jsonObject.put(
			"GEODISTANCE-CENTER-POINT",
			getString("centerPoint", actionRequest));
		jsonObject.put(
			"GEOLOCATION-FIELD-NAME",
			getString("geoLocationFieldName", actionRequest));

		int frequencyThreshold = ParamUtil.getInteger(
			actionRequest, getClassName() + "frequencyThreshold", 1);

		jsonObject.put("frequencyThreshold", frequencyThreshold);

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		String[] rangesIndexes = StringUtil.split(
			ParamUtil.getString(
				actionRequest, getClassName() + "rangesIndexes"));

		for (String rangesIndex : rangesIndexes) {
			JSONObject rangeJSONObject = JSONFactoryUtil.createJSONObject();

			String label = ParamUtil.getString(
				actionRequest, getClassName() + "label_" + rangesIndex);
			String range = ParamUtil.getString(
				actionRequest, getClassName() + "range_" + rangesIndex);

			rangeJSONObject.put("label", label);
			rangeJSONObject.put("range", range);

			jsonArray.put(rangeJSONObject);
		}

		jsonObject.put("ranges", jsonArray);

		return jsonObject;
	}

	@Override
	public String getLabel() {
		return "any-distance";
	}

	@Override
	public String getTitle() {
		return "geodistance";
	}

	@Override
	@Reference(
		target = "(osgi.web.symbolicname=com.liferay.portal.search.web)",
		unbind = "-"
	)
	public void setServletContext(ServletContext servletContext) {
		super.setServletContext(servletContext);
	}

	protected String getString(String string, ActionRequest actionRequest) {
		return ParamUtil.getString(actionRequest, getClassName() + string);
	}

	private static final String[] _LABELS = new String[] {
		"0m-to-10m", "10m-to-500m", "500m-to-1km", "1km-to-2km", "2km-to-5km",
		"5km-to-10km", "10km-to-20km"
	};

	private static final String[] _RANGES = new String[] {
		"[0 TO 10]", "[10 TO 500]", "[500 TO 1000]", "[1000 TO 2000]",
		"[2000 TO 5000]", "[5000 TO 10000]", "[10000 TO 20000]"
	};

}