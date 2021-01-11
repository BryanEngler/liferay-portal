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

package com.liferay.portal.search.semantic.search.internal.configuration;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

/**
 * @author Bryan Engler
 */
@ExtendedObjectClassDefinition(category = "search")
@Meta.OCD(
	id = "com.liferay.portal.search.semantic.search.internal.configuration.TextEmbedderConfiguration",
	localization = "content/Language", name = "text-embedder-configuration-name"
)
public interface TextEmbedderConfiguration {

	@Meta.AD(
		deflt = "path/to/embed.py", description = "python-script-path-help",
		name = "python-script-path", required = false
	)
	public String pythonScriptPath();

}