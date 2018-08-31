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

package com.liferay.journal.internal.search;

import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.search.DDMStructureIndexer;
import com.liferay.portal.kernel.search.IndexerRegistry;
import com.liferay.portal.kernel.search.SearchException;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Lucas Marques de Paula
 */

@Component(
	immediate = true,
	property = "ddm.structure.indexer.class.name=com.liferay.journal.model.JournalArticle",
	service = DDMStructureIndexer.class
)
public class JournalArticleDDMStructureIndexer implements DDMStructureIndexer {

	@Override
	public void reindexDDMStructures(List<Long> ddmStructureIds)
		throws SearchException {

		JournalArticleIndexer indexer = (JournalArticleIndexer)
			indexerRegistry.nullSafeGetIndexer(JournalArticle.class);

		indexer.reindexDDMStructures(ddmStructureIds);
	}

	@Reference
	protected IndexerRegistry indexerRegistry;

}