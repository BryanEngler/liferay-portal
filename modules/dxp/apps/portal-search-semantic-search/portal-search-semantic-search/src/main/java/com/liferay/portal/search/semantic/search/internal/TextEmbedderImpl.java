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

package com.liferay.portal.search.semantic.search.internal;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.semantic.search.TextEmbedder;
import com.liferay.portal.search.semantic.search.internal.configuration.TextEmbedderConfiguration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Bryan Engler
 */
@Component(
	configurationPid = "com.liferay.portal.search.semantic.search.internal.configuration.TextEmbedderConfiguration",
	immediate = true, service = TextEmbedder.class
)
public class TextEmbedderImpl implements TextEmbedder {

	@Override
	public Float[] embed(String text) throws Exception {
		if (_log.isDebugEnabled()) {
			_log.debug("Embedding \"" + text + "\"");
		}

		_writer.write(text + "\n");
		_writer.flush();

		StringBuilder sb = new StringBuilder();

		String output = sb.toString();

		while (!output.endsWith(")")) {
			sb.append(_br.readLine());

			output = sb.toString();
		}

		if (_log.isDebugEnabled()) {
			_log.debug("Output:" + output);
		}

		String trimmedOutput = output.substring(
			output.indexOf("[") + 2, output.indexOf("]"));

		String[] values = StringUtil.split(trimmedOutput, " ");

		Float[] floatvalues = new Float[512];

		int i = 0;

		for (String value : values) {
			if (Validator.isBlank(value)) {
				continue;
			}

			floatvalues[i] = Float.valueOf(value.trim());

			i++;
		}

		return floatvalues;
	}

	@Activate
	protected void activate(Map<String, Object> properties) throws Exception {
		textEmbedderConfiguration = ConfigurableUtil.createConfigurable(
			TextEmbedderConfiguration.class, properties);

		ProcessBuilder processBuilder = new ProcessBuilder(
			"python3.8", textEmbedderConfiguration.pythonScriptPath());

		_process = processBuilder.start();

		_br = new BufferedReader(
			new InputStreamReader(_process.getInputStream()));
		_writer = new PrintWriter(_process.getOutputStream());

		String startupMessage = _br.readLine();

		if (_log.isInfoEnabled()) {
			_log.info("embed.py startup message: " + startupMessage);
		}
	}

	@Deactivate
	protected void deactivate() {
		_process.destroy();
	}

	protected volatile TextEmbedderConfiguration textEmbedderConfiguration;

	private static final Log _log = LogFactoryUtil.getLog(
		TextEmbedderImpl.class);

	private BufferedReader _br;
	private Process _process;
	private PrintWriter _writer;

}