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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.CommandLineConfigurationException;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenCommandLineBuilder;
import org.apache.maven.shared.invoker.MavenInvocationException;

@Mojo(name = "jpacktool-invoke-pom", requiresDependencyResolution = ResolutionScope.NONE, defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true)
/**
 * Invoke clean install on generated pom.
 *
 * @author Bernd Eilers
 *
 */
public class InvokePomMojo extends AbstractMojo {

	
	@Parameter(defaultValue = "${project.build.directory}/jpacktool/build", required = true, readonly = true)
	protected File outputDirectoryBuild;

	/**
	 * GroupId of the generated project.
	 * If not set the businessGroupId is being used.
	 */
	@Parameter(required = false, readonly = false)
	protected String projectGroupId;

	/**
	 * ArtifactId of the generated project
	 * If not set businessArtifactId appended by "-jpacktool" is being used.
	 */
	@Parameter(required = false, readonly = false)
	protected String projectArtifactId;

	@Parameter(required = true, readonly = false)
	protected String businessGroupId;

	@Parameter(required = true, readonly = false)
	protected String businessArtifactId;


	/**
	 * mavenExecutable can either be a file relative to
	 * <code>${maven.home}/bin/</code> or an absolute file.
	 */
	@Parameter(property = "invoker.mavenExecutable")
	private String mavenExecutable;

	/**
	 * The home directory of the Maven installation to use for the forked builds.
	 * Defaults to the current Maven installation.
	 *
	 */
	@Parameter(property = "invoker.mavenHome")
	private File mavenHome;

	/**
	 * The local repository for caching artifacts. It is strongly recommended to
	 * specify a path to an isolated repository like
	 * <code>${project.build.directory}/it-repo</code>. Otherwise, your ordinary
	 * local repository will be used, potentially soiling it with broken artifacts.
	 */
	@Parameter(property = "invoker.localRepositoryPath", defaultValue = "${settings.localRepository}")
	private File localRepositoryPath;

	/**
	 * Whether to show errors in the build output.
	 */
	@Parameter(property = "invoker.showErrors", defaultValue = "false")
	private boolean showErrors;

	/**
	 * Whether to show debug statements in the build output.
	 */
	@Parameter(property = "invoker.debug", defaultValue = "false")
	private boolean debug;

    /**
     * Path to an alternate <code>settings.xml</code> to use for Maven invocation with all ITs. Note that the
     * <code>&lt;localRepository&gt;</code> element of this settings file is always ignored, i.e. the path given by the
     * parameter {@link #localRepositoryPath} is dominant.
     *
     */
    @Parameter( property = "invoker.settingsFile" )
    private File settingsFile;

    
    /**
     * The <code>JAVA_HOME</code> environment variable to use for forked Maven invocations. Defaults to the current Java
     * home directory.
     *
     */
    @Parameter( property = "invoker.javaHome" )
    private File javaHome;

	/**
	 */
	@Component
	private Invoker invoker;
	
	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	protected MavenProject project;
	
