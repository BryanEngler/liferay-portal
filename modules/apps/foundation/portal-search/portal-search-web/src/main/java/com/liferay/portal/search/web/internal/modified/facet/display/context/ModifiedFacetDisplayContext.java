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

package com.liferay.portal.search.web.internal.modified.facet.display.context;

import java.io.Serializable;

import java.util.List;

/**
 * @author Lino Alves
 */
public class ModifiedFacetDisplayContext implements Serializable {

	public ModifiedFacetTermDisplayContext getCustomRangeTermDisplayContext() {
		return _customRangeTermDisplayContext;
	}

	public ModifiedFacetTermDisplayContext getDefaultTermDisplayContext() {
		return _defaultTermDisplayContext;
	}

	public ModifiedFacetCalendarDisplayContext
		getModifiedFacetCalendarDisplayContext() {

		return _modifiedFacetCalendarDisplayContext;
	}

	public String getParameterName() {
		return _parameterName;
	}

	public List<ModifiedFacetTermDisplayContext> getTermDisplayContexts() {
		return _modifiedSearchFacetTermDisplayContext;
	}

	public boolean isNothingSelected() {
		return _nothingSelected;
	}

	public void setCalendarDisplayContext(
		ModifiedFacetCalendarDisplayContext
			modifiedFacetCalendarDisplayContext) {

		_modifiedFacetCalendarDisplayContext =
			modifiedFacetCalendarDisplayContext;
	}

	public void setCustomRangeTermDisplayContext(
		ModifiedFacetTermDisplayContext customRangeTermDisplayContext) {

		_customRangeTermDisplayContext = customRangeTermDisplayContext;
	}

	public void setDefaultTermDisplayContext(
		ModifiedFacetTermDisplayContext defaultTermDisplayContext) {

		_defaultTermDisplayContext = defaultTermDisplayContext;
	}

	public void setNothingSelected(boolean nothingSelected) {
		_nothingSelected = nothingSelected;
	}

	public void setParameterName(String paramName) {
		_parameterName = paramName;
	}

	public void setTermDisplayContexts(
		List<ModifiedFacetTermDisplayContext>
			modifiedSearchFacetTermDisplayContext) {

		_modifiedSearchFacetTermDisplayContext =
			modifiedSearchFacetTermDisplayContext;
	}

	private ModifiedFacetTermDisplayContext _customRangeTermDisplayContext;
	private ModifiedFacetTermDisplayContext _defaultTermDisplayContext;
	private ModifiedFacetCalendarDisplayContext
		_modifiedFacetCalendarDisplayContext;
	private List<ModifiedFacetTermDisplayContext>
		_modifiedSearchFacetTermDisplayContext;
	private boolean _nothingSelected;
	private String _parameterName;

}