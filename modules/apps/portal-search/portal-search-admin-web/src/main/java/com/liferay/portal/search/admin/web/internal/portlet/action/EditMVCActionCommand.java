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

package com.liferay.portal.search.admin.web.internal.portlet.action;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.list.model.AssetListEntry;
import com.liferay.asset.list.service.AssetListEntryLocalServiceUtil;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;
import com.liferay.dynamic.data.mapping.storage.Fields;
import com.liferay.dynamic.data.mapping.util.DDMIndexer;
import com.liferay.dynamic.data.mapping.util.FieldsToDDMFormValuesConverter;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.journal.util.JournalConverter;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.instances.service.PortalInstancesLocalService;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskConstants;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManager;
import com.liferay.portal.kernel.messaging.DestinationNames;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBus;
import com.liferay.portal.kernel.messaging.MessageListener;
import com.liferay.portal.kernel.messaging.MessageListenerException;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentImpl;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.IndexWriterHelper;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.uuid.PortalUUID;
import com.liferay.portal.search.admin.web.internal.constants.SearchAdminPortletKeys;
import com.liferay.portal.search.configuration.CustomRelevanceConfiguration;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.UpdateDocumentRequest;

import java.io.Serializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletSession;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Wade Cao
 */
@Component(
	configurationPid = "com.liferay.portal.search.configuration.CustomRelevanceConfiguration",
	property = {
		"javax.portlet.name=" + SearchAdminPortletKeys.SEARCH_ADMIN,
		"mvc.command.name=/search_admin/edit"
	},
	service = MVCActionCommand.class
)
public class EditMVCActionCommand extends BaseMVCActionCommand {

	@Override
	public void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		PermissionChecker permissionChecker =
			themeDisplay.getPermissionChecker();

		if (!permissionChecker.isOmniadmin()) {
			SessionErrors.add(
				actionRequest,
				PrincipalException.MustBeOmniadmin.class.getName());

			actionResponse.setRenderParameter("mvcPath", "/error.jsp");

			return;
		}

		String cmd = ParamUtil.getString(actionRequest, Constants.CMD);

		String redirect = ParamUtil.getString(actionRequest, "redirect");

		if (cmd.equals("reindex")) {
			reindex(actionRequest);
		}
		else if (cmd.equals("reindexDictionaries")) {
			reindexDictionaries(actionRequest);
		}
		else if (cmd.equals("applyCustomRelevance")) {
			applyCustomRelevance(actionRequest, themeDisplay);
		}

