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
import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.storage.StorageType;
import com.liferay.dynamic.data.mapping.test.util.DDMFormTestUtil;
import com.liferay.dynamic.data.mapping.test.util.DDMStructureTestHelper;
import com.liferay.dynamic.data.mapping.util.DDMIndexer;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistry;
import com.liferay.portal.kernel.search.SearchEngineHelper;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.search.test.util.IndexedFieldsFixture;
import com.liferay.portal.test.rule.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Luan Maoski
 * @author Lucas Marques
 */
public abstract class BaseDDLRecordTestCase {

	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		ddmStructure = _addDDMStructure();

		DDLRecordSet recordSet = _addRecordSet();

		_recordTestHelper = new DDLRecordTestHelper(_group, recordSet);

		ddlRecordFixture = createDDLRecordFixture();

		ddlRecordFixture.setUp();

		setGroup(ddlRecordFixture.addGroup());

		ddlRecordIndexerFixture = createDDLRecordIndexerFixture();

		indexedFieldsFixture = createIndexedFieldsFixture();
	}

	protected DDLRecordFixture createDDLRecordFixture() {
		return new DDLRecordFixture(_recordTestHelper, _groups, _ddlRecords);
	}

	protected DDLRecordIndexerFixture createDDLRecordIndexerFixture() {
		Indexer<DDLRecord> indexer = indexerRegistry.nullSafeGetIndexer(
			DDLRecord.class);

		return new DDLRecordIndexerFixture(indexer);
	}

	protected IndexedFieldsFixture createIndexedFieldsFixture() {
		return new IndexedFieldsFixture(
			resourcePermissionLocalService, searchEngineHelper);
	}

	protected void setGroup(Group group) {
		ddlRecordFixture.setGroup(group);
	}

	protected DDLRecordFixture ddlRecordFixture;
	protected DDLRecordIndexerFixture ddlRecordIndexerFixture;

	@Inject
	protected DDMIndexer ddmIndexer;

	protected DDMStructure ddmStructure;
	protected IndexedFieldsFixture indexedFieldsFixture;

	@Inject
	protected IndexerRegistry indexerRegistry;

	@Inject
	protected ResourcePermissionLocalService resourcePermissionLocalService;

	@Inject
	protected SearchEngineHelper searchEngineHelper;

	private DDMStructure _addDDMStructure() throws Exception {
		DDMStructureTestHelper ddmStructureTestHelper =
			new DDMStructureTestHelper(
				PortalUtil.getClassNameId(DDLRecordSet.class), _group);

		ddmStructure = ddmStructureTestHelper.addStructure(
			_createDDMForm(LocaleUtil.US), StorageType.JSON.toString());

		return ddmStructure;
	}

	private DDLRecordSet _addRecordSet() throws Exception {
		DDLRecordSetTestHelper recordSetTestHelper = new DDLRecordSetTestHelper(
			_group);

		DDMStructureTestHelper ddmStructureTestHelper =
			new DDMStructureTestHelper(
				PortalUtil.getClassNameId(DDLRecordSet.class), _group);

		ddmStructure = ddmStructureTestHelper.addStructure(
			_createDDMForm(LocaleUtil.US), StorageType.JSON.toString());

		return recordSetTestHelper.addRecordSet(ddmStructure);
	}

	private DDMForm _createDDMForm(Locale... locales) {
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

	@DeleteAfterTestRun
	private final List<DDLRecord> _ddlRecords = new ArrayList<>(1);

	@DeleteAfterTestRun
	private Group _group;

	@DeleteAfterTestRun
	private final List<Group> _groups = new ArrayList<>(1);

	private DDLRecordTestHelper _recordTestHelper;

}