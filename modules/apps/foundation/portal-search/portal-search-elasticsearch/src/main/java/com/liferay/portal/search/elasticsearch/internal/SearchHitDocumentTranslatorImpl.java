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

package com.liferay.portal.search.elasticsearch.internal;

import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentImpl;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.geolocation.GeoLocationPoint;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.Collection;
import java.util.Map;

import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.search.SearchHit;

import org.osgi.service.component.annotations.Component;

/**
 * @author André de Oliveira
 */
@Component(immediate = true, service = SearchHitDocumentTranslator.class)
public class SearchHitDocumentTranslatorImpl
	implements SearchHitDocumentTranslator {

	@Override
	public Document translate(SearchHit searchHit) {
		Document document = new DocumentImpl();

		Map<String, DocumentField> documentFields = searchHit.getFields();

		for (String documentFieldName : documentFields.keySet()) {
			addField(document, documentFieldName, documentFields);
		}

		return document;
	}

	protected void addField(
		Document document, String fieldName,
		Map<String, DocumentField> documentFields) {

		if (fieldName.endsWith(".geopoint")) {
			return;
		}

		DocumentField documentField = documentFields.get(fieldName);

		DocumentField geoPointField = documentFields.get(
			fieldName.concat(".geopoint"));

		Field field;

		if (geoPointField != null) {
			field = translateGeoPoint(documentField);
		}
		else {
			field = translate(documentField);
		}

		document.add(field);
	}

	protected Field translate(DocumentField documentField) {
		String name = documentField.getName();

		Collection<Object> values = documentField.getValues();

		Field field = new Field(
			name,
			ArrayUtil.toStringArray(values.toArray(new Object[values.size()])));

		return field;
	}

	protected Field translateGeoPoint(DocumentField documentField) {
		Field field = new Field(documentField.getName());

		String[] values = StringUtil.split(documentField.getValue());

		field.setGeoLocationPoint(
			new GeoLocationPoint(
				Double.valueOf(values[0]), Double.valueOf(values[1])));

		return field;
	}

}