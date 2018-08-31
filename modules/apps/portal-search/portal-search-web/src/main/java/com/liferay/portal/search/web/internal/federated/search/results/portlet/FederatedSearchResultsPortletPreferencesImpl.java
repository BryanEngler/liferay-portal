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

package com.liferay.portal.search.web.internal.federated.search.results.portlet;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.search.web.internal.util.PortletPreferencesHelper;
import com.liferay.portal.search.spi.federated.searcher.FederatedSearcher;

import javax.portlet.PortletPreferences;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Lino Alves
 */
public class FederatedSearchResultsPortletPreferencesImpl
	implements FederatedSearchResultsPortletPreferences {

	public FederatedSearchResultsPortletPreferencesImpl(
		Optional<PortletPreferences> portletPreferencesOptional, Collection<FederatedSearcher> federatedSearchers) {

		_portletPreferencesHelper = new PortletPreferencesHelper(
			portletPreferencesOptional);

		_federatedSearchers = federatedSearchers;
	}

	@Override
	public String getDisplayStyle() {
		return _portletPreferencesHelper.getString(
			PREFERENCE_KEY_FEDERATED_SEARCH_DISPLAY_STYLE, "standard");
	}

	public String[] getFederatedSearchSourceNames() {
		List<String> sources = new ArrayList<>();

		for (FederatedSearcher federatedSearcher : _federatedSearchers) {
			sources.add(federatedSearcher.getSourceDisplayName());
		}

		return ArrayUtil.toStringArray(sources);
	}

	@Override
	public String getSelectedFederatedSearchSourceName() {
		return _portletPreferencesHelper.getString(
			PREFERENCE_KEY_FEDERATED_SEARCH_SOURCE_NAME, StringPool.BLANK);
	}

	private final PortletPreferencesHelper _portletPreferencesHelper;

	private Collection<FederatedSearcher> _federatedSearchers;
}