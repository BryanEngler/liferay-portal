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

package com.liferay.portal.verify;

import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBFactoryUtil;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.Criterion;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.SecureRandomUtil;
import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.FriendlyURLNormalizerUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.KeyValuePair;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.DocumentException;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.Node;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.liferay.portal.kernel.xml.XPath;
import com.liferay.portal.service.ResourceLocalServiceUtil;
import com.liferay.portal.util.PortalInstances;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portlet.asset.model.AssetEntry;
import com.liferay.portlet.asset.service.AssetEntryLocalServiceUtil;
import com.liferay.portlet.documentlibrary.model.DLFileEntry;
import com.liferay.portlet.documentlibrary.service.DLFileEntryLocalServiceUtil;
import com.liferay.portlet.dynamicdatamapping.NoSuchStructureException;
import com.liferay.portlet.dynamicdatamapping.model.DDMStructure;
import com.liferay.portlet.dynamicdatamapping.model.DDMStructureConstants;
import com.liferay.portlet.dynamicdatamapping.model.DDMTemplate;
import com.liferay.portlet.dynamicdatamapping.service.DDMStructureLocalServiceUtil;
import com.liferay.portlet.dynamicdatamapping.service.DDMTemplateLocalServiceUtil;
import com.liferay.portlet.dynamicdatamapping.util.DDMFieldsCounter;
import com.liferay.portlet.journal.model.JournalArticle;
import com.liferay.portlet.journal.model.JournalArticleConstants;
import com.liferay.portlet.journal.model.JournalArticleImage;
import com.liferay.portlet.journal.model.JournalArticleResource;
import com.liferay.portlet.journal.model.JournalContentSearch;
import com.liferay.portlet.journal.model.JournalFolder;
import com.liferay.portlet.journal.service.JournalArticleImageLocalServiceUtil;
import com.liferay.portlet.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.portlet.journal.service.JournalArticleResourceLocalServiceUtil;
import com.liferay.portlet.journal.service.JournalContentSearchLocalServiceUtil;
import com.liferay.portlet.journal.service.JournalFolderLocalServiceUtil;
import com.liferay.portlet.journal.util.comparator.ArticleVersionComparator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

import javax.portlet.PortletPreferences;

/**
 * @author Alexander Chow
 * @author Shinn Lok
 */
public class VerifyJournal extends VerifyProcess {

	public static final long DEFAULT_GROUP_ID = 14;

	public static final int NUM_OF_ARTICLES = 5;

	@Override
	protected void doVerify() throws Exception {
		verifyArticleAssets();
		verifyArticleContents();
		verifyArticleStructures();
		verifyContentSearch();
		verifyDuplicateStructureFieldNames();
		verifyFolderAssets();
		verifyOracleNewLine();
		verifyPermissions();
		verifyTree();
		verifyURLTitle();
	}

	protected Document getCompleteStructureDefinition(DDMStructure structure)
		throws DocumentException, PortalException {

		Document childDocument = SAXReaderUtil.read(structure.getDefinition());

		long parentStructureId = structure.getParentStructureId();

		if (parentStructureId ==
				DDMStructureConstants.DEFAULT_PARENT_STRUCTURE_ID) {

			return childDocument;
		}

		Stack<Document> documents = new Stack<>();

		documents.push(childDocument);

		while (parentStructureId !=
					DDMStructureConstants.DEFAULT_PARENT_STRUCTURE_ID) {

			DDMStructure parentStructure =
				DDMStructureLocalServiceUtil.getStructure(parentStructureId);

			Document parentDocument = SAXReaderUtil.read(
				parentStructure.getDefinition());

			documents.push(parentDocument);

			parentStructureId = parentStructure.getParentStructureId();
		}

		Document mergedDocument = SAXReaderUtil.createDocument();

		Element mergedRootElement = mergedDocument.getRootElement();

		for (Document document : documents) {
			Element rootElement = document.getRootElement();

			List<Element> dynamicElements = rootElement.elements(
				"dynamic-element");

			for (Element dynamicElement : dynamicElements) {
				mergedRootElement.add(dynamicElement.createCopy());
			}
		}

		return mergedDocument;
	}

