package com.liferay.portal.search.facet.internal.geolocation;

import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.util.FacetFactory;

import org.osgi.service.component.annotations.Component;

@Component(
	immediate = true
)
public class GeoDistanceFacetFactory implements FacetFactory {

	@Override
	public String getFacetClassName() {
		return GeoDistanceFacet.class.getName();
	}

	@Override
	public Facet newInstance(SearchContext searchContext) {
		return new GeoDistanceFacet(searchContext);
	}

}
