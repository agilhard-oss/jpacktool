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
import java.util.HashMap;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import net.agilhard.maven.plugins.jpacktool.base.mojo.AbstractTemplateToolMojo;


/**
 * Generate pom for packaging a project using a Bootstrap and a Business Application.
 *
 * @author Bernd Eilers
 *
 */
@Mojo(name = "jpacktool-generate-pom", requiresDependencyResolution = ResolutionScope.NONE, defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresProject = true)
public class GenerateJPacktoolPomMojo extends AbstractTemplateToolMojo {

	/**
	 * Directory where the maven pom.xml generated are written to.
	 */
	@Parameter(defaultValue = "${project.build.directory}/maven-jpacktool/build", required = true, readonly = true)
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

	/**
	 * Version of the generated project
	 */
	@Parameter(required = false, readonly = false)
	protected String projectVersion;
	
	/**
	 * GroupId of the Business Application.
	 */
	@Parameter(required = true, readonly = false)
	protected String businessGroupId;

	/**
	 * ArtifactId of the Business Application.
	 */
	@Parameter(required = true, readonly = false)
	protected String businessArtifactId;

	/**
	 * Version of the Business Application.
	 */
	@Parameter(required = true, readonly = false)
	protected String businessVersion;

	/**
	 * GroupId of the Bootstrap Application.
	 */
	@Parameter(required = true, readonly = false)
	protected String bootstrapGroupId;

	/**
	 * ArtifactId of the Bootstrap Application.
	 */
	@Parameter(required = true, readonly = false)
	protected String bootstrapArtifactId;

	/**
	 * Version of the Bootstrap Application.
	 */
	@Parameter(required = true, readonly = false)
	protected String bootstrapVersion;

	/**
	 * Flag if the businessBasePath setting is relativ to ${user.dir}
	 */
	@Parameter(defaultValue = "true", required = false, readonly = false)
	protected boolean businessBasePathBelowUserDir;

	/**
	 * The Update4j basePath Setting for the Business Application.
	 */
	@Parameter(required = true, readonly = false)
	protected String businessBasePath;

	/**
	 * The Update4j baseUri Setting for the Business Application.
	 */
	@Parameter(required = true, readonly = false)
	protected String businessBaseUri;

	/**
	 * Flag if the bootstrapBasePath setting is relativ to ${user.dir}
	 */
	@Parameter(defaultValue = "true", required = false, readonly = false)
	protected boolean bootstrapBasePathBelowUserDir;

	/**
	 * The Update4j basePath Setting for the Bootstrap Application.
	 */
	@Parameter(required = false, readonly = false)
	protected String bootstrapBasePath;

	/**
	 * The Update4j baseUri Setting for the Bootstrap Application.
	 */
	@Parameter(required = false, readonly = false)
	protected String bootstrapBaseUri;

	/**
	 * The main class for the Bootstrap Application.
	 */
	@Parameter(required = true, readonly = false)
	protected String bootstrapMainClass;

	/**
	 * The main module for the Bootstrap Application.
	 */
	@Parameter(required = false, readonly = false)
	protected String bootstrapMainModule;
	
	/**
	 * The template for the generated main pom.xml where the others are submodules to.
	 */
	@Parameter(required = false, readonly = false, defaultValue = "resource:/templates/pom/main-jpacktool/pom.xml")
	protected String templateMain;

	/**
	 * The Template for generating the pom for the Bootstrap Application
	 */
	@Parameter(required = false, readonly = false, defaultValue = "resource:/templates/pom/main-jpacktool/bootstrap/pom.xml")
	protected String templateBootstrap;

	/**
	 * The Template for generating the pom for the Business Application
	 */
	@Parameter(required = false, readonly = false, defaultValue = "resource:/templates/pom/main-jpacktool/business/pom.xml")
	protected String templateBusiness;

	/***
	 * The Template for generating the pom for a final JLink Step
	 */
	@Parameter(required = false, readonly = false, defaultValue = "resource:/templates/pom/main-jpacktool/bootstrap-jlink/pom.xml")
	protected String templateBootstrapJLink;

	/***
	 * The Template for generating the pom for a final JPackage Step
	 */
	@Parameter(required = false, readonly = false, defaultValue = "resource:/templates/pom/main-jpacktool/bootstrap-jpackage/pom.xml")
	protected String templateBootstrapJPackage;
	
	/**
	 * JVM flags and options to pass to the application.
	 *
	 * <p>
	 * <code>--jvm-args &lt;args&gt;</code>
	 * </p>
	 */
	@Parameter(required = false, readonly = false)
	protected List<String> jvmArgs;
	
