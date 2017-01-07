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

package com.liferay.portal.search.web.internal.portlet.facet.site;

import java.util.Collection;
import java.util.Collections;

/**
 * @author André de Oliveira
 */
public class SiteFacetPortletDisplayContext {

	public static final String ATTRIBUTE = "SiteFacetPortletDisplayContext";

	public String getFieldParamInputName() {
		return _fieldParamInputName;
	}

	public String getFieldParamInputValue() {
		return _fieldParamInputValue;
	}

	public Collection<SiteFacetPortletTermDisplayContext>
		getTermDisplayContexts() {

		return _termDisplayContexts;
	}

	public boolean isMultipleSelection() {
		return _multipleSelection;
	}

	public boolean isNothingSelected() {
		return _nothingSelected;
	}

	public boolean isRenderNothing() {
		return _renderNothing;
	}

	public void setFieldParamInputName(String fieldParamInputName) {
		_fieldParamInputName = fieldParamInputName;
	}

	public void setFieldParamInputValue(String fieldParamInputValue) {
		_fieldParamInputValue = fieldParamInputValue;
	}

	public void setMultipleSelection(boolean multipleSelection) {
		_multipleSelection = multipleSelection;
	}

	public void setNothingSelected(boolean nothingSelected) {
		_nothingSelected = nothingSelected;
	}

	public void setRenderNothing(boolean renderNothing) {
		_renderNothing = renderNothing;
	}

	public void setTerms(
		Collection<SiteFacetPortletTermDisplayContext> termDisplayContexts) {

		_termDisplayContexts = termDisplayContexts;
	}

	private String _fieldParamInputName;
	private String _fieldParamInputValue;
	private boolean _multipleSelection;
	private boolean _nothingSelected;
	private boolean _renderNothing;
	private Collection<SiteFacetPortletTermDisplayContext>
		_termDisplayContexts = Collections.emptyList();

}