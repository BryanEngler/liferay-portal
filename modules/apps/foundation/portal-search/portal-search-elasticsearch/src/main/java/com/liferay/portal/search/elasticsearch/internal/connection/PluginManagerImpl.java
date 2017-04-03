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

import com.google.common.collect.Iterators;

import com.liferay.portal.kernel.exception.SystemException;

import java.io.IOException;

import java.lang.reflect.Constructor;

import java.net.URL;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.elasticsearch.cli.Terminal;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.plugins.PluginCli;
import org.elasticsearch.plugins.PluginInfo;

/**
 * @author Artur Aquino
 * @author André de Oliveira
 */
public class PluginManagerImpl implements PluginManager {

	public PluginManagerImpl(Environment environment, URL url) {
		this.environment = environment;
		_url = url;
	}

	@Override
	public void downloadAndExtract(
			String name, Terminal terminal, boolean batch)
		throws Exception {

		Settings settings = environment.settings();

		String[] args = {
			"install", "file://" + _url.getPath(),
			"-Epath.home=" + settings.get("path.home"), "-s"
		};

		try {
			JarHellWorkaround.execute(
				() -> {
					try {
						main(terminal, args);
					}
					catch (Exception e) {
						throw new SystemException(e);
					}
				});
		}
		catch (SystemException se) {
			throw (Exception)se.getCause();
		}
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
	public void removePlugin(String name, Terminal terminal) throws Exception {
		Settings settings = environment.settings();

		String[] args =
			{"remove", name, "-Epath.home=" + settings.get("path.home"), "-s"};

		main(terminal, args);
	}

	protected void main(Terminal terminal, String[] args) throws Exception {
		Constructor<PluginCli> constructor =
			PluginCli.class.getDeclaredConstructor();

		constructor.setAccessible(true);

		PluginCli pluginCli = constructor.newInstance();

		pluginCli.main(args, terminal);
	}

	protected final Environment environment;

	private final URL _url;

}