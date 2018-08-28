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

package com.liferay.dynamic.data.mapping.background.task;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.search.DDMStructureIndexer;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Lucas Marques de Paula
 */
@Component(immediate = true, service = DDMStructureIndexerServiceUtil.class)
public class DDMStructureIndexerServiceUtil {

	public DDMStructureIndexer getDDMStructureIndexer(String className)
		throws PortalException {

		ServiceTracker<DDMStructureIndexer, DDMStructureIndexer>
			serviceTracker = null;

		try {
			String filter = String.format(
				"(&(objectClass=%s)(ddm.structure.indexer.class.name=%s))",
				DDMStructureIndexer.class.getName(), className);

			serviceTracker = new ServiceTracker<>(
				_bundleContext, _bundleContext.createFilter(filter), null);

			serviceTracker.open();

			DDMStructureIndexer ddmStructureIndexer =
				serviceTracker.getService();

			if (ddmStructureIndexer == null) {
				throw new PortalException("Indexer not found");
			}

			return ddmStructureIndexer;
		}
		catch (InvalidSyntaxException ise) {
			throw new PortalException(ise.getMessage());
		}
		finally {
			if (serviceTracker != null) {
				serviceTracker.close();
			}
		}
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;
	}

	private BundleContext _bundleContext;

}