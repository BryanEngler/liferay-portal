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

package com.liferay.portal.search.internal.query.field;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.search.query.field.QueryPreProcessConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * @author Michael C. Han
 */
@Component(
	configurationPid = "com.liferay.portal.search.configuration.QueryPreProcessConfiguration",
	immediate = true, service = QueryPreProcessConfiguration.class
)
public class QueryPreProcessConfigurationImpl
	implements QueryPreProcessConfiguration {

	@Override
	public boolean isPrefixSearchAlways(String fieldName) {
		if (_prefixFieldNamePatterns.containsKey(fieldName)) {
			return true;
		}

		for (Pattern pattern : _prefixFieldNamePatterns.values()) {
			Matcher matcher = pattern.matcher(fieldName);

			if (matcher.matches()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isSubstringSearchAlways(String fieldName) {
		if (_substringFieldNamePatterns.containsKey(fieldName)) {
			return true;
		}

		for (Pattern pattern : _substringFieldNamePatterns.values()) {
			Matcher matcher = pattern.matcher(fieldName);

			if (matcher.matches()) {
				return true;
			}
		}

		return false;
	}

	@Activate
	protected void activate(Map<String, Object> properties) {
		com.liferay.portal.search.configuration.QueryPreProcessConfiguration
			queryPreProcessConfiguration = ConfigurableUtil.createConfigurable(
				com.liferay.portal.search.configuration.
					QueryPreProcessConfiguration.class,
				properties);

		String[] prefixFieldNamePatterns =
			queryPreProcessConfiguration.prefixFieldNamePatterns();

		for (String prefixFieldNamePattern : prefixFieldNamePatterns) {
			_prefixFieldNamePatterns.put(
				prefixFieldNamePattern,
				Pattern.compile(prefixFieldNamePattern));
		}

		String[] substringFieldNamePatterns =
			queryPreProcessConfiguration.fieldNamePatterns();

		for (String substringFieldNamePattern : substringFieldNamePatterns) {
			_substringFieldNamePatterns.put(
				substringFieldNamePattern,
				Pattern.compile(substringFieldNamePattern));
		}
	}

	private final Map<String, Pattern> _prefixFieldNamePatterns =
		new LinkedHashMap<>();
	private final Map<String, Pattern> _substringFieldNamePatterns =
		new LinkedHashMap<>();

}