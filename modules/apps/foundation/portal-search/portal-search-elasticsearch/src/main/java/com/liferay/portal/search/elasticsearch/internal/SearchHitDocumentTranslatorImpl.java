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

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

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

		String baseFieldName = removeSuffixes(fieldName, ".lat", ".lon");

		if (document.hasField(baseFieldName)) {
			return;
		}

		DocumentField documentField = documentFields.get(baseFieldName);

		Field field = translateGeoPoint(
			documentField, documentFields.get(baseFieldName + ".lat"),
			documentFields.get(baseFieldName + ".lon"));

		if (field == null) {
			field = translate(documentField);
		}

		document.add(field);
	}

	protected String removeSuffixes(String fieldName, String... suffixes) {
		for (String suffix : suffixes) {
			fieldName = StringUtils.removeEnd(fieldName, suffix);
		}

		return fieldName;
	}

	protected Field translate(DocumentField documentField) {
		String name = documentField.getName();

		Collection<Object> values = documentField.getValues();

		Field field = new Field(
			name,
			ArrayUtil.toStringArray(values.toArray(new Object[values.size()])));

		return field;
	}

	protected Field translateGeoPoint(
		DocumentField documentField, DocumentField latDocumentField,
		DocumentField lonDocumentField) {

		if ((latDocumentField == null) || (lonDocumentField == null)) {
			return null;
		}

		Field field = new Field(documentField.getName());

		field.setGeoLocationPoint(
			new GeoLocationPoint(
				(Double)latDocumentField.getValue(),
				(Double)lonDocumentField.getValue()));

		return field;
	}

}