	protected Set<String> getDuplicateElementNames(Document document)
		throws DocumentException, PortalException {

		XPath xPathSelector = SAXReaderUtil.createXPath("//dynamic-element");

		List<Node> nodes = xPathSelector.selectNodes(document);

		Set<String> elementNames = new HashSet<>();
		Set<String> duplicateElementNames = new HashSet<>();

		for (Node node : nodes) {
			Element element = (Element)node;

			String name = StringUtil.toLowerCase(
				element.attributeValue("name"));

			if (elementNames.contains(name)) {
				duplicateElementNames.add(name);
			}

			elementNames.add(name);
		}

		return duplicateElementNames;
	}

	protected List<KeyValuePair> getNewTemplateVariableNames(
			DDMStructure structure,
			Map<Long, List<KeyValuePair>> problemStructureMap)
		throws PortalException {

		List<KeyValuePair> newTemplateVariableNames = new ArrayList<>();

		newTemplateVariableNames.addAll(
			problemStructureMap.get(structure.getStructureId()));

		long ancestorStructureId = structure.getParentStructureId();

		while (ancestorStructureId !=
					DDMStructureConstants.DEFAULT_PARENT_STRUCTURE_ID) {

			List<KeyValuePair> ancestorNewTemplateVariableNames =
				problemStructureMap.get(ancestorStructureId);

			if (ancestorNewTemplateVariableNames == null) {
				break;
			}

			newTemplateVariableNames.addAll(ancestorNewTemplateVariableNames);

			DDMStructure ancestorStructure =
				DDMStructureLocalServiceUtil.getStructure(ancestorStructureId);

			ancestorStructureId = ancestorStructure.getParentStructureId();
		}

		Collections.sort(newTemplateVariableNames);

		Collections.reverse(newTemplateVariableNames);

		return newTemplateVariableNames;
	}

