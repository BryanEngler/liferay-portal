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

package com.liferay.portal.search.internal.info.list.provider;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.service.AssetEntryService;
import com.liferay.info.list.provider.InfoListProvider;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapDictionary;
import com.liferay.portal.search.configuration.BlueprintInfoListProviderConfiguration;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.search.sort.Sorts;

import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bryan Engler
 */
@Component(
	configurationPid = "com.liferay.portal.search.configuration.BlueprintInfoListProviderConfiguration",
	service = {}
)
public class BlueprintInfoListProviderConfigurationActivationHandler {

	@Activate
	protected void activate(
		Map<String, Object> properties, BundleContext bundleContext) {

		BlueprintInfoListProviderConfiguration
			blueprintInfoListProviderConfiguration =
				ConfigurableUtil.createConfigurable(
					BlueprintInfoListProviderConfiguration.class, properties);

		BlueprintInfoListProvider blueprintInfoListProvider =
			new BlueprintInfoListProvider(
				GetterUtil.getLong(properties.get("companyId")),
				blueprintInfoListProviderConfiguration.blueprintId(),
				blueprintInfoListProviderConfiguration.name(),
				assetEntryService, searcher, sorts,
				searchRequestBuilderFactory);

		_serviceRegistration = bundleContext.registerService(
			(Class<InfoListProvider<AssetEntry>>)
				(Class<?>)InfoListProvider.class,
			blueprintInfoListProvider, new HashMapDictionary());
	}

	@Deactivate
	protected void deactivate() {
		_serviceRegistration.unregister();
	}

	@Reference
	protected AssetEntryService assetEntryService;

	@Reference
	protected Searcher searcher;

	@Reference
	protected SearchRequestBuilderFactory searchRequestBuilderFactory;

	@Reference
	protected Sorts sorts;

	private ServiceRegistration<InfoListProvider<AssetEntry>>
		_serviceRegistration;

}