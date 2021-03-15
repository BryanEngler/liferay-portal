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

package com.liferay.portal.search.internal.info.list.provider;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.service.AssetEntryService;
import com.liferay.info.list.provider.InfoListProvider;
import com.liferay.info.list.provider.InfoListProviderContext;
import com.liferay.info.pagination.Pagination;
import com.liferay.info.sort.Sort;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.search.sort.FieldSort;
import com.liferay.portal.search.sort.SortOrder;
import com.liferay.portal.search.sort.Sorts;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * @author Bryan Engler
 */
public class BlueprintInfoListProvider implements InfoListProvider<AssetEntry> {

	public BlueprintInfoListProvider(
		long companyId, String blueprintId, String collectionProviderName,
		AssetEntryService assetEntryService, Searcher searcher, Sorts sorts,
		SearchRequestBuilderFactory searchRequestBuilderFactory) {

		_companyId = companyId;
		_blueprintId = blueprintId;
		_collectionProviderName = collectionProviderName;
		_assetEntryService = assetEntryService;
		_searcher = searcher;
		_sorts = sorts;
		_searchRequestBuilderFactory = searchRequestBuilderFactory;
	}

	@Override
	public List<AssetEntry> getInfoList(
		InfoListProviderContext infoListProviderContext) {

		SearchResponse searchResponse = getSearchResponse(
			infoListProviderContext);

		return getAssetEntries(searchResponse.getSearchHits());
	}

	@Override
	public List<AssetEntry> getInfoList(
		InfoListProviderContext infoListProviderContext, Pagination pagination,
		Sort sort) {

		SearchRequestBuilder searchRequestBuilder = getSearchRequestBuilder(
			infoListProviderContext);

		if (pagination != null) {
			searchRequestBuilder.from(
				pagination.getStart()
			).size(
				pagination.getDelta()
			);
		}

		if (sort != null) {
			SortOrder sortOrder = SortOrder.ASC;

			if (sort.isReverse()) {
				sortOrder = SortOrder.DESC;
			}

			FieldSort fieldSort = _sorts.field(sort.getFieldName(), sortOrder);

			searchRequestBuilder.sorts(fieldSort);
		}

		SearchResponse searchResponse = _searcher.search(
			searchRequestBuilder.build());

		return getAssetEntries(searchResponse.getSearchHits());
	}

	@Override
	public int getInfoListCount(
		InfoListProviderContext infoListProviderContext) {

		SearchResponse searchResponse = getSearchResponse(
			infoListProviderContext);

		SearchHits searchHits = searchResponse.getSearchHits();

		Long totalHits = Long.valueOf(searchHits.getTotalHits());

		return totalHits.intValue();
	}

	@Override
	public String getKey() {
		Class<?> clazz = getClass();

		return clazz.getName() + "_" + _collectionProviderName;
	}

	@Override
	public String getLabel(Locale locale) {
		return _collectionProviderName;
	}

	@Override
	public boolean isWithinScope(
		InfoListProviderContext infoListProviderContext) {

		Company company = infoListProviderContext.getCompany();

		if (company.getCompanyId() == _companyId) {
			return true;
		}

		return false;
	}

	protected List<AssetEntry> getAssetEntries(SearchHits searchHits) {
		List<AssetEntry> assetEntries = new ArrayList<>();

		List<SearchHit> searchHitList = searchHits.getSearchHits();

		for (SearchHit searchHit : searchHitList) {
			Document document = searchHit.getDocument();

			String className = document.getString("entryClassName");

			long classPK = document.getLong("entryClassPK");

			try {
				AssetEntry assetEntry = _assetEntryService.getEntry(
					className, classPK);

				assetEntries.add(assetEntry);
			}
			catch (Exception exception) {
				_log.error("Unable to get asset entry", exception);
			}
		}

		return assetEntries;
	}

	protected SearchRequestBuilder getSearchRequestBuilder(
		InfoListProviderContext infoListProviderContext) {

		Company company = infoListProviderContext.getCompany();
		User user = infoListProviderContext.getUser();

		Optional<Layout> layoutOptional =
			infoListProviderContext.getLayoutOptional();

		SearchRequestBuilder searchRequestBuilder =
			_searchRequestBuilderFactory.builder(
			).companyId(
				company.getCompanyId()
			).withSearchContext(
				searchContext -> {
					if (layoutOptional.isPresent()) {
						searchContext.setLayout(layoutOptional.get());
					}

					searchContext.setAttribute("blueprintId", _blueprintId);
					searchContext.setTimeZone(user.getTimeZone());
					searchContext.setUserId(user.getUserId());
				}
			);

		Optional<Group> groupOptional =
			infoListProviderContext.getGroupOptional();

		if (groupOptional.isPresent()) {
			Group group = groupOptional.get();

			searchRequestBuilder.groupIds(group.getGroupId());
		}

		return searchRequestBuilder;
	}

	protected SearchResponse getSearchResponse(
		InfoListProviderContext infoListProviderContext) {

		SearchRequestBuilder searchRequestBuilder = getSearchRequestBuilder(
			infoListProviderContext);

		return _searcher.search(searchRequestBuilder.build());
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BlueprintInfoListProvider.class);

	private final AssetEntryService _assetEntryService;
	private final String _blueprintId;
	private final String _collectionProviderName;
	private final long _companyId;
	private final Searcher _searcher;
	private final SearchRequestBuilderFactory _searchRequestBuilderFactory;
	private final Sorts _sorts;

}