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

package com.liferay.portal.search.test.util.japanese;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.search.test.util.document.SingleFieldFixture;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * @author Bryan Engler
 */
public abstract class BaseJapaneseHighlightTestCase {

	@Test
	public void testHighlightDM() throws Exception {
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
			"English Japanese",
			"<em>English</em> <em>Japanese</em>\n AND OR NOT");

		assertHighlights("あいう", StringPool.BLANK);

		assertHighlights("サンプル", "<em>サンプル</em>Ｂ");

		assertHighlights("推進", "技術<em>推進</em>部 商品開発部 業務部");

		assertHighlights(
			"推進部", "技術<em>推進</em><em>部</em> 商品開発<em>部</em> 業務<em>部</em>");

		assertHighlights("品川区", "これは東京都<em>品川</em><em>区</em>で登録したファイルです");
	}

	@Test
	public void testHighlightWCM() throws Exception {
		String content1 = "サンプルコンテンツＢ";
		String content2 = "技術推進部 商品開発部 業務部";
		String content3 = "愛知県名古屋市";

		index(content1, content2, content3);

		assertHighlights("サンプル", "<em>サンプル</em>コンテンツＢ");

		assertHighlights("推進", "技術<em>推進</em>部 商品開発部 業務部");

		assertHighlights(
			"推進部", "技術<em>推進</em><em>部</em> 商品開発<em>部</em> 業務<em>部</em>");

		assertHighlights("名古屋", "愛知県<em>名古屋</em>市");
	}

	@Rule
	public TestName testName = new TestName();

	protected void assertHighlights(String query, String... expected)
		throws Exception {

		singleFieldFixture.assertHighlights(query, expected);
	}

	protected void index(String... strings) {
		try {
			for (String string : strings) {
				singleFieldFixture.indexDocument(string);
			}
		}
		catch (Exception e) {
			_log.error("Unable to index documents", e);
		}
	}

	protected SingleFieldFixture singleFieldFixture;

	private static final Log _log = LogFactoryUtil.getLog(
		BaseJapaneseHighlightTestCase.class);

}