/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.search.experiences.internal.blueprint.search.request.enhancer.body.contributor;

import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.search.index.IndexInformation;
import com.liferay.portal.search.internal.legacy.searcher.SearchRequestBuilderImpl;
import com.liferay.portal.search.internal.searcher.SearchRequestBuilderFactoryImpl;
import com.liferay.portal.search.searcher.SearchRequest;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.search.experiences.internal.blueprint.search.request.body.contributor.IndexSXPSearchRequestBodyContributor;
import com.liferay.search.experiences.rest.dto.v1_0.Configuration;
import com.liferay.search.experiences.rest.dto.v1_0.IndexConfiguration;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Wade Cao
 */
public class IndexSXPSearchRequestBodyContributorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_indexSXPSearchRequestBodyContributor =
			new IndexSXPSearchRequestBodyContributor(_indexInformation);

		_searchContext = new SearchContext();

		_searchContext.setCompanyId(12345);

		Mockito.doReturn(
			"prod-12345"
		).when(
			_indexInformation
		).getCompanyIndexName(
			Mockito.anyLong()
		);
	}

	@Test
	public void testContribute() {
		Configuration configuration = new Configuration();

		SearchRequestBuilder searchRequestBuilder = _getSearchRequestBuilder();

		_indexSXPSearchRequestBodyContributor.contribute(
			configuration, searchRequestBuilder, null);

		SearchRequest searchRequest = searchRequestBuilder.build();

		Assert.assertTrue(ListUtil.isEmpty(searchRequest.getIndexes()));

		_setUpIndexInformation(
			new String[] {"prod-12345-search-tuning-rankings"});

		IndexConfiguration indexConfiguration = new IndexConfiguration();

		indexConfiguration.setExternal(false);
		indexConfiguration.setIndexName("search-tuning-rankings");

		configuration.setIndexConfiguration(indexConfiguration);

		_indexSXPSearchRequestBodyContributor.contribute(
			configuration, searchRequestBuilder, null);

		searchRequest = searchRequestBuilder.build();

		List<String> indexNames = searchRequest.getIndexes();

		Assert.assertEquals(
			"prod-12345-search-tuning-rankings", indexNames.get(0));
	}

	@Test
	public void testContributeWithException() {
		_setUpIndexInformation(
			new String[] {"prod-54321-search-tuning-rankings"});

		Configuration configuration = new Configuration();

		IndexConfiguration indexConfiguration = new IndexConfiguration();

		indexConfiguration.setExternal(false);
		indexConfiguration.setIndexName("search-tuning-rankings");

		configuration.setIndexConfiguration(indexConfiguration);

		SearchRequestBuilder searchRequestBuilder = _getSearchRequestBuilder();

		try {
			_indexSXPSearchRequestBodyContributor.contribute(
				configuration, searchRequestBuilder, null);

			Assert.fail();
		}
		catch (Exception exception) {
			Throwable throwable = exception.getSuppressed()[0];

			Assert.assertEquals(
				"Unable to resolve index name " +
					"prod-12345-search-tuning-rankings",
				throwable.getMessage());
		}
	}

	@Test
	public void testContributeWithExternalFlag() {
		_setUpIndexInformation(
			new String[] {"external:this-is-external-index"});

		Configuration configuration = new Configuration();

		IndexConfiguration indexConfiguration = new IndexConfiguration();

		indexConfiguration.setExternal(true);
		indexConfiguration.setIndexName("this-is-external-index");

		configuration.setIndexConfiguration(indexConfiguration);

		SearchRequestBuilder searchRequestBuilder = _getSearchRequestBuilder();

		_indexSXPSearchRequestBodyContributor.contribute(
			configuration, searchRequestBuilder, null);

		SearchRequest searchRequest = searchRequestBuilder.build();

		List<String> indexNames = searchRequest.getIndexes();

		Assert.assertEquals(
			"external:this-is-external-index", indexNames.get(0));
	}

	private SearchRequestBuilder _getSearchRequestBuilder() {
		SearchRequestBuilderFactory searchRequestBuilderFactory =
			new SearchRequestBuilderFactoryImpl();

		return new SearchRequestBuilderImpl(
			searchRequestBuilderFactory, _searchContext);
	}

	private void _setUpIndexInformation(String[] indexNames) {
		Mockito.doReturn(
			indexNames
		).when(
			_indexInformation
		).getIndexNames();
	}

	private final IndexInformation _indexInformation = Mockito.mock(
		IndexInformation.class);
	private IndexSXPSearchRequestBodyContributor
		_indexSXPSearchRequestBodyContributor;
	private SearchContext _searchContext;

}