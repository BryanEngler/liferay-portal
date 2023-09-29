/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.tuning.rankings.web.internal.index.lifecycle;

import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.search.index.creation.IndexCreationManager;
import com.liferay.portal.search.spi.index.lifecycle.IndexLifecycleManager;
import com.liferay.portal.search.tuning.rankings.web.internal.index.RankingIndexCreator;
import com.liferay.portal.search.tuning.rankings.web.internal.index.RankingIndexReader;
import com.liferay.portal.search.tuning.rankings.web.internal.index.importer.SingleIndexToMultipleIndexImporter;
import com.liferay.portal.search.tuning.rankings.web.internal.index.name.RankingIndexName;
import com.liferay.portal.search.tuning.rankings.web.internal.index.name.RankingIndexNameBuilder;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bryan Engler
 */
@Component(service = IndexLifecycleManager.class)
public class RankingIndexLifecycleManager implements IndexLifecycleManager {

	@Override
	public void createIndex(long companyId) {
		if (companyId == CompanyConstants.SYSTEM) {
			return;
		}

		RankingIndexName rankingIndexName =
			_rankingIndexNameBuilder.getRankingIndexName(companyId);

		if (_rankingIndexReader.isExists(rankingIndexName)) {
			return;
		}

		_rankingIndexCreator.create(rankingIndexName);

		if (_singleIndexToMultipleIndexImporter.needImport()) {
			_singleIndexToMultipleIndexImporter.importRankings(companyId);
		}
	}

	@Override
	public void deleteIndex(long companyId) {
		RankingIndexName rankingIndexName =
			_rankingIndexNameBuilder.getRankingIndexName(companyId);

		if (!_rankingIndexReader.isExists(rankingIndexName)) {
			return;
		}

		_rankingIndexCreator.delete(rankingIndexName);
	}

	@Activate
	protected void activate() {
		if (_indexCreationManager.shouldCreateIndexes()) {
			_companyLocalService.forEachCompanyId(
				companyId -> createIndex(companyId));
		}
	}

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private IndexCreationManager _indexCreationManager;

	@Reference
	private RankingIndexCreator _rankingIndexCreator;

	@Reference
	private RankingIndexNameBuilder _rankingIndexNameBuilder;

	@Reference
	private RankingIndexReader _rankingIndexReader;

	@Reference
	private SingleIndexToMultipleIndexImporter
		_singleIndexToMultipleIndexImporter;

}