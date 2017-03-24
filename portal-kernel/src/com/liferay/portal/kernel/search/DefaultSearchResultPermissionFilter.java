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

package com.liferay.portal.kernel.search;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.collector.DefaultTermCollector;
import com.liferay.portal.kernel.search.facet.collector.FacetCollector;
import com.liferay.portal.kernel.search.facet.collector.TermCollector;
import com.liferay.portal.kernel.search.facet.util.RangeParserUtil;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tina Tian
 */
public class DefaultSearchResultPermissionFilter
	extends BaseSearchResultPermissionFilter {

	public DefaultSearchResultPermissionFilter(
		BaseIndexer<?> baseIndexer, PermissionChecker permissionChecker) {

		_baseIndexer = baseIndexer;
		_permissionChecker = permissionChecker;
	}

	@Override
	protected void filterHits(Hits hits, SearchContext searchContext) {
		List<Document> docs = new ArrayList<>();
		int excludeDocsSize = 0;
		List<Float> scores = new ArrayList<>();

		boolean companyAdmin = _permissionChecker.isCompanyAdmin(
			_permissionChecker.getCompanyId());
		int status = GetterUtil.getInteger(
			searchContext.getAttribute(Field.STATUS),
			WorkflowConstants.STATUS_APPROVED);

		Document[] documents = hits.getDocs();

		Map<String, Facet> facetsMap = searchContext.getFacets();

		for (int i = 0; i < documents.length; i++) {
			Document document = documents[i];

			if (_isIncludeDocument(
					document, _permissionChecker.getCompanyId(), companyAdmin,
					status)) {

				docs.add(document);
				scores.add(hits.score(i));
			}
			else {
				excludeDocsSize++;

				for (Facet facet : facetsMap.values()) {
					FacetCollector facetCollector = facet.getFacetCollector();

					if (facetCollector == null) {
						continue;
					}

					List<TermCollector> updatedTermCollectors =
						new ArrayList<>();

					for (TermCollector termCollector :
							facetCollector.getTermCollectors()) {

						if (_documentContributedToTermCollectorFrequency(
								document, facet.getFieldName(),
								termCollector.getTerm())) {

							int frequency = termCollector.getFrequency();

							updatedTermCollectors.add(
								new DefaultTermCollector(
									termCollector.getTerm(), --frequency));
						}
						else {
							updatedTermCollectors.add(termCollector);
						}
					}

					FacetCollector updatedFacetCollector =
						new SimpleFacetCollector(
							facet.getFieldName(), updatedTermCollectors);

					facet.setFacetCollector(updatedFacetCollector);
				}
			}
		}

		hits.setDocs(docs.toArray(new Document[docs.size()]));
		hits.setScores(ArrayUtil.toFloatArray(scores));
		hits.setSearchTime(
			(float)(System.currentTimeMillis() - hits.getStart()) /
				Time.SECOND);
		hits.setLength(hits.getLength() - excludeDocsSize);
	}

	@Override
	protected Hits getHits(SearchContext searchContext) throws SearchException {
		return _baseIndexer.doSearch(searchContext);
	}

	@Override
	protected boolean isGroupAdmin(SearchContext searchContext) {
		long groupId = GetterUtil.getLong(
			searchContext.getAttribute(Field.GROUP_ID));

		if (groupId == 0) {
			return false;
		}

		if (!_permissionChecker.isGroupAdmin(groupId)) {
			return false;
		}

		return true;
	}

	private boolean _documentContributedToTermCollectorFrequency(
		Document document, String facetFieldName, String term) {

		Field field = document.getField(facetFieldName);

		if (field != null) {
			if (facetFieldName.equals(Field.MODIFIED_DATE)) {
				String[] range = RangeParserUtil.parserRange(term);

				String lower = range[0];
				String upper = range[1];

				if ((lower.compareTo(field.getValue()) < 0) &&
					(upper.compareTo(field.getValue()) > 0)) {

					return true;
				}

				return false;
			}
			else if (facetFieldName.equals(Field.ASSET_TAG_NAMES) ||
					 facetFieldName.equals(Field.ASSET_CATEGORY_IDS)) {

				String[] fieldValues = field.getValues();

				for (String fieldValue : fieldValues) {
					if (term.equals(fieldValue)) {
						return true;
					}
				}

				return false;
			}

			return term.equals(field.getValue());
		}

		return false;
	}

	private boolean _isIncludeDocument(
		Document document, long companyId, boolean companyAdmin, int status) {

		long entryCompanyId = GetterUtil.getLong(
			document.get(Field.COMPANY_ID));

		if (entryCompanyId != companyId) {
			return false;
		}

		if (companyAdmin) {
			return true;
		}

		String entryClassName = document.get(Field.ENTRY_CLASS_NAME);

		Indexer<?> indexer = IndexerRegistryUtil.getIndexer(entryClassName);

		if (indexer == null) {
			return true;
		}

		if (!indexer.isFilterSearch()) {
			return true;
		}

		long entryClassPK = GetterUtil.getLong(
			document.get(Field.ENTRY_CLASS_PK));

		try {
			if (indexer.hasPermission(
					_permissionChecker, entryClassName, entryClassPK,
					ActionKeys.VIEW) &&
				indexer.isVisibleRelatedEntry(entryClassPK, status)) {

				return true;
			}
		}
		catch (Exception e) {
			if (_log.isDebugEnabled()) {
				_log.debug(e, e);
			}
		}

		return false;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DefaultSearchResultPermissionFilter.class);

	private final BaseIndexer<?> _baseIndexer;
	private final PermissionChecker _permissionChecker;

	private class SimpleFacetCollector implements FacetCollector {

		public SimpleFacetCollector(
			String fieldName, List<TermCollector> termCollectors) {

			_fieldName = fieldName;

			for (TermCollector termCollector : termCollectors) {
				_termCollectors.put(termCollector.getTerm(), termCollector);
			}
		}

		@Override
		public String getFieldName() {
			return _fieldName;
		}

		@Override
		public TermCollector getTermCollector(String term) {
			return _termCollectors.get(term);
		}

		@Override
		public List<TermCollector> getTermCollectors() {
			return ListUtil.fromMapValues(_termCollectors);
		}

		private final String _fieldName;
		private final Map<String, TermCollector> _termCollectors =
			new HashMap<>();

	}

}