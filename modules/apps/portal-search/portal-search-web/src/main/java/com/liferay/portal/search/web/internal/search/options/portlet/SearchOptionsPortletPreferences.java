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

package com.liferay.portal.search.web.internal.search.options.portlet;

import com.liferay.portal.kernel.util.KeyValuePair;

import java.util.List;
import java.util.Optional;

/**
 * @author Wade Cao
 */
public interface SearchOptionsPortletPreferences {

	public static final String PREFERENCE_KEY_ALLOW_EMPTY_SEARCHES =
		"allowEmptySearches";

	public static final String PREFERENCE_KEY_BASIC_FACET_SELECTION =
		"basicFacetSelection";

	public static final String PREFERENCE_KEY_FEDERATED_SEARCH_ENABLED =
		"federatedSearchEnabled";

	public static final String PREFERENCE_KEY_FEDERATED_SEARCH_SOURCES =
		"federatedSearchSources";

	public boolean federatedSearchEnabled();

	public List<KeyValuePair> getAvailableFederatedSearchSources();

	public List<KeyValuePair> getCurrentFederatedSearchSources();

	public String[] getCurrentFederatedSearchSourcesArray();

	public Optional<String[]> getFederatedSearchSourcesArray();

	public String getFederatedSearchSourcesString();

	public boolean isAllowEmptySearches();

	public boolean isBasicFacetSelection();

}