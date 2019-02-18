package net.agilhard.maven.plugins.jpacktool.update4j;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.apache.maven.plugin.MojoFailureException;
import org.update4j.Configuration;
import org.update4j.FileMetadata;
import org.update4j.Configuration.Builder;

public class Update4jHelper {

	public Update4jHelper() {
		// TODO Auto-generated constructor stub
	}

	public static Builder addToBuilder(Builder builder, File dir, String name, Boolean classpath)
			throws MojoFailureException {

		String subdir = dir.getName();

		Path p = dir.toPath().resolve(name);
		String sp = subdir + "/" + name;
		String uri = "${baseUri}/" + subdir + "/" + name;

		if (p.toFile().exists()) {
			if (classpath) {
				builder.file(FileMetadata.readFrom(p).path(sp).uri(uri).classpath().ignoreBootConflict());
			} else {
				builder.file(FileMetadata.readFrom(p).path(sp).modulepath().ignoreBootConflict());
			}
		}
		return builder;
	}

	public static Builder addToBuilder(Builder builder, File dir, String pathPrefix) throws MojoFailureException {
		Path dirPath = dir.toPath();
		int beginIndex = dirPath.getNameCount();
		try (final Stream<Path> pathStream = Files.walk(dir.toPath(), FileVisitOption.FOLLOW_LINKS)) {
			pathStream.filter((p) -> !p.toFile().isDirectory()).forEach(p -> {
				String sp = pathPrefix + File.separator + p.subpath(beginIndex, p.getNameCount()).toString();
				builder.file(FileMetadata.readFrom(p).path(sp).classpath(false).modulepath(false));
			});
		} catch (final IOException e) {
			throw new MojoFailureException("i/o error");
		}

		return builder;
	}

	public static Builder addToBuilder(Builder builder, File dir, Boolean classpath) throws MojoFailureException {

		String subdir = dir.getName();
		File subdirFile=new File(dir, subdir);
		if ( subdirFile.exists() ) {
			try (final Stream<Path> pathStream = Files.walk(dir.toPath(), FileVisitOption.FOLLOW_LINKS)) {
				pathStream.filter((p) -> !p.toFile().isDirectory()).forEach(p -> {
					String name = p.getFileName().toString();
					String sp = subdir + "/" + name;
					if (classpath) {
						builder.file(FileMetadata.readFrom(p).path(sp).classpath().ignoreBootConflict());
					} else {
						builder.file(FileMetadata.readFrom(p).path(sp).modulepath().ignoreBootConflict());
					}
				});
			} catch (final IOException e) {
				throw new MojoFailureException("i/o error", e);
			}
		}
		return builder;
	}

	public static Builder createBuilder(String baseUri, String basePath, boolean basePathBelowUserDir, boolean basePathBelowHomeDir) {
		Builder builder = Configuration.builder();

		builder.baseUri(baseUri);

		if (basePathBelowHomeDir) {
			builder.basePath("${user.home}/" + basePath);
		} else if (basePathBelowUserDir) {
			builder.basePath("${user.dir}/" + basePath);
		} else {
			builder.basePath(basePath);
		}
		return builder;
	}
}