	/**
	 * Command line arguments to pass to the main class if no arguments are
	 * specified by the launcher.
	 *
	 * <p>
	 * <code>--arguments &lt;args&gt;</code>
	 * </p>
	 */
	@Parameter(required = false, readonly = false)
	protected List<String> arguments;

    /**
     * Set the JDK location to create a Java custom runtime image.
     */
    @Parameter
    protected File sourceJdkModules;

	/**
	 * Include additional paths on the <code>--module-path</code> option. Project
	 * dependencies and JDK modules are automatically added.
	 */
	@Parameter
	protected List<String> modulePaths;
	
	/**
	 * Limit the universe of observable modules. The following gives an example of
	 * the configuration which can be used in the <code>pom.xml</code> file.
	 *
	 * <pre>
	 *   &lt;limitModules&gt;
	 *     &lt;limitModule&gt;mod1&lt;/limitModule&gt;
	 *     &lt;limitModule&gt;xyz&lt;/limitModule&gt;
	 *     .
	 *     .
	 *   &lt;/limitModules&gt;
	 * </pre>
	 * <p>
	 * This configuration is the equivalent of the command line option:
	 * <code>--limit-modules &lt;mod&gt;[,&lt;mod&gt;...]</code>
	 * </p>
	 */
	@Parameter
	protected List<String> limitModules;

	/**
	 * Toggle whether to add all modules in the java boot path to the limitModules
	 * setting.
	 */
	@Parameter(defaultValue = "false")
	protected boolean addJDKToLimitModules;

	/**
	 * <p>
	 * Usually this is not necessary, cause this is handled automatically by the
	 * given dependencies.
	 * </p>
	 * <p>
	 * By using the --add-modules you can define the root modules to be resolved.
	 * The configuration in <code>pom.xml</code> file can look like this:
	 * </p>
	 *
	 * <pre>
	 * &lt;addModules&gt;
	 *   &lt;addModule&gt;mod1&lt;/addModule&gt;
	 *   &lt;addModule&gt;first&lt;/addModule&gt;
	 *   .
	 *   .
	 * &lt;/addModules&gt;
	 * </pre>
	 * <p>
	 * The command line equivalent for jlink is:
	 * <code>--add-modules &lt;mod&gt;[,&lt;mod&gt;...]</code>.
	 * </p>
	 */
	@Parameter
	protected List<String> addModules;

	
	/**
	 * Name of the classpath folder
	 */
	@Parameter(defaultValue = "jar")
	protected String classPathFolderName;

	/**
	 * Name of the automatic-modules folder
	 */
	@Parameter(defaultValue = "jar-automatic")
	protected String automaticModulesFolderName;

	/**
	 * Name of the modules folder
	 */
	@Parameter(defaultValue = "jmods")
	protected String modulesFolderName;

    
    /**
     * Packaging mode.
     * 
     * can be either &quot;jlink&quot; or &quot;jpackage&quot;
     */
	@Parameter(required = true, readonly = false, defaultValue="jlink")
	protected String packagingTool;
	
    
	public GenerateJPacktoolPomMojo() {
	}

	@Override
	public void executeToolMain() throws MojoExecutionException, MojoFailureException {		
		failIfParametersAreNotInTheirValidValueRanges();

		initDefaults();
		initTemplates();
		initJPacktoolModel();
		generatePoms();
	}

	protected void failIfParametersAreNotInTheirValidValueRanges() throws MojoFailureException {
		if ( ! ("jlink".equals(packagingTool) || "jpackage".equals(packagingTool) ) ) {
			throw new MojoFailureException("packagingMode must be jlink or jpackage");
		}
	}
	
	protected void initDefaults() {
		if ( projectGroupId == null ) {
			projectGroupId = businessGroupId;
		}
		if ( projectArtifactId == null ) {
			projectArtifactId = businessArtifactId + "-jpacktool";
		}
		if ( projectVersion == null ) {
			projectVersion = businessVersion;
		}
		
	}
	
