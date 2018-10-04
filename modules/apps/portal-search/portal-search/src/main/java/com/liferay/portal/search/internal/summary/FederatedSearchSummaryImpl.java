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

package com.liferay.portal.search.internal.summary;


import com.liferay.portal.search.summary.FederatedSearchSummary;

import java.util.Locale;

/**
 * @author Bryan Engler
 */
public class FederatedSearchSummaryImpl implements FederatedSearchSummary {

	public FederatedSearchSummaryImpl(
		String title, String content, String url, Locale locale) {

		_title = title;
		_content = content;
		_locale = locale;
		_url = url;
	}

	@Override
	public String getContent() {
		return _content;
	}

	@Override
	public Locale getLocale() {
		return _locale;
	}

	@Override
	public String getTitle() {
		return _title;
	}

	@Override
	public String getURL() {
		return _url;
	}

	private final String _content;
	private final Locale _locale;
	private final String _title;
	private final String _url;
}