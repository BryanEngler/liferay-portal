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

import java.util.Comparator;

/**
 * @author Wade Cao
 */
public abstract class SearchFacetTermDisplayContext implements Serializable {

	public static final Comparator<SearchFacetTermDisplayContext>
		COMPARATOR_FREQUENCY_ASC =
			new Comparator<SearchFacetTermDisplayContext>() {

				public int compare(
					SearchFacetTermDisplayContext
						searchFacetTermDisplayContext1,
					SearchFacetTermDisplayContext
						searchFacetTermDisplayContext2) {

					int result =
						searchFacetTermDisplayContext1.getFrequency() -
							searchFacetTermDisplayContext2.getFrequency();

					if (result == 0) {
						return _compareDisplayNames(
							searchFacetTermDisplayContext1.getDisplayName(),
							searchFacetTermDisplayContext2.getDisplayName());
					}

					return result;
				}

			};

	public static final Comparator<SearchFacetTermDisplayContext>
		COMPARATOR_FREQUENCY_DESC =
			new Comparator<SearchFacetTermDisplayContext>() {

				@Override
				public int compare(
					SearchFacetTermDisplayContext
						searchFacetTermDisplayContext1,
					SearchFacetTermDisplayContext
						searchFacetTermDisplayContext2) {

					int result =
						searchFacetTermDisplayContext2.getFrequency() -
							searchFacetTermDisplayContext1.getFrequency();

					if (result == 0) {
						return _compareDisplayNames(
							searchFacetTermDisplayContext1.getDisplayName(),
							searchFacetTermDisplayContext2.getDisplayName());
					}

					return result;
				}

			};

	public static final Comparator<SearchFacetTermDisplayContext>
		COMPARATOR_TERM_ASC = new Comparator<SearchFacetTermDisplayContext>() {

			@Override
			public int compare(
				SearchFacetTermDisplayContext searchFacetTermDisplayContext1,
				SearchFacetTermDisplayContext searchFacetTermDisplayContext2) {

				int result = _compareDisplayNames(
					searchFacetTermDisplayContext1.getDisplayName(),
					searchFacetTermDisplayContext2.getDisplayName());

				if (result == 0) {
					return searchFacetTermDisplayContext2.getFrequency() -
						searchFacetTermDisplayContext1.getFrequency();
				}

				return result;
			}

		};

	public static final Comparator<SearchFacetTermDisplayContext>
		COMPARATOR_TERM_DESC = new Comparator<SearchFacetTermDisplayContext>() {

			@Override
			public int compare(
				SearchFacetTermDisplayContext searchFacetTermDisplayContext1,
				SearchFacetTermDisplayContext searchFacetTermDisplayContext2) {

				int result = _compareDisplayNames(
					searchFacetTermDisplayContext2.getDisplayName(),
					searchFacetTermDisplayContext1.getDisplayName());

				if (result == 0) {
					return searchFacetTermDisplayContext2.getFrequency() -
						searchFacetTermDisplayContext1.getFrequency();
				}

				return result;
			}

		};

	public abstract String getDisplayName();

	public abstract int getFrequency();

	private static int _compareDisplayNames(
		String displayName1, String displayName2) {

		return displayName1.compareTo(displayName2);
	}

}