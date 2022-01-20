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

package com.liferay.portal.search.opensearch.internal.sort;

import com.liferay.portal.search.opensearch.internal.geolocation.DistanceUnitTranslator;
import com.liferay.portal.search.opensearch.internal.geolocation.GeoDistanceTypeTranslator;
import com.liferay.portal.search.opensearch.internal.geolocation.GeoLocationPointTranslator;
import com.liferay.portal.search.opensearch.internal.script.ScriptTranslator;
import com.liferay.portal.search.geolocation.GeoLocationPoint;
import com.liferay.portal.search.query.QueryTranslator;
import com.liferay.portal.search.sort.FieldSort;
import com.liferay.portal.search.sort.GeoDistanceSort;
import com.liferay.portal.search.sort.NestedSort;
import com.liferay.portal.search.sort.ScoreSort;
import com.liferay.portal.search.sort.ScriptSort;
import com.liferay.portal.search.sort.Sort;
import com.liferay.portal.search.sort.SortFieldTranslator;
import com.liferay.portal.search.sort.SortMode;
import com.liferay.portal.search.sort.SortOrder;
import com.liferay.portal.search.sort.SortVisitor;

import java.util.List;
import java.util.stream.Stream;

import org.opensearch.common.geo.GeoDistance;
import org.opensearch.common.geo.GeoPoint;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.script.Script;
import org.opensearch.search.sort.FieldSortBuilder;
import org.opensearch.search.sort.GeoDistanceSortBuilder;
import org.opensearch.search.sort.NestedSortBuilder;
import org.opensearch.search.sort.ScriptSortBuilder;
import org.opensearch.search.sort.SortBuilder;
import org.opensearch.search.sort.SortBuilders;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael C. Han
 */
