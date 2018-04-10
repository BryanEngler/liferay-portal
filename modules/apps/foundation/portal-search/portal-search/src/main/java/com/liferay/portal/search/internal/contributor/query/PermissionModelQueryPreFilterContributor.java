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

package com.liferay.portal.search.internal.contributor.query;

import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchEngineHelperUtil;
import com.liferay.portal.kernel.search.SearchPermissionChecker;
import com.liferay.portal.kernel.search.filter.BooleanFilter;
import com.liferay.portal.search.permission.SearchPermissionFilterContributor;
import com.liferay.portal.search.spi.model.query.contributor.ModelQueryPreFilterContributor;
import com.liferay.portal.search.spi.model.registrar.ModelSearchSettings;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * @author Bryan Engler
 */
@Component(immediate = true, service = ModelQueryPreFilterContributor.class)
public class PermissionModelQueryPreFilterContributor
	implements ModelQueryPreFilterContributor {

	@Override
	public void contribute(
		BooleanFilter booleanFilter, ModelSearchSettings modelSearchSettings,
		SearchContext searchContext) {

		if ((searchContext.getUserId() == 0) ||
			!modelSearchSettings.isPermissionAware()) {

			return;
		}

		SearchPermissionChecker searchPermissionChecker =
			SearchEngineHelperUtil.getSearchPermissionChecker();

		Optional<String> parentEntryClassNameOptional =
			_getParentEntryClassName(modelSearchSettings.getClassName());

		String permissionedEntryClassName = parentEntryClassNameOptional.orElse(
			modelSearchSettings.getClassName());

		searchPermissionChecker.getPermissionBooleanFilter(
			searchContext.getCompanyId(), searchContext.getGroupIds(),
			searchContext.getUserId(), permissionedEntryClassName,
			booleanFilter, searchContext);
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY,
		unbind = "removeSearchPermissionFilterContributor"
	)
	protected void addSearchPermissionFilterContributor(
		SearchPermissionFilterContributor searchPermissionFilterContributor) {

		_searchPermissionFilterContributors.add(
			searchPermissionFilterContributor);
	}

	protected void removeSearchPermissionFilterContributor(
		SearchPermissionFilterContributor searchPermissionFilterContributor) {

		_searchPermissionFilterContributors.remove(
			searchPermissionFilterContributor);
	}

	private Optional<String> _getParentEntryClassName(String entryClassName) {
		for (SearchPermissionFilterContributor
				searchPermissionFilterContributor :
					_searchPermissionFilterContributors) {

			Optional<String> parentEntryClassNameOptional =
				searchPermissionFilterContributor.
					getParentEntryClassNameOptional(entryClassName);

			if ((parentEntryClassNameOptional != null) &&
				parentEntryClassNameOptional.isPresent()) {

				return parentEntryClassNameOptional;
			}
		}

		return Optional.empty();
	}

	private final Collection<SearchPermissionFilterContributor>
		_searchPermissionFilterContributors = new CopyOnWriteArrayList<>();

}