/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.internal.index.creation;

import com.liferay.portal.search.index.creation.IndexCreationManager;
import com.liferay.portal.search.index.creation.IndexNamePrefixIndexCreationConditionContributor;
import com.liferay.portal.search.index.creation.ProductionModeIndexCreationConditionContributor;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bryan Engler
 */
@Component(service = IndexCreationManager.class)
public class IndexCreationManagerImpl implements IndexCreationManager {

	@Override
	public boolean shouldCreateIndexes() {
		if (_indexNamePrefixIndexCreationConditionContributor.
				shouldCreateIndexes() ||
			_productionModeIndexCreationConditionContributor.
				shouldCreateIndexes()) {

			return true;
		}

		return false;
	}

	@Reference
	private IndexNamePrefixIndexCreationConditionContributor
		_indexNamePrefixIndexCreationConditionContributor;

	@Reference
	private ProductionModeIndexCreationConditionContributor
		_productionModeIndexCreationConditionContributor;

}