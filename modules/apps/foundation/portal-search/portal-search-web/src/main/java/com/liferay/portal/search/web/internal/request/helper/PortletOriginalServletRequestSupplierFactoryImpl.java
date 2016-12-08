package com.liferay.portal.search.web.internal.request.helper;

import com.liferay.portal.kernel.util.Portal;

import javax.portlet.RenderRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service=PortletOriginalServletRequestSupplierFactory.class)
public class PortletOriginalServletRequestSupplierFactoryImpl implements PortletOriginalServletRequestSupplierFactory {

	@Override
	public OriginalHttpServletRequestSupplier get(RenderRequest renderRequest) {
		return new PortalOriginalHttpServletRequestSupplier(
			new LiferayPortletHttpServletRequestSupplier(renderRequest),
			portal);
	}

	@Reference
	protected Portal portal;

}