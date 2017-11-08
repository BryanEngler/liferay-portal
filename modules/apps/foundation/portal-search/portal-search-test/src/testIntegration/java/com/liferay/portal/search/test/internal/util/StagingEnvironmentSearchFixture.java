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

package com.liferay.portal.search.test.internal.util;

import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.exportimport.kernel.staging.StagingConstants;
import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.search.test.journal.util.JournalArticleBuilder;
import com.liferay.portal.search.test.journal.util.JournalArticleContent;
import com.liferay.portal.search.test.journal.util.JournalArticleSearchFixture;
import com.liferay.portal.search.test.journal.util.JournalArticleTitle;
import com.liferay.portlet.documentlibrary.util.test.DLAppTestUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Bryan Engler
 */
public class StagingEnvironmentSearchFixture {

	public Collection<String> getExpectedValuesScopeEverythingFromLive() {
		Collection<String> expectedValues = new HashSet<>();

		expectedValues.add(getTitleWithKeywordPrefix("alpha 1.0 live"));
		expectedValues.add(getTitleWithKeywordPrefix("charlie 1.0 live"));
		expectedValues.add(getTitleWithKeywordPrefix("delta 1.0 nonstaged"));

		return expectedValues;
	}

	public Collection<String> getExpectedValuesScopeEverythingFromNonStaged() {
		return getExpectedValuesScopeEverythingFromLive();
	}

	public Collection<String> getExpectedValuesScopeEverythingFromStaged() {
		Collection<String> expectedValues = new HashSet<>();

		expectedValues.add(getTitleWithKeywordPrefix("alpha 1.1 staged"));
		expectedValues.add(getTitleWithKeywordPrefix("charlie 1.0 live"));
		expectedValues.add(getTitleWithKeywordPrefix("delta 1.0 nonstaged"));

		return expectedValues;
	}

	public Collection<String> getExpectedValuesScopeThisSiteFromLive() {
		Collection<String> expectedValues = new HashSet<>();

		expectedValues.add(getTitleWithKeywordPrefix("alpha 1.0 live"));
		expectedValues.add(getTitleWithKeywordPrefix("charlie 1.0 live"));

		return expectedValues;
	}

	public Collection<String> getExpectedValuesScopeThisSiteFromNonStaged() {
		Collection<String> expectedValues = new HashSet<>();

		expectedValues.add(getTitleWithKeywordPrefix("delta 1.0 nonstaged"));

		return expectedValues;
	}

	public Collection<String> getExpectedValuesScopeThisSiteFromStaged() {
		Collection<String> expectedValues = new HashSet<>();

		expectedValues.add(getTitleWithKeywordPrefix("alpha 1.1 staged"));
		expectedValues.add(getTitleWithKeywordPrefix("charlie 1.0 live"));

		return expectedValues;
	}

	public String getKeyword() {
		return _keyword;
	}

	public Group getLiveGroup() {
		return _liveGroup;
	}

	public Group getNonStagedGroup() {
		return _nonStagedGroup;
	}

	public Group getStagedGroup() {
		return _stagedGroup;
	}

	public void setup() throws Exception {
		_configureStagingEnvironment();

		_keyword = StringUtil.toLowerCase(RandomTestUtil.randomString());

		_createStagedJournalArticles();

		_createLiveJournalArticles();

		_createNonStagedFileEntries();

		_createNonStagedJournalArticles();
	}

	public void tearDown() throws Exception {
		userSearchFixture.tearDown();

		journalArticleSearchFixture.tearDown();

		for (FileEntry fileEntry : _fileEntries) {
			DLAppLocalServiceUtil.deleteFileEntry(fileEntry.getFileEntryId());
		}

		_fileEntries.clear();
	}

	protected FileEntry addFileEntry(String title, User user, Group group)
		throws Exception {

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(group.getGroupId());

		FileEntry fileEntry = DLAppTestUtil.addFileEntryWithWorkflow(
			user.getUserId(), group.getGroupId(), 0, StringPool.BLANK, title,
			true, serviceContext);

		_fileEntries.add(fileEntry);

		return fileEntry;
	}

