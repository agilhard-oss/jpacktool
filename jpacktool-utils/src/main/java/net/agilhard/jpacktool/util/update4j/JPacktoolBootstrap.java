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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

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

	public static final String ARGUMENT_PROPERTY_KEY = "business.argument";

	private SplashScreenHelper splash;
	private List<String> businessArgs;
	private String businessBaseUri;
	private String projectConfigName;
	private boolean updateFailed;
	private boolean launchAnyway;

	public JPacktoolBootstrap() {
		splash = SplashScreenHelper.getInstance();
	}

	public static void main(String[] args) throws Throwable {
		JPacktoolBootstrap app = new JPacktoolBootstrap();
		app.main(Arrays.asList(args));
	}

	private String getOpt(String arg) {
		int i = arg.indexOf(":");
		if ((i < 0) || (i + 1 >= arg.length())) {
			return null;
		}
		return arg.substring(i + 1);
	}

	/**
	 * Parse Arguments and add unknown args to businessArgs list;
	 */
	public void parseArguments(List<String> args) {
		businessArgs = new ArrayList<String>();
		for (String arg : args) {

			if (arg.startsWith("-uri:")) {
				businessBaseUri = getOpt(arg);
			} else if (arg.startsWith("-config:")) {
				projectConfigName = getOpt(arg);
			} else if ("-launchAnyway".equals(arg)) {
				launchAnyway = true;
			} else {
				businessArgs.add(arg);
			}
		}
	}

	@Override
	public void main(List<String> args) throws Throwable {

		parseArguments(args);

		File propsFile = new File(new File("conf"), "jpacktool.properties");

		Properties props = null;

		if (propsFile.isFile() ) {

			props = new Properties();

			try (FileInputStream inStream = new FileInputStream(propsFile)) {
				props.load(inStream);
			} catch (IOException ioe) {
				error("I/O error reading properties file!");
			}
		}

		propsFile = new File(new File("conf"), "bootstrap.properties");

		if (propsFile.isFile()) {

			if (props == null) {
				props = new Properties();
			}

			try (FileInputStream inStream = new FileInputStream(propsFile)) {
				props.load(inStream);
			} catch (IOException ioe) {
				error("I/O error reading properties file!");
			}
		}

		if (props != null) {

			if (projectConfigName == null) {
				projectConfigName = props.getProperty("projectConfigName");
			}

			if (businessBaseUri == null) {
				businessBaseUri = props.getProperty("businessBaseUri");
			}
		}

		if ( projectConfigName == null ) {
			error("projectConfigName not set");
			return;
		}
		
		if (businessBaseUri == null) {
			error("businessBaseUri");
			return;
		}

		
        final String argument = props.getProperty(ARGUMENT_PROPERTY_KEY);
        if (argument != null) {
            this.businessArgs.add(argument);
        }

        // use TreeMap to sort
        Map<String,String> argMap=new TreeMap<>();
        props.entrySet().stream().forEach(e -> {
            final String pfx = ARGUMENT_PROPERTY_KEY + ".";
            // starts with but not equals, to filter missing <name> part
            if (((String)e.getKey()).startsWith(pfx) && !e.getKey().equals(pfx)) {
				String key = ((String)e.getKey()).substring(pfx.length());
				argMap.put(key, ((String)e.getValue()));
            }
        });
        argMap.entrySet().stream().forEach(e -> {
				businessArgs.add(e.getValue());
        });
        
		System.out.println("uri=" + businessBaseUri);
		System.out.println("config=" + projectConfigName);

		start();

	}

	public void message(String text) {
		if (text != null) {
			System.out.println(text);
			splash.setMessage(text);
		}
	}

	public void message(String textShort, String text) {
		if (text != null) {
			System.out.println(text);
		}
		if (textShort != null) {
			splash.setMessage(textShort);
		}
	}

	public void error(String text) {
		if (text != null) {
			System.err.println(text);
			splash.error(text);
		}
	}

	public void error(String textShort, String text) {
		if (text != null) {
			System.err.println(text);
		}
		if (textShort != null) {
			splash.error(textShort);
		}
	}

	void start() throws IOException {

		URL configUrl;
		try {
			configUrl = new URL(businessBaseUri + "/" + projectConfigName);
		} catch (MalformedURLException e1) {
			return;
		}

		Configuration config = null;
		try (Reader in = new InputStreamReader(configUrl.openStream(), StandardCharsets.UTF_8)) {
			config = Configuration.read(in);
		} catch (IOException e) {
			message("Could not load remote config, falling back to local.",
					"Could not load remote config " + configUrl.toString() + ", falling back to local");
			File confDir = new File("conf");
			if (!confDir.isDirectory()) {
				error("Directory \"conf\" does not exist or is not a directory.");
				return;
			}
			Path path = confDir.toPath().resolve(projectConfigName);
			try (Reader in = Files.newBufferedReader(path)) {
				config = Configuration.read(in);
			} catch (IOException ioe) {
				error("Could not read local config.", "Could not read local config " + path.toString());
				throw ioe;
			}
		}

		message("Checking if update is required");

		try {
			if (config.requiresUpdate()) {
				message("Updating application ...");
				config.update((UpdateHandler) this);
			} else {
				message("No update required.");
			}
		} catch (IOException ioe) {
			error("I/O error while checking for update");
			throw ioe;
		}

		if ((!updateFailed) || launchAnyway) {
			message("Launching application ...");

			JPacktoolLauncher launcher = new JPacktoolLauncher(businessArgs);

			config.launch(launcher);
		}
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
	public void startCheckUpdateFile(FileMetadata file) throws Throwable {
		message("Checking " + file.getPath().getFileName(), "Checking " + file.getPath().toString());
	}

	public void doneCheckUpdates() throws Throwable {
		message("... done checking for updates");
	}

	public void startDownloads() throws Throwable {
		message("Start downloading ...");
	}

	public void startDownloadFile(FileMetadata file) throws Throwable {
		message("Downloading " + file.getPath().getFileName(), "Downloading " + file.getPath().toString());
	}

	public void doneDownloads() throws Throwable {
		message("Download finished.");
	}

	public void failed(Throwable t) {
		updateFailed = true;
		if (!launchAnyway) {
			t.printStackTrace(System.err);
		}
		if (launchAnyway) {
			message("Update failed!", "Update failed: " + t.getMessage());
		} else {
			error("Update failed!", "Update failed: " + t.getMessage());
		}
	}

	public void succeeded() {
		message("Update succeeded!");
	}

}
