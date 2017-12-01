package com.liferay.portal.search.elasticsearch.internal.facet;

import com.liferay.portal.search.elasticsearch.internal.ElasticsearchIndexingFixture;
import com.liferay.portal.search.elasticsearch.internal.connection.ElasticsearchFixture;
import com.liferay.portal.search.elasticsearch.internal.connection.LiferayIndexCreator;
import com.liferay.portal.search.test.util.facet.BaseModifiedFacetTestCase;
import com.liferay.portal.search.test.util.indexing.BaseIndexingTestCase;
import com.liferay.portal.search.test.util.indexing.IndexingFixture;
import org.junit.Test;

/**
 * @author Bryan Engler
 */
public class ModifiedFacetTest extends BaseModifiedFacetTestCase {

	@Override
	@Test
	public void testRange() throws Exception {
		super.testRange();
	}

	@Override
	protected IndexingFixture createIndexingFixture() throws Exception {
		ElasticsearchFixture elasticsearchFixture = new ElasticsearchFixture(
			AssetTagNamesFacetTest.class.getSimpleName());

		ElasticsearchIndexingFixture elasticsearchIndexingFixture =
			new ElasticsearchIndexingFixture(
				elasticsearchFixture, BaseIndexingTestCase.COMPANY_ID,
				new LiferayIndexCreator(elasticsearchFixture));

		elasticsearchIndexingFixture.setFacetProcessor(
			new ModifiedFacetProcessor());

		return elasticsearchIndexingFixture;
	}

}
