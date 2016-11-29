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

/**
 * @author André de Oliveira
 */
public class SiteFacetPortletTermDisplayContext {

	public int getFrequency() {
		return _frequency;
	}

	public String getTerm() {
		return _term;
	}

	public String getValue() {
		return _value;
	}

	public boolean isFrequencyVisible() {
		return _frequencyVisible;
	}

	public boolean isSelected() {
		return _selected;
	}

	public void setFrequency(int frequency) {
		_frequency = frequency;
	}

	public void setFrequencyVisible(boolean frequencyVisible) {
		_frequencyVisible = frequencyVisible;
	}

	public void setSelected(boolean selected) {
		_selected = selected;
	}

	public void setTerm(String term) {
		_term = term;
	}

	public void setValue(String value) {
		_value = value;
	}

	private int _frequency;
	private boolean _frequencyVisible;
	private boolean _selected;
	private String _term;
	private String _value;

}