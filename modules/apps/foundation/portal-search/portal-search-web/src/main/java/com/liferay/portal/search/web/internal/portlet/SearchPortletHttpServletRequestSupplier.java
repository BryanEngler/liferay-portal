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

package com.liferay.portal.search.web.internal.portlet;

import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.search.web.internal.display.context.PortalHttpServletRequestSupplier;

import javax.portlet.RenderRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * @author André de Oliveira
 */
public class SearchPortletHttpServletRequestSupplier
	implements PortalHttpServletRequestSupplier {

	public SearchPortletHttpServletRequestSupplier(
		Portal portal, RenderRequest renderRequest) {

		_portal = portal;
		_renderRequest = renderRequest;
	}

	@Override
	public HttpServletRequest get() {
		return _portal.getHttpServletRequest(_renderRequest);
	}

	private final Portal _portal;
	private final RenderRequest _renderRequest;

}