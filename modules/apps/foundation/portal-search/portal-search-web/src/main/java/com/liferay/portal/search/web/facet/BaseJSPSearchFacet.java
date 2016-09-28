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

package com.liferay.portal.search.web.facet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Eudaldo Alonso
 */
public abstract class BaseJSPSearchFacet extends BaseSearchFacet {

	public abstract String getConfigurationJspPath();

	public abstract String getDisplayJspPath();

	@Override
	public void includeConfiguration(
			HttpServletRequest request, HttpServletResponse response)
		throws IOException {

		include(getConfigurationJspPath(), request, response);
	}

	@Override
	public void includeView(
			HttpServletRequest request, HttpServletResponse response)
		throws IOException {

		include(getDisplayJspPath(), request, response);
	}

	public void setServletContext(ServletContext servletContext) {
		_servletContext = servletContext;
	}

	protected void include(
			String path, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException {

		if (Validator.isNull(path)) {
			return;
		}

		try {
			ResourceHelper.include(path, request, response, _servletContext);
		}
		catch (ServletException se) {
			_log.error("Unable to include JSP " + path, se);

			throw new IOException("Unable to include " + path, se);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BaseJSPSearchFacet.class);

	private ServletContext _servletContext;

}