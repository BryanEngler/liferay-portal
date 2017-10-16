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

package com.liferay.portal.search.elasticsearch.configuration;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;
import com.liferay.portal.search.elasticsearch.connection.OperationMode;

/**
 * @author Michael C. Han
 */
@ExtendedObjectClassDefinition(category = "foundation")
@Meta.OCD(
	id = "com.liferay.portal.search.elasticsearch.configuration.ElasticsearchConfiguration",
	localization = "content/Language",
	name = "elastic-search-configuration-name"
)
public interface ElasticsearchConfiguration {

	@Meta.AD(
		deflt = "LiferayElasticsearchCluster", name = "cluster-name",
		required = false
	)
	public String clusterName();

	@Meta.AD(deflt = "EMBEDDED", name = "operation-mode", required = false)
	public OperationMode operationMode();

	@Meta.AD(
		deflt = "liferay-", description = "index-name-prefix-help",
		name = "index-name-prefix", required = false
	)
	public String indexNamePrefix();

	@Meta.AD(deflt = "false", name = "bootstrap-mlockall", required = false)
	public boolean bootstrapMlockAll();

	@Meta.AD(
		deflt = "true", description = "log-exceptions-only-help",
		name = "log-exceptions-only", required = false
	)
	public boolean logExceptionsOnly();

	@Meta.AD(deflt = "5", name = "retry-on-conflict", required = false)
	public int retryOnConflict();

	@Meta.AD(
		deflt = "9300-9400", name = "discovery-zen-ping-unicast-hosts-port",
		required = false
	)
	public String discoveryZenPingUnicastHostsPort();

	@Meta.AD(deflt = "", name = "network-host", required = false)
	public String networkHost();

	@Meta.AD(deflt = "", name = "network-bind-host", required = false)
	public String networkBindHost();

	@Meta.AD(deflt = "", name = "network-publish-host", required = false)
	public String networkPublishHost();

	@Meta.AD(deflt = "", name = "transport-tcp-port", required = false)
	public String transportTcpPort();

	@Meta.AD(
		deflt = "localhost:9300", name = "transport-addresses", required = false
	)
	public String[] transportAddresses();

	@Meta.AD(deflt = "true", name = "client-transport-sniff", required = false)
	public boolean clientTransportSniff();

	@Meta.AD(
		deflt = "false", name = "client-transport-ignore-cluster-name",
		required = false
	)
	public boolean clientTransportIgnoreClusterName();

	@Meta.AD(
		deflt = "5s", name = "client-transport-nodes-sampler-interval",
		required = false
	)
	public String clientTransportNodesSamplerInterval();

	@Meta.AD(deflt = "true", name = "http-enabled", required = false)
	public boolean httpEnabled();

	@Meta.AD(deflt = "true", name = "http-cors-enabled", required = false)
	public boolean httpCORSEnabled();

	@Meta.AD(
		deflt = "/https?:\\/\\/localhost(:[0-9]+)?/",
		name = "http-cors-allow-origin", required = false
	)
	public String httpCORSAllowOrigin();

	@Meta.AD(
		description = "http-cors-configurations-help",
		name = "http-cors-configurations", required = false
	)
	public String httpCORSConfigurations();

	@Meta.AD(
		description = "additional-configurations-help",
		name = "additional-configurations", required = false
	)
	public String additionalConfigurations();

	@Meta.AD(
		description = "additional-index-configurations-help",
		name = "additional-index-configurations", required = false
	)
	public String additionalIndexConfigurations();

	@Meta.AD(
		description = "additional-type-mappings-help",
		name = "additional-type-mappings", required = false
	)
	public String additionalTypeMappings();

	@Meta.AD(
		description = "override-type-mappings-help",
		name = "override-type-mappings", required = false
	)
	public String overrideTypeMappings();

	@Meta.AD(
		deflt = "true", description = "sync-search-help", name = "sync-search",
		required = false
	)
	public boolean syncSearch();

}