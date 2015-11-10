package com.liferay.demo.geolocation;

import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.search.filter.GeoDistanceFilter;
import com.liferay.portal.kernel.search.geolocation.GeoDistance;
import com.liferay.portal.kernel.search.geolocation.GeoLocationPoint;
import com.liferay.portal.kernel.test.util.SearchContextTestUtil;

import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.ArrayList;

import org.junit.Assert;

public class GeolocationDemoSearch {

	private static final GeoLocationPoint EPICENTER =
		new GeoLocationPoint(42.302, -71.0519);

	private static final String FIELD =
		"ddm__keyword__20733__geolocation_en_US";

	public void search() throws Exception {
		assertSearch(10, 1);
		assertSearch(500, 2);
		assertSearch(1000, 15);
		assertSearch(2000, 100);
		assertSearch(5000, 461);
		assertSearch(10000, 960);
		assertSearch(20000, 1000);
	}

	private void assertSearch(double distanceInMeters, int size) throws Exception {
		GeoDistanceFilter f =
			new GeoDistanceFilter(
				FIELD, EPICENTER, new GeoDistance(distanceInMeters));

		ArrayList<GeoLocationPoint> points = search(f);

		Assert.assertEquals(size, points.size());
	}

	protected ArrayList<GeoLocationPoint> search(GeoDistanceFilter f)
			throws Exception, SearchException {
		long groupId = 20146;

 		SearchContext searchContext = SearchContextTestUtil.getSearchContext(
			groupId);

		String searchableContent = "latitude";

		searchContext.setKeywords(searchableContent);

		Indexer<?> indexer = new GeolocationDemoFacetedSearcher(f);

		Hits hits = indexer.search(searchContext);

		Document[] docs = hits.getDocs();

		ArrayList<GeoLocationPoint> points = new ArrayList<>(docs.length);

		for (Document document : docs) {
			Field field = document.getField(FIELD);
			points.add(field.getGeoLocationPoint());
		}

		print(points);

		return points;
	}

	protected void print(ArrayList<GeoLocationPoint> points) {
		StringWriter w = new StringWriter();
		PrintWriter pw = new PrintWriter(w);

		for (GeoLocationPoint p : points)
			pw.println("" + p.getLatitude() + "," + p.getLongitude());

		System.out.println("--------");
		System.out.println(w.toString());
		System.out.println("--------");
	}

}
