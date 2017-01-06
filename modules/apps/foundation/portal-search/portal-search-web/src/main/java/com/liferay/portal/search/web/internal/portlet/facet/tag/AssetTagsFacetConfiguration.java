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

package com.liferay.portal.search.web.internal.portlet.facet.tag;

/**
 * @author Lino Alves
 */
public interface AssetTagsFacetConfiguration {

	public static final String DEFAULT_DISPLAY_STYLE = "cloud";

	public static final int DEFAULT_FREQUENCY_THRESHOLD = 1;

	public static final int DEFAULT_MAX_TERMS = 10;

	public int getFrequencyThreshold();

	public int getMaxTerms();

	public void setFrequencyThreshold(int frequencyThreshold);

	public void setMaxTerms(int maxTerms);

}