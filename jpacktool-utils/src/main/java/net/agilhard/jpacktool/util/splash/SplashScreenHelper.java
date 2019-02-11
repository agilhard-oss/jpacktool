package net.agilhard.jpacktool.util.splash;

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


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.SplashScreen;

/**
 * Helper Class for drawing on the SplashScreen.
 *
 * @author Bernd Eilers
 */
public class SplashScreenHelper {

    private final SplashScreen splashScreen;

    private Graphics2D graphics;

    private static SplashScreenHelper instance;

    private int x;

    private int y;

    private Color transparentColor;

    private Color drawColor;

    private SplashScreenHelper() {

        this.splashScreen = SplashScreen.getSplashScreen();
        this.graphics = null;
        if (this.splashScreen != null) {
            this.graphics = this.splashScreen.createGraphics();

            this.transparentColor = new Color(0, true);
            this.drawColor = new Color(178, 6, 41);
            this.graphics.setBackground(this.transparentColor);
            this.graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            this.graphics.setFont(new Font("TimesRoman", Font.PLAIN, 16));
            this.graphics.setColor(this.drawColor);
        }
        this.x = 60;
        this.y = 200;
    }

    /**
     * @return the instance
     */
    public static SplashScreenHelper getInstance() {

        if (instance == null) {
            instance = new SplashScreenHelper();
        }

        return instance;
    }

    /**
     * @return the splashScreen
     */
    public SplashScreen getSplashScreen() {
        return this.splashScreen;
    }

    /**
     * @return the graphics
     */
    public Graphics2D getGraphics() {
        return this.graphics;
    }

    /**
     * @return the x
     */
    public int getX() {
        return this.x;
    }

    /**
     * @param x
     *            the x to set
     */
    public void setX(final int x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public int getY() {
        return this.y;
    }

    /**
     * @param y
     *            the y to set
     */
    public void setY(final int y) {
        this.y = y;
    }

    public void clear() {
        if (this.graphics != null) {
            this.graphics.clearRect(0, 0, (int) this.splashScreen.getSize().getWidth(),
                (int) this.splashScreen.getSize().getHeight());
            this.splashScreen.update();
        }
    }

    public void close() {
        if (this.splashScreen != null) {
            this.splashScreen.close();
        }
    }

    public void setMessage(final String text) {
        if (this.graphics != null) {
            this.graphics.clearRect(0, 0, (int) this.splashScreen.getSize().getWidth(),
                (int) this.splashScreen.getSize().getHeight());
            this.graphics.drawString(text, this.x, this.y);
            this.splashScreen.update();
        }
    }

    /**
     * show error and wait a while
     * @param msg
     */
    public void error(final String msg) {
    	
    	setMessage(msg);
    	
        // just wait a while that the error message can be seen
        try {
            Thread.sleep(50000);
        } catch (final InterruptedException ignored) {//ignored

        }
    }
}
