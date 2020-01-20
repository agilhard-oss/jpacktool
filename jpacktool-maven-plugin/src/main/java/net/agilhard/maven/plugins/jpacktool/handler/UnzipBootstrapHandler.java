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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

public class UnzipBootstrapHandler extends AbstractEndVisitDependencyHandler {

	protected boolean bootstrapFound;

	protected List<File> fileList;

	protected boolean collect;

	public UnzipBootstrapHandler(final AbstractToolMojo mojo, final DependencyGraphBuilder dependencyGraphBuilder,
			final File outputDirectoryJPacktool, final boolean collect) throws MojoExecutionException {

		super(mojo, dependencyGraphBuilder);
		this.outputDirectoryJPacktool = outputDirectoryJPacktool;
		this.collect = collect;
		this.fileList = new ArrayList<File>();
	}

	/** {@inheritDoc} */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		this.getLog().info("import-jdeps");
		super.execute();
	}

	@Override
    protected void handleModJar(final DependencyNode dependencyNode, final Artifact artifact,
			final Map.Entry<File, JavaModuleDescriptor> entry) throws MojoExecutionException, MojoFailureException {

	}

	@Override
    protected void handleNonModJar(final DependencyNode dependencyNode, final Artifact artifact,
			final Map.Entry<File, JavaModuleDescriptor> entry) throws MojoExecutionException, MojoFailureException {

	}

	@Override
    protected void handleOther(final DependencyNode dependencyNode)
			throws MojoExecutionException, MojoFailureException {
		this.getLog().debug("handleOther " + dependencyNode.toNodeString());

		final Artifact artifact = dependencyNode.getArtifact();
		final String type = artifact.getType();

		if (("zip".equals(type)) && (artifact.getClassifier() != null)
				&& ("jpacktool_bootstrap".equals(artifact.getClassifier()))) {

			this.setBootstrapFound(true);
			this.handleBootstrap(dependencyNode);

		}
	}

	protected void handleBootstrap(final DependencyNode dependencyNode)
			throws MojoExecutionException, MojoFailureException {

		try {
			if (this.collect) {
				this.fileList.addAll(UnzipUtility.collect(dependencyNode.getArtifact().getFile(), this.outputDirectoryJPacktool));
			} else {
				UnzipUtility.unzip(dependencyNode.getArtifact().getFile(), this.outputDirectoryJPacktool);
			}
		} catch (final IOException e) {
			throw new MojoFailureException("i/o error:", e);
		}
	}

	protected void executeCommand(final Commandline cmd, final OutputStream outputStream) throws MojoExecutionException {
		ExecuteCommand.executeCommand(false, this.getLog(), cmd, outputStream);
	}

	public boolean isBootstrapFound() {
		return this.bootstrapFound;
	}

	public void setBootstrapFound(final boolean bootstrapFound) {
		this.bootstrapFound = bootstrapFound;
	}

	public void deleteFiles() throws MojoExecutionException {
		for (final File f : this.fileList) {
			if (f.isFile()) {
				this.getLog().debug("delete file "+f.getAbsolutePath());
				f.delete();
			}
		}
		
		// do not delete empty directory here it makes problems on Windows afterwards

	}
}
