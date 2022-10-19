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

package com.liferay.search.experiences.rest.internal.resource.v1_0;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.index.GetIndexIndexRequest;
import com.liferay.portal.search.engine.adapter.index.GetIndexIndexResponse;
import com.liferay.portal.search.index.IndexNameBuilder;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.search.experiences.rest.dto.v1_0.SearchIndex;
import com.liferay.search.experiences.rest.resource.v1_0.SearchIndexResource;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Brian Wing Shun Chan
 */
@Component(
	enabled = false,
	properties = "OSGI-INF/liferay/rest/v1_0/search-index.properties",
	scope = ServiceScope.PROTOTYPE, service = SearchIndexResource.class
)
public class SearchIndexResourceImpl extends BaseSearchIndexResourceImpl {

	public List<SearchIndex> getSearchIndexes() {
		List<SearchIndex> searchIndexes = new ArrayList<>();

		String externalPrefix = "external:";

		String prefix =
			_indexNameBuilder.getIndexName(contextCompany.getCompanyId()) +
				StringPool.DASH;

		GetIndexIndexRequest getIndexIndexRequest = new GetIndexIndexRequest(
			StringPool.STAR);

		GetIndexIndexResponse getIndexIndexResponse =
			_searchEngineAdapter.execute(getIndexIndexRequest);

		for (String indexName : getIndexIndexResponse.getIndexNames()) {
			if (indexName.startsWith(prefix) ||
				indexName.startsWith(externalPrefix)) {

				searchIndexes.add(
					new SearchIndex() {
						{
							external = _isExternal(externalPrefix, indexName);
							name = _getName(externalPrefix, indexName, prefix);
						}
					});
			}
		}

		return searchIndexes;
	}

	@Override
	public Page<SearchIndex> getSearchIndexesPage() throws Exception {
		return Page.of(getSearchIndexes());
	}

	private String _getName(
		String externalPrefix, String indexName, String prefix) {

		if (_isExternal(externalPrefix, indexName)) {
			return indexName.substring(externalPrefix.length());
		}

		return indexName.substring(prefix.length());
	}

	private boolean _isExternal(String externalPrefix, String indexName) {
		return indexName.startsWith(externalPrefix);
	}

	@Reference
	private IndexNameBuilder _indexNameBuilder;

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

}