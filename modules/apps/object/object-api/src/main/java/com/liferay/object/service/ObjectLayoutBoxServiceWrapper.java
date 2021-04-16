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

package com.liferay.object.service;

import com.liferay.portal.kernel.service.ServiceWrapper;

/**
 * Provides a wrapper for {@link ObjectLayoutBoxService}.
 *
 * @author Marco Leo
 * @see ObjectLayoutBoxService
 * @generated
 */
public class ObjectLayoutBoxServiceWrapper
	implements ObjectLayoutBoxService, ServiceWrapper<ObjectLayoutBoxService> {

	public ObjectLayoutBoxServiceWrapper(
		ObjectLayoutBoxService objectLayoutBoxService) {

		_objectLayoutBoxService = objectLayoutBoxService;
	}

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	@Override
	public String getOSGiServiceIdentifier() {
		return _objectLayoutBoxService.getOSGiServiceIdentifier();
	}

	@Override
	public ObjectLayoutBoxService getWrappedService() {
		return _objectLayoutBoxService;
	}

	@Override
	public void setWrappedService(
		ObjectLayoutBoxService objectLayoutBoxService) {

		_objectLayoutBoxService = objectLayoutBoxService;
	}

	private ObjectLayoutBoxService _objectLayoutBoxService;

}