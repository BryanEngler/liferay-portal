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

package com.liferay.portal.search.suggest;

import com.liferay.portal.kernel.util.Validator;

import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Bryan Engler
 */
public class DictionaryReaderTest {

	@Test
	public void testDictionaryReader() throws IOException {
		ClassLoader contextClassLoader =
			Thread.currentThread().getContextClassLoader();

		URL url = contextClassLoader.getResource("dictionary/en_US.txt");

		InputStream inputStream = url.openStream();

		DictionaryReader dictionaryReader = new DictionaryReader(inputStream);

		Iterator<DictionaryEntry> iterator =
			dictionaryReader.getDictionaryEntriesIterator();

		List<String> dictionaryWords = new ArrayList<>();

		List<Float> dictionaryWordWeights = new ArrayList<>();

		while (iterator.hasNext()) {
			DictionaryEntry dictionaryEntry = iterator.next();

			String dictionaryWord = dictionaryEntry.getWord();

			if (!Validator.isBlank(dictionaryWord)) {
				dictionaryWords.add(dictionaryWord);

				Float dictionaryWordWeight = dictionaryEntry.getWeight();

				dictionaryWordWeights.add(dictionaryWordWeight);
			}
		}

		String[] expectedWords = {"test", "one", "null", "two", "three"};

		Assert.assertArrayEquals(expectedWords, dictionaryWords.toArray());

		Float[] expectedWordWeights = {1.0F, 0.0F, 0.5F, 2.2F, 3.0F};

		Assert.assertArrayEquals(
			expectedWordWeights, dictionaryWordWeights.toArray());
	}

}