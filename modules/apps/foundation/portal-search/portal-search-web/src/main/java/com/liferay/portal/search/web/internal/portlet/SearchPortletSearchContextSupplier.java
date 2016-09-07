package com.liferay.portal.search.web.internal.portlet;

import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchContextFactory;
import com.liferay.portal.search.web.internal.display.context.SearchContextSupplier;
import com.liferay.portal.search.web.internal.display.context.PortalHttpServletRequestSupplier;

public class SearchPortletSearchContextSupplier implements SearchContextSupplier {

	public SearchPortletSearchContextSupplier(
		PortalHttpServletRequestSupplier requestSupplier) {

		_requestSupplier = requestSupplier;
	}

	@Override
	public SearchContext getSearchContext() {
		return SearchContextFactory.getInstance(_requestSupplier.get());
	}

	private final PortalHttpServletRequestSupplier _requestSupplier;

}