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

package com.liferay.dynamic.data.mapping.background.task.test;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFileEntryType;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLFileEntryLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLFileEntryTypeLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.dynamic.data.mapping.io.DDMFormDeserializer;
import com.liferay.dynamic.data.mapping.io.DDMFormDeserializerDeserializeRequest.Builder;
import com.liferay.dynamic.data.mapping.io.DDMFormDeserializerDeserializeResponse;
import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.DDMStructureConstants;
import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.dynamic.data.mapping.storage.StorageType;
import com.liferay.dynamic.data.mapping.test.util.DDMStructureTestHelper;
import com.liferay.dynamic.data.mapping.test.util.DDMTemplateTestUtil;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.model.JournalFolderConstants;
import com.liferay.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskExecutor;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestDataConstants;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.LocaleThreadLocal;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.Inject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Lucas Marques de Paula
 */
public abstract class BaseDDMStructureBackgroundTaskExecutor
	extends BaseBackgroundTaskExecutor {

	protected final DDMStructure addDDMStructure(Group group, Class<?> clazz)
		throws Exception, PortalException {

		long classNameId = PortalUtil.getClassNameId(clazz);

		Builder builder = Builder.newBuilder(_getStructureDefinition());

		DDMFormDeserializerDeserializeResponse
			ddmFormDeserializerDeserializeResponse =
				ddmFormDeserializer.deserialize(builder.build());

		DDMForm ddmForm = ddmFormDeserializerDeserializeResponse.getDDMForm();

		DDMStructureTestHelper ddmStructureTestHelper =
			new DDMStructureTestHelper(classNameId, group);

		DDMStructure ddmStructure = ddmStructureTestHelper.addStructure(
			classNameId, null, RandomTestUtil.randomString(), ddmForm,
			StorageType.JSON.getValue(), DDMStructureConstants.TYPE_DEFAULT);

		_ddmStructures.add(ddmStructure);

		return ddmStructure;
	}

	protected final DDMTemplate addDDMTemplate(
			Group group, DDMStructure ddmStructure)
		throws Exception, PortalException {

		DDMTemplate ddmTemplate = DDMTemplateTestUtil.addTemplate(
			group.getGroupId(), ddmStructure.getStructureId(),
			PortalUtil.getClassNameId(JournalArticle.class));

		_ddmTemplates.add(ddmTemplate);

		return ddmTemplate;
	}

	protected DLFileEntry addDLFileEntry(
			Group group, DLFolder dlFolder, DLFileEntryType dlFileEntryType,
			String title)
		throws Exception {

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(group.getGroupId());

		serviceContext.setAttribute(
			"fileEntryTypeId", dlFileEntryType.getFileEntryTypeId());

		byte[] bytes = TestDataConstants.TEST_BYTE_ARRAY;

		InputStream inputStream = new ByteArrayInputStream(bytes);

		FileEntry fileEntry = DLAppLocalServiceUtil.addFileEntry(
			TestPropsValues.getUserId(), dlFolder.getRepositoryId(),
			dlFolder.getFolderId(), RandomTestUtil.randomString(),
			ContentTypes.TEXT_PLAIN, title, StringPool.BLANK, StringPool.BLANK,
			inputStream, bytes.length, serviceContext);

		DLFileEntry dlFileEntry = DLFileEntryLocalServiceUtil.getFileEntry(
			fileEntry.getFileEntryId());

		_dlFileEntries.add(dlFileEntry);

		return dlFileEntry;
	}

	protected DLFileEntryType addDLFileEntryType(
			Group group, DDMStructure ddmStructure)
		throws Exception {

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(group.getGroupId());

		DLFileEntryType dlFileEntryType =
			DLFileEntryTypeLocalServiceUtil.addFileEntryType(
				TestPropsValues.getUserId(), group.getGroupId(),
				RandomTestUtil.randomString(), StringPool.BLANK,
				new long[] {ddmStructure.getStructureId()}, serviceContext);

		_dlFileEntryTypes .add(dlFileEntryType);

		return dlFileEntryType;
	}

	protected DLFolder addDLFolder(Group group) throws PortalException {
		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(group.getGroupId());

		DLFolder dlFolder = DLFolderLocalServiceUtil.addFolder(
			TestPropsValues.getUserId(), group.getGroupId(), group.getGroupId(),
			false, 0L, RandomTestUtil.randomString(),
			RandomTestUtil.randomString(), false, serviceContext);

		_dlFolders.add(dlFolder);

		return dlFolder;
	}

	protected final JournalArticle addJournalArticle(
			Group group, DDMStructure ddmStructure, DDMTemplate template,
			String title)
		throws Exception, PortalException {

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(group.getGroupId());

		serviceContext.setWorkflowAction(WorkflowConstants.ACTION_PUBLISH);

		Map<Locale, String> titleMap = Collections.singletonMap(
			LocaleThreadLocal.getSiteDefaultLocale(), title);

		JournalArticle journalArticle =
			JournalArticleLocalServiceUtil.addArticle(
				TestPropsValues.getUserId(), group.getGroupId(),
				JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID, titleMap,
				Collections.emptyMap(), _getJournalArticleContent(),
				ddmStructure.getStructureKey(), template.getTemplateKey(),
				serviceContext);

		_journalArticles.add(journalArticle);

		return journalArticle;
	}

	protected void reindex(DDMStructure ddmStructure) throws Exception {
		BackgroundTask backgroundTask = new BackgroundTask();

		backgroundTask.setTaskContextMap(
			Collections.singletonMap(
				"structureId", ddmStructure.getStructureId()));

		backgroundTaskExecutor.execute(backgroundTask);
	}

	protected static final int NUMBER_OF_SAMPLES = 100;

	@Inject(
		filter = "background.task.executor.class.name=com.liferay.dynamic.data.mapping.background.task.DDMStructureIndexerBackgroundTaskExecutor"
	)
	protected BackgroundTaskExecutor backgroundTaskExecutor;

	@Inject(filter = "ddm.form.deserializer.type=json")
	protected DDMFormDeserializer ddmFormDeserializer;

	private String _getJournalArticleContent() throws Exception {
		if (_journalArticleContent == null) {
			_journalArticleContent = getResourceAsString(
				"dynamic-data-mapping-journal-article-content.xml");
		}

		return _journalArticleContent;
	}

	private String _getStructureDefinition() throws Exception {
		if (_structureDefinition == null) {
			_structureDefinition = getResourceAsString(
				"dynamic-data-mapping-structure-definition.json");
		}

		return _structureDefinition;
	}

	@DeleteAfterTestRun
	private final List<DDMStructure> _ddmStructures = new ArrayList<>(1);

	@DeleteAfterTestRun
	private final List<DDMTemplate> _ddmTemplates = new ArrayList<>(1);

	@DeleteAfterTestRun
	private final List<DLFileEntry> _dlFileEntries = new ArrayList<>(
		NUMBER_OF_SAMPLES);

	@DeleteAfterTestRun
	private final List<DLFileEntryType> _dlFileEntryTypes = new ArrayList<>(1);

	@DeleteAfterTestRun
	private final List<DLFolder> _dlFolders = new ArrayList<>(1);

	private String _journalArticleContent;

	@DeleteAfterTestRun
	private final List<JournalArticle> _journalArticles = new ArrayList<>(
		NUMBER_OF_SAMPLES);

	private String _structureDefinition;

}