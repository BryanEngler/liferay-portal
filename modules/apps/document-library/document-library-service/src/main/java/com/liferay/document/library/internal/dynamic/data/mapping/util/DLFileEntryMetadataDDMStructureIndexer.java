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

package com.liferay.document.library.internal.dynamic.data.mapping.util;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFileEntryMetadata;
import com.liferay.document.library.kernel.service.DLFileEntryLocalServiceUtil;
import com.liferay.portal.kernel.search.DDMStructureIndexer;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistry;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.search.index.IndexStatusManager;

/**
 * @author Lucas Marques de Paula
 * @author Marcellus Tavares
 */
@Component(
	immediate = true,
	property = "ddm.structure.indexer.class.name=com.liferay.document.library.kernel.model.DLFileEntryMetadata",
	service = DDMStructureIndexer.class
)
public class DLFileEntryMetadataDDMStructureIndexer
	implements DDMStructureIndexer {

	@Override
	public void reindexDDMStructures(List<Long> ddmStructureIds)
		throws SearchException {

		if (indexStatusManager.isIndexReadOnly(DLFileEntryMetadata.class.getName()) || !isIndexerEnabled()) {
			return;
		}

		try {
			Indexer<DLFileEntry> indexer =
					indexerRegistry.nullSafeGetIndexer(DLFileEntry.class);

			if (indexStatusManager.isIndexReadOnly(indexer.getClassName())) {
				return;
			}

			List<DLFileEntry> dlFileEntries =
				DLFileEntryLocalServiceUtil.getDDMStructureFileEntries(
					ArrayUtil.toLongArray(ddmStructureIds));

			for (DLFileEntry dlFileEntry : dlFileEntries) {
				indexer.reindex(dlFileEntry);
			}
		}
		catch (Exception e) {
			throw new SearchException(e);
		}
		
	}

	private boolean isIndexerEnabled() {
		Indexer<?> indexer =
				indexerRegistry.nullSafeGetIndexer(DLFileEntryMetadata.class);
		
		return indexer.isIndexerEnabled();
	}

	@Reference
	protected IndexStatusManager indexStatusManager;
	
	@Reference
	protected IndexerRegistry indexerRegistry;

}