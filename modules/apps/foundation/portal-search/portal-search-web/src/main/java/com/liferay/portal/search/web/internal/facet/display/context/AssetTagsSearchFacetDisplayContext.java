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

import java.util.List;

/**
 * @author André de Oliveira
 */
public class AssetTagsSearchFacetDisplayContext {

	public static final String ATTRIBUTE = "assetTagsSearchFacetDisplayContext";

	public String getFacetLabel() {
		return _facetLabel;
	}

	public String getParamName() {
		return _paramName;
	}

	public String getParamValue() {
		return _paramValue;
	}

	public List<AssetTagsSearchFacetTermDisplayContext>
		getTermDisplayContexts() {

		return _assetTagsSearchFacetTermDisplayContexts;
	}

	public boolean isCloudWithCount() {
		return _cloudWithCount;
	}

	public boolean isNothingSelected() {
		return _nothingSelected;
	}

	public boolean isRenderNothing() {
		return _renderNothing;
	}

	public void setCloudWithCount(boolean cloudWithCount) {
		_cloudWithCount = cloudWithCount;
	}

	public void setFacetLabel(String facetLabel) {
		_facetLabel = facetLabel;
	}

	public void setNothingSelected(boolean nothingSelected) {
		_nothingSelected = nothingSelected;
	}

	public void setParamName(String paramName) {
		_paramName = paramName;
	}

	public void setParamValue(String paramValue) {
		_paramValue = paramValue;
	}

	public void setRenderNothing(boolean renderNothing) {
		_renderNothing = renderNothing;
	}

	public void setTermDisplayContexts(
		List<AssetTagsSearchFacetTermDisplayContext>
			assetTagsSearchFacetTermDisplayContexts) {

		_assetTagsSearchFacetTermDisplayContexts =
			assetTagsSearchFacetTermDisplayContexts;
	}

	private List<AssetTagsSearchFacetTermDisplayContext>
		_assetTagsSearchFacetTermDisplayContexts;
	private boolean _cloudWithCount;
	private String _facetLabel;
	private boolean _nothingSelected;
	private String _paramName;
	private String _paramValue;
	private boolean _renderNothing;

}