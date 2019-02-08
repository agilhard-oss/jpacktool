package net.agilhard.maven.plugins.jpacktool.handler;

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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.codehaus.plexus.languages.java.jpms.JavaModuleDescriptor;
import org.codehaus.plexus.util.cli.Commandline;

import net.agilhard.maven.plugins.jpacktool.base.handler.AbstractEndVisitDependencyHandler;
import net.agilhard.maven.plugins.jpacktool.base.mojo.AbstractToolMojo;
import net.agilhard.maven.plugins.jpacktool.base.mojo.ExecuteCommand;

public class ImportJDepsAndUdate4jConfigHandler extends AbstractEndVisitDependencyHandler {

	/**
	 * replace this with nothing in the name of the config file
	 */
	protected String stripConfigName;

	protected String baseUri;

	protected List<String> linkedSystemModules = new ArrayList<>();;

	public ImportJDepsAndUdate4jConfigHandler(AbstractToolMojo mojo, DependencyGraphBuilder dependencyGraphBuilder,
			String stripConfigName) throws MojoExecutionException {

		super(mojo, dependencyGraphBuilder);
		this.stripConfigName = stripConfigName;
	}

	/** {@inheritDoc} */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("import-jdeps");
		super.execute();
	}

	protected void handleModJar(final DependencyNode dependencyNode, final Artifact artifact,
			Map.Entry<File, JavaModuleDescriptor> entry) throws MojoExecutionException, MojoFailureException {

	}

	protected void handleNonModJar(final DependencyNode dependencyNode, final Artifact artifact,
			Map.Entry<File, JavaModuleDescriptor> entry) throws MojoExecutionException, MojoFailureException {

	}

	protected void handleOther(final DependencyNode dependencyNode)
			throws MojoExecutionException, MojoFailureException {
		getLog().debug("handleOther " + dependencyNode.toNodeString());

		Artifact artifact = dependencyNode.getArtifact();
		String type = artifact.getType();

		if (("properties".equals(type)) && (artifact.getClassifier() != null)
				&& ("jpacktool_jdeps".equals(artifact.getClassifier()))) {

			handleJPacktoolProperties(dependencyNode);

		} else if (("xml".equals(type)) && (artifact.getClassifier() != null)
				&& ("jpacktool_business_update4j".equals(artifact.getClassifier()))) {

			handleUpdate4JConfig(dependencyNode);

		} else if (("xml".equals(type)) && (artifact.getClassifier() != null)
				&& ("jpacktool_bootstrap_update4j".equals(artifact.getClassifier()))) {

			handleUpdate4JConfig(dependencyNode);

		}
	}

	protected void handleUpdate4JConfig(final DependencyNode dependencyNode)
			throws MojoExecutionException, MojoFailureException {

		Artifact artifact = dependencyNode.getArtifact();
		Path source = artifact.getFile().toPath();
		Path target = outputDirectoryJPacktool.toPath().resolve("conf");
		if (!target.toFile().exists()) {
			target.toFile().mkdirs();
		}

		String artName = artifact.getArtifactId();

		if (stripConfigName != null) {
			artName = artName.replaceAll(stripConfigName, "");
		}
		target = target.resolve("update4j_" + artifact.getGroupId() + "_" + artName + ".xml");

		try {
			Files.copy(source, target, REPLACE_EXISTING);
		} catch (IOException e) {
			throw new MojoFailureException("i/o error", e);
		}
	}

	protected void handleJPacktoolProperties(final DependencyNode dependencyNode)
			throws MojoExecutionException, MojoFailureException {

		File file = dependencyNode.getArtifact().getFile();

		this.getLog().debug("adding system modules from " + file.getAbsolutePath());

		try (FileInputStream fin = new FileInputStream(file)) {
			Properties props = new Properties();
			props.load(fin);
			for (String s : props.getProperty("linked_system_deps").split(",")) {
				s = s.trim();
				if (!linkedSystemModules.contains(s)) {
					linkedSystemModules.add(s);
				}
			}
		} catch (IOException e) {
			throw new MojoFailureException("i/o error:", e);
		}
	}

	protected void executeCommand(final Commandline cmd, OutputStream outputStream) throws MojoExecutionException {
		ExecuteCommand.executeCommand(false, this.getLog(), cmd, outputStream);
	}

	public List<String> getLinkedSystemModules() {
		return linkedSystemModules;
	}
}
