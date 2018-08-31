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
import com.liferay.document.library.kernel.service.DLFileEntryLocalServiceUtil;
import com.liferay.portal.kernel.search.DDMStructureIndexer;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.search.indexer.IndexerWriter;

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

			// TODO Use batch pattern
			List<DLFileEntry> dlFileEntries =
				DLFileEntryLocalServiceUtil.getDDMStructureFileEntries(
					ArrayUtil.toLongArray(ddmStructureIds));

			indexerWriter.reindex(dlFileEntries);
		
	}


	@Reference(
		target = "(indexer.class.name=com.liferay.document.library.kernel.model.DLFileEntry)"
	)	
	protected IndexerWriter<DLFileEntry> indexerWriter;

}