@Component(
	property = "search.engine.impl=Elasticsearch",
	service = {SortFieldTranslator.class, SortVisitor.class}
)
public class ElasticsearchSortFieldTranslator
	implements SortFieldTranslator<SortBuilder<?>>,
			   SortVisitor<SortBuilder<?>> {

	@Override
	public SortBuilder<?> translate(Sort sort) {
		return sort.accept(this);
	}

	@Override
	public SortBuilder<?> visit(FieldSort fieldSort) {
		FieldSortBuilder fieldSortBuilder = SortBuilders.fieldSort(
			fieldSort.getField());

		fieldSortBuilder.order(translate(fieldSort.getSortOrder()));

		if (fieldSort.getMissing() != null) {
			fieldSortBuilder.missing(fieldSort.getMissing());
		}

		if (fieldSort.getNestedSort() != null) {
			fieldSortBuilder.setNestedSort(
				translate(fieldSort.getNestedSort()));
		}

		if (fieldSort.getSortMode() != null) {
			fieldSortBuilder.sortMode(translate(fieldSort.getSortMode()));
		}

		return fieldSortBuilder.unmappedType("keyword");
	}

	@Override
	public SortBuilder<?> visit(GeoDistanceSort geoDistanceSort) {
		List<GeoLocationPoint> geoLocationPoints =
			geoDistanceSort.getGeoLocationPoints();

		Stream<GeoLocationPoint> stream = geoLocationPoints.stream();

		GeoDistanceSortBuilder geoDistanceSortBuilder =
			SortBuilders.geoDistanceSort(
				geoDistanceSort.getField(),
				stream.map(
					GeoLocationPointTranslator::translate
				).toArray(
					GeoPoint[]::new
				));

		geoDistanceSortBuilder.order(translate(geoDistanceSort.getSortOrder()));

		if (geoDistanceSort.getDistanceUnit() != null) {
			geoDistanceSortBuilder.unit(
				_distanceUnitTranslator.translate(
					geoDistanceSort.getDistanceUnit()));
		}

		if (geoDistanceSort.getGeoDistanceType() != null) {
			GeoDistance geoDistance = _geoDistanceTypeTranslator.translate(
				geoDistanceSort.getGeoDistanceType());

			geoDistanceSortBuilder.geoDistance(geoDistance);
		}

		if (geoDistanceSort.getNestedSort() != null) {
			geoDistanceSortBuilder.setNestedSort(
				translate(geoDistanceSort.getNestedSort()));
		}

		if (geoDistanceSort.getSortMode() != null) {
			geoDistanceSortBuilder.sortMode(
				translate(geoDistanceSort.getSortMode()));
		}

		return geoDistanceSortBuilder;
	}

	@Override
	public SortBuilder<?> visit(ScoreSort scoreSort) {
		return SortBuilders.scoreSort(
		).order(
			translate(scoreSort.getSortOrder())
		);
	}

	@Override
	public SortBuilder<?> visit(ScriptSort scriptSort) {
		Script script = _scriptTranslator.translate(scriptSort.getScript());

		ScriptSortBuilder.ScriptSortType scriptSortType =
			ScriptSortBuilder.ScriptSortType.NUMBER;

		if (scriptSort.getScriptSortType() ==
				ScriptSort.ScriptSortType.STRING) {

			scriptSortType = ScriptSortBuilder.ScriptSortType.STRING;
		}

		ScriptSortBuilder scriptSortBuilder = SortBuilders.scriptSort(
			script, scriptSortType);

		if (scriptSort.getNestedSort() != null) {
			scriptSortBuilder.setNestedSort(
				translate(scriptSort.getNestedSort()));
		}

		if (scriptSort.getSortMode() != null) {
			scriptSortBuilder.sortMode(translate(scriptSort.getSortMode()));
		}

		scriptSortBuilder.order(translate(scriptSort.getSortOrder()));

		return scriptSortBuilder;
	}

	@Reference(target = "(search.engine.impl=Elasticsearch)", unbind = "-")
	protected void setQueryTranslator(
		QueryTranslator<QueryBuilder> queryTranslator) {

		_queryTranslator = queryTranslator;
	}

	protected NestedSortBuilder translate(NestedSort nestedSort) {
		NestedSortBuilder nestedSortBuilder = new NestedSortBuilder(
			nestedSort.getPath());

		if (nestedSort.getFilterQuery() != null) {
			QueryBuilder queryBuilder = _queryTranslator.translate(
				nestedSort.getFilterQuery());

			nestedSortBuilder.setFilter(queryBuilder);
		}

		if (nestedSort.getNestedSort() != null) {
			NestedSort childNestedSort = nestedSort.getNestedSort();

			nestedSortBuilder.setNestedSort(translate(childNestedSort));
		}

		nestedSortBuilder.setMaxChildren(nestedSort.getMaxChildren());

		return nestedSortBuilder;
	}

	protected org.opensearch.search.sort.SortMode translate(
		SortMode sortMode) {

		if (sortMode == SortMode.AVG) {
			return org.opensearch.search.sort.SortMode.AVG;
		}
		else if (sortMode == SortMode.MAX) {
			return org.opensearch.search.sort.SortMode.MAX;
		}
		else if (sortMode == SortMode.MEDIAN) {
			return org.opensearch.search.sort.SortMode.MEDIAN;
		}
		else if (sortMode == SortMode.MIN) {
			return org.opensearch.search.sort.SortMode.MIN;
		}
		else if (sortMode == SortMode.SUM) {
			return org.opensearch.search.sort.SortMode.SUM;
		}
		else {
			throw new IllegalArgumentException(
				"Invalid sort mode: " + sortMode);
		}
	}

	protected org.opensearch.search.sort.SortOrder translate(
		SortOrder sortOrder) {

		if ((sortOrder == SortOrder.ASC) || (sortOrder == null)) {
			return org.opensearch.search.sort.SortOrder.ASC;
		}
		else if (sortOrder == SortOrder.DESC) {
			return org.opensearch.search.sort.SortOrder.DESC;
		}
		else {
			throw new IllegalArgumentException(
				"Invalid sort order: " + sortOrder);
		}
	}

	private final DistanceUnitTranslator _distanceUnitTranslator =
		new DistanceUnitTranslator();
	private final GeoDistanceTypeTranslator _geoDistanceTypeTranslator =
		new GeoDistanceTypeTranslator();
	private QueryTranslator<QueryBuilder> _queryTranslator;
	private final ScriptTranslator _scriptTranslator = new ScriptTranslator();

}