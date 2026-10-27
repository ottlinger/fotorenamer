/*
 * Copyright 2026, Aiki IT, FotoRenamer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.aikiit.fotorenamer.gui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.awt.SplashScreen;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * Uses the JVM splash when available, otherwise shows the bundled splash photograph
 * in a Swing window. Show and close on the Swing event dispatch thread.
 */
public final class StartupSplash implements AutoCloseable {

    private static final Logger LOG = LogManager.getLogger(StartupSplash.class);
    static final String IMAGE_LOCATION = "/image/splash-camera.jpg";

    private final SplashScreen nativeSplash;
    private final JWindow window;
    private boolean closed;

    StartupSplash(final SplashScreen nativeSplash, final JWindow window) {
        this.nativeSplash = nativeSplash;
        this.window = window;
    }

    /**
     * Shows the splash without delaying application startup artificially.
     * In headless mode, no native or Swing splash is accessed.
     *
     * @return A splash handle to close when the main window is visible or startup fails.
     */
    public static StartupSplash show() {
        if (GraphicsEnvironment.isHeadless()) {
            return new StartupSplash(null, null);
        }
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Show the splash on the Swing event dispatch thread");
        }

        JWindow fallback = null;
        try {
            SplashScreen nativeSplash = SplashScreen.getSplashScreen();
            if (nativeSplash != null) {
                return new StartupSplash(nativeSplash, null);
            }
            URL resource = StartupSplash.class.getResource(IMAGE_LOCATION);
            if (resource == null) {
                throw new IOException("Splash image not found: " + IMAGE_LOCATION);
            }
            BufferedImage image = ImageIO.read(resource);
            if (image == null) {
                throw new IOException("Unable to decode splash image: " + IMAGE_LOCATION);
            }
            fallback = new JWindow();
            fallback.setFocusableWindowState(false);
            fallback.getContentPane().add(new JLabel(new ImageIcon(image)));
            fallback.pack();
            fallback.setLocationRelativeTo(null);
            fallback.setVisible(true);
            // Paint before main-window initialization occupies the event thread.
            fallback.getRootPane().paintImmediately(0, 0, fallback.getWidth(), fallback.getHeight());
            return new StartupSplash(null, fallback);
        } catch (IOException | RuntimeException e) {
            LOG.warn("Unable to show startup splash using '{}'", IMAGE_LOCATION, e);
            StartupSplash failedSplash = new StartupSplash(null, fallback);
            failedSplash.close();
            return failedSplash;
        }
    }

    /** Closes the splash, including on startup failure. Safe to call repeatedly. */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        try {
            // The JVM normally closes its splash when the main window appears.
            if (nativeSplash != null && nativeSplash.isVisible()) {
                nativeSplash.close();
            }
            if (window != null) {
                window.dispose();
            }
        } catch (RuntimeException e) {
            LOG.warn("Unable to close startup splash", e);
        }
    }
}
