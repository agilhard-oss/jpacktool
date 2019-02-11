package net.agilhard.maven.plugins.jpacktool.extend.mojo;
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

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.update4j.Configuration.Builder;

import net.agilhard.maven.plugins.jpacktool.base.mojo.jlink.JLinkMojo;
import net.agilhard.maven.plugins.jpacktool.update4j.Update4jHelper;

/**
 * The JLink goal is intended to create a Java Run Time Image file based on
 * <a href=
 * "http://openjdk.java.net/jeps/282">http://openjdk.java.net/jeps/282</a>,
 * <a href=
 * "http://openjdk.java.net/jeps/220">http://openjdk.java.net/jeps/220</a>.
 *
 * This extends the base class by some features for update4j.
 * 
 * @author Bernd Eilers
 */
@Mojo(name = "jlink", requiresDependencyResolution = ResolutionScope.RUNTIME, defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true)
public class JPacktoolJLinkMojo extends JLinkMojo {

	/**
	 * Flag if the basePath setting is relativ to ${user.dir}
	 */
	@Parameter(defaultValue = "true", required = false, readonly = false)
	private boolean basePathBelowUserDir;

	/**
	 * The update4j basePath
	 */
	@Parameter(required = false, readonly = false)
	protected String basePath;

	/**
	 * The update4j baseUri
	 */
	@Parameter(required = false, readonly = false)
	protected String baseUri;

	/**
	 * The jpacktool.properties file
	 */
	@Parameter(required = false, readonly = false)
	protected File jpacktoolProperties;

	/**
	 * Set if to generate an update4j config file
	 */
	@Parameter(required = false, readonly = false, defaultValue = "true")
	protected boolean generateUpdate4jConfig;

	/**
	 * replace this with nothing in the name of the config file
	 */
	@Parameter(required = false, readonly = false, defaultValue = "-jpacktool")
	protected String stripConfigName;

	public JPacktoolJLinkMojo() {
		super();
	}

	protected void generateContent(File outputDirectory) throws MojoExecutionException {

		Path dir = outputDirectoryJPacktool.toPath().resolve("conf");

		if (jpacktoolProperties != null) {
			if (dir.toFile().isDirectory()) {
				Path outDir = outputDirectory.toPath().resolve("conf");
				Path target = outDir.resolve("jpacktool.properties");
				try {
					Files.copy(jpacktoolProperties.toPath(), target, REPLACE_EXISTING);
				} catch (IOException e) {
					throw new MojoExecutionException("i/o error", e);
				}
			}
		}

		if (generateUpdate4jConfig) {
			if (dir.toFile().isDirectory()) {
				Path outDir = outputDirectory.toPath().resolve("conf");
				int i = dir.getNameCount();

				try (final Stream<Path> pathStream = Files.walk(dir, FileVisitOption.FOLLOW_LINKS)) {
					pathStream.filter((p) -> !p.toFile().isDirectory()).forEach(p -> {
						Path target = outDir.resolve(p.subpath(i, p.getNameCount()));
						File parent = target.getParent().toFile();
						if (!parent.exists()) {
							parent.mkdirs();
						}
						try {
							Files.copy(p, target, REPLACE_EXISTING);
						} catch (IOException e) {
							//
						}

					});
				} catch (final IOException e) {
					throw new MojoExecutionException("i/o error");
				}

				if ((basePath != null) && (baseUri != null)) {
					Builder builder = Update4jHelper.createBuilder(baseUri, basePath, basePathBelowUserDir);
					try {
						Update4jHelper.addToBuilder(builder, outputDirectory, "update");
					} catch (MojoFailureException e) {
						throw new MojoExecutionException("error generating update4j config", e);
					}

					String artName = project.getArtifactId();
					if (stripConfigName != null) {
						artName = artName.replaceAll(stripConfigName, "");
					}
					Path configPath = outputDirectory.toPath().resolve("conf")
							.resolve("update4j_" + project.getGroupId() + "_" + artName + ".xml");

					try (Writer out = Files.newBufferedWriter(configPath)) {
						builder.build().write(out);
					} catch (IOException e) {
						throw new MojoExecutionException("i/o error");
					}
				}
			}
		}
	}
}
