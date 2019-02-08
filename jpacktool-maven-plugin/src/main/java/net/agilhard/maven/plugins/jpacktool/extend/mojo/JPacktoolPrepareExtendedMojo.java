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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import net.agilhard.maven.plugins.jpacktool.base.mojo.JPackToolPrepareMojo;
import net.agilhard.maven.plugins.jpacktool.handler.ImportJDepsAndUdate4jConfigHandler;
import net.agilhard.maven.plugins.jpacktool.handler.UnzipBootstrapHandler;


/**
 * Extended class to prepare execution of the jlink and jpackager goals by analyzing java module dependencies and copying files.
 *  
 * This extends the base class by some features for update4j.
 *
 * @author Bernd Eilers
 *
 */
@Mojo(name = "jpacktool-prepare", requiresDependencyResolution = ResolutionScope.RUNTIME, defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresProject = true)
public class JPacktoolPrepareExtendedMojo extends JPackToolPrepareMojo {


	@Parameter(defaultValue = "true", required = true, readonly = false)
	protected boolean addLinkedSystemJDepsImports;
	
	@Parameter(defaultValue = "false", required = true, readonly = false)
	protected boolean packForBusinessApp;
	
	/**
	 * replace this with nothing in the name of the config file
	 */	
	@Parameter(required = false, readonly = false, defaultValue = "-jpacktool")
	protected String stripConfigName;

	protected ImportJDepsAndUdate4jConfigHandler importJDepsAndConfigHandler;
	
	UnzipBootstrapHandler unzipBootstrapHandler;
	
	public JPacktoolPrepareExtendedMojo() {
		// TODO Auto-generated constructor stub
	}

	
	/**
	 * Hook for from base classes to execute something before has CollectJarsHandler is being called.
	 * 
	 * @throws MojoExecutionException on plugin execution error
	 * @throws MojoFailureException	on plugin failure
	 */
	public void executeBeforeCopy() throws MojoExecutionException, MojoFailureException {
		unzipBootstrapHandler = createUnzipBootstrapHandler();
		unzipBootstrapHandler.execute();
		setSkipCopy(unzipBootstrapHandler.isBootstrapFound() && (!packForBusinessApp));
		setSkipJDeps(false);
	}
	
	/**
	 * Hook from base class to execute something after the JDeps Handler has finished.
	 * 
	 * @throws MojoExecutionException on plugin execution error
	 * @throws MojoFailureException	on plugin failure
	 */
	@Override
	public void executeAfterJDeps() throws MojoExecutionException, MojoFailureException {

		importJDepsAndConfigHandler = createImportJDepsAndConfigHandler();
		importJDepsAndConfigHandler.execute();
		
		if ( addLinkedSystemJDepsImports ) {
			getHandler().getLinkedSystemModules().addAll(importJDepsAndConfigHandler.getLinkedSystemModules());
		}
		if ( (unzipBootstrapHandler != null) && packForBusinessApp) {
			unzipBootstrapHandler.deleteFiles();
		}
	}
	
	/**
	 * Create the ImportJDepsAndUdate4jConfigHandler
	 * @throws MojoExecutionException on plugin execution error
	 * @throws MojoFailureException	on plugin failure
	 * @return the ImportJDepsAndUdate4jConfigHandler
	 */
	public ImportJDepsAndUdate4jConfigHandler createImportJDepsAndConfigHandler() throws MojoExecutionException, MojoFailureException {
		return new ImportJDepsAndUdate4jConfigHandler(this, dependencyGraphBuilder, stripConfigName);

	}
	
	/**
	 * Create the UnzipBootstrapHandler
	 * @throws MojoExecutionException on plugin execution error
	 * @throws MojoFailureException	on plugin failure
	 * @return the UnzipBootstrapHandler
	 */
	public UnzipBootstrapHandler createUnzipBootstrapHandler()  throws MojoExecutionException, MojoFailureException {
		return new UnzipBootstrapHandler(this, dependencyGraphBuilder, outputDirectoryJPacktool, packForBusinessApp);
	}
}
