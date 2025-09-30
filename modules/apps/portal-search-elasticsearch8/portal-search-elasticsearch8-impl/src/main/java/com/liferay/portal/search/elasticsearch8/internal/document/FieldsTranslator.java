/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch8.internal.document;

import co.elastic.clients.json.JsonData;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.document.DocumentBuilder;
import com.liferay.portal.search.elasticsearch8.internal.util.JsonpUtil;
import com.liferay.portal.search.geolocation.GeoBuilders;
import com.liferay.portal.search.geolocation.GeoLocationPoint;

import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.geo.GeoPoint;

/**
 * @author Bryan Engler
 */
public class FieldsTranslator {

	public FieldsTranslator(GeoBuilders geoBuilders) {
		_geoBuilders = geoBuilders;
	}

	public void populateAlternateUID(
		Map<String, DocumentField> documentFieldsMap,
		DocumentBuilder documentBuilder, String alternateUidFieldName) {

		if (MapUtil.isEmpty(documentFieldsMap) ||
			documentFieldsMap.containsKey(_UID_FIELD_NAME) ||
			Validator.isBlank(alternateUidFieldName)) {

			return;
		}

		DocumentField documentField = documentFieldsMap.get(
			alternateUidFieldName);

		if (documentField != null) {
			documentBuilder.setValues(
				_UID_FIELD_NAME, documentField.getValues());
		}
	}

	public void translate(
		DocumentBuilder documentBuilder,
		Map<String, Object> documentSourceMap) {

		if (MapUtil.isEmpty(documentSourceMap)) {
			return;
		}

		documentSourceMap.forEach(
			(name, value) -> translate(name, value, documentBuilder));
	}

	public void translate(
		Map<String, DocumentField> documentFieldsMap,
		DocumentBuilder documentBuilder) {

		if (MapUtil.isEmpty(documentFieldsMap)) {
			return;
		}

		documentFieldsMap.forEach(
			(name, documentField) -> translate(
				documentField, documentBuilder, documentFieldsMap));
	}

	public void translateSource(
		DocumentBuilder documentBuilder, JsonData jsonData) {

		if (jsonData == null) {
			return;
		}

		JsonValue jsonValue = jsonData.toJson(JsonpUtil.getJsonpMapper());

		JsonObject jsonObject = jsonValue.asJsonObject();

		jsonObject.forEach(
			(fieldName, value) -> translateSourceField(
				documentBuilder, fieldName, value));
	}

	protected void translate(
		DocumentField documentField, DocumentBuilder documentBuilder,
		Map<String, DocumentField> documentFieldsMap) {

		if (_translateGeoLocationPoint(
				documentField, documentBuilder, documentFieldsMap)) {

			return;
		}

		documentBuilder.setValues(
			documentField.getName(), documentField.getValues());
	}

	protected void translate(
		String name, Object value, DocumentBuilder documentBuilder) {

		if (name.endsWith(_GEOPOINT_SUFFIX)) {
			documentBuilder.setGeoLocationPoint(
				name, _geoBuilders.geoLocationPoint((String)value));
		}
		else {
			if (value instanceof Collection) {
				documentBuilder.setValues(name, (Collection)value);
			}
			else {
				documentBuilder.setValue(name, value);
			}
		}
	}

	protected void translateSourceField(
		DocumentBuilder documentBuilder, String fieldName,
		JsonValue jsonValue) {

		if (fieldName.endsWith(_GEOPOINT_SUFFIX)) {
			documentBuilder.setGeoLocationPoint(
				fieldName, _geoBuilders.geoLocationPoint(jsonValue.toString()));
		}
		else {
			JsonValue.ValueType valueType = jsonValue.getValueType();

			if ((valueType == JsonValue.ValueType.ARRAY) ||
				(valueType == JsonValue.ValueType.OBJECT)) {

				documentBuilder.setValues(
					fieldName, _toCollectionValue(jsonValue));
			}
			else {
				documentBuilder.setValue(fieldName, _toSingleValue(jsonValue));
			}
		}
	}

	private GeoLocationPoint _getGeoLocationPoint(
		DocumentField documentField1, DocumentField documentField2) {

		Object value1 = documentField1.getValue();
		String value2 = documentField2.getValue();

		if (StringUtil.startsWith(value2, StringPool.OPEN_CURLY_BRACE) &&
			(value1 instanceof Map)) {

			return _getGeoLocationPoint((Map<String, Object>)value1);
		}

		GeoPoint geoPoint = GeoPoint.fromGeohash(value2);

		return _geoBuilders.geoLocationPoint(
			geoPoint.getLat(), geoPoint.getLon());
	}

	private GeoLocationPoint _getGeoLocationPoint(Map<String, Object> map) {
		if (MapUtil.isEmpty(map) || !map.containsKey("coordinates")) {
			return null;
		}

		List<Double> list = (List<Double>)map.get("coordinates");

		return _geoBuilders.geoLocationPoint(list.get(1), list.get(0));
	}

	private Collection<Object> _toCollectionValue(JsonValue jsonValue) {
		List<Object> values = new ArrayList<>();

		JsonValue.ValueType valueType = jsonValue.getValueType();

		if (valueType == JsonValue.ValueType.ARRAY) {
			JsonArray jsonArray = jsonValue.asJsonArray();

			jsonArray.forEach(value -> values.add(_toSingleValue(value)));
		}
		else {
			values.add(_toSingleValue(jsonValue));
		}

		return values;
	}

	private Map<String, Object> _toMap(JsonObject jsonObject) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();

			TypeReference<HashMap<String, Object>> typeReference =
				new TypeReference<HashMap<String, Object>>() {
				};

			return objectMapper.readValue(jsonObject.toString(), typeReference);
		}
		catch (JsonProcessingException jsonProcessingException) {
			throw new RuntimeException(jsonProcessingException);
		}
	}

	private Object _toSingleValue(JsonValue jsonValue) {
		JsonValue.ValueType valueType = jsonValue.getValueType();

		if ((valueType == JsonValue.ValueType.FALSE) ||
			(valueType == JsonValue.ValueType.TRUE)) {

			return Boolean.valueOf(jsonValue.toString());
		}
		else if (valueType == JsonValue.ValueType.NULL) {
			return null;
		}
		else if (valueType == JsonValue.ValueType.NUMBER) {
			JsonNumber jsonNumber = (JsonNumber)jsonValue;

			return jsonNumber.numberValue();
		}
		else if (valueType == JsonValue.ValueType.OBJECT) {
			return _toMap((JsonObject)jsonValue);
		}
		else if (valueType == JsonValue.ValueType.STRING) {
			JsonString jsonString = (JsonString)jsonValue;

			return jsonString.getString();
		}

		return jsonValue.toString();
	}

	private boolean _translateGeoLocationPoint(
		DocumentField documentField1, DocumentBuilder documentBuilder,
		Map<String, DocumentField> documentFieldsMap) {

		String fieldName1 = documentField1.getName();

		if (fieldName1.endsWith(_GEOPOINT_SUFFIX)) {
			return true;
		}

		String fieldName2 = fieldName1.concat(_GEOPOINT_SUFFIX);

		DocumentField documentField2 = documentFieldsMap.get(fieldName2);

		if (documentField2 == null) {
			return false;
		}

		documentBuilder.setGeoLocationPoint(
			fieldName1, _getGeoLocationPoint(documentField1, documentField2));

		return true;
	}

	private static final String _GEOPOINT_SUFFIX = ".geopoint";

	private static final String _UID_FIELD_NAME = "uid";

	private final GeoBuilders _geoBuilders;

}