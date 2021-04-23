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
import com.liferay.object.exception.NoSuchEntryException;
import com.liferay.object.exception.ObjectEntryValuesException;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.object.service.ObjectFieldLocalServiceUtil;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.kernel.service.ServiceContext;
import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.liferay.portal.kernel.search.BaseModelSearchResult;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Marco Leo
 * @author Brian Wing Shun Chan
 */
@RunWith(Arquillian.class)
public class ObjectEntryLocalServiceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_objectDefinition =
			ObjectDefinitionLocalServiceUtil.addObjectDefinition(
				TestPropsValues.getUserId(), "Test",
				Arrays.asList(
					_createObjectField("ageOfDeath", "Long"),
					_createObjectField("authorOfGospel", "Boolean"),
					_createObjectField("birthday", "Date"),
					_createObjectField("emailAddress", "String"),
					_createObjectField("firstName", "String"),
					_createObjectField("height", "Double"),
					_createObjectField("lastName", "String"),
					_createObjectField("middleName", "String"),
					_createObjectField("numberOfBooksWritten", "Integer"),
					_createObjectField("portrait", "Blob"),
					_createObjectField("speed", "BigDecimal"),
					_createObjectField("weight", "Double")));
	}

	@Test
	public void testAddObjectEntry() throws Exception {
		_assertCount(0);

		ObjectEntryLocalServiceUtil.addObjectEntry(
			TestPropsValues.getUserId(), TestPropsValues.getGroupId(),
			_objectDefinition.getObjectDefinitionId(),
			HashMapBuilder.<String, Object>put(
				"emailAddress", "peter@liferay.com"
			).put(
				"firstName", "Peter"
			).build());

		_assertCount(1);

		ObjectEntryLocalServiceUtil.addObjectEntry(
			TestPropsValues.getUserId(), TestPropsValues.getGroupId(),
			_objectDefinition.getObjectDefinitionId(),
			HashMapBuilder.<String, Object>put(
				"emailAddress", "james@liferay.com"
			).put(
				"firstName", "James"
			).build());

		_assertCount(2);

		ObjectEntryLocalServiceUtil.addObjectEntry(
			TestPropsValues.getUserId(), TestPropsValues.getGroupId(),
			_objectDefinition.getObjectDefinitionId(),
			HashMapBuilder.<String, Object>put(
				"emailAddress", "john@liferay.com"
			).put(
				"firstName", "John"
			).build());

		_assertCount(3);
	}

	@Test
	public void testDeleteObjectEntry() throws Exception {
		ObjectEntry objectEntry1 = ObjectEntryLocalServiceUtil.addObjectEntry(
			TestPropsValues.getUserId(), TestPropsValues.getGroupId(),
			_objectDefinition.getObjectDefinitionId(),
			HashMapBuilder.<String, Object>put(
				"emailAddress", "peter@liferay.com"
			).put(
				"firstName", "Peter"
			).build());
		ObjectEntry objectEntry2 = ObjectEntryLocalServiceUtil.addObjectEntry(
			TestPropsValues.getUserId(), TestPropsValues.getGroupId(),
			_objectDefinition.getObjectDefinitionId(),
			HashMapBuilder.<String, Object>put(
				"emailAddress", "james@liferay.com"
			).put(
				"firstName", "James"
			).build());
		ObjectEntry objectEntry3 = ObjectEntryLocalServiceUtil.addObjectEntry(
			TestPropsValues.getUserId(), TestPropsValues.getGroupId(),
			_objectDefinition.getObjectDefinitionId(),
			HashMapBuilder.<String, Object>put(
				"emailAddress", "john@liferay.com"
			).put(
				"firstName", "John"
			).build());

		_assertCount(3);

		ObjectEntryLocalServiceUtil.deleteObjectEntry(
			objectEntry1.getObjectEntryId());

		try {
			ObjectEntryLocalServiceUtil.deleteObjectEntry(
				objectEntry1.getObjectEntryId());

			Assert.fail();
		}
		catch (NoSuchEntryException noSuchEntryException) {
			Assert.assertEquals(
				"No ObjectEntry exists with the primary key " +
					objectEntry1.getObjectEntryId(),
				noSuchEntryException.getMessage());
		}

		try {
			ObjectEntryLocalServiceUtil.deleteObjectEntry(objectEntry1);

			Assert.fail();
		}
		catch (NullPointerException nullPointerException) {
			Assert.assertNotNull(nullPointerException);
		}

		try {
			ObjectEntryLocalServiceUtil.getObjectEntryValues(
				objectEntry1.getObjectEntryId());

			Assert.fail();
		}
		catch (NoSuchEntryException noSuchEntryException) {
			Assert.assertEquals(
				"No ObjectEntry exists with the primary key " +
					objectEntry1.getObjectEntryId(),
				noSuchEntryException.getMessage());
		}

		_assertCount(2);

		ObjectEntryLocalServiceUtil.deleteObjectEntry(
			objectEntry2.getObjectEntryId());

		_assertCount(1);

		ObjectEntryLocalServiceUtil.deleteObjectEntry(objectEntry3);

		_assertCount(0);
	}

	@Test
	public void testGetObjectEntry() throws Exception {
		ObjectEntry objectEntry = ObjectEntryLocalServiceUtil.addObjectEntry(
			TestPropsValues.getUserId(), TestPropsValues.getGroupId(),
			_objectDefinition.getObjectDefinitionId(),
			HashMapBuilder.<String, Object>put(
				"emailAddress", "john@liferay.com"
			).put(
				"firstName", "John"
			).build());

		Map<String, Object> values =
			ObjectEntryLocalServiceUtil.getObjectEntryValues(
				objectEntry.getObjectEntryId());

		Assert.assertEquals(0L, values.get("ageOfDeath"));
		Assert.assertEquals(false, values.get("authorOfGospel"));
		Assert.assertEquals(null, values.get("birthday"));
		Assert.assertEquals("john@liferay.com", values.get("emailAddress"));
		Assert.assertEquals("John", values.get("firstName"));
		Assert.assertEquals(0D, values.get("height"));
		Assert.assertEquals(null, values.get("lastName"));
		Assert.assertEquals(null, values.get("middleName"));
		Assert.assertEquals(0, values.get("numberOfBooksWritten"));
		Assert.assertEquals(0L, values.get("mvccVersion"));
		Assert.assertEquals(null, values.get("portrait"));
		Assert.assertEquals(null, values.get("speed"));
		Assert.assertEquals(0D, values.get("weight"));
		Assert.assertEquals(
			objectEntry.getObjectEntryId(), values.get("testId"));
		Assert.assertEquals(values.toString(), 14, values.size());

		try {
			ObjectEntryLocalServiceUtil.getObjectEntryValues(0);

			Assert.fail();
		}
		catch (NoSuchEntryException noSuchEntryException) {
			Assert.assertEquals(
				"No ObjectEntry exists with the primary key 0",
				noSuchEntryException.getMessage());
		}
	}

	@Test
	public void testGetObjectEntriesValuesList() throws Exception {
		List<Map<String, Object>> objectEntriesValuesList =
			ObjectEntryLocalServiceUtil.getObjectEntriesValuesList(
				_objectDefinition.getObjectDefinitionId(), null,
				QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		Assert.assertEquals(0, objectEntriesValuesList.size());

		_assertCount(0);

		ObjectEntry objectEntry1 = ObjectEntryLocalServiceUtil.addObjectEntry(
			TestPropsValues.getUserId(), TestPropsValues.getGroupId(),
			_objectDefinition.getObjectDefinitionId(),
			HashMapBuilder.<String, Object>put(
				"emailAddress", "peter@liferay.com"
			).put(
				"firstName", "Peter"
			).build());

		objectEntriesValuesList =
			ObjectEntryLocalServiceUtil.getObjectEntriesValuesList(
				_objectDefinition.getObjectDefinitionId(), null,
				QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		Assert.assertEquals(1, objectEntriesValuesList.size());

		_assertCount(1);

		Map<String, Object> values = objectEntriesValuesList.get(0);

		Assert.assertEquals("peter@liferay.com", values.get("emailAddress"));
		Assert.assertEquals("Peter", values.get("firstName"));
		Assert.assertEquals(14, values.size());

		ObjectEntry objectEntry2 = ObjectEntryLocalServiceUtil.addObjectEntry(
			TestPropsValues.getUserId(), TestPropsValues.getGroupId(),
			_objectDefinition.getObjectDefinitionId(),
			HashMapBuilder.<String, Object>put(
				"emailAddress", "james@liferay.com"
			).put(
				"firstName", "James"
			).build());

		objectEntriesValuesList =
			ObjectEntryLocalServiceUtil.getObjectEntriesValuesList(
				_objectDefinition.getObjectDefinitionId(), null,
				QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		Assert.assertEquals(2, objectEntriesValuesList.size());

		_assertCount(2);

		values = objectEntriesValuesList.get(0);

		Assert.assertEquals("peter@liferay.com", values.get("emailAddress"));
		Assert.assertEquals("Peter", values.get("firstName"));
		Assert.assertEquals(14, values.size());

		values = objectEntriesValuesList.get(1);

		Assert.assertEquals("james@liferay.com", values.get("emailAddress"));
		Assert.assertEquals("James", values.get("firstName"));
		Assert.assertEquals(14, values.size());

		ObjectEntry objectEntry3 = ObjectEntryLocalServiceUtil.addObjectEntry(
			TestPropsValues.getUserId(), TestPropsValues.getGroupId(),
			_objectDefinition.getObjectDefinitionId(),
			HashMapBuilder.<String, Object>put(
				"emailAddress", "john@liferay.com"
			).put(
				"firstName", "John"
			).build());

		objectEntriesValuesList =
			ObjectEntryLocalServiceUtil.getObjectEntriesValuesList(
				_objectDefinition.getObjectDefinitionId(), null,
				QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		Assert.assertEquals(3, objectEntriesValuesList.size());

		_assertCount(3);

		values = objectEntriesValuesList.get(0);

		Assert.assertEquals(0L, values.get("ageOfDeath"));
		Assert.assertEquals(false, values.get("authorOfGospel"));
		Assert.assertEquals(null, values.get("birthday"));
		Assert.assertEquals("peter@liferay.com", values.get("emailAddress"));
		Assert.assertEquals("Peter", values.get("firstName"));
		Assert.assertEquals(0D, values.get("height"));
		Assert.assertEquals(null, values.get("lastName"));
		Assert.assertEquals(null, values.get("middleName"));
		Assert.assertEquals(0, values.get("numberOfBooksWritten"));
		Assert.assertEquals(0L, values.get("mvccVersion"));
		Assert.assertEquals(null, values.get("portrait"));
		Assert.assertEquals(null, values.get("speed"));
		Assert.assertEquals(
			objectEntry1.getObjectEntryId(), values.get("testId"));
		Assert.assertEquals(0D, values.get("weight"));
		Assert.assertEquals(14, values.size());

		values = objectEntriesValuesList.get(1);

		Assert.assertEquals(0L, values.get("ageOfDeath"));
		Assert.assertEquals(false, values.get("authorOfGospel"));
		Assert.assertEquals(null, values.get("birthday"));
		Assert.assertEquals("james@liferay.com", values.get("emailAddress"));
		Assert.assertEquals("James", values.get("firstName"));
		Assert.assertEquals(0D, values.get("height"));
		Assert.assertEquals(null, values.get("lastName"));
		Assert.assertEquals(null, values.get("middleName"));
		Assert.assertEquals(0, values.get("numberOfBooksWritten"));
		Assert.assertEquals(0L, values.get("mvccVersion"));
		Assert.assertEquals(null, values.get("portrait"));
		Assert.assertEquals(null, values.get("speed"));
		Assert.assertEquals(
			objectEntry2.getObjectEntryId(), values.get("testId"));
		Assert.assertEquals(0D, values.get("weight"));
		Assert.assertEquals(
			objectEntry2.getObjectEntryId(), values.get("testId"));
		Assert.assertEquals(14, values.size());

		values = objectEntriesValuesList.get(2);

		Assert.assertEquals(0L, values.get("ageOfDeath"));
		Assert.assertEquals(false, values.get("authorOfGospel"));
		Assert.assertEquals(null, values.get("birthday"));
		Assert.assertEquals("john@liferay.com", values.get("emailAddress"));
		Assert.assertEquals("John", values.get("firstName"));
		Assert.assertEquals(0D, values.get("height"));
		Assert.assertEquals(null, values.get("lastName"));
		Assert.assertEquals(null, values.get("middleName"));
		Assert.assertEquals(0, values.get("numberOfBooksWritten"));
		Assert.assertEquals(0L, values.get("mvccVersion"));
		Assert.assertEquals(null, values.get("portrait"));
		Assert.assertEquals(null, values.get("speed"));
		Assert.assertEquals(
			objectEntry3.getObjectEntryId(), values.get("testId"));
		Assert.assertEquals(0D, values.get("weight"));
		Assert.assertEquals(14, values.size());
	}

	@Test
	public void testSearchObjectEntries() throws Exception {
		System.out.println("\n\n\n");

		BaseModelSearchResult<ObjectEntry> baseModelSearchResult =
			ObjectEntryLocalServiceUtil.searchObjectEntries(
				_objectDefinition.getObjectDefinitionId(), null, null,
				1, 20, null, false);

		//Assert.assertEquals(0, baseModelSearchResult.getLength());
		System.out.println("## testSearchObjectEntries a " + baseModelSearchResult.getLength());

		_assertCount(0);

		ObjectEntryLocalServiceUtil.addObjectEntry(
			TestPropsValues.getUserId(), TestPropsValues.getGroupId(),
			_objectDefinition.getObjectDefinitionId(),
			HashMapBuilder.<String, Object>put(
				"emailAddress", "peter@liferay.com"
			).put(
				"firstName", "Peter"
			).build());

		baseModelSearchResult =
			ObjectEntryLocalServiceUtil.searchObjectEntries(
				_objectDefinition.getObjectDefinitionId(), null, null,
				1, 20, null, false);

		//Assert.assertEquals(1, baseModelSearchResult.getLength());
		System.out.println("## testSearchObjectEntries b " + baseModelSearchResult.getLength());

		_assertCount(1);
	}

	@Test
	public void testUpdateObjectEntry() throws Exception {
		_assertCount(0);

		ObjectEntry objectEntry = ObjectEntryLocalServiceUtil.addObjectEntry(
			TestPropsValues.getUserId(), TestPropsValues.getGroupId(),
			_objectDefinition.getObjectDefinitionId(),
			HashMapBuilder.<String, Object>put(
				"emailAddress", "john@liferay.com"
			).put(
				"firstName", "John"
			).build());

		_assertCount(1);

		ObjectEntryLocalServiceUtil.updateObjectEntry(
			objectEntry.getObjectEntryId(),
			HashMapBuilder.<String, Object>put(
				"firstName", "João"
			).put(
				"lastName", "o Discípulo Amado"
			).build());

		_assertCount(1);

		Map<String, Object> values =
			ObjectEntryLocalServiceUtil.getObjectEntryValues(
				objectEntry.getObjectEntryId());

		Assert.assertEquals(0L, values.get("ageOfDeath"));
		Assert.assertEquals(false, values.get("authorOfGospel"));
		Assert.assertEquals(null, values.get("birthday"));
		Assert.assertEquals("john@liferay.com", values.get("emailAddress"));
		Assert.assertEquals("João", values.get("firstName"));
		Assert.assertEquals(0D, values.get("height"));
		Assert.assertEquals("o Discípulo Amado", values.get("lastName"));
		Assert.assertEquals(null, values.get("middleName"));
		Assert.assertEquals(0, values.get("numberOfBooksWritten"));
		Assert.assertEquals(0L, values.get("mvccVersion"));
		Assert.assertEquals(null, values.get("portrait"));
		Assert.assertEquals(null, values.get("speed"));
		Assert.assertEquals(0D, values.get("weight"));
		Assert.assertEquals(
			objectEntry.getObjectEntryId(), values.get("testId"));
		Assert.assertEquals(values.toString(), 14, values.size());

		Calendar calendar = new GregorianCalendar();

		calendar.set(6, Calendar.DECEMBER, 27);

		Date birthdayDate = calendar.getTime();

		String portrait = "In the beginning was the Logos";

		ObjectEntryLocalServiceUtil.updateObjectEntry(
			objectEntry.getObjectEntryId(),
			HashMapBuilder.<String, Object>put(
				"ageOfDeath", "94"
			).put(
				"authorOfGospel", true
			).put(
				"birthday", birthdayDate
			).put(
				"height", 180
			).put(
				"numberOfBooksWritten", 5D
			).put(
				"portrait", portrait.getBytes()
			).put(
				"speed", BigDecimal.valueOf(45L)
			).put(
				"weight", 60
			).build());

		_assertCount(1);

		values = ObjectEntryLocalServiceUtil.getObjectEntryValues(
			objectEntry.getObjectEntryId());

		Assert.assertEquals(94L, values.get("ageOfDeath"));
		Assert.assertEquals(true, values.get("authorOfGospel"));
		Assert.assertEquals(birthdayDate, values.get("birthday"));
		Assert.assertEquals("john@liferay.com", values.get("emailAddress"));
		Assert.assertEquals("João", values.get("firstName"));
		Assert.assertEquals(180D, values.get("height"));
		Assert.assertEquals("o Discípulo Amado", values.get("lastName"));
		Assert.assertEquals(null, values.get("middleName"));
		Assert.assertEquals(5, values.get("numberOfBooksWritten"));
		Assert.assertEquals(0L, values.get("mvccVersion"));
		Assert.assertArrayEquals(
			portrait.getBytes(), (byte[])values.get("portrait"));
		Assert.assertEquals(_getBigDecimal(45L), values.get("speed"));
		Assert.assertEquals(60D, values.get("weight"));
		Assert.assertEquals(
			objectEntry.getObjectEntryId(), values.get("testId"));
		Assert.assertEquals(values.toString(), 14, values.size());

		ObjectEntryLocalServiceUtil.updateObjectEntry(
			objectEntry.getObjectEntryId(),
			HashMapBuilder.<String, Object>put(
				"weight", 65D
			).build());

		_assertCount(1);

		values = ObjectEntryLocalServiceUtil.getObjectEntryValues(
			objectEntry.getObjectEntryId());

		Assert.assertEquals(94L, values.get("ageOfDeath"));
		Assert.assertEquals(true, values.get("authorOfGospel"));
		Assert.assertEquals(birthdayDate, values.get("birthday"));
		Assert.assertEquals("john@liferay.com", values.get("emailAddress"));
		Assert.assertEquals("João", values.get("firstName"));
		Assert.assertEquals(180D, values.get("height"));
		Assert.assertEquals("o Discípulo Amado", values.get("lastName"));
		Assert.assertEquals(null, values.get("middleName"));
		Assert.assertEquals(5, values.get("numberOfBooksWritten"));
		Assert.assertEquals(0L, values.get("mvccVersion"));
		Assert.assertArrayEquals(
			portrait.getBytes(), (byte[])values.get("portrait"));
		Assert.assertEquals(_getBigDecimal(45L), values.get("speed"));
		Assert.assertEquals(65D, values.get("weight"));
		Assert.assertEquals(
			objectEntry.getObjectEntryId(), values.get("testId"));
		Assert.assertEquals(values.toString(), 14, values.size());

		try {
			ObjectEntryLocalServiceUtil.updateObjectEntry(
				objectEntry.getObjectEntryId(), new HashMap<String, Object>());

			Assert.fail();
		}
		catch (ObjectEntryValuesException objectEntryValuesException) {
			Assert.assertEquals(
				"No values were provided for object definition " +
					_objectDefinition.getObjectDefinitionId(),
				objectEntryValuesException.getMessage());
		}

		try {
			ObjectEntryLocalServiceUtil.updateObjectEntry(
				objectEntry.getObjectEntryId(),
				HashMapBuilder.<String, Object>put(
					"invalidName", ""
				).put(
					"testId", ""
				).build());

			Assert.fail();
		}
		catch (ObjectEntryValuesException objectEntryValuesException) {
			Assert.assertEquals(
				"No values were provided for object definition " +
					_objectDefinition.getObjectDefinitionId(),
				objectEntryValuesException.getMessage());
		}
	}

	private void _assertCount(int count) throws Exception {
		Assert.assertEquals(
			count,
			ObjectEntryLocalServiceUtil.getObjectEntriesCount(
				_objectDefinition.getObjectDefinitionId()));
		Assert.assertEquals(count, _count());
	}

	private int _count() throws Exception {
		try (Connection connection = DataAccess.getConnection();
			PreparedStatement preparedStatement = connection.prepareStatement(
				"select count(*) from " + _objectDefinition.getDBTableName());
			ResultSet resultSet = preparedStatement.executeQuery()) {

			resultSet.next();

			return resultSet.getInt(1);
		}
	}

	private ObjectField _createObjectField(String name, String type) {
		ObjectField objectField = ObjectFieldLocalServiceUtil.createObjectField(
			0);

		objectField.setName(name);
		objectField.setType(type);

		return objectField;
	}

	private BigDecimal _getBigDecimal(long value) {
		BigDecimal bigDecimal = BigDecimal.valueOf(value);

		return bigDecimal.setScale(16);
	}

	@DeleteAfterTestRun
	private ObjectDefinition _objectDefinition;

}