package net.agilhard.maven.plugins.jpacktool.mojo;
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
import java.io.Writer;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.update4j.Configuration.Builder;

import net.agilhard.maven.plugins.jpacktool.base.mojo.AbstractToolMojo;
import net.agilhard.maven.plugins.jpacktool.update4j.Update4jHelper;

/**
 * Package a Bootstrap Application which can start a Business Application.
 *
 * @author Bernd Eilers
 *
 */
@Mojo(name = "jpacktool-pack-bootstrap", requiresDependencyResolution = ResolutionScope.RUNTIME, defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true)
public class PackageBootstrapMojo extends AbstractToolMojo {

	/**
	 * Name of the generated ZIP file in the <code>target</code> directory. This
	 * will not change the name of the installed/deployed file.
	 */
	@Parameter(defaultValue = "${project.build.finalName}", readonly = true)
	protected String finalName;

	/**
	 * replace this with nothing in the name of the config file
	 */
	@Parameter(required = false, readonly = false)
	protected String stripConfigName;

	/**
	 * Flag if the basePath setting is relativ to ${user.dir}
	 */
	@Parameter(defaultValue = "true", required = false, readonly = false)
	private boolean basePathBelowUserDir;

	/**
	 * The Update4j basePath Setting for the Business Application.
	 */
	@Parameter(required = false, readonly = false)
	protected String basePath;

	/**
	 * The update4j baseUri
	 */
	@Parameter(required = false, readonly = false)
	protected String baseUri;

	@Parameter(required = false, readonly = false, defaultValue = "true")
	protected boolean generateUpdate4jConfig;

	public PackageBootstrapMojo() {
		super();
	}

	/**
	 * Flag if jpacktool-prepare goal has been used before
	 */
	protected boolean jpacktoolPrepareUsed;

    @SuppressWarnings({ "unchecked", "boxing" })
	@Override
	public void executeToolMain() throws MojoExecutionException, MojoFailureException {

		this.initJPacktoolModel();

		this.publishJPacktoolProperties();

		final File file = new File(this.outputDirectoryJPacktool, "java_modules.list");
		if (file.exists()) {
			file.delete();
		}

		try (final Stream<Path> pathStream = Files.walk(this.outputDirectoryJPacktool.toPath(),
				FileVisitOption.FOLLOW_LINKS)) {
			pathStream.filter((p) -> !p.toFile().isDirectory() && p.toFile().getAbsolutePath().endsWith(".jdeps"))
					.forEach(p -> {
						p.toFile().delete();
					});
		} catch (final IOException e) {
			throw new MojoFailureException("i/o error");
		}

		Path configPath = null;

		if (this.generateUpdate4jConfig) {

			if ((this.baseUri != null) && (this.basePath != null)) {
				final Builder builder = Update4jHelper.createBuilder(this.baseUri, this.basePath, this.basePathBelowUserDir);

				for (final String jarOnClassPath : (List<String>) this.jpacktoolModel.get("jarsOnClassPath")) {
					Update4jHelper.addToBuilder(builder, this.outputDirectoryClasspathJars, jarOnClassPath, true);
				}

				if (this.outputDirectoryAutomaticJars.isDirectory()) {
					Update4jHelper.addToBuilder(builder, this.outputDirectoryAutomaticJars, false);
				}
				if (this.outputDirectoryModules.isDirectory()) {
					Update4jHelper.addToBuilder(builder, this.outputDirectoryModules, false);
				}

				String artName = this.project.getArtifactId();
				if (this.stripConfigName != null) {
					artName = artName.replaceAll(this.stripConfigName, "");
				}

				configPath = this.outputDirectoryJPacktool.toPath()
						.resolve("update4j_" + this.project.getGroupId() + "_" + artName + ".xml");

				try (Writer out = Files.newBufferedWriter(configPath)) {
					builder.build().write(out);
				} catch (final IOException e) {
					throw new MojoFailureException("i/o error");
				}
			}
		}

		final File createZipArchiveFromDirectory = this.createZipArchiveFromDirectory(this.buildDirectory,
				this.outputDirectoryJPacktool);

		this.mavenProjectHelper.attachArtifact(this.project, "zip", "jpacktool_bootstrap",
				createZipArchiveFromDirectory);

		if (configPath != null) {
			this.mavenProjectHelper.attachArtifact(this.project, "xml", "jpacktool_bootstrap_update4j",
					configPath.toFile());
		}

	}

	/**
	 * set jpacktoolPrepareUsed variable based on maven property
	 */
	protected void checkJpacktoolPrepareUsed() {
		final Boolean b = (Boolean) this.project.getProperties().get(this.jpacktoolPropertyPrefix + ".used");
		this.jpacktoolPrepareUsed = b == null ? false : b.booleanValue();
	}

	/**
	 * initialize jpacktooModel
	 */
	@SuppressWarnings("unchecked")
	protected void initJPacktoolModel() {
		this.checkJpacktoolPrepareUsed();
		if (this.jpacktoolPrepareUsed) {
			this.jpacktoolModel = (Map<String, Object>) this.project.getProperties()
					.get(this.jpacktoolPropertyPrefix + ".model");
		}
	}

	@Override
    public String getFinalName() {
		return this.finalName;
	}
}
