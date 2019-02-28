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

package com.liferay.dynamic.data.lists.search.test;

import com.liferay.dynamic.data.lists.helper.DDLRecordSetTestHelper;
import com.liferay.dynamic.data.lists.helper.DDLRecordTestHelper;
import com.liferay.dynamic.data.lists.model.DDLRecord;
import com.liferay.dynamic.data.lists.model.DDLRecordSet;
import com.liferay.dynamic.data.lists.service.DDLRecordSetLocalService;
import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.LocalizedValue;
import com.liferay.dynamic.data.mapping.model.Value;
import com.liferay.dynamic.data.mapping.storage.DDMFormFieldValue;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;
import com.liferay.dynamic.data.mapping.storage.StorageType;
import com.liferay.dynamic.data.mapping.test.util.DDMFormTestUtil;
import com.liferay.dynamic.data.mapping.test.util.DDMFormValuesTestUtil;
import com.liferay.dynamic.data.mapping.test.util.DDMStructureTestHelper;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.service.test.ServiceTestUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Luan Maoski
 * @author Lucas Marques
 */
public class DDLRecordFixture {

	public DDLRecordFixture(DDLRecordSetLocalService ddlRecordSetLocalService)
	{

		this.ddlRecordSetLocalService = ddlRecordSetLocalService;
	}

	public DDLRecord createDDLRecord() throws Exception {
		try {
			DDLRecord ddlRecord = addRecord("Joe Bloggs", "Simple description");

			_ddlRecords.add(ddlRecord);

			return ddlRecord;
		}
		catch (PortalException e)
		{
			throw new RuntimeException(e);
		}
	}

	public List<DDLRecord> getDdlRecords() {
		return _ddlRecords;
	}

	public Group getGroup() {
		return _group;
	}

	public ServiceContext getServiceContext() throws Exception {
		return ServiceContextTestUtil.getServiceContext(
			_group.getGroupId(), getUserId());
	}

	public void setGroup(Group group) {
		_group = group;
	}

	public void setUp() throws Exception {
		ServiceTestUtil.setUser(TestPropsValues.getUser());
		CompanyThreadLocal.setCompanyId(TestPropsValues.getCompanyId());

		setUpDdmStructureTestHelper();

		setUpDdlRecordTestHelper();
	}

	public void updateDisplaySettings(Locale locale) throws Exception {
		Group group = GroupTestUtil.updateDisplaySettings(
			_group.getGroupId(), null, locale);

		_group.setModelAttributes(group.getModelAttributes());
	}

	protected DDMStructure addDDMStructure() throws Exception {
		DDMStructure ddmStructure = ddmStructureTestHelper.addStructure(
			createDDMForm(_LOCALE), StorageType.JSON.toString());

		return ddmStructure;
	}

	protected DDLRecord addRecord(String name, String description)
		throws Exception {

		Map<Locale, String> nameMap = new HashMap<>();

		nameMap.put(_LOCALE, name);

		Map<Locale, String> descriptionMap = new HashMap<>();

		descriptionMap.put(_LOCALE, description);

		Locale[] locales = new Locale[nameMap.size()];

		nameMap.keySet().toArray(locales);

		DDMFormValues ddmFormValues = createDDMFormValues(locales);

		DDMFormFieldValue nameDDMFormFieldValue =
			createLocalizedDDMFormFieldValue("name", nameMap);

		ddmFormValues.addDDMFormFieldValue(nameDDMFormFieldValue);

		DDMFormFieldValue descriptionDDMFormFieldValue =
			createLocalizedDDMFormFieldValue("description", descriptionMap);

		ddmFormValues.addDDMFormFieldValue(descriptionDDMFormFieldValue);

		return recordTestHelper.addRecord(
			ddmFormValues, WorkflowConstants.ACTION_PUBLISH);
	}

	protected DDLRecordSet addRecordSet() throws Exception {
		DDLRecordSetTestHelper recordSetTestHelper = new DDLRecordSetTestHelper(
			_group);

		DDMStructure ddmStructure = ddmStructureTestHelper.addStructure(
			createDDMForm(_LOCALE), StorageType.JSON.toString());

		return recordSetTestHelper.addRecordSet(ddmStructure);
	}

	protected DDMForm createDDMForm(Locale... locales) {
		DDMForm ddmForm = DDMFormTestUtil.createDDMForm(
			DDMFormTestUtil.createAvailableLocales(locales), locales[0]);

		DDMFormField nameDDMFormField = DDMFormTestUtil.createTextDDMFormField(
			"name", true, false, false);

		nameDDMFormField.setIndexType("keyword");

		ddmForm.addDDMFormField(nameDDMFormField);

		DDMFormField descriptionDDMFormField =
			DDMFormTestUtil.createTextDDMFormField(
				"description", true, false, false);

		descriptionDDMFormField.setIndexType("text");

		ddmForm.addDDMFormField(descriptionDDMFormField);

		return ddmForm;
	}

	protected DDMFormValues createDDMFormValues(Locale... locales)
		throws Exception {

		DDLRecordSet recordSet = recordTestHelper.getRecordSet();

		DDMStructure ddmStructure = recordSet.getDDMStructure();

		return DDMFormValuesTestUtil.createDDMFormValues(
			ddmStructure.getDDMForm(),
			DDMFormValuesTestUtil.createAvailableLocales(locales), locales[0]);
	}

	protected DDMFormFieldValue createLocalizedDDMFormFieldValue(
		String name, Map<Locale, String> values) {

		Value localizedValue = new LocalizedValue(_LOCALE);

		for (Map.Entry<Locale, String> value : values.entrySet()) {
			localizedValue.addString(value.getKey(), value.getValue());
		}

		return DDMFormValuesTestUtil.createDDMFormFieldValue(
			name, localizedValue);
	}

	protected DDLRecordSet getDdlRecordSet(long recordSetId)
		throws PortalException {

		return ddlRecordSetLocalService.getRecordSet(recordSetId);
	}

	protected long getUserId() throws Exception {
		return TestPropsValues.getUserId();
	}

	protected void setUpDdlRecordTestHelper() throws Exception {
		DDLRecordSet recordSet = addRecordSet();

		recordTestHelper = new DDLRecordTestHelper(_group, recordSet);
	}

	protected void setUpDdmStructureTestHelper() throws Exception {
		ddmStructureTestHelper = new DDMStructureTestHelper(
			PortalUtil.getClassNameId(DDLRecordSet.class), _group);
	}

	protected DDLRecordSetLocalService ddlRecordSetLocalService;
	protected DDMStructureTestHelper ddmStructureTestHelper;
	protected DDLRecordTestHelper recordTestHelper;

	private static final Locale _LOCALE = LocaleUtil.US;

	private final List<DDLRecord> _ddlRecords = new ArrayList<>();
	private Group _group;

}