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

package com.liferay.asset.search.test;

import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistry;
import com.liferay.portal.kernel.search.SearchEngineHelper;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.search.test.util.IndexedFieldsFixture;
import com.liferay.portal.test.rule.Inject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Luan Maoski
 * @author Luca Marques
 */
public abstract class BaseAssetVocabularyTestCase {

	public void setUp() throws Exception {
		assetVocabularyFixture = createAssetVocabularyFixture();

		assetVocabularyFixture.setUp();

		setGroup(assetVocabularyFixture.addGroup());

		assetVocabularyIndexerFixture = createAssetVocabularyIndexerFixture();

		indexedFieldsFixture = createIndexedFieldsFixture();
	}

	protected AssetVocabularyFixture createAssetVocabularyFixture() {
		return new AssetVocabularyFixture(_groups, _assetVocabularies);
	}

	protected AssetVocabularyIndexerFixture
		createAssetVocabularyIndexerFixture() {

		Indexer<AssetVocabulary> indexer = indexerRegistry.getIndexer(
			AssetVocabulary.class);

		return new AssetVocabularyIndexerFixture(indexer);
	}

	protected IndexedFieldsFixture createIndexedFieldsFixture() {
		return new IndexedFieldsFixture(
			resourcePermissionLocalService, searchEngineHelper);
	}

	protected void setGroup(Group group) {
		assetVocabularyFixture.setGroup(group);
	}

	protected AssetVocabularyFixture assetVocabularyFixture;
	protected AssetVocabularyIndexerFixture assetVocabularyIndexerFixture;
	protected IndexedFieldsFixture indexedFieldsFixture;

	@Inject
	protected IndexerRegistry indexerRegistry;

	@Inject
	protected ResourcePermissionLocalService resourcePermissionLocalService;

	@Inject
	protected SearchEngineHelper searchEngineHelper;

	@DeleteAfterTestRun
	private final List<AssetVocabulary> _assetVocabularies = new ArrayList<>(1);

	@DeleteAfterTestRun
	private final List<Group> _groups = new ArrayList<>(1);

}