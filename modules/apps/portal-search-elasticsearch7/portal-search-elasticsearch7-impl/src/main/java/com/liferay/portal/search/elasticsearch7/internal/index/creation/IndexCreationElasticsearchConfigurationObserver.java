/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch7.internal.index.creation;

import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerList;
import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerListFactory;
import com.liferay.portal.search.elasticsearch7.internal.configuration.ElasticsearchConfigurationObserver;
import com.liferay.portal.search.elasticsearch7.internal.configuration.ElasticsearchConfigurationWrapper;
import com.liferay.portal.search.elasticsearch7.internal.index.util.IndexFactoryCompanyIdRegistryUtil;
import com.liferay.portal.search.index.creation.IndexNamePrefixIndexCreationConditionContributor;
import com.liferay.portal.search.index.creation.ProductionModeIndexCreationConditionContributor;
import com.liferay.portal.search.spi.index.lifecycle.IndexLifecycleManager;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bryan Engler
 */
@Component(service = {})
public class IndexCreationElasticsearchConfigurationObserver
	implements ElasticsearchConfigurationObserver {

	@Override
	public int compareTo(
		ElasticsearchConfigurationObserver elasticsearchConfigurationObserver) {

		return _elasticsearchConfigurationWrapper.compare(
			this, elasticsearchConfigurationObserver);
	}

	@Override
	public int getPriority() {
		return 4;
	}

	@Override
	public void onElasticsearchConfigurationUpdate() {
		if (_indexNamePrefixIndexCreationConditionContributor.
				shouldCreateIndexes() ||
			_productionModeIndexCreationConditionContributor.
				shouldCreateIndexes()) {

			_createApplicationAndExternalIndexes();
		}
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_elasticsearchConfigurationWrapper.register(this);

		_serviceTrackerList = ServiceTrackerListFactory.open(
			bundleContext, IndexLifecycleManager.class);
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerList.close();

		_elasticsearchConfigurationWrapper.unregister(this);
	}

	private void _createApplicationAndExternalIndexes() {
		for (Long companyId :
				IndexFactoryCompanyIdRegistryUtil.getCompanyIds()) {

			for (IndexLifecycleManager indexLifecycleManager :
					_serviceTrackerList) {

				indexLifecycleManager.createIndex(companyId);
			}
		}
	}

	@Reference
	private volatile ElasticsearchConfigurationWrapper
		_elasticsearchConfigurationWrapper;

	@Reference
	private IndexNamePrefixIndexCreationConditionContributor
		_indexNamePrefixIndexCreationConditionContributor;

	@Reference
	private ProductionModeIndexCreationConditionContributor
		_productionModeIndexCreationConditionContributor;

	private ServiceTrackerList<IndexLifecycleManager> _serviceTrackerList;

}