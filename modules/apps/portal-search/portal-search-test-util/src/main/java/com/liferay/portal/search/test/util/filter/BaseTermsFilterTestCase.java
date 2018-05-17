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

package com.liferay.portal.search.test.util.filter;

import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.filter.TermsFilter;
import com.liferay.portal.search.test.util.indexing.BaseIndexingTestCase;
import com.liferay.portal.search.test.util.indexing.DocumentCreationHelpers;

import java.util.Arrays;

import org.junit.Test;

/**
 * @author André de Oliveira
 */
public abstract class BaseTermsFilterTestCase extends BaseIndexingTestCase {

	@Test
	public void testKeywordField() throws Exception {
		String fieldName = Field.FOLDER_ID;

		addDocuments(
			value -> DocumentCreationHelpers.singleKeyword(fieldName, value),
			Arrays.asList("One", "Two", "Three"));

		TermsFilter termsFilter = new TermsFilter(fieldName);

		termsFilter.addValues("Two", "Three");

		assertSearch(termsFilter, fieldName, Arrays.asList("Two", "Three"));
	}

	@Test
	public void testLuceneSpecialCharacters() throws Exception {
		String fieldName = Field.FOLDER_ID;

		addDocuments(
			value -> DocumentCreationHelpers.singleKeyword(fieldName, value),
			Arrays.asList("One\\+-!():^[]\"{}~*?|&/Two", "Three"));

		TermsFilter termsFilter = new TermsFilter(fieldName);

		termsFilter.addValues("One\\+-!():^[]\"{}~*?|&/Two", "Three");

		assertSearch(
			termsFilter, fieldName,
			Arrays.asList("One\\+-!():^[]\"{}~*?|&/Two", "Three"));
	}

	@Test
	public void testSolrSpecialCharacters() throws Exception {
		String fieldName = Field.FOLDER_ID;

		addDocuments(
			value -> DocumentCreationHelpers.singleKeyword(fieldName, value),
			Arrays.asList("One\\+-!():^[]\"{}~*?|&/; Two", "Three"));

		TermsFilter termsFilter = new TermsFilter(fieldName);

		termsFilter.addValues("One\\+-!():^[]\"{}~*?|&/; Two", "Three");

		assertSearch(
			termsFilter, fieldName,
			Arrays.asList("One\\+-!():^[]\"{}~*?|&/; Two", "Three"));
	}

	@Test
	public void testSpaces() throws Exception {
		String fieldName = Field.FOLDER_ID;

		addDocuments(
			value -> DocumentCreationHelpers.singleKeyword(fieldName, value),
			Arrays.asList("One Two", "Three"));

		TermsFilter termsFilter = new TermsFilter(fieldName);

		termsFilter.addValues("One Two", "Three");

		assertSearch(termsFilter, fieldName, Arrays.asList("One Two", "Three"));
	}

}