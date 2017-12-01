package com.liferay.portal.search.test.util.facet;

import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.ModifiedFacet;

import java.util.Arrays;

/**
 * @author Bryan Engler
 */
public abstract class BaseModifiedFacetTestCase extends BaseFacetTestCase {

	@Override
	protected Facet createFacet(SearchContext searchContext) {
		Facet facet = new ModifiedFacet(searchContext);

		String customRange = "[20170101000000 TO 20170105000000]";

		setCustomRange(facet, searchContext, customRange);

		return facet;
	}

	@Override
	protected String getField() {
		return Field.MODIFIED_DATE;
	}

	protected void testRange() throws Exception {
		addDocument("20170102000000");
		addDocument("20170104000000");
		addDocument("20170106000000");

		assertFacet(Arrays.asList("[20170101000000 TO 20170105000000]=2"));
	}

	protected static void setCustomRange(
		Facet facet, SearchContext searchContext, String customRange) {

		searchContext.setAttribute(facet.getFieldId(), customRange);
	}

}
