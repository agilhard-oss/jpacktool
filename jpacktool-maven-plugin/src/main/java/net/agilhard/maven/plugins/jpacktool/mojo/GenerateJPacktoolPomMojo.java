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
 */
@Mojo(
    name = "jpacktool-generate-pom", requiresDependencyResolution = ResolutionScope.NONE,
    defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresProject = true)
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
     * The main class for the Bootstrap Application.
     */
    @Parameter(required = false, readonly = false)
    protected String businessMainClass;

    /**
     * The template for the generated main pom.xml where the others are submodules to.
     */
    @Parameter(required = false, readonly = false, defaultValue = "resource:/templates/pom/main-jpacktool/pom.xml")
    protected String templateMain;

    /**
     * The Template for generating the pom for the Bootstrap Application
     */
    @Parameter(
        required = false, readonly = false, defaultValue = "resource:/templates/pom/main-jpacktool/bootstrap/pom.xml")
    protected String templateBootstrap;

    /**
     * The Template for generating the pom for the Business Application
     */
    @Parameter(
        required = false, readonly = false, defaultValue = "resource:/templates/pom/main-jpacktool/business/pom.xml")
    protected String templateBusiness;

    /***
     * The Template for generating the pom for a final JLink Step
     */
    @Parameter(
        required = false, readonly = false,
        defaultValue = "resource:/templates/pom/main-jpacktool/bootstrap-jlink/pom.xml")
    protected String templateBootstrapJLink;

    /***
     * The Template for generating the pom for a final JPackage Step
     */
    @Parameter(
        required = false, readonly = false,
        defaultValue = "resource:/templates/pom/main-jpacktool/bootstrap-jpackage/pom.xml")
    protected String templateBootstrapJPackage;

    /**
     * JVM flags and options to pass to the application.
     * <p>
     * <code>--jvm-args &lt;args&gt;</code>
     * </p>
     */
    @Parameter(required = false, readonly = false)
    protected List<String> jvmArgs;

    /**
     * Command line arguments to pass to the main class if no arguments are
     * specified by the launcher.
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
     * can be either &quot;jlink&quot; or &quot;jpackage&quot;
     */
    @Parameter(required = true, readonly = false, defaultValue = "jlink")
    protected String packagingTool;

    public GenerateJPacktoolPomMojo() {}

    @Override
    public void executeToolMain() throws MojoExecutionException, MojoFailureException {
        this.failIfParametersAreNotInTheirValidValueRanges();

        this.initDefaults();
        this.initTemplates();
        this.initJPacktoolModel();
        this.generatePoms();
    }

    protected void failIfParametersAreNotInTheirValidValueRanges() throws MojoFailureException {
        if (!("jlink".equals(this.packagingTool) || "jpackage".equals(this.packagingTool))) {
            throw new MojoFailureException("packagingMode must be jlink or jpackage");
        }
    }

    protected void initDefaults() {
        if (this.projectGroupId == null) {
            this.projectGroupId = this.businessGroupId;
        }
        if (this.projectArtifactId == null) {
            this.projectArtifactId = this.businessArtifactId + "-jpacktool";
        }
        if (this.projectVersion == null) {
            this.projectVersion = this.businessVersion;
        }

    }

    @SuppressWarnings("boxing")
    protected void initJPacktoolModel() throws MojoFailureException {
        this.jpacktoolModel = new HashMap<>();

        this.jpacktoolModel.put("jpacktoolVersion", this.getPluginVersion());
        this.jpacktoolModel.put("packagingTool", this.packagingTool);
        this.jpacktoolModel.put("projectGroupId", this.projectGroupId);
        this.jpacktoolModel.put("projectArtifactId", this.projectArtifactId);
        this.jpacktoolModel.put("projectVersion", this.projectVersion);
        this.jpacktoolModel.put("businessGroupId", this.businessGroupId);
        this.jpacktoolModel.put("businessArtifactId", this.businessArtifactId);
        this.jpacktoolModel.put("businessVersion", this.businessVersion);
        this.jpacktoolModel.put("bootstrapGroupId", this.bootstrapGroupId);
        this.jpacktoolModel.put("bootstrapArtifactId", this.bootstrapArtifactId);
        this.jpacktoolModel.put("bootstrapVersion", this.bootstrapVersion);
        this.jpacktoolModel.put("businessBasePathBelowUserDir", this.businessBasePathBelowUserDir);
        this.jpacktoolModel.put("businessBasePath", this.businessBasePath);
        this.jpacktoolModel.put("businessBaseUri", this.businessBaseUri);
        this.jpacktoolModel.put("bootstrapBasePathBelowUserDir", this.bootstrapBasePathBelowUserDir);
        this.jpacktoolModel.put("bootstrapMainClass", this.bootstrapMainClass);
        this.jpacktoolModel.put("bootstrapMainModule", this.bootstrapMainModule);
        this.jpacktoolModel.put("businessMainClass", this.businessMainClass);


        if (this.bootstrapBasePath != null) {
            this.jpacktoolModel.put("bootstrapBasePath", this.bootstrapBasePath);
        }

        if (this.bootstrapBaseUri != null) {
            this.jpacktoolModel.put("bootstrapBaseUri", this.bootstrapBaseUri);
        }

        if (this.arguments != null) {
            final StringBuffer sb = new StringBuffer();
            for (final String arg : this.arguments) {
                sb.append("              <argument>");
                sb.append(arg);
                sb.append("</argument>\n");
            }
            this.jpacktoolModel.put("argumentsXML", sb.toString());
        }

        if (this.jvmArgs != null) {
            final StringBuffer sb = new StringBuffer();
            for (final String arg : this.jvmArgs) {
                sb.append("              <jvmArg>");
                sb.append(arg);
                sb.append("</jvmArg>\n");
            }
            this.jpacktoolModel.put("jvmArgsXML", sb.toString());
        }

        if (this.sourceJdkModules != null) {
            this.jpacktoolModel.put("sourceJdkModules", this.sourceJdkModules);
        }

        if (this.modulePaths != null) {
            final StringBuffer sb = new StringBuffer();
            for (final String s : this.modulePaths) {
                sb.append("              <modulePath>");
                sb.append(s);
                sb.append("</modulePath>\n");
            }
            this.jpacktoolModel.put("modulePathsXML", sb.toString());
        }

        if (this.limitModules != null) {
            final StringBuffer sb = new StringBuffer();
            for (final String s : this.limitModules) {
                sb.append("              <limitModule>");
                sb.append(s);
                sb.append("</limitModule>\n");
            }
            this.jpacktoolModel.put("limitModulesXML", sb.toString());
        }

        this.jpacktoolModel.put("addJDKToLimitModules", Boolean.valueOf(this.addJDKToLimitModules).toString());

        if (this.addModules != null) {
            final StringBuffer sb = new StringBuffer();
            for (final String s : this.addModules) {
                sb.append("              <addModule>");
                sb.append(s);
                sb.append("</addModules>\n");
            }
            this.jpacktoolModel.put("addModulesXML", sb.toString());
        }

    }

    @Override
    protected void initTemplates() throws MojoFailureException {
        this.templateMain = this.initTemplate(this.templateMain, "main_pom.xml");
        this.templateBootstrap = this.initTemplate(this.templateBootstrap, "bootstrap_pom.xml");
        this.templateBusiness = this.initTemplate(this.templateBusiness, "business_pom.xml");
        if ("jlink".equals(this.packagingTool)) {
            this.templateBootstrapJLink = this.initTemplate(this.templateBootstrapJLink, "bootstrap_jlink_pom.xml");
        } else {
            this.templateBootstrapJPackage =
                this.initTemplate(this.templateBootstrapJPackage, "bootstrap_jpackage_pom.xml");
        }
    }

    protected void generatePoms() throws MojoFailureException {
        final File mainDir = new File(this.outputDirectoryBuild, this.projectArtifactId);
        this.generatePom("main_pom.xml", mainDir);

        File dir = new File(mainDir, this.projectArtifactId + "-bootstrap");
        this.generatePom("bootstrap_pom.xml", dir);

        dir = new File(mainDir, this.projectArtifactId + "-business");
        this.generatePom("business_pom.xml", dir);

        if ("jlink".equals(this.packagingTool)) {
            dir = new File(mainDir, this.projectArtifactId + "-jlink");
            this.generatePom("bootstrap_jlink_pom.xml", dir);
        } else {
            dir = new File(mainDir, this.projectArtifactId + "-jlink");
            this.generatePom("bootstrap_jlink_pom.xml", dir);
        }
    }

    protected void generatePom(final String templateName, final File outputDir) throws MojoFailureException {
        final File outputFile = new File(outputDir, "pom.xml");
        this.generateFromTemplate(templateName, outputFile);
    }

}
