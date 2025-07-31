/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.search.spi.model.index.contributor;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectEntryVersion;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectEntryVersionLocalService;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.search.batch.BatchIndexingActionable;
import com.liferay.portal.search.batch.DynamicQueryBatchIndexingActionableFactory;
import com.liferay.portal.search.spi.model.index.contributor.ModelIndexerWriterContributor;
import com.liferay.portal.search.spi.model.index.contributor.helper.ModelIndexerWriterDocumentHelper;

import java.util.Collections;
import java.util.List;

/**
 * @author Marco Leo
 * @author Brian Wing Shun Chan
 */
public class ObjectEntryModelIndexerWriterContributor
	implements ModelIndexerWriterContributor<ObjectEntry> {

	public ObjectEntryModelIndexerWriterContributor(
		DynamicQueryBatchIndexingActionableFactory
			dynamicQueryBatchIndexingActionableFactory,
		long objectDefinitionId,
		ObjectDefinitionLocalService objectDefinitionLocalService,
		ObjectEntryLocalService objectEntryLocalService,
		ObjectEntryVersionLocalService objectEntryVersionLocalService) {

		_dynamicQueryBatchIndexingActionableFactory =
			dynamicQueryBatchIndexingActionableFactory;
		_objectDefinitionId = objectDefinitionId;
		_objectDefinitionLocalService = objectDefinitionLocalService;
		_objectEntryLocalService = objectEntryLocalService;
		_objectEntryVersionLocalService = objectEntryVersionLocalService;
	}

	@Override
	public void customize(
		BatchIndexingActionable batchIndexingActionable,
		ModelIndexerWriterDocumentHelper modelIndexerWriterDocumentHelper) {

		batchIndexingActionable.setAddCriteriaMethod(
			dynamicQuery -> {
				Property objectDefinitionIdProperty =
					PropertyFactoryUtil.forName("objectDefinitionId");

				dynamicQuery.add(
					objectDefinitionIdProperty.eq(_objectDefinitionId));
			});

		Indexer<ObjectEntryVersion> indexer = IndexerRegistryUtil.getIndexer(
			ObjectEntryVersion.class);

		if (indexer != null) {
			batchIndexingActionable.setPerformActionMethod(
				(ObjectEntry objectEntry) -> {
					batchIndexingActionable.addDocuments(
						modelIndexerWriterDocumentHelper.getDocument(
							objectEntry));

					try {
						indexer.reindex(_getObjectEntryVersions(objectEntry));
					}
					catch (SearchException searchException) {
						if (_log.isDebugEnabled()) {
							_log.debug(
								"Unable to index object entry versions for " +
									"the object entry with ID " +
										objectEntry.getObjectEntryId(),
								searchException);
						}
					}
				});
		}
		else {
			batchIndexingActionable.setPerformActionMethod(
				(ObjectEntry objectEntry) ->
					batchIndexingActionable.addDocuments(
						modelIndexerWriterDocumentHelper.getDocument(
							objectEntry)));
		}
	}

	@Override
	public BatchIndexingActionable getBatchIndexingActionable() {
		return _dynamicQueryBatchIndexingActionableFactory.
			getBatchIndexingActionable(
				_objectEntryLocalService.getIndexableActionableDynamicQuery());
	}

	@Override
	public long getCompanyId(ObjectEntry objectEntry) {
		return objectEntry.getCompanyId();
	}

	private List<ObjectEntryVersion> _getObjectEntryVersions(
		ObjectEntry objectEntry) {

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.fetchObjectDefinition(
				_objectDefinitionId);

		if ((objectDefinition == null) ||
			!objectDefinition.isEnableObjectEntryVersioning()) {

			return Collections.emptyList();
		}

		return _objectEntryVersionLocalService.getObjectEntryVersions(
			objectEntry.getObjectEntryId());
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ObjectEntryModelIndexerWriterContributor.class.getName());

	private final DynamicQueryBatchIndexingActionableFactory
		_dynamicQueryBatchIndexingActionableFactory;
	private final Long _objectDefinitionId;
	private final ObjectDefinitionLocalService _objectDefinitionLocalService;
	private final ObjectEntryLocalService _objectEntryLocalService;
	private final ObjectEntryVersionLocalService
		_objectEntryVersionLocalService;

}