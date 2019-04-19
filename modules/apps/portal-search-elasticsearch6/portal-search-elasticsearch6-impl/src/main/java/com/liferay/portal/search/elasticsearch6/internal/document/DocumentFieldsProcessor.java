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

package com.liferay.portal.search.elasticsearch6.internal.document;

import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.document.DocumentBuilder;
import com.liferay.portal.search.document.DocumentBuilderFactory;
import com.liferay.portal.search.geolocation.GeoBuilders;
import com.liferay.portal.search.geolocation.GeoLocationPoint;

import java.util.Map;

import org.elasticsearch.common.document.DocumentField;

/**
 * @author Bryan Engler
 */
public class DocumentFieldsProcessor {

	public DocumentFieldsProcessor(
		DocumentBuilderFactory documentBuilderFactory,
		GeoBuilders geoBuilders) {

		_documentBuilderFactory = documentBuilderFactory;
		_geoBuilders = geoBuilders;
	}

	public Document process(
		Map<String, DocumentField> documentFieldsMap,
		String alternateUidFieldName) {

		DocumentBuilder documentBuilder = _documentBuilderFactory.builder();

		if (MapUtil.isNotEmpty(documentFieldsMap)) {
			documentFieldsMap.forEach(
				(fieldName, documentField) -> {
					String documentFieldName = documentField.getName();

					if (documentFieldName.endsWith(_GEOPOINT_SUFFIX)) {
						String[] values = StringUtil.split(
							documentField.getValue());

						GeoLocationPoint geoLocationPoint = null;

						if (values.length == 2) {
							geoLocationPoint = _geoBuilders.geoLocationPoint(
								Double.valueOf(values[0]),
								Double.valueOf(values[1]));
						}
						else {
							geoLocationPoint = _geoBuilders.geoLocationPoint(
								values[0]);
						}

						documentBuilder.setGeoLocationPoint(
							documentFieldName, geoLocationPoint);
					}
					else {
						documentBuilder.setValues(
							documentFieldName, documentField.getValues());
					}
				});

			populateUID(
				documentBuilder, alternateUidFieldName, documentFieldsMap);
		}

		return documentBuilder.build();
	}

	protected void populateUID(
		DocumentBuilder documentBuilder, String alternateUidFieldName,
		Map<String, DocumentField> documentFieldsMap) {

		if (documentFieldsMap.containsKey(_UID_FIELD_NAME)) {
			return;
		}

		if (Validator.isBlank(alternateUidFieldName)) {
			return;
		}

		DocumentField documentField = documentFieldsMap.get(
			alternateUidFieldName);

		if (documentField != null) {
			documentBuilder.setValues(
				_UID_FIELD_NAME, documentField.getValues());
		}
	}

	private static final String _GEOPOINT_SUFFIX = ".geopoint";

	private static final String _UID_FIELD_NAME = "uid";

	private final DocumentBuilderFactory _documentBuilderFactory;
	private final GeoBuilders _geoBuilders;

}