		sendRedirect(actionRequest, actionResponse, redirect);
	}

	protected void applyCustomRelevance(
		ActionRequest actionRequest, ThemeDisplay themeDisplay) throws Exception {

		//get the field name from the config
		String boostFieldName = customRelevanceConfiguration.boostFieldName();

		//get the journalaritcle Id from the config
		long templateJournalArticleId = customRelevanceConfiguration.templateAssetId();

		//get the journalaritcle from the Id

		//journal-api
		JournalArticle journalArticle =
			JournalArticleLocalServiceUtil.fetchJournalArticle(
				templateJournalArticleId);

		//get the content/boostwords from the template journalarticle
		String content = extractDDMContent(journalArticle, "en_US");

		//get the assetlistId from the config
		long assetListEntryId = customRelevanceConfiguration.assetListId();

		//get the asset list from the assetlistId
		//asset-list-api
		AssetListEntry assetListEntry =
			AssetListEntryLocalServiceUtil.fetchAssetListEntry(
				assetListEntryId);

		//quick way to test boosting without having to create journal article/structure/template
		boolean useAssetListTitleAsBoostWords = false;

		if (useAssetListTitleAsBoostWords) {
			content = assetListEntry.getTitle();
		}

		//get the assets in the list
		List<AssetEntry> assetEntries = assetListEntry.getAssetEntries();

		//for each asset, add the field and content to the document and index it
		for (AssetEntry assetEntry : assetEntries) {
			String uid = Field.getUID(
				assetEntry.getClassName(),
				String.valueOf(assetEntry.getClassPK())); //WARNING, may not work for JournalArticle
			//see JournalArticleIndexer.doGetDocument() for document.addUID with ClassPK

			Document document = new DocumentImpl();

			document.addKeyword(Field.UID, uid);
			document.addText(boostFieldName, content);

			//_indexWriterHelper.partiallyUpdateDocument(
			//	null, themeDisplay.getCompanyId(), document, true);


			//use low level api

			String indexName = "liferay-" + themeDisplay.getCompanyId();

			UpdateDocumentRequest updateDocumentRequest =
				new UpdateDocumentRequest(indexName, uid, document);

			updateDocumentRequest.setType("LiferayDocumentType");

			searchEngineAdapter.execute(updateDocumentRequest);
		}
	}

	protected String extractDDMContent(
			JournalArticle article, String languageId)
		throws Exception {

		DDMStructure ddmStructure = _ddmStructureLocalService.fetchStructure(
			_portal.getSiteGroupId(article.getGroupId()),
			_portal.getClassNameId(JournalArticle.class),
			article.getDDMStructureKey(), true);

		if (ddmStructure == null) {
			return StringPool.BLANK;
		}

		DDMFormValues ddmFormValues = null;

		try {
			Fields fields = _journalConverter.getDDMFields(
				ddmStructure, article.getDocument());

			ddmFormValues = _fieldsToDDMFormValuesConverter.convert(
				ddmStructure, fields);
		}
		catch (Exception e) {
			return StringPool.BLANK;
		}

		if (ddmFormValues == null) {
			return StringPool.BLANK;
		}

		return _ddmIndexer.extractIndexableAttributes(
			ddmStructure, ddmFormValues, LocaleUtil.fromLanguageId(languageId));
	}


	@Reference(unbind = "-")
	protected void setJournalConverter(JournalConverter journalConverter) {
		_journalConverter = journalConverter;
	}

	private JournalConverter _journalConverter;

	@Reference(unbind = "-")
	protected void setDDMStructureLocalService(
		DDMStructureLocalService ddmStructureLocalService) {

		_ddmStructureLocalService = ddmStructureLocalService;
	}

	private DDMStructureLocalService _ddmStructureLocalService;

	@Reference
	private Portal _portal;

	private DDMIndexer _ddmIndexer;


	@Reference(unbind = "-")
	protected void setDDMIndexer(DDMIndexer ddmIndexer) {
		_ddmIndexer = ddmIndexer;
	}

	private FieldsToDDMFormValuesConverter _fieldsToDDMFormValuesConverter;


	@Reference(unbind = "-")
	protected void setFieldsToDDMFormValuesConverter(
		FieldsToDDMFormValuesConverter fieldsToDDMFormValuesConverter) {

		_fieldsToDDMFormValuesConverter = fieldsToDDMFormValuesConverter;
	}

	protected void reindex(final ActionRequest actionRequest) throws Exception {
		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		Map<String, Serializable> taskContextMap = new HashMap<>();
		String className = ParamUtil.getString(actionRequest, "className");

		if (!ParamUtil.getBoolean(actionRequest, "blocking")) {
			_indexWriterHelper.reindex(
				themeDisplay.getUserId(), "reindex",
				_portalInstancesLocalService.getCompanyIds(), className,
				taskContextMap);

			return;
		}

		final String jobName = "reindex-".concat(_portalUUID.generate());

		final CountDownLatch countDownLatch = new CountDownLatch(1);

		MessageListener messageListener = new MessageListener() {

			@Override
			public void receive(Message message)
				throws MessageListenerException {

				int status = message.getInteger("status");

				if ((status != BackgroundTaskConstants.STATUS_CANCELLED) &&
					(status != BackgroundTaskConstants.STATUS_FAILED) &&
					(status != BackgroundTaskConstants.STATUS_SUCCESSFUL)) {

					return;
				}

				if (!jobName.equals(message.getString("name"))) {
					return;
				}

				PortletSession portletSession =
					actionRequest.getPortletSession();

				long lastAccessedTime = portletSession.getLastAccessedTime();
				int maxInactiveInterval =
					portletSession.getMaxInactiveInterval();

				int extendedMaxInactiveIntervalTime =
					(int)(System.currentTimeMillis() - lastAccessedTime +
						maxInactiveInterval);

				portletSession.setMaxInactiveInterval(
					extendedMaxInactiveIntervalTime);

				countDownLatch.countDown();
			}

		};

		_messageBus.registerMessageListener(
			DestinationNames.BACKGROUND_TASK_STATUS, messageListener);

		try {
			_indexWriterHelper.reindex(
				themeDisplay.getUserId(), jobName,
				_portalInstancesLocalService.getCompanyIds(), className,
				taskContextMap);

			countDownLatch.await(
				ParamUtil.getLong(actionRequest, "timeout", Time.HOUR),
				TimeUnit.MILLISECONDS);
		}
		finally {
			_messageBus.unregisterMessageListener(
				DestinationNames.BACKGROUND_TASK_STATUS, messageListener);
		}
	}

	protected void reindexDictionaries(ActionRequest actionRequest)
		throws Exception {

		long[] companyIds = _portalInstancesLocalService.getCompanyIds();

		for (long companyId : companyIds) {
			_indexWriterHelper.indexQuerySuggestionDictionaries(companyId);
			_indexWriterHelper.indexSpellCheckerDictionaries(companyId);
		}
	}

	@Reference
	private BackgroundTaskManager _backgroundTaskManager;

	@Reference
	private IndexWriterHelper _indexWriterHelper;

	@Reference
	private MessageBus _messageBus;

	@Reference
	private PortalInstancesLocalService _portalInstancesLocalService;

	@Reference
	private PortalUUID _portalUUID;

	@Reference
	protected SearchEngineAdapter searchEngineAdapter;

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		customRelevanceConfiguration =
			ConfigurableUtil.createConfigurable(
				CustomRelevanceConfiguration.class, properties);
	}

	protected CustomRelevanceConfiguration customRelevanceConfiguration;

}