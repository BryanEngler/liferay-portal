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

package com.liferay.portal.search.elasticsearch.internal.index;

import com.liferay.portal.search.elasticsearch.internal.connection.IndexName;
import com.liferay.portal.search.elasticsearch.internal.document.ElasticsearchSingleFieldFixture;
import com.liferay.portal.search.elasticsearch.internal.query.QueryBuilderFactories;
import com.liferay.portal.search.test.util.japanese.BaseJapaneseHighlightTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author André de Oliveira
 */
public class LiferayTypeMappingsJapaneseHighlightTest
	extends BaseJapaneseHighlightTestCase {

	@Before
	public void setUp() throws Exception {
		IndexName indexName = new IndexName(testName.getMethodName());

		_liferayIndexFixture = new LiferayIndexFixture(_PREFIX, indexName);

		_liferayIndexFixture.setUp();

		singleFieldFixture = new ElasticsearchSingleFieldFixture(
			_liferayIndexFixture.getClient(), indexName,
			LiferayTypeMappingsConstants.LIFERAY_DOCUMENT_TYPE);

		singleFieldFixture.setField(_PREFIX + "_ja");
		singleFieldFixture.setSingleFieldQueryFactory(
			QueryBuilderFactories.MATCH);
	}

	@After
	public void tearDown() throws Exception {
		_liferayIndexFixture.tearDown();
	}

	@Test
	public void testHighlightDMElastic() throws Exception {
		String content1 = "あいうえお　かきくけこ　日本語";
		String content2 = "さしすせそ　たちつてと　日本語";
		String content3 = "English Japanese\n AND OR NOT";
		String content4 = "組織情報B";
		String content5 = "技術推進部 商品開発部 業務部";
		String content6 = "サンプルＢ";
		String content7 = "これは東京都品川区で登録したファイルです";

		index(
			content1, content2, content3, content4, content5, content6,
			content7);

		assertHighlights(
			"あいうえお　日本語", "<em>あい</em>うえ<em>お</em>　かきくけこ　<em>日本語</em>");
	}

	private static final String _PREFIX =
		LiferayTypeMappingsJapaneseHighlightTest.class.getSimpleName();

	private LiferayIndexFixture _liferayIndexFixture;

}