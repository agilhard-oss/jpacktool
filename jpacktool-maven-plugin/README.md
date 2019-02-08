<!---
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->

# jpacktool-maven-plugin

Maven Plugin for Projects using jlink or jpackage Java Tools 
and [update4j](https://github.com/update4j/update4j).

Based on Agilhard-OSS [jlink-jpackager-maven-plugin](https://github.com/agilhard-oss/jlink-jpackager-maven-plugin).
Based on Apache [maven-jlink-plugin](https://github.com/apache/maven-jlink-plugin)

[Apache License](http://www.apache.org/licenses/LICENSE-2.0)

[Maven Plugin Documentation](https://agilhard-oss.github.io/jlink-jpackager-maven-plugin/site/index.html)


## Introduction

  The JPacktool Maven plugin is intended to create [Modular Run-Time Images](http://openjdk.java.net/jeps/220) with **JDK 9**
  and above or native installable packages via [jpackage](http://openjdk.java.net/jeps/343) with **JDK 12** and above.
    
  The JPacktool Maven Plugin is based on the Agilhard-OSS 
  [jlink-jpackager-maven-plugin](https://github.com/agilhard-oss/jlink-jpackager-maven-plugin).

  This in turn is based on the Apache [maven-jlink-plugin](https://github.com/apache/maven-jlink-plugin).

  The JPacktool Maven Plugin enhances the [jlink-jpackager-maven-plugin](https://github.com/agilhard-oss/jlink-jpackager-maven-plugin)
  by adding special features for projects using [update4j](https://github.com/update4j/update4j).

  The JPacktool Maven plugin is intended to be part of a solution to replace the use of the 
  [Java Webstart](https://en.wikipedia.org/wiki/Java_Web_Start) technology
  which has been discontinued in newer JDK versions.

## Usage of the JPacktool Maven Plugin

Usually you will use the Maven JPacktool Maven Plugin to create
a Run Time Image or an installable Package from one or more modules within 
a multi module build.

You will than use one submodule there with a pom.xml which uses one of the 
special Maven packaging types the plugin provides and a configuration for the plugin.

In other words it is not possible to create a Run Time Image or Installation Package
from a single Maven Project within the same single Maven Project and you usually would not call
the goals of the plugin by using a plugin execution configuration.

General usage and 
[Usage for projects that are using update4j](https://agilhard-oss.github.io/jlink-jpackager-maven-plugin/site/usage.html#Usage_for_projects_that_are_using_update4j)
 is described in detail on the 
[Maven Plugin Documentations usage page](https://agilhard-oss.github.io/jlink-jpackager-maven-plugin/site/index.html)


## Prerequisites

- [JDK](http://jdk.java.net/)
- [Maven](https://maven.apache.org/)


You need to use the special JDK-12 or above Early Access build that includes
JPackager support to use the plugin with the jpackage(r) Java tool
as long as the jpackage(r) tool is not officially part of the JDK.

This JPackager JDK-?? Early Access build can be downloaded from 
[https://jdk.java.net/jpackage/](https://jdk.java.net/jpackage/)

Alternatively you can also use the JDK-11 backported JPackager tool wich is mentioned in
[Filling the Packager gap - OpenJDK mailing list - Java.net](http://mail.openjdk.java.net/pipermail/openjfx-dev/2018-September/022500.html)

The [jpacktool-maven-plugin](https://github.com/agilhard-oss/jpacktool-maven-plugin) is not (yet?)
available on maven central you must download,
compile and install that to your maven Repository before you can use it.

Native packages will be generated using tools on the target platform. 

For Linux and Mac make sure you have the packaging tools for the used packaging type installed.

For Windows, there are two additional tools that developers will need to install if they want to generate native packages:

- exe — Inno Setup, a third-party tool, is required to generate exe installers
- msi — Wix, a third-party tool, is required to generate msi installers

[Inno Setup](http://www.jrsoftware.org/isinfo.php)
[Inno Setup Download](http://www.jrsoftware.org/isdl.php)

[Wix Toolset](http://wixtoolset.org)
[Wix Toolset Downloads](http://wixtoolset.org/releases/)



## Examples

... to be done ...

