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

package com.liferay.dynamic.data.mapping.search.test;

import com.liferay.dynamic.data.mapping.helper.DDMFormInstanceRecordTestHelper;
import com.liferay.dynamic.data.mapping.helper.DDMFormInstanceTestHelper;
import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.DDMFormInstance;
import com.liferay.dynamic.data.mapping.model.DDMFormInstanceRecord;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceRecordLocalService;
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
public abstract class BaseDDMFormInstanceRecordTestCase {

	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		ddmStructure = _addDDMStructure();

		DDMFormInstance ddmFormInstance = _addDDMFormInstance();

		_ddmFormInstanceRecordTestHelper = new DDMFormInstanceRecordTestHelper(
			_group, ddmFormInstance);

		ddmFormInstanceRecordFixture = createDDMFormInstanceRecordFixture();

		ddmFormInstanceRecordFixture.setUp();

		setGroup(ddmFormInstanceRecordFixture.addGroup());

		ddmFormInstanceRecordIndexerFixture =
			createDDMFormInstanceRecordIndexerFixture();

		indexedFieldsFixture = createIndexedFieldsFixture();
	}

	protected DDMFormInstanceRecordFixture
		createDDMFormInstanceRecordFixture() {

		return new DDMFormInstanceRecordFixture(
			_ddmFormInstanceRecordTestHelper, _groups, _ddmFormInstanceRecords);
	}

	protected DDMFormInstanceRecordIndexerFixture
		createDDMFormInstanceRecordIndexerFixture() {

		Indexer<DDMFormInstanceRecord> indexer =
			indexerRegistry.nullSafeGetIndexer(DDMFormInstanceRecord.class);

		return new DDMFormInstanceRecordIndexerFixture(indexer);
	}

	protected IndexedFieldsFixture createIndexedFieldsFixture() {
		return new IndexedFieldsFixture(
			resourcePermissionLocalService, searchEngineHelper);
	}

	protected void setGroup(Group group) {
		ddmFormInstanceRecordFixture.setGroup(group);
	}

	protected DDMFormInstanceRecordFixture ddmFormInstanceRecordFixture;
	protected DDMFormInstanceRecordIndexerFixture
		ddmFormInstanceRecordIndexerFixture;

	@Inject
	protected DDMFormInstanceRecordLocalService
		ddmFormInstanceRecordLocalService;

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

	private DDMFormInstance _addDDMFormInstance() throws Exception {
		DDMFormInstanceTestHelper ddmFormInstanceTestHelper =
			new DDMFormInstanceTestHelper(_group);

		return ddmFormInstanceTestHelper.addDDMFormInstance(ddmStructure);
	}

	private DDMStructure _addDDMStructure() throws Exception {
		DDMStructureTestHelper ddmStructureTestHelper =
			new DDMStructureTestHelper(
				PortalUtil.getClassNameId(DDMFormInstance.class), _group);

		DDMStructure ddmStructure = ddmStructureTestHelper.addStructure(
			_createDDMForm(LocaleUtil.US), StorageType.JSON.toString());

		return ddmStructure;
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
	private final List<DDMFormInstanceRecord> _ddmFormInstanceRecords =
		new ArrayList<>(1);

	private DDMFormInstanceRecordTestHelper _ddmFormInstanceRecordTestHelper;

	@DeleteAfterTestRun
	private Group _group;

	@DeleteAfterTestRun
	private final List<Group> _groups = new ArrayList<>(1);

}