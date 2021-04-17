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

package com.liferay.object.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.object.exception.DuplicateObjectFieldException;
import com.liferay.object.exception.ObjectFieldNameException;
import com.liferay.object.exception.ObjectFieldTypeException;
import com.liferay.object.exception.ReservedObjectFieldException;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.service.ObjectFieldLocalServiceUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Marco Leo
 * @author Brian Wing Shun Chan
 */
@RunWith(Arquillian.class)
public class ObjectFieldLocalServiceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testAddObjectField() throws Exception {

		// Name is null

		try {
			_testAddObjectFields(_createObjectField("", "String"));

			Assert.fail();
		}
		catch (ObjectFieldNameException objectFieldNameException) {
			Assert.assertEquals(
				"Name is null", objectFieldNameException.getMessage());
		}

		// Name must only contain letters and digits

		_testAddObjectFields(_createObjectField(" able ", "String"));

		try {
			_testAddObjectFields(_createObjectField("abl e", "String"));

			Assert.fail();
		}
		catch (ObjectFieldNameException objectFieldNameException) {
			Assert.assertEquals(
				"Name must only contain letters and digits",
				objectFieldNameException.getMessage());
		}

		try {
			_testAddObjectFields(_createObjectField("abl-e", "String"));

			Assert.fail();
		}
		catch (ObjectFieldNameException objectFieldNameException) {
			Assert.assertEquals(
				"Name must only contain letters and digits",
				objectFieldNameException.getMessage());
		}

		// First character

		try {
			_testAddObjectFields(_createObjectField("Able", "String"));

			Assert.fail();
		}
		catch (ObjectFieldNameException objectFieldNameException) {
			Assert.assertEquals(
				"The first character of a name must be a lower case letter",
				objectFieldNameException.getMessage());
		}

		// Name has a 40 character limit

		_testAddObjectFields(
			_createObjectField(
				"a123456789a123456789a123456789a1234567891", "String"));

		try {
			_testAddObjectFields(
				_createObjectField(
					"a123456789a123456789a123456789a12345678912", "String"));

			Assert.fail();
		}
		catch (ObjectFieldNameException objectFieldNameException) {
			Assert.assertEquals(
				"Names must be less than 41 characters",
				objectFieldNameException.getMessage());
		}

		// Name is reserved

		String[] reservedNames = {
			"companyId", "createDate", "groupId", "id", "lastPublishDate",
			"modifiedDate", "status", "statusByUserId", "statusByUserName",
			"statusDate", "userId", "userName"
		};

		for (String reservedName : reservedNames) {
			try {
				_testAddObjectFields(
					_createObjectField(reservedName, "String"));

				Assert.fail();
			}
			catch (ReservedObjectFieldException reservedObjectFieldException) {
				Assert.assertEquals(
					"Reserved name " + reservedName,
					reservedObjectFieldException.getMessage());
			}
		}

		// Name is the primary key

		try {
			_testAddObjectFields(_createObjectField("testId", "String"));

			Assert.fail();
		}
		catch (ReservedObjectFieldException reservedObjectFieldException) {
			Assert.assertEquals(
				"Reserved name testId",
				reservedObjectFieldException.getMessage());
		}

		try {
			_testAddObjectFields(_createObjectField("testid", "String"));

			Assert.fail();
		}
		catch (ReservedObjectFieldException reservedObjectFieldException) {
			Assert.assertEquals(
				"Reserved name testid",
				reservedObjectFieldException.getMessage());
		}

		// Name is a duplicate

		try {
			_testAddObjectFields(
				_createObjectField("able", "String"),
				_createObjectField("able", "String"));

			Assert.fail();
		}
		catch (DuplicateObjectFieldException duplicateObjectFieldException) {
			Assert.assertEquals(
				"Duplicate name able",
				duplicateObjectFieldException.getMessage());
		}

		// Types

		String[] types = {
			"BigDecimal", "Blob", "Boolean", "Date", "Double", "Integer",
			"Long", "String"
		};

		for (String type : types) {
			_testAddObjectFields(_createObjectField("able", type));
		}

		try {
			_testAddObjectFields(_createObjectField("able", "STRING"));

			Assert.fail();
		}
		catch (ObjectFieldTypeException objectFieldTypeException) {
			Assert.assertEquals(
				"Invalid type STRING", objectFieldTypeException.getMessage());
		}
	}

	private ObjectField _createObjectField(String name, String type) {
		ObjectField objectField = ObjectFieldLocalServiceUtil.createObjectField(
			0);

		objectField.setName(name);
		objectField.setType(type);

		return objectField;
	}

	private void _testAddObjectFields(ObjectField... objectFields)
		throws Exception {

		ObjectDefinition objectDefinition = null;

		try {
			objectDefinition =
				ObjectDefinitionLocalServiceUtil.addObjectDefinition(
					TestPropsValues.getUserId(), "Test",
					Arrays.asList(objectFields));
		}
		finally {
			if (objectDefinition != null) {
				ObjectDefinitionLocalServiceUtil.deleteObjectDefinition(
					objectDefinition);
			}
		}
	}

}