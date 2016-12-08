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

package com.liferay.portal.search.web.internal.request.helper;

import com.liferay.portal.kernel.util.Portal;

import javax.portlet.RenderRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author André de Oliveira
 */
@Component(service = PortletOriginalServletRequestSupplierFactory.class)
public class PortletOriginalServletRequestSupplierFactoryImpl
	implements PortletOriginalServletRequestSupplierFactory {

	@Override
	public OriginalHttpServletRequestSupplier get(RenderRequest renderRequest) {
		return new PortalOriginalHttpServletRequestSupplier(
			new LiferayPortletHttpServletRequestSupplier(renderRequest),
			portal);
	}

	@Reference
	protected Portal portal;

}