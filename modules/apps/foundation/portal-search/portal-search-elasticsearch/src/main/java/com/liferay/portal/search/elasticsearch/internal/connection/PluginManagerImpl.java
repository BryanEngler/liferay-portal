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

package com.liferay.portal.search.elasticsearch.internal.connection;

import java.io.IOException;

import java.lang.reflect.Constructor;
import java.net.URL;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.collect.Iterators;
import org.elasticsearch.cli.Terminal;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.env.Environment;
import org.elasticsearch.plugins.PluginInfo;
import org.elasticsearch.plugins.PluginManager.OutputMode;
import org.elasticsearch.plugins.PluginCli;
import org.elasticsearch.plugins.PluginInfo;

/**
 * @author Artur Aquino
 * @author André de Oliveira
 */
public class PluginManagerImpl implements PluginManager {

	public PluginManagerImpl(
		Environment environment, URL url, OutputMode outputMode,
		TimeValue timeout) {

		this.environment = environment;
		this.url = url;
		this.outputMode = outputMode;
		this.timeout = timeout;
	}

	@Override
	public void downloadAndExtract(
			String name, Terminal terminal, boolean batch)
		throws IOException {

		try {
			PluginCli pluginCli = getPluginCli();

			pluginCli.main(new String[]{"install", name,
					"-Epath.home=" + environment.settings().get("path.home"), "-s"},
				terminal);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private PluginCli getPluginCli() throws Exception {
		Constructor<PluginCli> constructor =
			PluginCli.class.getDeclaredConstructor();

		constructor.setAccessible(true);
		return constructor.newInstance();
	}

	@Override
	public Path[] getInstalledPluginsPaths() throws IOException {
		if (!Files.exists(environment.pluginsFile())) {
			return new Path[0];
		}

		try (DirectoryStream<Path> stream =
			     Files.newDirectoryStream(environment.pluginsFile())) {

			return Iterators.toArray(stream.iterator(), Path.class);
		}
	}

	@Override
	public boolean isCurrentVersion(Path path) throws IOException {
		try {
			PluginInfo.readFromProperties(path);

			return true;
		}
		catch (IllegalArgumentException iae) {
			String message = iae.getMessage();

			if ((message != null) && message.contains("designed for version")) {
				return false;
			}

			throw iae;
		}
	}

	@Override
	public void removePlugin(String name, Terminal terminal)
	    throws IOException {

	    try {
		    PluginCli pluginCli = getPluginCli();

		    pluginCli.main(new String[]{"remove", name,
				    "-Epath.home=" + environment.settings().get("path.home"), "-s"},
			    terminal);
	    }
	    catch (Exception e) {
		    e.printStackTrace();
	    }
	}

	public enum OutputMode {
		DEFAULT, SILENT, VERBOSE
	}

	private final Environment environment;
	private final URL url;
	private final OutputMode outputMode;
	private final TimeValue timeout;
}