	protected JournalArticleBuilder createJournalArticleBuilder(
		String title, Group group) {

		JournalArticleBuilder journalArticleBuilder =
			new JournalArticleBuilder();

		journalArticleBuilder.setContent(
			new JournalArticleContent() {
				{
					name = "content";
					defaultLocale = LocaleUtil.US;

					put(LocaleUtil.US, RandomTestUtil.randomString());
				}
			});
		journalArticleBuilder.setGroupId(group.getGroupId());
		journalArticleBuilder.setTitle(
			new JournalArticleTitle() {
				{
					put(LocaleUtil.US, title);
				}
			});

		return journalArticleBuilder;
	}

	protected String getTitleWithKeywordPrefix(String title) {
		return _keyword.concat(StringPool.SPACE).concat(title);
	}

	protected final JournalArticleSearchFixture journalArticleSearchFixture =
		new JournalArticleSearchFixture();
	protected final UserSearchFixture userSearchFixture =
		new UserSearchFixture();

	private void _configureStagingEnvironment() throws Exception {
		_stagedGroup = userSearchFixture.addGroup();

		_liveGroup = userSearchFixture.addGroup();

		_nonStagedGroup = userSearchFixture.addGroup();

		UnicodeProperties stagedGroupTypeSettingsProperties =
			new UnicodeProperties();

		stagedGroupTypeSettingsProperties.setProperty(
			"remoteGroupId", String.valueOf(_liveGroup.getGroupId()));

		_stagedGroup.setTypeSettingsProperties(
			stagedGroupTypeSettingsProperties);

		GroupLocalServiceUtil.updateGroup(_stagedGroup);

		UnicodeProperties liveGroupTypeSettingsProperties =
			new UnicodeProperties();

		AssetRendererFactory<?> factory =
			AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClassName(
				DLFileEntry.class.getName());

		liveGroupTypeSettingsProperties.setProperty(
			StagingConstants.STAGED_PORTLET + factory.getPortletId(), "false");

		factory =
			AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClassName(
				JournalArticle.class.getName());

		liveGroupTypeSettingsProperties.setProperty(
			StagingConstants.STAGED_PORTLET + factory.getPortletId(), "true");

		_liveGroup.setTypeSettingsProperties(liveGroupTypeSettingsProperties);

		GroupLocalServiceUtil.updateGroup(_liveGroup);
	}

	private void _createLiveJournalArticles() throws Exception {
		JournalArticleBuilder journalArticleBuilder =
			createJournalArticleBuilder(
				getTitleWithKeywordPrefix("alpha 1.0 live"), _liveGroup);

		journalArticleSearchFixture.addArticle(journalArticleBuilder);
	}

	private void _createNonStagedFileEntries() throws Exception {
		addFileEntry(
			getTitleWithKeywordPrefix("bravo 1.0 staged"),
			TestPropsValues.getUser(), _stagedGroup);

		addFileEntry(
			getTitleWithKeywordPrefix("charlie 1.0 live"),
			TestPropsValues.getUser(), _liveGroup);
	}

	private void _createNonStagedJournalArticles() throws Exception {
		JournalArticleBuilder journalArticleBuilder =
			createJournalArticleBuilder(
				getTitleWithKeywordPrefix("delta 1.0 nonstaged"),
				_nonStagedGroup);

		journalArticleSearchFixture.addArticle(journalArticleBuilder);
	}

	private void _createStagedJournalArticles() throws Exception {
		JournalArticleBuilder journalArticleBuilder =
			createJournalArticleBuilder(
				getTitleWithKeywordPrefix("alpha 1.0 staged"), _stagedGroup);

		JournalArticle article = journalArticleSearchFixture.addArticle(
			journalArticleBuilder);

		Map<Locale, String> titleMap = article.getTitleMap();

		titleMap.put(
			LocaleUtil.US, getTitleWithKeywordPrefix("alpha 1.1 staged"));

		article.setTitleMap(titleMap);

		journalArticleSearchFixture.updateArticle(article);
	}

	private final List<FileEntry> _fileEntries = new ArrayList<>();
	private String _keyword;
	private Group _liveGroup;
	private Group _nonStagedGroup;
	private Group _stagedGroup;

}