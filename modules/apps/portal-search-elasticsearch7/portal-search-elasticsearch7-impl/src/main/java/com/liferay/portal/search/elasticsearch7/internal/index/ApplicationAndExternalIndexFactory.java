/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch7.internal.index;

import com.liferay.portal.search.elasticsearch7.internal.configuration.ElasticsearchConfigurationObserver;
import com.liferay.portal.search.elasticsearch7.internal.configuration.ElasticsearchConfigurationWrapper;
import com.liferay.portal.search.elasticsearch7.internal.index.util.IndexFactoryCompanyIdRegistryUtil;
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
public class ApplicationAndExternalIndexFactory
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
	public void onElasticsearchConfigurationUpdate(Object object) {
		if (object.isContextChanged()) {
			_createApplicationAndExternalIndexes();
		}
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_elasticsearchConfigurationWrapper.register(this);

		//can we compare old and new values of config here?

		if (contextChanged) {
			_createApplicationAndExternalIndexes();
		}
	}

	@Deactivate
	protected void deactivate() {
		_elasticsearchConfigurationWrapper.unregister(this);
	}

	private void _createApplicationAndExternalIndexes() {
		for (Long companyId :
				IndexFactoryCompanyIdRegistryUtil.getCompanyIds()) {

			for (IndexLifecycleManager indexLifecycleManager :
					getIndexLifecycleManagers()) {

				indexLifecycleManager.createIndex(companyId);
			}
		}
	}

	@Reference
	private volatile ElasticsearchConfigurationWrapper
		_elasticsearchConfigurationWrapper;

}