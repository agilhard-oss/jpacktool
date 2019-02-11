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

@Mojo(name = "jpacktool-generate-jpackage", requiresDependencyResolution = ResolutionScope.NONE, defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresProject = true)
public class GenerateJPackagePomMojo extends AbstractGenerateJPacktoolPomMojo {

	
	/***
	 * The Template for generating the pom for a final JPackage Step
	 */
	@Parameter(required = false, readonly = false, defaultValue = "resource:/templates/pom/main-jpacktool/bootstrap-jpackage/pom.xml")
	protected String templateBootstrapJPackage;

	public GenerateJPackagePomMojo() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getPackagingTool() {
		return "jpackage";
	}

	protected void initTemplates() throws MojoFailureException {
		super.initTemplates();
		this.templateBootstrapJPackage = this.initTemplate(this.templateBootstrapJPackage,
				"bootstrap_jpackage_pom.xml");
	}

	@Override
	protected void generatePoms(File mainDir) throws MojoFailureException {
		File dir = new File(mainDir, this.projectArtifactId + "-jlink");
		this.generatePom("bootstrap_jpackage_pom.xml", dir);		
	}
	
	
	
}
