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

package com.liferay.portal.search.test.util.expando;

import com.liferay.expando.kernel.model.ExpandoBridge;
import com.liferay.expando.kernel.model.ExpandoColumn;
import com.liferay.expando.kernel.model.ExpandoColumnConstants;
import com.liferay.expando.kernel.service.ExpandoColumnLocalService;
import com.liferay.expando.kernel.util.ExpandoBridgeFactory;
import com.liferay.expando.kernel.util.ExpandoBridgeIndexer;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.ExpandoQueryContributor;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.generic.BooleanQueryImpl;
import com.liferay.portal.kernel.search.query.FieldQueryFactory;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.search.internal.analysis.SimpleKeywordTokenizer;
import com.liferay.portal.search.internal.analysis.SubstringFieldQueryBuilder;
import com.liferay.portal.search.internal.analysis.TitleFieldQueryBuilder;
import com.liferay.portal.search.internal.expando.BaseIndexerExpandoQueryContributor;
import com.liferay.portal.search.internal.expando.ExpandoFieldQueryBuilderFactory;
import com.liferay.portal.search.internal.query.FieldQueryFactoryImpl;
import com.liferay.portal.search.test.util.DocumentsAssert;
import com.liferay.portal.search.test.util.IdempotentRetryAssert;
import com.liferay.portal.search.test.util.indexing.BaseIndexingTestCase;
import com.liferay.portal.search.test.util.indexing.DocumentCreationHelpers;
import com.liferay.registry.BasicRegistryImpl;
import com.liferay.registry.Registry;
import com.liferay.registry.RegistryUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * @author Bryan Engler
 */
public abstract class BaseExpandoTestCase extends BaseIndexingTestCase {

	protected void assertSearch(
			SearchContext searchContext, BooleanQuery booleanQuery,
			int expectedCount)
		throws Exception {

		IdempotentRetryAssert.retryAssert(
			3, TimeUnit.SECONDS,
			() -> {
				Hits hits = search(searchContext, booleanQuery);

				DocumentsAssert.assertCount(
					booleanQuery.toString(), hits.getDocs(), getField(),
					expectedCount);

				return null;
			});
	}

	protected void assertSearch(String keywords, int expectedCount)
		throws Exception {

		ExpandoFieldQueryBuilderFactory expandoFieldQueryBuilderFactory =
			new ExpandoFieldQueryBuilderFactory() {
				{
					substringQueryBuilder = new SubstringFieldQueryBuilder() {
						{
							keywordTokenizer = new SimpleKeywordTokenizer();
						}
					};
				}
			};

		TitleFieldQueryBuilder titleFieldQueryBuilder =
			new TitleFieldQueryBuilder() {
				{
					keywordTokenizer = new SimpleKeywordTokenizer();
				}
			};

		FieldQueryFactoryImpl fieldQueryFactory = new FieldQueryFactoryImpl() {
			{
				titleQueryBuilder = titleFieldQueryBuilder;
				addFieldQueryBuilderFactory(expandoFieldQueryBuilderFactory);
			}
		};

		Registry registry = new BasicRegistryImpl();

		registry.registerService(FieldQueryFactory.class, fieldQueryFactory);

		RegistryUtil.setRegistry(registry);

		BooleanQuery booleanQuery = new BooleanQueryImpl();

		SearchContext searchContext = createSearchContext();

		ExpandoQueryContributor expandoQueryContributor =
			getBaseIndexerExpandoQueryContributor();

		searchContext.setAttribute(
			"BaseIndexer.searchClassNames", new String[] {_CLASS_NAME});

		expandoQueryContributor.contribute(
			keywords, booleanQuery, searchContext);

		assertSearch(searchContext, booleanQuery, expectedCount);
	}

	protected ExpandoQueryContributor getBaseIndexerExpandoQueryContributor() {
		return new BaseIndexerExpandoQueryContributor() {
			{
				expandoBridgeFactory = getExpandoBridgeFactory();
				expandoBridgeIndexer = getExpandoBridgeIndexer();
				expandoColumnLocalService = getExpandoColumnLocalService();
			}
		};
	}

