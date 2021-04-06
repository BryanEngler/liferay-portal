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

package com.liferay.portal.search.tuning.rankings.web.background.task.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManager;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.model.UserConstants;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.uuid.PortalUUID;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.index.DeleteIndexRequest;
import com.liferay.portal.search.engine.adapter.index.IndicesExistsIndexRequest;
import com.liferay.portal.search.engine.adapter.index.IndicesExistsIndexResponse;
import com.liferay.portal.search.index.IndexNameBuilder;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Adam Brandizzi
 */
@RunWith(Arquillian.class)
public class RankingIndexCreationBackgroundTaskExecutorTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() {
		Assume.assumeNotNull(_backgroundTaskManager);
	}

	@Test
	public void testRankingIndexCreationBackgroundTaskExecutorCreatesIndex()
		throws Exception {

		Company company = createCompanyWithoutRankingsIndex();

		addBackgroundTask();

		Assert.assertTrue(
			isIndexExists(getRankingsIndexName(company.getCompanyId())));
	}

	@Test
	public void testRankingIndexCreationBackgroundTaskExecutorCreatesIndices()
		throws Exception {

		List<Company> companies = createCompaniesWithoutRankingsIndices(5);

		addBackgroundTask();

		for (Company company : companies) {
			Assert.assertTrue(
				isIndexExists(getRankingsIndexName(company.getCompanyId())));
		}
	}

	protected void addBackgroundTask() {
		try {
			_backgroundTaskManager.addBackgroundTask(
				UserConstants.USER_ID_DEFAULT, CompanyConstants.SYSTEM,
				"createRankingIndex-" + _portalUUID.generate(),
				_BACKGROUND_TASK_EXECUTOR_CLASS_NAME, new HashMap<>(),
				new ServiceContext());
		}
		catch (PortalException portalException) {
			System.out.println(
				"Unable to schedule the job for RankingIndexRename");
		}
	}

	protected List<Company> createCompaniesWithoutRankingsIndices(
			int numberOfCompanies)
		throws Exception {

		List<Company> companies = new ArrayList<>();

		for (int i = 0; i < numberOfCompanies; i++) {
			Company company = CompanyTestUtil.addCompany();

			companies.add(company);
		}

		Stream<Company> stream = companies.stream();

		deleteIndex(
			stream.map(
				Company::getCompanyId
			).map(
				this::getRankingsIndexName
			).collect(
				Collectors.toList()
			).toArray(
				new String[companies.size()]
			));

		return companies;
	}

	protected Company createCompanyWithoutRankingsIndex() throws Exception {
		Company company = CompanyTestUtil.addCompany();

		deleteIndex(getRankingsIndexName(company.getCompanyId()));

		return company;
	}

	protected void deleteIndex(String... rankingsIndexNames) {
		DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(
			rankingsIndexNames);

		_searchEngineAdapter.execute(deleteIndexRequest);
	}

	protected String getRankingsIndexName(long companyId) {
		return _indexNameBuilder.getIndexName(companyId) +
			"-search-tuning-rankings";
	}

	protected boolean isIndexExists(String rankingsIndexName) {
		IndicesExistsIndexRequest indicesExistsIndexRequest =
			new IndicesExistsIndexRequest(rankingsIndexName);

		IndicesExistsIndexResponse indicesExistsIndexResponse =
			_searchEngineAdapter.execute(indicesExistsIndexRequest);

		return indicesExistsIndexResponse.isExists();
	}

	private static final String _BACKGROUND_TASK_EXECUTOR_CLASS_NAME =
		"com.liferay.portal.search.tuning.rankings.web.internal.background." +
			"task.RankingIndexCreationBackgroundTaskExecutor";

	@Inject
	private BackgroundTaskManager _backgroundTaskManager;

	@Inject
	private IndexNameBuilder _indexNameBuilder;

	@Inject
	private PortalUUID _portalUUID;

	@Inject
	private SearchEngineAdapter _searchEngineAdapter;

}