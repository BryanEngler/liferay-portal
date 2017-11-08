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

package com.liferay.portal.search.facet.internal.faceted.searcher;

import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.expando.kernel.model.ExpandoBridge;
import com.liferay.expando.kernel.model.ExpandoColumnConstants;
import com.liferay.expando.kernel.util.ExpandoBridgeFactory;
import com.liferay.exportimport.kernel.staging.StagingConstants;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.search.BaseSearcher;
import com.liferay.portal.kernel.search.BooleanClause;
import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.IndexSearcherHelper;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerPostProcessor;
import com.liferay.portal.kernel.search.IndexerRegistry;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.QueryConfig;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchEngineHelper;
import com.liferay.portal.kernel.search.SearchEngineHelperUtil;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.search.SearchPermissionChecker;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcher;
import com.liferay.portal.kernel.search.filter.BooleanFilter;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.search.filter.TermsFilter;
import com.liferay.portal.kernel.search.generic.BooleanQueryImpl;
import com.liferay.portal.kernel.search.generic.MatchAllQuery;
import com.liferay.portal.kernel.search.generic.StringQuery;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.permission.SearchPermissionFilterContributor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

/**
 * @author Raymond Augé
 */
public class FacetedSearcherImpl
	extends BaseSearcher implements FacetedSearcher {

	public FacetedSearcherImpl(
		ExpandoBridgeFactory expandoBridgeFactory,
		GroupLocalService groupLocalService, IndexerRegistry indexerRegistry,
		IndexSearcherHelper indexSearcherHelper,
		SearchEngineHelper searchEngineHelper,
		Collection<SearchPermissionFilterContributor>
			searchPermissionFilterContributors) {

		_expandoBridgeFactory = expandoBridgeFactory;
		_groupLocalService = groupLocalService;
		_indexerRegistry = indexerRegistry;
		_indexSearcherHelper = indexSearcherHelper;
		_searchEngineHelper = searchEngineHelper;
		_searchPermissionFilterContributors =
			searchPermissionFilterContributors;
	}

	protected void addSearchExpando(
			BooleanQuery searchQuery, SearchContext searchContext,
			String keywords, String className)
		throws Exception {

		ExpandoBridge expandoBridge = _expandoBridgeFactory.getExpandoBridge(
			searchContext.getCompanyId(), className);

		Set<String> attributeNames = SetUtil.fromEnumeration(
			expandoBridge.getAttributeNames());

		for (String attributeName : attributeNames) {
			UnicodeProperties properties = expandoBridge.getAttributeProperties(
				attributeName);

			int indexType = GetterUtil.getInteger(
				properties.getProperty(ExpandoColumnConstants.INDEX_TYPE));

			if (indexType != ExpandoColumnConstants.INDEX_TYPE_NONE) {
				String fieldName = getExpandoFieldName(
					searchContext, expandoBridge, attributeName);

				if (searchContext.isAndSearch()) {
					searchQuery.addRequiredTerm(fieldName, keywords);
				}
				else {
					searchQuery.addTerm(fieldName, keywords);
				}
			}
		}
	}

	protected void addSearchKeywords(
			BooleanQuery searchQuery, String keywords, boolean luceneSyntax,
			Map<String, Indexer<?>> entryClassNameIndexerMap,
			SearchContext searchContext)
		throws Exception {

		if (Validator.isNull(keywords)) {
			return;
		}

		if (luceneSyntax) {
			searchQuery.add(new StringQuery(keywords), BooleanClauseOccur.MUST);
		}
		else {
			addSearchLocalizedTerm(
				searchQuery, searchContext, Field.ASSET_CATEGORY_TITLES, false);

			searchQuery.addTerm(Field.ASSET_TAG_NAMES, keywords);

			searchQuery.addTerms(Field.KEYWORDS, keywords);

			for (String entryClassName : entryClassNameIndexerMap.keySet()) {
				addSearchExpando(
					searchQuery, searchContext, keywords, entryClassName);
			}
		}
	}

	protected BooleanQuery createFullQuery(SearchContext searchContext)
		throws Exception {

		BooleanQuery fullQuery = new BooleanQueryImpl();

		BooleanQuery searchQuery = new BooleanQueryImpl();

		String keywords = searchContext.getKeywords();

		boolean luceneSyntax = GetterUtil.getBoolean(
			searchContext.getAttribute("luceneSyntax"));

		Map<String, Indexer<?>> entryClassNameIndexerMap =
			_getEntryClassNameIndexerMap(
				_getEntryClassNames(searchContext),
				searchContext.getSearchEngineId());

		addSearchKeywords(
			searchQuery, keywords, luceneSyntax, entryClassNameIndexerMap,
			searchContext);

		BooleanFilter fullQueryPreBooleanFilter = new BooleanFilter();

		_addSearchTerms(
			searchQuery, fullQueryPreBooleanFilter, luceneSyntax,
			entryClassNameIndexerMap, searchContext);

		if (searchQuery.hasClauses()) {
			fullQuery.add(searchQuery, BooleanClauseOccur.MUST);
		}

		_addPreFilters(
			fullQueryPreBooleanFilter, entryClassNameIndexerMap, searchContext);

		if (fullQueryPreBooleanFilter.hasClauses()) {
			fullQuery.setPreBooleanFilter(fullQueryPreBooleanFilter);
		}

		BooleanFilter fullQueryPostBooleanFilter = new BooleanFilter();

		Map<String, Facet> facets = searchContext.getFacets();

		for (Facet facet : facets.values()) {
			BooleanClause<Filter> facetClause =
				facet.getFacetFilterBooleanClause();

			if (facetClause != null) {
				fullQueryPostBooleanFilter.add(
					facetClause.getClause(),
					facetClause.getBooleanClauseOccur());
			}
		}

		addFacetClause(
			searchContext, fullQueryPostBooleanFilter, facets.values());

		if (fullQueryPostBooleanFilter.hasClauses()) {
			fullQuery.setPostFilter(fullQueryPostBooleanFilter);
		}

		BooleanClause<Query>[] booleanClauses =
			searchContext.getBooleanClauses();

		if (booleanClauses != null) {
			for (BooleanClause<Query> booleanClause : booleanClauses) {
				fullQuery.add(
					booleanClause.getClause(),
					booleanClause.getBooleanClauseOccur());
			}
		}

		_postProcessFullQuery(
			entryClassNameIndexerMap, fullQuery, searchContext);

		return fullQuery;
	}

	@Override
	protected Hits doSearch(SearchContext searchContext)
		throws SearchException {

		try {
			searchContext.setSearchEngineId(getSearchEngineId());

			Query fullQuery = createFullQuery(searchContext);

			if (!fullQuery.hasChildren()) {
				BooleanFilter preBooleanFilter =
					fullQuery.getPreBooleanFilter();

				fullQuery = new MatchAllQuery();

				fullQuery.setPreBooleanFilter(preBooleanFilter);
			}

			QueryConfig queryConfig = searchContext.getQueryConfig();

			fullQuery.setQueryConfig(queryConfig);

			return _indexSearcherHelper.search(searchContext, fullQuery);
		}
		catch (Exception e) {
			throw new SearchException(e);
		}
	}

	@Override
	protected boolean isUseSearchResultPermissionFilter(
		SearchContext searchContext) {

		String[] entryClassNames = _getEntryClassNames(searchContext);

		if (ArrayUtil.isEmpty(entryClassNames)) {
			return super.isFilterSearch();
		}

		for (String entryClassName : entryClassNames) {
			Indexer<?> indexer = _indexerRegistry.getIndexer(entryClassName);

			if (indexer == null) {
				continue;
			}

			if (indexer.isFilterSearch()) {
				return true;
			}
		}

		return super.isFilterSearch();
	}

	private void _addInactiveGroupsBooleanFilter(
		BooleanFilter fullQueryPreBooleanFilter, SearchContext searchContext) {

		List<Group> inactiveGroups = _groupLocalService.getActiveGroups(
			searchContext.getCompanyId(), false);

		if (ListUtil.isNotEmpty(inactiveGroups)) {
			TermsFilter groupIdTermsFilter = new TermsFilter(Field.GROUP_ID);

			groupIdTermsFilter.addValues(
				ArrayUtil.toStringArray(
					ListUtil.toArray(inactiveGroups, Group.GROUP_ID_ACCESSOR)));

			fullQueryPreBooleanFilter.add(
				groupIdTermsFilter, BooleanClauseOccur.MUST_NOT);
		}
	}

	private void _addIndexerProvidedPreFilters(
			BooleanFilter contextBooleanFilter, Indexer<?> indexer,
			SearchContext searchContext)
		throws Exception {

		indexer.postProcessContextBooleanFilter(
			contextBooleanFilter, searchContext);

		for (IndexerPostProcessor indexerPostProcessor :
				indexer.getIndexerPostProcessors()) {

			indexerPostProcessor.postProcessContextBooleanFilter(
				contextBooleanFilter, searchContext);
		}
	}

	private void _addIndexerProvidedSearchTerms(
			BooleanQuery searchQuery, Indexer<?> indexer,
			BooleanFilter booleanFilter, boolean luceneSyntax,
			SearchContext searchContext)
		throws Exception {

		if (!luceneSyntax) {
			indexer.postProcessSearchQuery(
				searchQuery, booleanFilter, searchContext);
		}

		for (IndexerPostProcessor indexerPostProcessor :
				indexer.getIndexerPostProcessors()) {

			indexerPostProcessor.postProcessSearchQuery(
				searchQuery, booleanFilter, searchContext);
		}
	}

	private void _addOwnerBooleanFilter(
		BooleanFilter fullQueryPreBooleanFilter, SearchContext searchContext) {

		long ownerUserId = searchContext.getOwnerUserId();

		if (ownerUserId > 0) {
			fullQueryPreBooleanFilter.addRequiredTerm(
				Field.USER_ID, ownerUserId);
		}
	}

	private void _addPermissionFilter(
			BooleanFilter contextBooleanFilter, String entryClassName,
			SearchContext searchContext)
		throws Exception {

		if (searchContext.getUserId() == 0) {
			return;
		}

		SearchPermissionChecker searchPermissionChecker =
			SearchEngineHelperUtil.getSearchPermissionChecker();

		Optional<String> parentEntryClassNameOptional =
			_getParentEntryClassName(entryClassName);

		String permissionedEntryClassName = parentEntryClassNameOptional.orElse(
			entryClassName);

		searchPermissionChecker.getPermissionBooleanFilter(
			searchContext.getCompanyId(), searchContext.getGroupIds(),
			searchContext.getUserId(), permissionedEntryClassName,
			contextBooleanFilter, searchContext);
	}

	private void _addPreFilters(
			BooleanFilter fullQueryPreBooleanFilter,
			Map<String, Indexer<?>> entryClassNameIndexerMap,
			SearchContext searchContext)
		throws Exception {

		fullQueryPreBooleanFilter.addRequiredTerm(
			Field.COMPANY_ID, searchContext.getCompanyId());

		_addInactiveGroupsBooleanFilter(
			fullQueryPreBooleanFilter, searchContext);
		_addOwnerBooleanFilter(fullQueryPreBooleanFilter, searchContext);

		for (Entry<String, Indexer<?>> entry :
				entryClassNameIndexerMap.entrySet()) {

			String entryClassName = entry.getKey();
			Indexer<?> indexer = entry.getValue();

			fullQueryPreBooleanFilter.add(
				_createPreFilterForEntryClassName(
					entryClassName, indexer, searchContext),
				BooleanClauseOccur.SHOULD);
		}
	}

	private void _addScopeBooleanFilter(
			BooleanFilter contextBooleanFilter, String entryClassName,
			boolean stagingAware, SearchContext searchContext)
		throws Exception {

		long[] groupIds = searchContext.getGroupIds();

		if (ArrayUtil.isNotEmpty(groupIds)) {
			BooleanFilter siteBooleanFilter = new BooleanFilter();

			for (int i = 0; i < groupIds.length; i++) {
				long filterBySiteGroupId = groupIds[i];

				if (isStagingGroup(filterBySiteGroupId) &&
					!_isDataStaged(filterBySiteGroupId, entryClassName)) {

					filterBySiteGroupId = _getLiveGroupId(filterBySiteGroupId);
				}

				if (filterBySiteGroupId != 0) {
					siteBooleanFilter.addTerm(
						Field.GROUP_ID, filterBySiteGroupId);
				}
			}

			if (siteBooleanFilter.hasClauses()) {
				contextBooleanFilter.add(
					siteBooleanFilter, BooleanClauseOccur.MUST);
			}

			return;
		}

		if (!stagingAware) {
			return;
		}

		long scopeGroupId = GetterUtil.getLong(
			searchContext.getAttribute(Field.SCOPE_GROUP_ID));

		if (isStagingGroup(scopeGroupId)) {
			if (_isDataStaged(scopeGroupId, entryClassName)) {
				contextBooleanFilter.addTerm(Field.GROUP_ID, scopeGroupId);

				BooleanFilter liveGroupsPreBooleanFilter = new BooleanFilter();

				liveGroupsPreBooleanFilter.addTerm(
					Field.STAGING_GROUP, "true", BooleanClauseOccur.MUST_NOT);
				liveGroupsPreBooleanFilter.addTerm(
					Field.GROUP_ID,
					String.valueOf(_getLiveGroupId(scopeGroupId)),
					BooleanClauseOccur.MUST_NOT);

				contextBooleanFilter.add(liveGroupsPreBooleanFilter);
			}
			else {
				contextBooleanFilter.addTerm(
					Field.STAGING_GROUP, "true", BooleanClauseOccur.MUST_NOT);
			}
		}
		else {
			contextBooleanFilter.addTerm(
				Field.STAGING_GROUP, "true", BooleanClauseOccur.MUST_NOT);
		}
	}

	private void _addSearchTerms(
			BooleanQuery searchQuery, BooleanFilter fullQueryBooleanFilter,
			boolean luceneSyntax,
			Map<String, Indexer<?>> entryClassNameIndexerMap,
			SearchContext searchContext)
		throws Exception {

		for (Indexer<?> indexer : entryClassNameIndexerMap.values()) {
			_addIndexerProvidedSearchTerms(
				searchQuery, indexer, fullQueryBooleanFilter, luceneSyntax,
				searchContext);
		}
	}

	private Filter _createPreFilterForEntryClassName(
			String entryClassName, Indexer<?> indexer,
			SearchContext searchContext)
		throws Exception {

		BooleanFilter contextBooleanFilter = new BooleanFilter();

		contextBooleanFilter.addTerm(
			Field.ENTRY_CLASS_NAME, entryClassName, BooleanClauseOccur.MUST);

		_addScopeBooleanFilter(
			contextBooleanFilter, entryClassName, indexer.isStagingAware(),
			searchContext);

		_addPermissionFilter(
			contextBooleanFilter, entryClassName, searchContext);

		_addIndexerProvidedPreFilters(
			contextBooleanFilter, indexer, searchContext);

		return contextBooleanFilter;
	}

	private Map<String, Indexer<?>> _getEntryClassNameIndexerMap(
		String[] entryClassNames, String searchEngineId) {

		Map<String, Indexer<?>> entryClassNameIndexerMap = new LinkedHashMap<>(
			entryClassNames.length);

		for (String entryClassName : entryClassNames) {
			Indexer<?> indexer = _indexerRegistry.getIndexer(entryClassName);

			if (indexer == null) {
				continue;
			}

			if (!searchEngineId.equals(indexer.getSearchEngineId())) {
				continue;
			}

			entryClassNameIndexerMap.put(entryClassName, indexer);
		}

		return entryClassNameIndexerMap;
	}

	private String[] _getEntryClassNames(SearchContext searchContext) {
		String[] entryClassNames = searchContext.getEntryClassNames();

		if (!ArrayUtil.isEmpty(entryClassNames)) {
			return entryClassNames;
		}

		return _searchEngineHelper.getEntryClassNames();
	}

	private long _getLiveGroupId(long groupId) throws Exception {
		Group group = _groupLocalService.fetchGroup(groupId);

		if (group == null) {
			return 0;
		}

		long liveGroupId = group.getLiveGroupId();

		if (liveGroupId == 0) {
			liveGroupId = Long.valueOf(
				group.getTypeSettingsProperty("remoteGroupId"));
		}

		return liveGroupId;
	}

	private Optional<String> _getParentEntryClassName(String entryClassName) {
		for (SearchPermissionFilterContributor
				searchPermissionFilterContributor :
					_searchPermissionFilterContributors) {

			Optional<String> parentEntryClassNameOptional =
				searchPermissionFilterContributor.
					getParentEntryClassNameOptional(entryClassName);

			if ((parentEntryClassNameOptional != null) &&
				parentEntryClassNameOptional.isPresent()) {

				return parentEntryClassNameOptional;
			}
		}

		return Optional.empty();
	}

	private boolean _isDataStaged(long groupId, String entryClassName)
		throws Exception {

		long liveGroupId = _getLiveGroupId(groupId);

		Group liveGroup = _groupLocalService.fetchGroup(liveGroupId);

		if (liveGroup == null) {
			return false;
		}

		AssetRendererFactory<?> factory =
			AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClassName(
				entryClassName);

		if (factory == null) {
			return false;
		}

		boolean dataIsStaged = GetterUtil.getBoolean(
			liveGroup.getTypeSettingsProperty(
				StagingConstants.STAGED_PORTLET + factory.getPortletId()));

		return dataIsStaged;
	}

	private void _postProcessFullQuery(
			Map<String, Indexer<?>> entryClassNameIndexerMap,
			BooleanQuery fullQuery, SearchContext searchContext)
		throws Exception {

		for (Indexer<?> indexer : entryClassNameIndexerMap.values()) {
			for (IndexerPostProcessor indexerPostProcessor :
					indexer.getIndexerPostProcessors()) {

				indexerPostProcessor.postProcessFullQuery(
					fullQuery, searchContext);
			}
		};
	}

	private final ExpandoBridgeFactory _expandoBridgeFactory;
	private final GroupLocalService _groupLocalService;
	private final IndexerRegistry _indexerRegistry;
	private final IndexSearcherHelper _indexSearcherHelper;
	private final SearchEngineHelper _searchEngineHelper;
	private final Collection<SearchPermissionFilterContributor>
		_searchPermissionFilterContributors;

}