	public InvokePomMojo() {
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		if ( projectGroupId == null ) {
			projectGroupId = businessGroupId;
		}
		if ( projectArtifactId == null ) {
			projectArtifactId = businessArtifactId + "-jpacktool";
		}
		
		File mainDir = new File(outputDirectoryBuild, projectArtifactId);
		
		File pomFile = new File(mainDir,"pom.xml");
		
		runBuild(mainDir, pomFile, settingsFile, javaHome, "clean", "install");
	
		File propertiesFile = new File(this.outputDirectoryBuild,"jpacktool.properties");

		if ( ! propertiesFile.exists() ) {
			Properties prop=new Properties();
			SimpleDateFormat sdf;
			sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			String text = sdf.format(new Date());
			prop.setProperty("timestamp",text);
		
			try ( FileOutputStream out = new FileOutputStream(propertiesFile)) {
				prop.store(out, "jpacktool");
			} catch (FileNotFoundException e) {
				throw new MojoExecutionException("file not found", e);
			} catch (IOException e) {
				throw new MojoExecutionException("i/o error", e);
			}
		}
		
		project.getArtifact().setFile(propertiesFile);

	}

	
	/**
	 * Runs the specified project.
	 *
	 * @param basedir           The base directory of the project, must not be
	 *                          <code>null</code>.
	 * @param pomFile           The (already interpolated) POM file, may be
	 *                          <code>null</code> for a POM-less Maven invocation.
	 * @param settingsFile      The (already interpolated) user settings file for
	 *                          the build, may be <code>null</code>. Will be merged
	 *                          with the settings file of the invoking Maven
	 *                          process.
	 * @param goals				The list of goals                         
	 *                          
	 * @return <code>true</code> if the project was launched or <code>false</code>
	 *         if the selector script indicated that the project should be skipped.
	 * @throws org.apache.maven.plugin.MojoExecutionException If the project could
	 *         not be launched.
	 *
	 */
	private boolean runBuild(File basedir, File pomFile, File settingsFile, File actualJavaHome, String... goals)
			throws MojoExecutionException {
		return runBuild(basedir, pomFile, settingsFile, actualJavaHome, Arrays.asList(goals));
	}
	
	/**
	 * Runs the specified project.
	 *
	 * @param basedir           The base directory of the project, must not be
	 *                          <code>null</code>.
	 * @param pomFile           The (already interpolated) POM file, may be
	 *                          <code>null</code> for a POM-less Maven invocation.
	 * @param settingsFile      The (already interpolated) user settings file for
	 *                          the build, may be <code>null</code>. Will be merged
	 *                          with the settings file of the invoking Maven
	 *                          process.
	 * @param goals				The list of goals                         
	 *                          
	 * @return <code>true</code> if the project was launched or <code>false</code>
	 *         if the selector script indicated that the project should be skipped.
	 * @throws org.apache.maven.plugin.MojoExecutionException If the project could
	 *         not be launched.
	 *
	 */
	private boolean runBuild(File basedir, File pomFile, File settingsFile, File actualJavaHome, List<String> goals)
			throws MojoExecutionException {

		List<String> profiles = new ArrayList<>();

		Map<String, Object> context = new LinkedHashMap<>();

		boolean selectorResult = true;

		final InvocationRequest request = new DefaultInvocationRequest();

		request.setLocalRepositoryDirectory(localRepositoryPath);

		request.setBatchMode(true);

		request.setShowErrors(showErrors);

		request.setDebug(debug);

		if (mavenHome != null) {
			invoker.setMavenHome(mavenHome);
			// FIXME: Should we really take care of M2_HOME?
			request.addShellEnvironment("M2_HOME", mavenHome.getAbsolutePath());
		}

		if (mavenExecutable != null) {
			invoker.setMavenExecutable(new File(mavenExecutable));
		}

		if (actualJavaHome != null) {
			request.setJavaHome(actualJavaHome);
		}

		request.setBaseDirectory(basedir);

		request.setPomFile(pomFile);

		request.setGoals(goals);

		request.setProfiles(profiles);

		request.setOffline(false);

		request.setTimeoutInSeconds(0);

		if (getLog().isDebugEnabled()) {
			try {
				getLog().debug("Using MAVEN_OPTS: " + request.getMavenOpts());
				getLog().debug("Executing: " + new MavenCommandLineBuilder().build(request));
			} catch (CommandLineConfigurationException e) {
				getLog().debug("Failed to display command line: " + e.getMessage());
			}
		}

		InvocationResult result;
		
		boolean b=true;
		
		try {
			result = invoker.execute(request);
			b = result.getExitCode() == 0;
			if ( ! b  ) {
				getLog().error("ExitCode != 0 from invoke");
				throw new MojoExecutionException("error during invoke: ExitCode != 0");
			}
		} catch (final MavenInvocationException e) {
			getLog().debug("Error invoking Maven: " + e.getMessage(), e);
			throw new MojoExecutionException("Maven invocation failed. ", e);
		}

		return b;
	}

}
