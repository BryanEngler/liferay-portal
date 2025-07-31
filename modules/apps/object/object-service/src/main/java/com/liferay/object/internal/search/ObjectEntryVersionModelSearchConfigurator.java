/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.search;

import com.liferay.object.internal.search.spi.model.index.contributor.ObjectEntryVersionModelIndexerWriterContributor;
import com.liferay.object.model.ObjectEntryVersion;
import com.liferay.object.service.ObjectEntryVersionLocalService;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.search.batch.DynamicQueryBatchIndexingActionableFactory;
import com.liferay.portal.search.spi.model.index.contributor.ModelIndexerWriterContributor;
import com.liferay.portal.search.spi.model.registrar.ModelSearchConfigurator;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Mikel Lorza
 */
@Component(service = ModelSearchConfigurator.class)
public class ObjectEntryVersionModelSearchConfigurator
	implements ModelSearchConfigurator<ObjectEntryVersion> {

	@Override
	public String getClassName() {
		return ObjectEntryVersion.class.getName();
	}

	@Override
	public String[] getDefaultSelectedFieldNames() {
		return new String[] {
			Field.COMPANY_ID, Field.ENTRY_CLASS_NAME, Field.ENTRY_CLASS_PK,
			Field.TITLE, Field.UID
		};
	}

	@Override
	public ModelIndexerWriterContributor<ObjectEntryVersion>
		getModelIndexerWriterContributor() {

		return _modelIndexWriterContributor;
	}

	@Override
	public boolean isReindexEnabled() {
		return false;
	}

	@Activate
	protected void activate() {
		_modelIndexWriterContributor =
			new ObjectEntryVersionModelIndexerWriterContributor(
				_dynamicQueryBatchIndexingActionableFactory,
				_objectEntryVersionLocalService);
	}

	@Reference
	private DynamicQueryBatchIndexingActionableFactory
		_dynamicQueryBatchIndexingActionableFactory;

	private ModelIndexerWriterContributor<ObjectEntryVersion>
		_modelIndexWriterContributor;

	@Reference
	private ObjectEntryVersionLocalService _objectEntryVersionLocalService;

}