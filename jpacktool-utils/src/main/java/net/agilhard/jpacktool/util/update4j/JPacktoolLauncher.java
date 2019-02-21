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

import static org.update4j.service.DefaultLauncher.ARGUMENT_PROPERTY_KEY_PREFIX;
import static org.update4j.service.DefaultLauncher.MAIN_CLASS_PROPERTY_KEY;
import static org.update4j.service.DefaultLauncher.SYSTEM_PROPERTY_KEY_PREFIX;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.update4j.Configuration;
import org.update4j.LaunchContext;
import org.update4j.inject.InjectTarget;
import org.update4j.service.Launcher;

import net.agilhard.jpacktool.util.splash.SplashScreenHelper;

public class JPacktoolLauncher implements Launcher {


    @InjectTarget(required = false)
    private List<String> args;

    private SplashScreenHelper splashScreenHelper;

    @Override
    public long version() {
        return Long.MIN_VALUE;
    }

    public JPacktoolLauncher() {
        this.splashScreenHelper = SplashScreenHelper.getInstance();
    }

    public JPacktoolLauncher(final List<String> args) {
        this.args = args;
        this.splashScreenHelper = SplashScreenHelper.getInstance();
    }

    public void illegalState(final String message) {
        this.splashScreenHelper.error(message);
        throw new IllegalStateException(message);
    }

    @Override
    public void run(final LaunchContext context) {
        final Configuration config = context.getConfiguration();

        final String mainClass = config.getResolvedProperty(MAIN_CLASS_PROPERTY_KEY);
        if (mainClass == null) {
            usage();

            this.illegalState("No main class property found at key '" + MAIN_CLASS_PROPERTY_KEY + "'.");
        }

        if (!StringUtils.isClassName(mainClass)) {
            this.illegalState(
                "Main class at key '" + MAIN_CLASS_PROPERTY_KEY + "' is not a valid Java class name.");
        }

        if (this.args == null) {
            this.args = new ArrayList<>();
        }

        final String argument = context.getConfiguration().getResolvedProperty(ARGUMENT_PROPERTY_KEY_PREFIX);
        if (argument != null) {
            this.args.add(argument);
        }

        // use TreeMap to sort
        Map<String,String> argMap=new TreeMap<>();
        context.getConfiguration().getResolvedProperties().entrySet().stream().forEach(e -> {
            final String pfx = ARGUMENT_PROPERTY_KEY_PREFIX + ".";
            // starts with but not equals, to filter missing <name> part
            if (e.getKey().startsWith(pfx) && !e.getKey().equals(pfx)) {
				String key = e.getKey().substring(pfx.length());
            	argMap.put(key, e.getValue());
            }
        });
        argMap.entrySet().stream().forEach(e -> {
				this.args.add(e.getValue());
        });
        
        final String[] argsArray = this.args.toArray(new String[this.args.size()]);

        context.getConfiguration().getResolvedProperties().entrySet().stream().forEach(e -> {
            final String pfx = SYSTEM_PROPERTY_KEY_PREFIX + ".";
            // starts with but not equals, to filter missing <name> part
            if (e.getKey().startsWith(pfx) && !e.getKey().equals(pfx)) {
                final String key = e.getKey().substring(pfx.length());
                System.setProperty(key, e.getValue());
            }
        });

        // we are fully aware, so no need to warn
        // if NoClassDefFoundError arises for any other reason
        System.setProperty("suppress.warning.access", "true");


        try {
            final Class<?> clazz = Class.forName(mainClass, true, context.getClassLoader());

            // first check for JavaFx start method
            Class<?> javafx = null;
            try {
                javafx = Class.forName("javafx.application.Application", true, context.getClassLoader());
            } catch (final ClassNotFoundException e) {
                // no JavaFx present, skip.
            }

            // clear SplashScreen now
            this.splashScreenHelper.clear();

            if (javafx != null && javafx.isAssignableFrom(clazz)) {
                final Method launch = javafx.getMethod("launch", Class.class, String[].class);
                launch.invoke(null, clazz, argsArray);
            } else {
                final Method main = clazz.getMethod("main", String[].class);
                main.invoke(null, new Object[] { argsArray });
            }

        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException
            | NoSuchMethodException e) {

            this.splashScreenHelper.error("Error in application startup");

            throw new RuntimeException(e);
        }
    }

    // @formatter:off
    private static void usage() {
        System.err.println("Customize the setup of the launcher by setting properties in the config\n"
                        + "\taccording to the following table:\n\n" + table() );
    }

    private static String table() {
        return "\t\t+--------------------------------+------------------------------------+\n"
             + "\t\t| default.launcher.main.class    | The main class of the business app.|\n"
             + "\t\t|                                | Required.                          |\n"
             + "\t\t+--------------------------------+------------------------------------+\n"
             + "\t\t| default.launcher.argument      | A single string to be augmented    |\n"
             + "\t\t|                                | to the args list.                  |\n"
             + "\t\t+--------------------------------+------------------------------------+\n"
             + "\t\t| default.launcher.argument.<num>| Additional arguments for           |\n"
             + "\t\t|                                | the args list ordered by <num>     |\n"
             + "\t\t+--------------------------------+------------------------------------+\n"
             + "\t\t| default.launcher.system.<name> | Pass a system property with the    |\n"
             + "\t\t|                                | provided value using the <name> as |\n"
             + "\t\t|                                | the system property key.           |\n"
             + "\t\t|                                | May be used for many properties.   |\n"
             + "\t\t+--------------------------------+------------------------------------+\n\n\n";
    }

}
