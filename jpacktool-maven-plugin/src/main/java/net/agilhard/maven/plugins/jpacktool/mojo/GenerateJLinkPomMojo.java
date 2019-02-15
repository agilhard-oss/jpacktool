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

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Generate pom for packaging a project using a Bootstrap and a Business
 * Application and use JLink as the final step.
 *
 * @author Bernd Eilers
 */
@Mojo(name = "jpacktool-generate-jlink", requiresDependencyResolution = ResolutionScope.NONE, defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresProject = true)
public class GenerateJLinkPomMojo extends AbstractGenerateJPacktoolPomMojo {

	
	/**
	 * Name of the script generated from launcherTemplate for linux
	 */
	@Parameter(defaultValue = "start.sh")
	protected String launcherTemplateScriptWindows;

	/**
	 * Name of the script generated from launcherTemplate for linux
	 */
	@Parameter(defaultValue = "start.sh")
	protected String launcherTemplateScriptMac;

	/**
	 * Name of the script generated from launcherTemplate for linux
	 */
	@Parameter(defaultValue = "start.sh")
	protected String launcherTemplateScriptLinux;
	
	/***
	 * The Template for generating the pom for a final JLink Step
	 */
	@Parameter(required = false, readonly = false, defaultValue = "resource:/templates/pom/main-jpacktool/bootstrap-jlink/pom.xml")
	protected String templateBootstrapJLink;
	
	/**
	 * The output directory for the resulting Run Time Image when packagingTool is
	 * &quot;jlink&quot; The created Run Time Image is stored in non compressed
	 * form. This will later being packaged into a <code>zip</code> file.
	 * <code>--output &lt;path&gt;</code>
	 */
	// TODO: is this a good final location?
	@Parameter(defaultValue = "${project.build.directory}/maven-jlink", required = true, readonly = true)
	protected File outputDirectoryImage;

	public GenerateJLinkPomMojo() {
	}

	@Override
	public String getPackagingTool() {
		// TODO Auto-generated method stub
		return "jlink";
	}

	protected void initTemplates() throws MojoFailureException {
		super.initTemplates();
		this.templateBootstrapJLink = this.initTemplate(this.templateBootstrapJLink, "bootstrap_jlink_pom.xml");
	}

	@Override
	protected void generatePoms(File mainDir) throws MojoFailureException {
		File dir = new File(mainDir, this.projectArtifactId + "-jlink");
		this.generatePom("bootstrap_jlink_pom.xml", dir);
	}

	protected void initJPacktoolModel() throws MojoFailureException {
		super.initJPacktoolModel();
		if (this.outputDirectoryImage != null) {
			this.jpacktoolModel.put("outputDirectoryImage", this.outputDirectoryImage.getPath());
		}
		if ( launcherTemplateScriptWindows != null ) {
			this.jpacktoolModel.put("launcherTemplateScriptWindows", launcherTemplateScriptWindows);
		}
		if ( launcherTemplateScriptLinux != null ) {
			this.jpacktoolModel.put("launcherTemplateScriptLinux", launcherTemplateScriptLinux);
		}
		if ( launcherTemplateScriptMac != null ) {
			this.jpacktoolModel.put("launcherTemplateScriptMac", launcherTemplateScriptMac);
		}

		
		
	}

}
