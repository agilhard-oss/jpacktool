package net.agilhard.jpacktool.util.update4j;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.update4j.Configuration;
import org.update4j.FileMetadata;
import org.update4j.service.Delegate;
import org.update4j.service.Launcher;
import org.update4j.service.UpdateHandler;

import net.agilhard.jpacktool.util.splash.SplashScreenHelper;

/**
 * @author Bernd Eilers
 */
public class JPacktoolBootstrap implements Delegate, UpdateHandler {

	private SplashScreenHelper splash;

	public JPacktoolBootstrap() {
        splash = SplashScreenHelper.getInstance();
	}

	public static void main(String[] args) throws Throwable {
		JPacktoolBootstrap app = new JPacktoolBootstrap();
		app.main(Arrays.asList(args));
	}
	
	@Override
	public void main(List<String> args) throws Throwable {
		File propsFile = new File(new File("conf"),"jpacktool.properties");
		if ( ! propsFile.exists() ) {
			error("Properties file not found!");
			throw new FileNotFoundException("conf/jpacktool.properties");
		}
		Properties props = new Properties();
		try ( FileInputStream inStream = new FileInputStream(propsFile) ) {
			props.load(inStream);
		} catch (IOException ioe ) {
			error("I/O error reading properties file!");
		}
		String projectConfigName=props.getProperty("projectConfigName");
		String businessBaseUri=props.getProperty("businessBaseUri");
		if ( (projectConfigName == null) || (businessBaseUri == null) ) {
			error("Needed properties not found!");
			return;
		}
		start(businessBaseUri, projectConfigName);
	}

	
	public void message(String text) {
		System.out.println(text);
		splash.setMessage(text);
	}
	
	public void message(String textShort, String text) {
		System.out.println(text);
		splash.setMessage(textShort);
	}
	
	public void error(String text) {
		System.err.println(text);
		splash.error(text);
	}
	public void error(String textShort, String text) {
		System.err.println(text);
		splash.error(textShort);
	}
	
	void start(String baseUri, String configName) throws IOException {
		
		URL configUrl;
		try {
			configUrl = new URL(baseUri+"/"+configName);
		} catch (MalformedURLException e1) {
			return;
		}
		
		Configuration config = null;
		try (Reader in = new InputStreamReader(configUrl.openStream(), StandardCharsets.UTF_8)) {
			config = Configuration.read(in);
		} catch (IOException e) {
			message("Could not load remote config, falling back to local.", "Could not load remote config "+configUrl.toString()+", falling back to local");
			File confDir = new File("conf");
			if ( ! confDir.isDirectory()  ) {
				error("Directory \"conf\" does not exist or is not a directory.");
				return;
			}
			Path path = confDir.toPath().resolve(configName);
			try (Reader in = Files.newBufferedReader(path)) {
				config = Configuration.read(in);
			} catch (IOException ioe) {
				error("Could not read local config.", "Could not read local config "+path.toString());
				throw ioe;
			}
		}
		
		message("Checking if update is required");
		
		try {
			if ( config.requiresUpdate()) {
				message("Updating application ...");
				config.update((UpdateHandler)this);
			} else {
				message("No update required.");
			}
		} catch (IOException ioe) {
			error("I/O error while checking for update");
			throw ioe;
		}
		
		message("Launching application ...");
		Launcher launcher = new JPacktoolLauncher();
		config.launch(launcher);
	}

	
	public void startCheckUpdates() throws Throwable {
		message("Starting to check for updates ...");
	}
	
	/**
	 * Called on each file before any check is performed.
	 * 
	 * <p>
	 * If {@code false} is returned, this file will completely skip the update
	 * process and the system will immediately proceed with the next file.
	 * {@link #doneCheckUpdateFile(FileMetadata, boolean)} will <em>not</em> be
	 * called.
	 */
	public boolean startCheckUpdateFile(FileMetadata file) throws Throwable {
		message("Checking "+file.getPath().getFileName(),"Checking "+file.getPath().toString());
		return true;
	}
	
	public void doneCheckUpdates() throws Throwable {
		message("... done checking for updates");
	}
	
	public void startDownloads() throws Throwable {
		message("Start downloading ...");
	}
	
	public void startDownloadFile(FileMetadata file) throws Throwable {
		message("Downloading "+file.getPath().getFileName(),"Downloading "+file.getPath().toString());
	}
	
	public void doneDownloads() throws Throwable {
		message("Download finished.");
	}

	public void failed(Throwable t) {
		//t.printStackTrace(System.err);
		error("Update failed!", "Update failed: "+t.getMessage());
	}

	public void succeeded() {
		message("Update succeeded!");
	}

}
