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

package com.liferay.portal.upgrade.v6_2_0;

import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.metadata.RawMetadataProcessor;
import com.liferay.portal.kernel.upgrade.UpgradeException;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.liferay.portal.upgrade.v6_2_0.util.DDMTemplateTable;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.xml.XMLUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Juan Fernández
 * @author Marcellus Tavares
 */
public class UpgradeDynamicDataMapping extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		try {
			runSQL("alter table DDMTemplate add classNameId LONG");

			runSQL("alter table DDMTemplate add templateKey STRING");

			runSQL("alter_column_name DDMTemplate structureId classPK LONG");
		}
		catch (SQLException sqle) {
			upgradeTable(
				DDMTemplateTable.TABLE_NAME, DDMTemplateTable.TABLE_COLUMNS,
				DDMTemplateTable.TABLE_SQL_CREATE,
				DDMTemplateTable.TABLE_SQL_ADD_INDEXES);
		}

		long classNameId = PortalUtil.getClassNameId(
			"com.liferay.portlet.dynamicdatamapping.DDMStructure");

		try {
			runSQL("update DDMTemplate set classNameId = " + classNameId);
		}
		catch (Exception e) {
			if (_log.isWarnEnabled()) {
				_log.warn(e, e);
			}
		}

		updateStructures();

		updateTemplates();
	}

	protected Set<String> getDuplicateElementNames(
		Element element, Set<String> elementNames,
		Set<String> duplicateElementNames) {

		String elementName = element.attributeValue("name");

		if (!elementNames.add(elementName)) {
			duplicateElementNames.add(elementName);
		}

		List<Element> dynamicElements = element.elements("dynamic-element");

		for (Element dynamicElement : dynamicElements) {
			duplicateElementNames = getDuplicateElementNames(
				dynamicElement, elementNames, duplicateElementNames);
		}

		return duplicateElementNames;
	}

	protected Set<String> getDuplicateElementNames(long structureId)
		throws Exception {

		String xml =
			"<root>" + getFullStructureXML(structureId, StringPool.BLANK) +
				"</root>";

		Document document = SAXReaderUtil.read(xml);

		return getDuplicateElementNames(
			document.getRootElement(), new HashSet<String>(),
			new HashSet<String>());
	}

	protected String getFullStructureXML(long structureId, String xml)
		throws Exception {

		long parentStructureId = getParentStructureId(structureId);

		if (parentStructureId != 0) {
			xml = getFullStructureXML(parentStructureId, xml);
		}

		Document document = SAXReaderUtil.read(getXsd(structureId));

		Element rootElement = document.getRootElement();

		List<Element> dynamicElements = rootElement.elements("dynamic-element");

		for (Element dynamicElement : dynamicElements) {
			xml += dynamicElement.asXML();
		}

		return xml;
	}

	protected long getParentStructureId(long structureId) throws Exception {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = DataAccess.getUpgradeOptimizedConnection();

			ps = con.prepareStatement(
				"select parentStructureId from DDMStructure where structureId" +
					" = ?");

			ps.setLong(1, structureId);

			rs = ps.executeQuery();

			if (rs.next()) {
				return rs.getLong("parentStructureId");
			}

			return 0;
		}
		finally {
			DataAccess.cleanUp(con, ps, rs);
		}
	}

	protected String getXsd(long structureId) throws Exception {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = DataAccess.getUpgradeOptimizedConnection();

			ps = con.prepareStatement(
				"select xsd from DDMStructure where structureId = ?");

			ps.setLong(1, structureId);

			rs = ps.executeQuery();

			if (rs.next()) {
				return rs.getString("xsd");
			}

			return StringPool.BLANK;
		}
		finally {
			DataAccess.cleanUp(con, ps, rs);
		}
	}

	protected void logDuplicateNames(
			long classNameId, String structureKey,
			Set<String> duplicateElementNames)
		throws Exception {

		if (!_log.isWarnEnabled()) {
			return;
		}

		StringBundler sb = new StringBundler(
			duplicateElementNames.size() * 2 + 7);

		sb.append("Structure with class name ID ");
		sb.append(classNameId);
		sb.append(" and structure key = ");
		sb.append(structureKey);
		sb.append(" contains more than one element that is identified by the ");
		sb.append("same name either within itself or within any of its ");
		sb.append("parent structures. The duplicate element names are: ");

		for (String duplicateElementName : duplicateElementNames) {
			sb.append(duplicateElementName);
			sb.append(StringPool.COMMA_AND_SPACE);
		}

		sb.setIndex(sb.index() - 1);

		_log.warn(sb.toString());
	}

	protected void updateMetadataElement(
		Element metadataElement, String[] relocatedMetadadaEntryNames,
		String[] removedMetadataEntryNames) {

		Element parentElement = metadataElement.getParent();

		List<Element> entryElements = metadataElement.elements("entry");

		for (Element entryElement : entryElements) {
			String name = entryElement.attributeValue("name");

			if (ArrayUtil.contains(removedMetadataEntryNames, name)) {
				metadataElement.remove(entryElement);
			}
			else if (ArrayUtil.contains(relocatedMetadadaEntryNames, name)) {
				parentElement.addAttribute(name, entryElement.getText());

				metadataElement.remove(entryElement);
			}
		}
	}

	protected void updateStructure(
			long structureId, String structureKey, String xsd)
		throws Exception {

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = DataAccess.getUpgradeOptimizedConnection();

			ps = con.prepareStatement(
				"update DDMStructure set structureKey = ?, xsd = ? where " +
					"structureId = ?");

			ps.setString(1, structureKey);
			ps.setString(2, xsd);
			ps.setLong(3, structureId);

			ps.executeUpdate();
		}
		catch (SQLException sqle) {
			if (_log.isWarnEnabled()) {
				_log.warn(sqle, sqle);
			}
		}
		finally {
			DataAccess.cleanUp(con, ps, rs);
		}
	}

	protected void updateStructures() throws Exception {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = DataAccess.getUpgradeOptimizedConnection();

			ps = con.prepareStatement(
				"select classNameId, structureId, structureKey, xsd from " +
					"DDMStructure");

			rs = ps.executeQuery();

			boolean duplicateExists = false;

			while (rs.next()) {
				long classNameId = rs.getLong("classNameId");
				long structureId = rs.getLong("structureId");
				String structureKey = rs.getString("structureKey");
				String xsd = rs.getString("xsd");

				if (Validator.isNull(structureKey)) {
					structureKey = String.valueOf(System.currentTimeMillis());
				}
				else {
					structureKey = StringUtil.toUpperCase(structureKey.trim());
				}

				Set<String> duplicateElementNames = getDuplicateElementNames(
					structureId);

				if (!duplicateElementNames.isEmpty()) {
					duplicateExists = true;

					logDuplicateNames(
						classNameId, structureKey, duplicateElementNames);
				}

				if (!duplicateExists) {
					updateStructure(
						structureId, structureKey,
						updateXSD(xsd, structureKey));
				}
			}

			if (duplicateExists) {
				throw new UpgradeException(
					"Duplicate element name found in structures. See https://" +
						"issues.liferay.com/browse/LPS-52278 for more " +
							"information");
			}
		}
		finally {
			DataAccess.cleanUp(con, ps, rs);
		}
	}

	protected void updateTemplate(
			long templateId, String templateKey, String script)
		throws Exception {

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = DataAccess.getUpgradeOptimizedConnection();

			ps = con.prepareStatement(
				"update DDMTemplate set templateKey = ?, script = ? where " +
					"templateId = ?");

			ps.setString(1, templateKey);
			ps.setString(2, script);
			ps.setLong(3, templateId);

			ps.executeUpdate();
		}
		finally {
			DataAccess.cleanUp(con, ps, rs);
		}
	}

	protected void updateTemplates() throws Exception {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = DataAccess.getUpgradeOptimizedConnection();

			ps = con.prepareStatement(
				"select templateId, templateKey, script from DDMTemplate " +
					"where language = 'xsd'");

			rs = ps.executeQuery();

			while (rs.next()) {
				long templateId = rs.getLong("templateId");
				String templateKey = rs.getString("templateKey");
				String script = rs.getString("script");

				if (Validator.isNull(templateKey)) {
					templateKey = String.valueOf(System.currentTimeMillis());
				}
				else {
					templateKey = StringUtil.toUpperCase(templateKey.trim());
				}

				updateTemplate(
					templateId, templateKey,
					updateXSD(script, StringPool.BLANK));
			}
		}
		finally {
			DataAccess.cleanUp(con, ps, rs);
		}
	}

	protected String updateXSD(String xsd, String structureKey)
		throws Exception {

		Document document = SAXReaderUtil.read(xsd);

		Element rootElement = document.getRootElement();

		List<Element> dynamicElementElements = rootElement.elements(
			"dynamic-element");

		for (Element dynamicElementElement : dynamicElementElements) {
			updateXSDDynamicElement(dynamicElementElement, structureKey);
		}

		return XMLUtil.formatXML(document);
	}

	protected void updateXSDDynamicElement(
		Element element, String structureKey) {

		Element metadataElement = element.element("meta-data");

		updateMetadataElement(
			metadataElement,
			new String[] {
				"multiple", "name", "readOnly", "repeatable", "required",
				"showLabel", "type", "width"
			},
			new String[] {
				"acceptFiles", "displayChildLabelAsValue", "fieldCssClass",
				"folder"
			});

		if (StringUtil.equalsIgnoreCase(
				structureKey, RawMetadataProcessor.TIKA_RAW_METADATA)) {

			element.addAttribute("indexType", "text");
		}

		List<Element> dynamicElementElements = element.elements(
			"dynamic-element");

		for (Element dynamicElementElement : dynamicElementElements) {
			updateXSDDynamicElement(dynamicElementElement, structureKey);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		UpgradeDynamicDataMapping.class);

}