	protected ExpandoBridgeFactory getExpandoBridgeFactory() {
		ExpandoBridgeFactory expandoBridgeFactory = Mockito.mock(
			ExpandoBridgeFactory.class);

		Mockito.doReturn(
			_getExpandoBridge()
		).when(
			expandoBridgeFactory
		).getExpandoBridge(
			Mockito.anyLong(), Matchers.eq(_CLASS_NAME)
		);

		return expandoBridgeFactory;
	}

	protected ExpandoBridgeIndexer getExpandoBridgeIndexer() {
		ExpandoBridgeIndexer expandoBridgeIndexer = Mockito.mock(
			ExpandoBridgeIndexer.class);

		int indexType = ExpandoColumnConstants.INDEX_TYPE_TEXT;

		if (_keywordField) {
			indexType = ExpandoColumnConstants.INDEX_TYPE_KEYWORD;
		}

		Mockito.doReturn(
			getField()
		).when(
			expandoBridgeIndexer
		).encodeFieldName(
			Mockito.anyString(), Matchers.eq(indexType)
		);

		return expandoBridgeIndexer;
	}

	protected ExpandoColumnLocalService getExpandoColumnLocalService() {
		ExpandoColumnLocalService expandoColumnLocalService = Mockito.mock(
			ExpandoColumnLocalService.class);

		Mockito.doReturn(
			_getExpandoColumn()
		).when(
			expandoColumnLocalService
		).getDefaultTableColumn(
			Mockito.anyLong(), Mockito.anyString(), Mockito.anyString()
		);

		return expandoColumnLocalService;
	}

	protected String getField() {
		if (_keywordField) {
			return "expando__keyword__custom_fields__testColumnName";
		}

		return "expando__custom_fields__testColumnName";
	}

	protected void testBasicWordMatches() throws Exception {
		_keywordField = false;

		addDocuments(
			value -> DocumentCreationHelpers.singleText(getField(), value),
			Arrays.asList("alpha", "alpha", "alpha bravo", "charlie", "delta"));

		assertSearch("alpha", 3);
		assertSearch("bravo", 1);
		assertSearch("alpha bravo", 3);
		assertSearch("charlie", 1);
		assertSearch("echo", 0);
	}

	protected void testBasicWordMatchesKeyword() throws Exception {
		_keywordField = true;

		addDocuments(
			value -> DocumentCreationHelpers.singleKeyword(getField(), value),
			Arrays.asList("alpha", "alpha", "alpha bravo", "charlie", "delta"));

		assertSearch("alpha", 3);
		assertSearch("bravo", 1);
		assertSearch("alpha bravo", 3);
		assertSearch("charlie", 1);
		assertSearch("echo", 0);
	}

	private ExpandoBridge _getExpandoBridge() {
		ExpandoBridge expandoBridge = Mockito.mock(ExpandoBridge.class);

		Mockito.doReturn(
			Collections.enumeration(
				Collections.singletonList(RandomTestUtil.randomString()))
		).when(
			expandoBridge
		).getAttributeNames();

		Mockito.doReturn(
			_getUnicodeProperties()
		).when(
			expandoBridge
		).getAttributeProperties(
			Mockito.anyString()
		);

		return expandoBridge;
	}

	private ExpandoColumn _getExpandoColumn() {
		ExpandoColumn expandoColumn = Mockito.mock(ExpandoColumn.class);

		Mockito.doReturn(
			_getUnicodeProperties()
		).when(
			expandoColumn
		).getTypeSettingsProperties();

		Mockito.doReturn(
			ExpandoColumnConstants.STRING
		).when(
			expandoColumn
		).getType();

		return expandoColumn;
	}

	private UnicodeProperties _getUnicodeProperties() {
		UnicodeProperties unicodeProperties = Mockito.mock(
			UnicodeProperties.class);

		int indexType = ExpandoColumnConstants.INDEX_TYPE_TEXT;

		if (_keywordField) {
			indexType = ExpandoColumnConstants.INDEX_TYPE_KEYWORD;
		}

		Mockito.doReturn(
			String.valueOf(indexType)
		).when(
			unicodeProperties
		).getProperty(
			ExpandoColumnConstants.INDEX_TYPE
		);

		return unicodeProperties;
	}

	private static final String _CLASS_NAME = User.class.getName();

	private boolean _keywordField;

}