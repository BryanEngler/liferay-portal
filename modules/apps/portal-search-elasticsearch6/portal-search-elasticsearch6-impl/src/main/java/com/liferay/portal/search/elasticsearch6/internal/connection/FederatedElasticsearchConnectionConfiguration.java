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

package com.liferay.portal.search.elasticsearch6.internal.connection;

import com.liferay.portal.kernel.util.Validator;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bryan Engler
 */
@Component(immediate = true, service = FederatedElasticsearchConnectionConfiguration.class)
public class FederatedElasticsearchConnectionConfiguration {

	@Activate
	protected void activate(Map<String, Object> properties) {
		String connectionId = (String)properties.get("connectionId");

		if (Validator.isNull(connectionId)) {
			System.out.println("Not adding connection: connectionId is null");

			return;
		}

		_properties.put("connectionId", connectionId);

		_properties.put("port", (String)properties.get("port"));

		_properties.put("host", (String)properties.get("host"));

		_properties.put("clusterName", (String)properties.get("clusterName"));

		elasticsearchConnectionManager.
			addFederatedElasticsearchConnection(_properties);
	}

	@Deactivate
	protected void deactivate(Map<String, Object> properties) {
		String connectionId = _properties.get("connectionId");

		if (Validator.isNull(connectionId)) {
			return;
		}

		elasticsearchConnectionManager.
			removeFederatedElasticsearchConnection(_properties);
	}

	public String getPropertyValue(String key) {
		return _properties.get(key);
	}

	private Map<String, String> _properties = new HashMap<>();

	@Reference
	protected ElasticsearchConnectionManager
		elasticsearchConnectionManager;
}