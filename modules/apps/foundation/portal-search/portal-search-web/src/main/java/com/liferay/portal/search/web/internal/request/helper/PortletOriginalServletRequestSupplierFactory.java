package com.liferay.portal.search.web.internal.request.helper;

import javax.portlet.RenderRequest;

public interface PortletOriginalServletRequestSupplierFactory {

	public OriginalHttpServletRequestSupplier get(RenderRequest renderRequest);

}