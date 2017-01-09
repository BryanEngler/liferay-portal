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

package com.liferay.portal.search.web.internal.facet.display.context;

import java.io.Serializable;

import java.util.Locale;

/**
 * @author Lino Alves
 */
public class AssetCategoriesSearchFacetTermDisplayContext
	implements Serializable {

	public long getAssetCategoryId() {
		return _assetCategoryId;
	}

	public int getFrequency() {
		return _frequency;
	}

	public Locale getLocale() {
		return _locale;
	}

	public int getPopularity() {
		return _popularity;
	}

	public String getTitle() {
		return _title;
	}

	public boolean isFrequencyVisible() {
		return _frequencyVisible;
	}

	public boolean isSelected() {
		return _selected;
	}

	public void setAssetCategoryId(long assetCategoryId) {
		_assetCategoryId = assetCategoryId;
	}

	public void setFrequency(int frequency) {
		_frequency = frequency;
	}

	public void setFrequencyVisible(boolean frequencyVisible) {
		_frequencyVisible = frequencyVisible;
	}

	public void setLocale(Locale locale) {
		_locale = locale;
	}

	public void setPopularity(int popularity) {
		_popularity = popularity;
	}

	public void setSelected(boolean selected) {
		_selected = selected;
	}

	public void setTitle(String title) {
		_title = title;
	}

	private long _assetCategoryId;
	private int _frequency;
	private boolean _frequencyVisible;
	private Locale _locale;
	private int _popularity;
	private boolean _selected;
	private String _title;

}