	protected void initJPacktoolModel() throws MojoFailureException {
		jpacktoolModel = new HashMap<String, Object>();
				
		jpacktoolModel.put("jpacktoolVersion", getPluginVersion());
		jpacktoolModel.put("packagingTool", packagingTool);
		jpacktoolModel.put("projectGroupId", projectGroupId);
		jpacktoolModel.put("projectArtifactId", projectArtifactId);
		jpacktoolModel.put("projectVersion", projectVersion);
		jpacktoolModel.put("businessGroupId", businessGroupId);
		jpacktoolModel.put("businessArtifactId", businessArtifactId);
		jpacktoolModel.put("businessVersion", businessVersion);
		jpacktoolModel.put("bootstrapGroupId", bootstrapGroupId);
		jpacktoolModel.put("bootstrapArtifactId", bootstrapArtifactId);
		jpacktoolModel.put("bootstrapVersion", bootstrapVersion);
		jpacktoolModel.put("businessBasePathBelowUserDir", businessBasePathBelowUserDir);
		jpacktoolModel.put("businessBasePath", businessBasePath);
		jpacktoolModel.put("businessBaseUri", businessBaseUri);
		jpacktoolModel.put("bootstrapBasePathBelowUserDir", bootstrapBasePathBelowUserDir);
		jpacktoolModel.put("bootstrapMainClass", bootstrapMainClass);
		jpacktoolModel.put("bootstrapMainModule", bootstrapMainModule);
		
		if (bootstrapBasePath != null) {
			jpacktoolModel.put("bootstrapBasePath", bootstrapBasePath);
		}
		
		if (bootstrapBaseUri != null) {
			jpacktoolModel.put("bootstrapBaseUri", bootstrapBaseUri);
		}
		
		if ( arguments != null ) {
			StringBuffer sb = new StringBuffer();
			for ( String arg : arguments ) {
				sb.append("              <argument>");
				sb.append(arg);
				sb.append("</argument>\n");
			}
			jpacktoolModel.put("argumentsXML", sb.toString());
		}
		
		if ( jvmArgs != null ) {
			StringBuffer sb = new StringBuffer();
			for ( String arg : jvmArgs ) {
				sb.append("              <jvmArg>");
				sb.append(arg);
				sb.append("</jvmArg>\n");
			}
			jpacktoolModel.put("jvmArgsXML", sb.toString());
		}
		
		if ( sourceJdkModules != null ) {
			jpacktoolModel.put("sourceJdkModules", sourceJdkModules);
		}
		
		if ( modulePaths != null ) {
			StringBuffer sb = new StringBuffer();
			for ( String s : modulePaths ) {
				sb.append("              <modulePath>");
				sb.append(s);
				sb.append("</modulePath>\n");
			}
			jpacktoolModel.put("modulePathsXML", sb.toString());
		}
		
		if ( limitModules != null ) {
			StringBuffer sb = new StringBuffer();
			for ( String s : limitModules ) {
				sb.append("              <limitModule>");
				sb.append(s);
				sb.append("</limitModule>\n");
			}
			jpacktoolModel.put("limitModulesXML", sb.toString());
		}
		
		jpacktoolModel.put("addJDKToLimitModules", Boolean.valueOf(addJDKToLimitModules).toString() );
	
		
		if ( addModules != null ) {
			StringBuffer sb = new StringBuffer();
			for ( String s : addModules ) {
				sb.append("              <addModule>");
				sb.append(s);
				sb.append("</addModules>\n");
			}
			jpacktoolModel.put("addModulesXML", sb.toString());
		}
		
	}


	protected void initTemplates() throws MojoFailureException {
		templateMain = initTemplate(templateMain, "main_pom.xml");
		templateBootstrap = initTemplate(templateBootstrap, "bootstrap_pom.xml");
		templateBusiness = initTemplate(templateBusiness, "business_pom.xml");
		if ( "jlink".equals(packagingTool) ) {
			templateBootstrapJLink = initTemplate(templateBootstrapJLink, "bootstrap_jlink_pom.xml");
		} else {
			templateBootstrapJPackage = initTemplate(templateBootstrapJPackage, "bootstrap_jpackage_pom.xml");
		}
	}

	protected void generatePoms() throws MojoFailureException {
		File mainDir = new File(outputDirectoryBuild, projectArtifactId);
		generatePom("main_pom.xml", mainDir);

		File dir = new File(mainDir, projectArtifactId + "-bootstrap");
		generatePom("bootstrap_pom.xml", dir);
		
		dir = new File(mainDir, projectArtifactId + "-business");
		generatePom("business_pom.xml", dir);
		
		if ( "jlink".equals(packagingTool) ) {
			dir = new File(mainDir, projectArtifactId + "-jlink");
			generatePom("bootstrap_jlink_pom.xml", dir);
		} else {
			dir = new File(mainDir, projectArtifactId + "-jlink");
			generatePom("bootstrap_jlink_pom.xml", dir);
		}
	}
	
	protected void generatePom(String templateName, File outputDir) throws MojoFailureException {
		File outputFile=new File(outputDir, "pom.xml");
		generateFromTemplate(templateName, outputFile);
	}

}
