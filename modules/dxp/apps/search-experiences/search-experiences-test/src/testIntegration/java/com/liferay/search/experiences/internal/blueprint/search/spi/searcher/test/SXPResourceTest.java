/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.search.experiences.internal.blueprint.search.spi.searcher.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.test.util.DLAppTestUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.vulcan.accept.language.AcceptLanguage;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.search.experiences.rest.dto.v1_0.SXPBlueprint;
import com.liferay.search.experiences.rest.dto.v1_0.SXPElement;
import com.liferay.search.experiences.rest.resource.v1_0.SXPBlueprintResource;
import com.liferay.search.experiences.rest.resource.v1_0.SXPElementResource;
import com.liferay.search.experiences.service.SXPBlueprintLocalService;
import com.liferay.search.experiences.service.SXPElementLocalService;
import com.liferay.wiki.model.WikiNode;
import com.liferay.wiki.model.WikiPage;
import com.liferay.wiki.service.WikiNodeLocalService;
import com.liferay.wiki.service.WikiPageLocalService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Bryan Engler
 */
@RunWith(Arquillian.class)
public class SXPResourceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_sxpElementResource.setContextAcceptLanguage(new TestAcceptLanguage());
		_sxpBlueprintResource.setContextAcceptLanguage(
			new TestAcceptLanguage());

		HttpServletRequest httpServletRequest = new MockHttpServletRequest(
			"GET", "");

		httpServletRequest.setAttribute(WebKeys.CURRENT_URL, "");

		_sxpElementResource.setContextHttpServletRequest(httpServletRequest);
		_sxpBlueprintResource.setContextHttpServletRequest(httpServletRequest);
	}

	@Test
	public void test() throws Exception {
		_addFileEntry("alpha");
		_addWikiPage("alpha");

		//create element via REST api
		String elementJSON = StringUtil.read(
			getClass(), "dependencies/boost_asset_type.json");

		SXPElement sxpElement = _sxpElementResource.postSXPElement(
			SXPElement.toDTO(elementJSON));

		//fetch element model from DB
		com.liferay.search.experiences.model.SXPElement sxpElementModel =
			_sxpElementLocalService.fetchSXPElement(sxpElement.getId());

		//create blueprint via REST api
		String blueprintJSON = StringUtil.read(
			getClass(), "dependencies/default_blueprint.json");

		SXPBlueprint sxpBlueprint = _sxpBlueprintResource.postSXPBlueprint(
			SXPBlueprint.toDTO(blueprintJSON));

		//fetch blueprint model from DB
		com.liferay.search.experiences.model.SXPBlueprint sxpBlueprintModel =
			_sxpBlueprintLocalService.fetchSXPBlueprint(sxpBlueprint.getId());

		//set element in blueprint?
		//sxpBlueprintModel.setConfigurationJSON(sxpElement.toString());
		//sxpBlueprintModel.setElementInstancesJSON(sxpElementModel.getElementDefinitionJSON());

		//save blueprint via REST api
		DTOConverter
			<com.liferay.search.experiences.model.SXPBlueprint, SXPBlueprint>
				dtoConverter = _getDTOConverter();

		_sxpBlueprintResource.patchSXPBlueprint(
			sxpBlueprint.getId(), dtoConverter.toDTO(sxpBlueprintModel));

		//search using blueprint with WikiPage Boost
		_assertSearch(
			sxpBlueprintModel.getSXPBlueprintId(),
			new String[] {
				WikiPage.class.getName(), DLFileEntry.class.getName()
			},
			"entryClassName", "alpha");

		//update blueprint config. classname -> com.liferay.document.library.kernel.model.DLFileEntry
		//sxpBlueprintModel.setConfigurationJSON(sxpElement.toString());
		//sxpBlueprintModel.setElementInstancesJSON(sxpElementModel.getElementDefinitionJSON());

		//save blueprint via REST api
		_sxpBlueprintResource.patchSXPBlueprint(
			sxpBlueprint.getId(), dtoConverter.toDTO(sxpBlueprintModel));

		//search using blueprint with DLFileEntry Boost
		_assertSearch(
			sxpBlueprintModel.getSXPBlueprintId(),
			new String[] {
				DLFileEntry.class.getName(), WikiPage.class.getName()
			},
			"entryClassName", "alpha");
	}

	public class TestAcceptLanguage implements AcceptLanguage {

		@Override
		public List<Locale> getLocales() {
			return null;
		}

		@Override
		public String getPreferredLanguageId() {
			return "en_US";
		}

		@Override
		public Locale getPreferredLocale() {
			return LocaleUtil.ENGLISH;
		}

	}

	private void _addFileEntry(String title) throws Exception {
		_fileEntries.add(
			DLAppTestUtil.addFileEntryWithWorkflow(
				TestPropsValues.getUserId(), TestPropsValues.getGroupId(), 0,
				StringPool.BLANK, title, true,
				ServiceContextTestUtil.getServiceContext(
					TestPropsValues.getGroupId())));
	}

	private void _addWikiPage(String title) throws Exception {
		ServiceContext serviceContext = _createServiceContext();

		WikiNode wikiNode = _wikiNodeLocalService.addDefaultNode(
			TestPropsValues.getUserId(), serviceContext);

		_wikiNodes.add(wikiNode);

		_wikiPages.add(
			_wikiPageLocalService.addPage(
				TestPropsValues.getUserId(), wikiNode.getNodeId(), title,
				"content", "Summary", false, serviceContext));
	}

	private void _assertSearch(
			long blueprintId, String[] expected, String fieldName,
			String keywords)
		throws Exception {

		SearchResponse searchResponse = _searcher.search(
			_searchRequestBuilderFactory.builder(
			).companyId(
				TestPropsValues.getCompanyId()
			).queryString(
				keywords
			).withSearchContext(
				searchContext -> searchContext.setAttribute(
					"search.experiences.blueprint.id", blueprintId)
			).build());

		_assertValues(
			searchResponse.getRequestString(),
			searchResponse.getDocumentsStream(), fieldName, expected);
	}

	private void _assertValues(
		String message, Stream<Document> stream, String fieldName,
		String[] expected) {

		Document[] documents = stream.toArray(Document[]::new);

		Assert.assertEquals(message, documents.length, expected.length);

		for (int i = 0; i < documents.length; i++) {
			Document document = documents[i];

			Assert.assertEquals(
				message, expected[i], document.getString(fieldName));
		}
	}

	private ServiceContext _createServiceContext() throws Exception {
		return ServiceContextTestUtil.getServiceContext(
			TestPropsValues.getGroupId(), TestPropsValues.getUserId());
	}

	private DTOConverter
		<com.liferay.search.experiences.model.SXPBlueprint, SXPBlueprint>
			_getDTOConverter() {

		String dtoClassName =
			com.liferay.search.experiences.model.SXPBlueprint.class.getName();

		return (DTOConverter
			<com.liferay.search.experiences.model.SXPBlueprint, SXPBlueprint>)
				_dtoConverterRegistry.getDTOConverter(dtoClassName);
	}

	@Inject
	private DTOConverterRegistry _dtoConverterRegistry;

	@DeleteAfterTestRun
	private List<FileEntry> _fileEntries = new ArrayList<>();

	@Inject
	private Searcher _searcher;

	@Inject
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

	@Inject
	private SXPBlueprintLocalService _sxpBlueprintLocalService;

	@Inject
	private SXPBlueprintResource _sxpBlueprintResource;

	@Inject
	private SXPElementLocalService _sxpElementLocalService;

	@Inject
	private SXPElementResource _sxpElementResource;

	@Inject
	private WikiNodeLocalService _wikiNodeLocalService;

	@DeleteAfterTestRun
	private final List<WikiNode> _wikiNodes = new ArrayList<>();

	@Inject
	private WikiPageLocalService _wikiPageLocalService;

	@DeleteAfterTestRun
	private final List<WikiPage> _wikiPages = new ArrayList<>();

}