	protected void updateContentSearch(long groupId, String portletId)
		throws Exception {

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = DataAccess.getUpgradeOptimizedConnection();

			ps = con.prepareStatement(
				"select preferences from PortletPreferences inner join " +
					"Layout on PortletPreferences.plid = Layout.plid where " +
						"groupId = ? and portletId = ?");

			ps.setLong(1, groupId);
			ps.setString(2, portletId);

			rs = ps.executeQuery();

			while (rs.next()) {
				String xml = rs.getString("preferences");

				PortletPreferences portletPreferences =
					PortletPreferencesFactoryUtil.fromDefaultXML(xml);

				String articleId = portletPreferences.getValue(
					"articleId", null);

				List<JournalContentSearch> contentSearches =
					JournalContentSearchLocalServiceUtil.
						getArticleContentSearches(groupId, articleId);

				if (contentSearches.isEmpty()) {
					continue;
				}

				JournalContentSearch contentSearch = contentSearches.get(0);

				JournalContentSearchLocalServiceUtil.updateContentSearch(
					contentSearch.getGroupId(), contentSearch.isPrivateLayout(),
					contentSearch.getLayoutId(), contentSearch.getPortletId(),
					articleId, true);
			}
		}
		finally {
			DataAccess.cleanUp(con, ps, rs);
		}
	}

	protected void updateCreateAndModifiedDates() throws Exception {
		ActionableDynamicQuery actionableDynamicQuery =
			JournalArticleResourceLocalServiceUtil.getActionableDynamicQuery();

		if (_log.isDebugEnabled()) {
			long count = actionableDynamicQuery.performCount();

			_log.debug(
				"Processing " + count +
					" article resources for create and modified dates");
		}

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod() {

				@Override
				public void performAction(Object object) {
					JournalArticleResource articleResource =
						(JournalArticleResource)object;

					updateCreateDate(articleResource);
					updateModifiedDate(articleResource);
				}

			});

		actionableDynamicQuery.performActions();

		if (_log.isDebugEnabled()) {
			_log.debug("Create and modified dates verified for articles");
		}
	}

	protected void updateCreateDate(JournalArticleResource articleResource) {
		List<JournalArticle> articles =
			JournalArticleLocalServiceUtil.getArticles(
				articleResource.getGroupId(), articleResource.getArticleId(),
				QueryUtil.ALL_POS, QueryUtil.ALL_POS,
				new ArticleVersionComparator(true));

		if (articles.size() <= 1) {
			return;
		}

		JournalArticle firstArticle = articles.get(0);

		Date createDate = firstArticle.getCreateDate();

		for (JournalArticle article : articles) {
			if (!createDate.equals(article.getCreateDate())) {
				article.setCreateDate(createDate);

				JournalArticleLocalServiceUtil.updateJournalArticle(article);
			}
		}
	}

	protected void updateDocumentLibraryElements(Element element) {
		Element dynamicContentElement = element.element("dynamic-content");

		String path = dynamicContentElement.getStringValue();

		String[] pathArray = StringUtil.split(path, CharPool.SLASH);

		if (pathArray.length != 5) {
			return;
		}

		long groupId = GetterUtil.getLong(pathArray[2]);
		long folderId = GetterUtil.getLong(pathArray[3]);
		String title = HttpUtil.decodeURL(HtmlUtil.escape(pathArray[4]));

		DLFileEntry dlFileEntry = DLFileEntryLocalServiceUtil.fetchFileEntry(
			groupId, folderId, title);

		if (dlFileEntry == null) {
			return;
		}

		Node node = dynamicContentElement.node(0);

		node.setText(path + StringPool.SLASH + dlFileEntry.getUuid());
	}

	protected List<KeyValuePair> updateDuplicateStructureFieldNames(
			DDMStructure structure, Set<String> duplicateElementNames)
		throws DocumentException {

		Document document = SAXReaderUtil.read(structure.getDefinition());

		Element rootElement = document.getRootElement();

		List<KeyValuePair> newTemplateVariableNames = new ArrayList<>();

		for (Element element : rootElement.elements()) {
			updateDuplicateStructureFieldNames(
				StringPool.DOLLAR, StringPool.DOLLAR, element,
				duplicateElementNames, newTemplateVariableNames);
		}

		structure.setDefinition(document.asXML());

		DDMStructureLocalServiceUtil.updateDDMStructure(structure);

		return newTemplateVariableNames;
	}

	protected void updateDuplicateStructureFieldNames(
		String oldPrefix, String newPrefix, Element element,
		Set<String> duplicateElementNames,
		List<KeyValuePair> newTemplateVariableNames) {

		String type = element.attributeValue("type");

		if (type.equals("option")) {
			return;
		}

		String oldName = element.attributeValue("name");
		String newName = oldName;

		if (duplicateElementNames.contains(oldName)) {
			while (duplicateElementNames.contains(newName)) {
				int nextRandomId = (SecureRandomUtil.nextInt() % 9000) + 1000;

				newName = oldName + nextRandomId;
			}

			duplicateElementNames.add(newName);

			element.addAttribute("name", newName);

			KeyValuePair kvp = new KeyValuePair(
				oldPrefix.concat(oldName), newPrefix.concat(newName));

			newTemplateVariableNames.add(kvp);
		}

		oldPrefix = oldPrefix.concat(oldName).concat(StringPool.PERIOD);
		newPrefix = newPrefix.concat(newName).concat(StringPool.PERIOD);

		List<Element> dynamicElements = element.elements("dynamic-element");

		for (Element dynamicElement : dynamicElements) {
			updateDuplicateStructureFieldNames(
				oldPrefix, newPrefix, dynamicElement, duplicateElementNames,
				newTemplateVariableNames);
		}
	}

	protected void updateDuplicateStructureFieldNameTemplateVariables(
			DDMStructure structure, List<KeyValuePair> newTemplateVariableNames)
		throws DocumentException, PortalException {

		List<DDMTemplate> templates =
			DDMTemplateLocalServiceUtil.getTemplatesByClassPK(
				structure.getGroupId(), structure.getStructureId());

		for (DDMTemplate template : templates) {
			String script = template.getScript();

			for (KeyValuePair kvp : newTemplateVariableNames) {
				String oldTemplateVariableName = kvp.getKey();
				String newTemplateVariableName = kvp.getValue();

				script = StringUtil.replace(
					script, oldTemplateVariableName, newTemplateVariableName);
			}

			template.setScript(script);

			DDMTemplateLocalServiceUtil.updateDDMTemplate(template);
		}

		if (_log.isWarnEnabled()) {
			StringBundler sb = new StringBundler(6 + (templates.size() * 2));

			sb.append("Absolute references to field names in templates ");

			for (DDMTemplate template : templates) {
				sb.append(template.getTemplateKey());
				sb.append(StringPool.COMMA);
			}

			sb.setIndex(sb.index() - 1);

			sb.append(" in site ");
			sb.append(structure.getGroupId());
			sb.append(" were automatically updated due to duplicate field ");
			sb.append("names not being allowed in Liferay 6.2, but relative ");
			sb.append(" references will need to be manually updated by site");
			sb.append("administrators after the verify process completes");

			_log.warn(sb.toString());
		}
	}

	protected void updateDynamicElements(JournalArticle article)
		throws Exception {

		Document document = SAXReaderUtil.read(article.getContent());

		Element rootElement = document.getRootElement();

		updateDynamicElements(rootElement.elements("dynamic-element"));

		article.setContent(document.asXML());

		JournalArticleLocalServiceUtil.updateJournalArticle(article);
	}

	protected void updateDynamicElements(List<Element> dynamicElements)
		throws PortalException {

		DDMFieldsCounter ddmFieldsCounter = new DDMFieldsCounter();

		for (Element dynamicElement : dynamicElements) {
			updateDynamicElements(dynamicElement.elements("dynamic-element"));

			String name = dynamicElement.attributeValue("name");

			int index = ddmFieldsCounter.get(name);

			dynamicElement.addAttribute("index", String.valueOf(index));

			String type = dynamicElement.attributeValue("type");

			if (type.equals("image")) {
				updateImageElement(dynamicElement, name, index);
			}

			ddmFieldsCounter.incrementKey(name);
		}
	}

	protected void updateElement(long groupId, Element element) {
		List<Element> dynamicElementElements = element.elements(
			"dynamic-element");

		for (Element dynamicElementElement : dynamicElementElements) {
			updateElement(groupId, dynamicElementElement);
		}

		String type = element.attributeValue("type");

		if (type.equals("document_library")) {
			updateDocumentLibraryElements(element);
		}
		else if (type.equals("link_to_layout")) {
			updateLinkToLayoutElements(groupId, element);
		}
	}

	protected void updateImageElement(Element element, String name, int index)
		throws PortalException {

		Element dynamicContentElement = element.element("dynamic-content");

		long articleImageId = GetterUtil.getLong(
			dynamicContentElement.attributeValue("id"));

		JournalArticleImage articleImage =
			JournalArticleImageLocalServiceUtil.getArticleImage(articleImageId);

		articleImage.setElName(name + StringPool.UNDERLINE + index);

		JournalArticleImageLocalServiceUtil.updateJournalArticleImage(
			articleImage);
	}

	protected void updateLinkToLayoutElements(long groupId, Element element) {
		Element dynamicContentElement = element.element("dynamic-content");

		Node node = dynamicContentElement.node(0);

		String text = node.getText();

		if (!text.isEmpty() && !text.endsWith(StringPool.AT + groupId)) {
			node.setText(
				dynamicContentElement.getStringValue() + StringPool.AT +
					groupId);
		}
	}

	protected void updateModifiedDate(JournalArticleResource articleResource) {
		JournalArticle article =
			JournalArticleLocalServiceUtil.fetchLatestArticle(
				articleResource.getResourcePrimKey(),
				WorkflowConstants.STATUS_APPROVED, true);

		if (article == null) {
			return;
		}

		AssetEntry assetEntry = AssetEntryLocalServiceUtil.fetchEntry(
			articleResource.getGroupId(), articleResource.getUuid());

		if (assetEntry == null) {
			return;
		}

		Date modifiedDate = article.getModifiedDate();

		if (modifiedDate.equals(assetEntry.getModifiedDate())) {
			return;
		}

		article.setModifiedDate(assetEntry.getModifiedDate());

		JournalArticleLocalServiceUtil.updateJournalArticle(article);
	}

	protected void updateResourcePrimKey() throws PortalException {
		ActionableDynamicQuery actionableDynamicQuery =
			JournalArticleLocalServiceUtil.getActionableDynamicQuery();

		actionableDynamicQuery.setAddCriteriaMethod(
			new ActionableDynamicQuery.AddCriteriaMethod() {

				@Override
				public void addCriteria(DynamicQuery dynamicQuery) {
					Property resourcePrimKey = PropertyFactoryUtil.forName(
						"resourcePrimKey");

					dynamicQuery.add(resourcePrimKey.le(0l));
				}

			}
		);

		if (_log.isDebugEnabled()) {
			long count = actionableDynamicQuery.performCount();

			_log.debug(
				"Processing " + count +
					" default article versions in draft mode");
		}

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod() {

				@Override
				public void performAction(Object object)
					throws PortalException {

					JournalArticle article = (JournalArticle)object;

					long groupId = article.getGroupId();
					String articleId = article.getArticleId();
					double version = article.getVersion();

					JournalArticleLocalServiceUtil.checkArticleResourcePrimKey(
						groupId, articleId, version);
				}

			});

		actionableDynamicQuery.performActions();
	}

	protected void updateURLTitle(
			long groupId, String articleId, String urlTitle)
		throws Exception {

		String normalizedURLTitle = FriendlyURLNormalizerUtil.normalize(
			urlTitle, _friendlyURLPattern);

		if (urlTitle.equals(normalizedURLTitle)) {
			return;
		}

		normalizedURLTitle = JournalArticleLocalServiceUtil.getUniqueUrlTitle(
			groupId, articleId, normalizedURLTitle);

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = DataAccess.getUpgradeOptimizedConnection();

			ps = con.prepareStatement(
				"update JournalArticle set urlTitle = ? where urlTitle = ?");

			ps.setString(1, normalizedURLTitle);
			ps.setString(2, urlTitle);

			ps.executeUpdate();
		}
		finally {
			DataAccess.cleanUp(con, ps);
		}
	}

	protected void verifyArticleAssets() throws Exception {
		List<JournalArticle> journalArticles =
			JournalArticleLocalServiceUtil.getNoAssetArticles();

		if (_log.isDebugEnabled()) {
			_log.debug(
				"Processing " + journalArticles.size() +
					" articles with no asset");
		}

		for (JournalArticle journalArticle : journalArticles) {
			try {
				JournalArticleLocalServiceUtil.updateAsset(
					journalArticle.getUserId(), journalArticle, null, null,
					null);
			}
			catch (Exception e) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						"Unable to update asset for article " +
							journalArticle.getId() + ": " + e.getMessage());
				}
			}
		}

		ActionableDynamicQuery actionableDynamicQuery =
			JournalArticleLocalServiceUtil.getActionableDynamicQuery();

		actionableDynamicQuery.setAddCriteriaMethod(
			new ActionableDynamicQuery.AddCriteriaMethod() {

				@Override
				public void addCriteria(DynamicQuery dynamicQuery) {
					Property versionProperty = PropertyFactoryUtil.forName(
						"version");

					dynamicQuery.add(
						versionProperty.eq(
							JournalArticleConstants.VERSION_DEFAULT));

					Property statusProperty = PropertyFactoryUtil.forName(
						"status");

					dynamicQuery.add(
						statusProperty.eq(WorkflowConstants.STATUS_DRAFT));
				}

			});

		if (_log.isDebugEnabled()) {
			long count = actionableDynamicQuery.performCount();

			_log.debug(
				"Processing " + count +
					" default article versions in draft mode");
		}

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod() {

				@Override
				public void performAction(Object object)
					throws PortalException {

					JournalArticle article = (JournalArticle)object;

					AssetEntry assetEntry =
						AssetEntryLocalServiceUtil.fetchEntry(
							JournalArticle.class.getName(),
							article.getResourcePrimKey());

					AssetEntryLocalServiceUtil.updateEntry(
						assetEntry.getClassName(), assetEntry.getClassPK(),
						null, assetEntry.isVisible());
				}

			});

		actionableDynamicQuery.performActions();

		if (_log.isDebugEnabled()) {
			_log.debug("Assets verified for articles");
		}

		updateCreateAndModifiedDates();
		updateResourcePrimKey();
	}

	protected void verifyArticleContents() throws Exception {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = DataAccess.getUpgradeOptimizedConnection();

			ps = con.prepareStatement(
				"select id_ from JournalArticle where (content like " +
					"'%document_library%' or content like '%link_to_layout%')" +
						" and DDMStructureKey != ''");

			rs = ps.executeQuery();

			while (rs.next()) {
				long id = rs.getLong("id_");

				JournalArticle article =
					JournalArticleLocalServiceUtil.getArticle(id);

				Document document = SAXReaderUtil.read(article.getContent());

				Element rootElement = document.getRootElement();

				for (Element element : rootElement.elements()) {
					updateElement(article.getGroupId(), element);
				}

				article.setContent(document.asXML());

				JournalArticleLocalServiceUtil.updateJournalArticle(article);
			}
		}
		finally {
			DataAccess.cleanUp(con, ps, rs);
		}
	}

	protected void verifyArticleStructures() throws PortalException {
		ActionableDynamicQuery actionableDynamicQuery =
			JournalArticleLocalServiceUtil.getActionableDynamicQuery();

		if (_log.isDebugEnabled()) {
			long count = actionableDynamicQuery.performCount();

			_log.debug(
				"Processing " + count + " articles for invalid structures " +
					"and dynamic elements");
		}

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod() {

				@Override
				public void performAction(Object object) {
					JournalArticle article = (JournalArticle)object;

					try {
						JournalArticleLocalServiceUtil.checkStructure(
							article.getGroupId(), article.getArticleId(),
							article.getVersion());
					}
					catch (NoSuchStructureException nsse) {
						if (_log.isWarnEnabled()) {
							_log.warn(
								"Removing reference to missing structure for " +
									"article " + article.getId());
						}

						article.setDDMStructureKey(StringPool.BLANK);
						article.setDDMTemplateKey(StringPool.BLANK);

						JournalArticleLocalServiceUtil.updateJournalArticle(
							article);
					}
					catch (Exception e) {
						_log.error(
							"Unable to check the structure for article " +
								article.getId(),
							e);
					}

					try {
						updateDynamicElements(article);
					}
					catch (Exception e) {
						_log.error(
							"Unable to update content for article " +
								article.getId(),
							e);
					}
				}

			});

		actionableDynamicQuery.performActions();
	}

	protected void verifyContentSearch() throws Exception {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = DataAccess.getUpgradeOptimizedConnection();

			ps = con.prepareStatement(
				"select groupId, portletId from JournalContentSearch group " +
					"by groupId, portletId having count(groupId) > 1 and " +
						"count(portletId) > 1");

			rs = ps.executeQuery();

			while (rs.next()) {
				long groupId = rs.getLong("groupId");
				String portletId = rs.getString("portletId");

				updateContentSearch(groupId, portletId);
			}
		}
		finally {
			DataAccess.cleanUp(con, ps, rs);
		}
	}

	protected void verifyDuplicateStructureFieldNames()
		throws DocumentException, PortalException {

		ActionableDynamicQuery actionableDynamicQuery =
			DDMStructureLocalServiceUtil.getActionableDynamicQuery();

		if (_log.isDebugEnabled()) {
			long count = actionableDynamicQuery.performCount();

			_log.debug(
				"Processing " + count + " structures for duplicate field " +
					"names");
		}

		actionableDynamicQuery.setAddCriteriaMethod(
			new ActionableDynamicQuery.AddCriteriaMethod() {

				@Override
				public void addCriteria(DynamicQuery dynamicQuery) {
					Property classNameIdProperty = PropertyFactoryUtil.forName(
						"classNameId");

					Criterion classNameIdCriterion = classNameIdProperty.eq(
						PortalUtil.getClassNameId(JournalArticle.class));

					dynamicQuery.add(classNameIdCriterion);
				}
			}
		);

		final Map<Long, List<KeyValuePair>> problemStructureMap =
			new HashMap<>();

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod() {

				@Override
				public void performAction(Object object) {
					DDMStructure structure = (DDMStructure)object;

					try {
						Document document = getCompleteStructureDefinition(
							structure);

						Set<String> duplicateElementNames =
							getDuplicateElementNames(document);

						if (duplicateElementNames.isEmpty()) {
							return;
						}

						List<KeyValuePair> templateVariableNames =
							updateDuplicateStructureFieldNames(
								structure, duplicateElementNames);

						problemStructureMap.put(
							structure.getStructureId(), templateVariableNames);
					}
					catch (Exception e) {
						_log.error(e, e);
					}
				}
			});

		for (long structureId : problemStructureMap.keySet()) {
			DDMStructure structure = DDMStructureLocalServiceUtil.getStructure(
				structureId);

			List<KeyValuePair> newTemplateVariableNames =
				getNewTemplateVariableNames(structure, problemStructureMap);

			updateDuplicateStructureFieldNameTemplateVariables(
				structure, newTemplateVariableNames);
		}
	}

	protected void verifyFolderAssets() throws Exception {
		List<JournalFolder> folders =
			JournalFolderLocalServiceUtil.getNoAssetFolders();

		if (_log.isDebugEnabled()) {
			_log.debug(
				"Processing " + folders.size() + " folders with no asset");
		}

		for (JournalFolder folder : folders) {
			try {
				JournalFolderLocalServiceUtil.updateAsset(
					folder.getUserId(), folder, null, null, null);
			}
			catch (Exception e) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						"Unable to update asset for folder " +
							folder.getFolderId() + ": " + e.getMessage());
				}
			}
		}

		if (_log.isDebugEnabled()) {
			_log.debug("Assets verified for folders");
		}
	}

	protected void verifyOracleNewLine() throws Exception {
		DB db = DBFactoryUtil.getDB();

		String dbType = db.getType();

		if (!dbType.equals(DB.TYPE_ORACLE)) {
			return;
		}

		// This is a workaround for a limitation in Oracle sqlldr's inability
		// insert new line characters for long varchar columns. See
		// http://forums.liferay.com/index.php?showtopic=2761&hl=oracle for more
		// information. Check several articles because some articles may not
		// have new lines.

		boolean checkNewLine = false;

		List<JournalArticle> articles =
			JournalArticleLocalServiceUtil.getArticles(
				DEFAULT_GROUP_ID, 0, NUM_OF_ARTICLES);

		for (JournalArticle article : articles) {
			String content = article.getContent();

			if ((content != null) && content.contains("\\n")) {
				articles = JournalArticleLocalServiceUtil.getArticles(
					DEFAULT_GROUP_ID);

				for (int j = 0; j < articles.size(); j++) {
					article = articles.get(j);

					JournalArticleLocalServiceUtil.checkNewLine(
						article.getGroupId(), article.getArticleId(),
						article.getVersion());
				}

				checkNewLine = true;

				break;
			}
		}

		// Only process this once

		if (!checkNewLine) {
			if (_log.isInfoEnabled()) {
				_log.info("Do not fix oracle new line");
			}

			return;
		}
		else {
			if (_log.isInfoEnabled()) {
				_log.info("Fix oracle new line");
			}
		}
	}

	protected void verifyPermissions() throws PortalException {
		List<JournalArticle> articles =
			JournalArticleLocalServiceUtil.getNoPermissionArticles();

		for (JournalArticle article : articles) {
			ResourceLocalServiceUtil.addResources(
				article.getCompanyId(), 0, 0, JournalArticle.class.getName(),
				article.getResourcePrimKey(), false, false, false);
		}
	}

	protected void verifyTree() throws Exception {
		long[] companyIds = PortalInstances.getCompanyIdsBySQL();

		for (long companyId : companyIds) {
			JournalFolderLocalServiceUtil.rebuildTree(companyId);
		}
	}

	protected void verifyURLTitle() throws Exception {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = DataAccess.getUpgradeOptimizedConnection();

			ps = con.prepareStatement(
				"select distinct groupId, articleId, urlTitle from " +
					"JournalArticle");

			rs = ps.executeQuery();

			while (rs.next()) {
				long groupId = rs.getLong("groupId");
				String articleId = rs.getString("articleId");
				String urlTitle = GetterUtil.getString(
					rs.getString("urlTitle"));

				updateURLTitle(groupId, articleId, urlTitle);
			}
		}
		finally {
			DataAccess.cleanUp(con, ps, rs);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(VerifyJournal.class);

	private static final Pattern _friendlyURLPattern = Pattern.compile(
		"[^a-z0-9_-]");

}