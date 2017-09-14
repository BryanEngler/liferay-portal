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

import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.search.elasticsearch.internal.connection.IndexName;
import com.liferay.portal.search.elasticsearch.internal.document.SingleFieldFixture;
import com.liferay.portal.search.elasticsearch.internal.query.QueryBuilderFactories;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * @author André de Oliveira
 */
public class LiferayTypeMappingsJapaneseHighlightTest {

	@Before
	public void setUp() throws Exception {
		IndexName indexName = new IndexName(testName.getMethodName());

		_liferayIndexFixture = new LiferayIndexFixture(_PREFIX, indexName);

		_liferayIndexFixture.setUp();

		_singleFieldFixture = new SingleFieldFixture(
			_liferayIndexFixture.getClient(), indexName,
			LiferayTypeMappingsConstants.LIFERAY_DOCUMENT_TYPE);

		_singleFieldFixture.setField(_PREFIX + "_ja");
		_singleFieldFixture.setQueryBuilderFactory(QueryBuilderFactories.MATCH);
	}

	@After
	public void tearDown() throws Exception {
		_liferayIndexFixture.tearDown();
	}

	@Test
	public void testHighlightDM() throws Exception {
		//https://issues.liferay.com/browse/LPS-74642

		String content1 = "あいうえお　かきくけこ　日本語"; //Document1.txt content
		String content2 = "さしすせそ　たちつてと　日本語"; //Document2.txt content
		String content3 = "English Japanese\n AND OR NOT"; //Document3.txt content
		String content4 = "組織情報B"; //pdf title
		String content5 = "技術推進部 商品開発部 業務部"; //pdf content
		String content6 = "サンプルＢ"; //pdf metadata title
		String content7 = "これは東京都品川区で登録したファイルです"; //pdf metadata description


		index(content1, content2, content3, content4, content5, content6,
			content7);

		//LPS-74642 a.
		assertHighlights(
			"English Japanese",
			"<em>English</em> <em>Japanese</em>\n AND OR NOT");


		//LPS-74642 b. *** incorrect highlights displayed in portal ***
		/*
			user expectation:
			<em>あいうえお</em>　かきくけこ　<em>日本語</em>
			さしすせそ　たちつてと　<em>日本語</em>
		*/
		assertHighlights(
			"あいうえお　日本語",
			"<em>あい</em>うえお　かきくけこ　<em>日本語</em>",
			"さしすせそ　たちつてと　<em>日本</em><em>語</em>");


		//LPS-74642 c. *** no search results/highlights displayed in portal ***
		/*
			user expectation ???:
			<em>あいう</em>えお　かきくけこ　日本語
		*/
		assertHighlights(
			"あいう",
			StringPool.BLANK);


		//LPS-74642 d.
		assertHighlights(
			"サンプル",
			"<em>サンプル</em>Ｂ");


		//LPS-74642 e.
		assertHighlights(
			"推進",
			"技術<em>推進</em>部 商品開発部 業務部");


		//LPS-74642 f. *** incorrect highlights displayed in portal ***
		/*
			user expectation:
			技術<em>推進部</em> 商品開発部 業務部
		*/
		assertHighlights(
			"推進部",
			"技術<em>推進</em><em>部</em> 商品開発<em>部</em> 業務<em>部</em>");


		//LPS-74642 g. *** no highlights displayed in portal ***
		/*
			user expectation ???:
			これは東京都<em>品川区</em>で登録したファイルです
		*/
		assertHighlights(
			"品川区",
			"これは東京都<em>品川</em><em>区</em>で登録したファイルです");
	}

	@Test
	public void testHighlightWCM() throws Exception {
		//https://issues.liferay.com/browse/LPS-74653

		String content1 = "サンプルコンテンツＢ"; //web content title
		String content2 = "技術推進部 商品開発部 業務部"; //web content description
		String content3 = "愛知県名古屋市"; //web content content

		index(content1, content2, content3);

		//LPS-74653 a.
		assertHighlights(
			"サンプル",
			"<em>サンプル</em>コンテンツＢ");


		//LPS-74653 b. *** no highlights displayed in portal ***
		/*
			user expectation:
			技術<em>推進</em>部 商品開発部 業務部
		*/
		assertHighlights(
			"推進",
			"技術<em>推進</em>部 商品開発部 業務部");


		//LPS-74653 c. *** no highlights displayed in portal ***
		/*
			user expectation:
			技術<em>推進部</em> 商品開発部 業務部
		*/
		assertHighlights(
			"推進部",
			"技術<em>推進</em><em>部</em> 商品開発<em>部</em> 業務<em>部</em>");


		//LPS-74653 d.  *** no highlights displayed in portal ***
		/*
			user expectation ???:
			愛知県<em>名古屋</em>市
		*/
		assertHighlights(
			"名古屋",
			"愛知県<em>名古屋</em>市");
	}

	@Rule
	public TestName testName = new TestName();

	protected void assertHighlights(String query, String... expected)
		throws Exception {

		_singleFieldFixture.assertHighlights(query, expected);
	}

	protected void index(String... strings) {
		for (String string : strings) {
			_singleFieldFixture.indexDocument(string);
		}
	}

	private static final String _PREFIX =
		LiferayTypeMappingsJapaneseHighlightTest.class.getSimpleName();

	private LiferayIndexFixture _liferayIndexFixture;
	private SingleFieldFixture _singleFieldFixture;

}