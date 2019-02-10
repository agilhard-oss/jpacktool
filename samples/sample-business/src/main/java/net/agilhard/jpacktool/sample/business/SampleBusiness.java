package net.agilhard.jpacktool.sample.business;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import net.agilhard.jpacktool.util.splash.SplashScreenHelper;

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

/**
 * @author bei
 *
 */
public class SampleBusiness {

    private final JFrame frame;

    SampleBusiness(final String[] args) {
        // Invoked on the event dispatching thread.
        // Do any initialization here.
        this.frame = new JFrame("SampleBusiness");
        this.frame.add(new JLabel("Hello Sample Business Application!", SwingConstants.CENTER));
        this.frame.setSize(new Dimension(640, 480));
        this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public void show() {
        this.frame.setVisible(true);
    }

    public static void main(final String args[]) {
        final SplashScreenHelper helper = SplashScreenHelper.getInstance();

        // counter for demo init phase with splashscreen
        for (int i = 0; i <= 20; i++) {
            helper.setMessage("Initializing: " + i);
            try {
                Thread.sleep(500);
            } catch (final InterruptedException ignored) {//ignored

            }
        }

        // close the splashscreen
        helper.close();

        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                new SampleBusiness(args).show();
            }
        });
    }

}
