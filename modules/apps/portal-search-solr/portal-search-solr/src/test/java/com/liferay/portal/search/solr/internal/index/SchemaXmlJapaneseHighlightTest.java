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

package com.liferay.portal.search.solr.internal.index;

import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.search.solr.internal.connection.SolrFixture;
import com.liferay.portal.search.solr.internal.document.SolrSingleFieldFixture;
import com.liferay.portal.search.solr.internal.query.QueryFactories;
import com.liferay.portal.search.test.util.japanese.BaseJapaneseHighlightTestCase;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Bryan Engler
 */
public class SchemaXmlJapaneseHighlightTest
	extends BaseJapaneseHighlightTestCase {

	@Before
	public void setUp() throws Exception {
		singleFieldFixture = new SolrSingleFieldFixture(
			_solrFixture.getClient());

		singleFieldFixture.setField(
			_PREFIX + RandomTestUtil.randomString() + "_ja");
		singleFieldFixture.setSingleFieldQueryFactory(QueryFactories.MATCH);
	}

	@Test
	public void testHighlightDMSolr() throws Exception {
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

		assertHighlights("あいうえお　日本語", "<em>あい</em>うえ<em>お</em>　かきくけこ　日本語");
	}

	private static final String _PREFIX =
		SchemaXmlJapaneseHighlightTest.class.getSimpleName() + "_";

	private final SolrFixture _solrFixture = new SolrFixture();

}