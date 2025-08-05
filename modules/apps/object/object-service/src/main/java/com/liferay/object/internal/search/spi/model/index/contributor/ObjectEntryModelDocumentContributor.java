/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.search.spi.model.index.contributor;

import com.liferay.account.service.AccountEntryOrganizationRelLocalService;
import com.liferay.object.internal.search.spi.model.index.contributor.manager.ObjectEntryModelContributorManager;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryFolderLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectFolderLocalService;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;

/**
 * @author Marco Leo
 * @author Brian Wing Shun Chan
 */
public class ObjectEntryModelDocumentContributor
	implements ModelDocumentContributor<ObjectEntry> {

	public ObjectEntryModelDocumentContributor(
		AccountEntryOrganizationRelLocalService
			accountEntryOrganizationRelLocalService,
		ObjectDefinitionLocalService objectDefinitionLocalService,
		ObjectEntryFolderLocalService objectEntryFolderLocalService,
		ObjectFieldLocalService objectFieldLocalService,
		ObjectFolderLocalService objectFolderLocalService) {

		_objectEntryModelContributorManager =
			new ObjectEntryModelContributorManager(
				accountEntryOrganizationRelLocalService,
				objectDefinitionLocalService, objectEntryFolderLocalService,
				objectFieldLocalService, objectFolderLocalService);
	}

	@Override
	public void contribute(Document document, ObjectEntry objectEntry) {
		_objectEntryModelContributorManager.contribute(document, objectEntry);
	}

	private final ObjectEntryModelContributorManager
		_objectEntryModelContributorManager;

}