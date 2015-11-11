package com.liferay.portal.search.facet.internal.geolocation;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.BooleanClause;
import com.liferay.portal.kernel.search.BooleanClauseFactoryUtil;
import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.BaseFacet;
import com.liferay.portal.kernel.search.facet.config.FacetConfiguration;
import com.liferay.portal.kernel.search.facet.util.RangeParserUtil;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.search.filter.GeoDistanceRangeFilter;
import com.liferay.portal.kernel.search.geolocation.GeoDistance;
import com.liferay.portal.kernel.search.geolocation.GeoLocationPoint;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

public class GeoDistanceFacet extends BaseFacet {

	public GeoDistanceFacet(SearchContext searchContext) {
		super(searchContext);

		setFieldName(Field.GEO_LOCATION);
	}

	@Override
	protected BooleanClause<Filter> doGetFacetFilterBooleanClause() {
		SearchContext searchContext = getSearchContext();

		FacetConfiguration facetConfiguration = getFacetConfiguration();

		JSONObject dataJSONObject = facetConfiguration.getData();

		String start = StringPool.BLANK;
		String end = StringPool.BLANK;

		if (isStatic() && dataJSONObject.has("ranges")) {
			JSONArray rangesJSONArray = dataJSONObject.getJSONArray("ranges");

			JSONObject rangeJSONObject = rangesJSONArray.getJSONObject(0);

			String rangeString = rangeJSONObject.getString("range");

			String[] range = RangeParserUtil.parserRange(rangeString);

			start = range[0];
			end = range[1];
		}

		String rangeParam = GetterUtil.getString(
			searchContext.getAttribute(getFieldId()));

		if (!isStatic() && Validator.isNotNull(rangeParam)) {
			String[] range = RangeParserUtil.parserRange(rangeParam);

			start = range[0];
			end = range[1];
		}

		if (Validator.isNull(start) && Validator.isNull(end)) {
			return null;
		}

		Double from = Double.valueOf(start);
		Double to = Double.valueOf(end);

		if (from > to) {
			throw new IllegalArgumentException(
				"End value must be greater than start value");
		}

		String centerPoint = dataJSONObject.getString("GEODISTANCE-CENTER-POINT");
		String geoLocationFieldName = dataJSONObject.getString("GEOLOCATION-FIELD-NAME");

		GeoDistanceRangeFilter geoDistanceRangeFilter =
			new GeoDistanceRangeFilter(
				geoLocationFieldName, true, true, new GeoDistance(from),
				getPoint(centerPoint), new GeoDistance(to));

		return BooleanClauseFactoryUtil.createFilter(
			searchContext, geoDistanceRangeFilter, BooleanClauseOccur.MUST);
	}

	protected GeoLocationPoint getPoint(String latlon) {
		String[] parts = StringUtil.split(latlon, ',');

		double lat = Double.valueOf(parts[0]);
		double lon = Double.valueOf(parts[1]);

		return new GeoLocationPoint(lat, lon);
	}

}
