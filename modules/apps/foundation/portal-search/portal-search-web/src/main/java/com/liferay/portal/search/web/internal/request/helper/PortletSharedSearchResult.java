package com.liferay.portal.search.web.internal.request.helper;

import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.search.web.internal.results.data.SearchResultsData;

public interface PortletSharedSearchResult {

	Facet getFacet(String fieldName);

	SearchResultsData getSearchResultsData();

}
