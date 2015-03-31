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

package com.liferay.portlet.dynamicdatalists.lar;

import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.lar.BaseStagedModelDataHandler;
import com.liferay.portal.kernel.lar.ExportImportHelperUtil;
import com.liferay.portal.kernel.lar.ExportImportPathUtil;
import com.liferay.portal.kernel.lar.PortletDataContext;
import com.liferay.portal.kernel.lar.PortletDataException;
import com.liferay.portal.kernel.lar.StagedModelDataHandlerUtil;
import com.liferay.portal.kernel.lar.StagedModelModifiedDateComparator;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portlet.dynamicdatalists.model.DDLRecord;
import com.liferay.portlet.dynamicdatalists.model.DDLRecordSet;
import com.liferay.portlet.dynamicdatalists.model.DDLRecordVersion;
import com.liferay.portlet.dynamicdatalists.service.DDLRecordLocalServiceUtil;
import com.liferay.portlet.dynamicdatamapping.model.DDMStructure;
import com.liferay.portlet.dynamicdatamapping.model.Value;
import com.liferay.portlet.dynamicdatamapping.service.DDMStructureLocalServiceUtil;
import com.liferay.portlet.dynamicdatamapping.storage.DDMFormFieldValue;
import com.liferay.portlet.dynamicdatamapping.storage.DDMFormValues;
import com.liferay.portlet.dynamicdatamapping.storage.Fields;
import com.liferay.portlet.dynamicdatamapping.storage.StorageEngineUtil;
import com.liferay.portlet.dynamicdatamapping.util.DDMFormValuesToFieldsConverterUtil;
import com.liferay.portlet.dynamicdatamapping.util.DDMImpl;
import com.liferay.portlet.dynamicdatamapping.util.FieldsToDDMFormValuesConverterUtil;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Daniel Kocsis
 * @author Steven Smith
 */
