package com.liferay.demo.geolocation;

import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.FacetedSearcher;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.filter.BooleanFilter;
import com.liferay.portal.kernel.search.filter.GeoDistanceFilter;

public class GeolocationDemoFacetedSearcher  extends FacetedSearcher {

	private GeoDistanceFilter distance;

	public GeolocationDemoFacetedSearcher(GeoDistanceFilter distance) {
		this.distance = distance;
	}

	@Override
	protected BooleanQuery createFullQuery(
			BooleanFilter fullQueryBooleanFilter, SearchContext searchContext)
			throws Exception {

		fullQueryBooleanFilter.add(distance, BooleanClauseOccur.MUST);

		return super.createFullQuery(fullQueryBooleanFilter, searchContext);
	}

}
