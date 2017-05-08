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

package com.liferay.portal.kernel.search.util;

import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.highlight.HighlightUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Tibor Lipusz
 */
public class HighlightUtilTest {

	@Test
	public void testAddSnippet() {
		assertAddSnippet("<liferay-hl>Hello World</liferay-hl>", "Hello World");
	}

	@Test
	public void testHighlight() {
		String s = "Hello World Liferay";

		assertHighlight(s, "[[Hello]] World Liferay", "hello");
		assertHighlight(s, "Hello World [[Liferay]]", "LIFERAY");
		assertHighlight(s, "[[Hello]] [[World]] Liferay", "hello", "WORLD");
		assertHighlight(s, "[[Hello]] World [[Liferay]]", "HELLO", "liferay");
		assertHighlight(s, "Hello [[World]] [[Liferay]]", "Liferay", "World");
		assertHighlight(
			s, "[[Hello]] [[World]] [[Liferay]]", "Hello", "Liferay", "World");
	}

	@Test
	public void testHighlightWithExtraSpaces() {
		String s = "japanese     food is better in japan";

		assertHighlight(
			s, "[[japanese     food]] is better in japan", "japanese     food");
	}

	@Test
	public void testHighlightWithSpaces() {
		String s = "Hello World Liferay";

		assertHighlight(s, "[[Hello World]] Liferay", "Hello World");
		assertHighlight(s, "Hello [[World Liferay]]", "world LIFERAY");
		assertHighlight(
			s, "[[Hello World]] [[Liferay]]", "HELLO WORLD", "LiferaY");
		assertHighlight(s, "[[Hello World Liferay]]", "hello world liferay");
	}

	@Test
	public void testHighlightWithSuffixes() {
		assertHighlight(
			"Life at Liferay", "[[Life]] at [[Liferay]]", "life", "liferay");
		assertHighlight(
			"LIFERAY FOR LIFE", "[[LIFERAY]] FOR [[LIFE]]", "life", "liferay");
		assertHighlight(
			"Sidewalk Repair/Concrete | Case Closed Sidewalk repaired",
			"[[Sidewalk Repair]]/Concrete | Case Closed [[Sidewalk repaired]]",
			"sidewalk repair", "sidewalk repaired");
		assertHighlight(
			"japanese food is better in japan",
			"japanese food is [[better]] in [[japan]]", "better", "japan");
		assertHighlight(
			"japanese food is better in japan",
			"[[japanese]] food is better in [[japan]]", "japan", "japanese");
	}

	@Test
	public void testNoHighlight() {
		String s = "Hello";

		assertHighlight(s, "Hello");
		assertHighlight(s, "Hello", "world");

		s = "Hello World Liferay";

		assertHighlight(s, "Hello World Liferay");
		assertHighlight(s, "Hello World Liferay", "foo");
		assertHighlight(s, "Hello World Liferay", "foo", "bar");
	}

	protected void assertAddSnippet(String snippet, String fieldValue) {
		Document document = Mockito.mock(Document.class);

		Set<String> queryTerms = new HashSet<>();

		String snippetFieldName = RandomTestUtil.randomString();

		HighlightUtil.addSnippet(
			document, queryTerms, snippet, snippetFieldName);

		Assert.assertEquals(Collections.singleton(fieldValue), queryTerms);

		Mockito.verify(
			document
		).addText(
			"snippet_".concat(snippetFieldName), fieldValue
		);
	}

	protected void assertHighlight(
		String s, String expected, String... queryTerms) {

		expected = StringUtil.replace(
			expected, new String[] {"[[", "]]"}, HighlightUtil.HIGHLIGHTS);

		Assert.assertEquals(
			Arrays.toString(queryTerms), expected,
			HighlightUtil.highlight(s, queryTerms));
	}

}