public class DDLRecordStagedModelDataHandler
	extends BaseStagedModelDataHandler<DDLRecord> {

	public static final String[] CLASS_NAMES = {DDLRecord.class.getName()};

	@Override
	public void deleteStagedModel(
			String uuid, long groupId, String className, String extraData)
		throws PortalException {

		DDLRecord record = fetchStagedModelByUuidAndGroupId(uuid, groupId);

		if (record != null) {
			DDLRecordLocalServiceUtil.deleteRecord(record);
		}
	}

	@Override
	public DDLRecord fetchStagedModelByUuidAndGroupId(
		String uuid, long groupId) {

		return DDLRecordLocalServiceUtil.fetchDDLRecordByUuidAndGroupId(
			uuid, groupId);
	}

	@Override
	public List<DDLRecord> fetchStagedModelsByUuidAndCompanyId(
		String uuid, long companyId) {

		return DDLRecordLocalServiceUtil.getDDLRecordsByUuidAndCompanyId(
			uuid, companyId, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
			new StagedModelModifiedDateComparator<DDLRecord>());
	}

	@Override
	public String[] getClassNames() {
		return CLASS_NAMES;
	}

	@Override
	public String getDisplayName(DDLRecord record) {
		return record.getUuid();
	}

	@Override
	protected void doExportStagedModel(
			PortletDataContext portletDataContext, DDLRecord record)
		throws Exception {

		StagedModelDataHandlerUtil.exportReferenceStagedModel(
			portletDataContext, record, record.getRecordSet(),
			PortletDataContext.REFERENCE_TYPE_STRONG);

		DDLRecordVersion recordVersion = record.getRecordVersion();

		DDLRecordSet recordSet = record.getRecordSet();

		DDMFormValues ddmFormValues = StorageEngineUtil.getDDMFormValues(
			recordVersion.getDDMStorageId());

		List<DDMFormFieldValue> formFieldValues =
			ddmFormValues.getDDMFormFieldValues();

		for (DDMFormFieldValue formFieldValue : formFieldValues) {
			String type = formFieldValue.getType();

			if (type.contains("ddm-link-to-page")) {
				Value value = formFieldValue.getValue();

				Map<Locale, String> values = value.getValues();

				for (Map.Entry<Locale, String> entry : values.entrySet()) {
					String jsonValue = entry.getValue();

					if (!jsonValue.contains("privateLayout")) {
						continue;
					}

					Map<String, Object> jsonMap =
						(Map<String, Object>)JSONFactoryUtil.looseDeserialize(
							jsonValue);

					long groupId = GetterUtil.getLong(jsonMap.get("groupId"));
					long layoutId = GetterUtil.getLong(jsonMap.get("layoutId"));

					boolean privateLayout = GetterUtil.getBoolean(
						jsonMap.get("privateLayout"));

					String layoutType = privateLayout ? "private" : "public";

					StringBundler sb = new StringBundler(7);

					sb.append(StringPool.CDATA_OPEN);
					sb.append(layoutId);
					sb.append(StringPool.AT);
					sb.append(layoutType);
					sb.append(StringPool.AT);
					sb.append(groupId);
					sb.append(StringPool.CDATA_CLOSE);

					String layoutLinkUrl = sb.toString();

					String reference =
						ExportImportHelperUtil.replaceExportLayoutReferences(
							portletDataContext, layoutLinkUrl);

					entry.setValue(reference);
				}
			}

			if (type.contains("ddm-documentlibrary")) {
				Value value = formFieldValue.getValue();

				Map<Locale, String> values = value.getValues();

				for (Map.Entry<Locale, String> entry : values.entrySet()) {
					String jsonValue = entry.getValue();

					if (!jsonValue.contains("uuid")) {
						continue;
					}

					Map<String, Object> jsonMap =
						(Map<String, Object>)JSONFactoryUtil.looseDeserialize(
							jsonValue);

					long groupId = GetterUtil.getLong(jsonMap.get("groupId"));
					String uuid = GetterUtil.getString(jsonMap.get("uuid"));

					StringBundler sb = new StringBundler(6);

					sb.append(StringPool.CDATA_OPEN);
					sb.append("/documents/");
					sb.append(groupId);
					sb.append(StringPool.SLASH);
					sb.append(uuid);
					sb.append(StringPool.CDATA_CLOSE);

					String documentLibraryURL = sb.toString();

					boolean includeReferencedContent =
						portletDataContext.getBooleanParameter(
							DDLPortletDataHandler.NAMESPACE,
							"referenced-content");

					String reference =
						ExportImportHelperUtil.replaceExportDLReferences(
							portletDataContext, record, documentLibraryURL,
							includeReferencedContent);

					entry.setValue(reference);
				}
			}
		}

		Fields fields = DDMFormValuesToFieldsConverterUtil.convert(
			recordSet.getDDMStructure(), ddmFormValues);

		String fieldsPath = ExportImportPathUtil.getModelPath(
			record, "fields.xml");

		portletDataContext.addZipEntry(fieldsPath, fields);

		Element recordElement = portletDataContext.getExportDataElement(record);

		recordElement.addAttribute("fields-path", fieldsPath);

		portletDataContext.addClassedModel(
			recordElement, ExportImportPathUtil.getModelPath(record), record);
	}

	@Override
	protected void doImportStagedModel(
			PortletDataContext portletDataContext, DDLRecord record)
		throws Exception {

		long userId = portletDataContext.getUserId(record.getUserUuid());

		Map<Long, Long> recordSetIds =
			(Map<Long, Long>)portletDataContext.getNewPrimaryKeysMap(
				DDLRecordSet.class);

		long recordSetId = MapUtil.getLong(
			recordSetIds, record.getRecordSetId(), record.getRecordSetId());

		ServiceContext serviceContext = portletDataContext.createServiceContext(
			record);

		Element recordElement = portletDataContext.getImportDataElement(record);

		Fields fields = (Fields)portletDataContext.getZipEntryAsObject(
			recordElement.attributeValue("fields-path"));

		Map<Long, Long> newStructureKeysMap =
			(Map<Long, Long>)portletDataContext.getNewPrimaryKeysMap(
				DDMStructure.class);

		long newStructureId = GetterUtil.getLong(
			newStructureKeysMap.get(fields.getDDMStructureId()));

		fields.get(DDMImpl.FIELDS_DISPLAY_NAME).setDDMStructureId(
			newStructureId);

		DDMStructure ddmStructure =
			DDMStructureLocalServiceUtil.fetchDDMStructure(newStructureId);

		DDMFormValues ddmFormValues =
			FieldsToDDMFormValuesConverterUtil.convert(ddmStructure, fields);

		List<DDMFormFieldValue> formFieldValues =
			ddmFormValues.getDDMFormFieldValues();

		for (DDMFormFieldValue formFieldValue : formFieldValues) {
			String type = formFieldValue.getType();

			if (type.contains("ddm-link-to-page")) {
				Value value = formFieldValue.getValue();

				Map<Locale, String> values = value.getValues();

				for (Map.Entry<Locale, String> entry : values.entrySet()) {
					String jsonValue = entry.getValue();

					if (!jsonValue.contains("CDATA")) {
						continue;
					}

					String reference =
						ExportImportHelperUtil.replaceImportLayoutReferences(
							portletDataContext, jsonValue);

					reference = StringUtil.stripCDATA(reference);

					String[] fieldPath = reference.split(StringPool.AT);

					boolean isPrivateLayout = true;

					if (fieldPath[1].contains("public")) {
						isPrivateLayout = false;
					}

					JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

					jsonObject.put("layoutId", fieldPath[0]);
					jsonObject.put("groupId", fieldPath[2]);
					jsonObject.put("privateLayout", isPrivateLayout);

					reference = jsonObject.toString();

					entry.setValue(reference);
				}
			}

			if (type.contains("ddm-documentlibrary")) {
				Value value = formFieldValue.getValue();

				Map<Locale, String> values = value.getValues();

				for (Map.Entry<Locale, String> entry : values.entrySet()) {
					String jsonValue = entry.getValue();

					if (!jsonValue.contains("dl-reference=")) {
						continue;
					}

					String reference =
						ExportImportHelperUtil.replaceImportDLReferences(
							portletDataContext, record, jsonValue);

					reference = StringUtil.stripCDATA(reference);

					String[] fieldPath = reference.split("[?/]");

					JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

					jsonObject.put("groupId", fieldPath[2]);
					jsonObject.put("title", fieldPath[4]);
					jsonObject.put("uuid", fieldPath[5]);

					reference = jsonObject.toString();

					entry.setValue(reference);
				}
			}
		}

		fields = DDMFormValuesToFieldsConverterUtil.convert(
			ddmStructure, ddmFormValues);

		DDLRecord importedRecord = null;

		if (portletDataContext.isDataStrategyMirror()) {
			DDLRecord existingRecord = fetchStagedModelByUuidAndGroupId(
				record.getUuid(), portletDataContext.getScopeGroupId());

			if (existingRecord == null) {
				serviceContext.setUuid(record.getUuid());

				importedRecord = DDLRecordLocalServiceUtil.addRecord(
					userId, portletDataContext.getScopeGroupId(), recordSetId,
					record.getDisplayIndex(), fields, serviceContext);
			}
			else {
				importedRecord = DDLRecordLocalServiceUtil.updateRecord(
					userId, existingRecord.getRecordId(), false,
					record.getDisplayIndex(), fields, true, serviceContext);
			}
		}
		else {
			importedRecord = DDLRecordLocalServiceUtil.addRecord(
				userId, portletDataContext.getScopeGroupId(), recordSetId,
				record.getDisplayIndex(), fields, serviceContext);
		}

		portletDataContext.importClassedModel(record, importedRecord);
	}

	@Override
	protected void validateExport(
			PortletDataContext portletDataContext, DDLRecord record)
		throws PortletDataException {

		int status = WorkflowConstants.STATUS_ANY;

		try {
			status = record.getStatus();
		}
		catch (Exception e) {
			throw new PortletDataException(e);
		}

		if (!ArrayUtil.contains(getExportableStatuses(), status)) {
			PortletDataException pde = new PortletDataException(
				PortletDataException.STATUS_UNAVAILABLE);

			pde.setStagedModel(record);

			throw pde;
		}
	}

}