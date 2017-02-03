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

package com.liferay.portal.search.elasticsearch.internal.index;

import com.liferay.portal.instance.lifecycle.BasePortalInstanceLifecycleListener;
import com.liferay.portal.instance.lifecycle.PortalInstanceLifecycleListener;
import com.liferay.portal.kernel.concurrent.ConcurrentHashSet;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.search.elasticsearch.index.IndexFactory;

import java.util.Set;

import org.elasticsearch.client.AdminClient;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bryan Engler
 */
@Component(
	immediate = true,
	service = {PortalInstanceLifecycleListener.class, SearchIndexBuilder.class}
)
public class SearchIndexBuilderPortalInstanceLifecycleListener
	extends BasePortalInstanceLifecycleListener implements SearchIndexBuilder {

	@Override
	public void createAllIndicies(AdminClient adminClient) {
		_companyIds.add(CompanyConstants.SYSTEM);

		for (Long companyId : _companyIds) {
			try {
				_indexFactory.createIndices(adminClient, companyId);
			}
			catch (Exception e) {
				_log.error(
					"Unable to create index for company " + companyId, e);

				throw new IllegalStateException(e);
			}
		}
	}

	@Override
	public void portalInstancePreregistered(long companyId) {
		_companyIds.add(companyId);
	}

	@Override
	public void portalInstanceRegistered(Company company) throws Exception {
		_companyIds.add(company.getCompanyId());
	}

	@Override
	public void portalInstanceUnregistered(Company company) throws Exception {
		_companyIds.remove(company.getCompanyId());
	}

	@Reference(unbind = "-")
	public void setIndexFactory(IndexFactory indexFactory) {
		_indexFactory = indexFactory;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SearchIndexBuilderPortalInstanceLifecycleListener.class);

	private final Set<Long> _companyIds = new ConcurrentHashSet<>();

	@Reference
	private IndexFactory _indexFactory;

}