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

import java.io.File;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * Options for the splash screen
 * @author Bernd Eilers
 *
 */
public class SplashScreenOptions {

	/**
	 * The File for the image
	 */
	@Parameter(required = true, readonly = false)
	protected File file;
	
	/**
	 * name in the installation
	 */
	@Parameter(required = true, readonly = false)
	protected String name;
	
	/**
	 * X Position where message output starts.
	 */
	@Parameter(required = false, readonly = false)
	protected int x;

	/**
	 * X Position where message output starts.
	 */
	@Parameter(required = false, readonly = false)
	protected int y;
	
	/**
	 * FontName
	 */
	@Parameter(required = false, readonly = false)
	protected String fontName;
	
	
	@Parameter(required = false, readonly=false)
	protected int fontSize;
	
	public SplashScreenOptions() {
	}

	public File getFile() {
		return file;
	}

	public String getName() {
		return name;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public String getFontName() {
		return fontName;
	}

	public int getFontSize() {
		return fontSize;
	}

	
	
}
