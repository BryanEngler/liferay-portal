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

package com.liferay.dynamic.data.mapping.background.task.test;

import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.util.LocaleThreadLocal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;

/**
 * @author Lucas Marques de Paula
 */
public abstract class BaseBackgroundTaskExecutor {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	public final Group addGroup() throws Exception {
		Group group = GroupTestUtil.addGroup();

		_groups.add(group);

		return group;
	}

	protected void assertHitsLength(int expected, Hits hits) {
		int actual = hits.getLength();

		String message = String.format(
			"Hits length expected:<%d> but was:<%d>", expected, actual);

		Assert.assertEquals(message, expected, actual);
	}

	protected final String getResourceAsString(String filename)
		throws Exception {

		Class<?> clazz = getClass();

		ClassLoader classLoader = clazz.getClassLoader();

		String name =
			"com/liferay/dynamic/data/mapping/background/task/" + filename;

		InputStream inputStream = classLoader.getResourceAsStream(name);

		return StringUtil.read(inputStream);
	}

	protected void setUp() {
		LocaleThreadLocal.setSiteDefaultLocale(Locale.JAPAN);
	}

	@DeleteAfterTestRun
	private final List<Group> _groups = new ArrayList<>(1);

}