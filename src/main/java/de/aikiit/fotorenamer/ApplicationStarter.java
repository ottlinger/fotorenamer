/*
Copyright 2011, Aiki IT, FotoRenamer
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package de.aikiit.fotorenamer;

import de.aikiit.fotorenamer.gui.MainUIWindow;
import de.aikiit.fotorenamer.gui.StartupSplash;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main class to start the application in standalone mode.
 *
 * @author hirsch
 * @version 2011-03-21, 13:06
 */
public final class ApplicationStarter {

    private ApplicationStarter() {
        // Static application entry point.
    }

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LogManager.getLogger(ApplicationStarter.class);

    /**
     * Creates an application window and runs the application.
     *
     * @param args Runtime/CLI arguments.
     */
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(ApplicationStarter::start);
    }

    /** Initializes Swing and keeps the splash open until the main window is visible. */
    static void start() {
        try (StartupSplash splash = StartupSplash.show()) {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e) {
                LOG.error("Error setting application look and feel", e);
            }
            // Already on the event dispatch thread: build completes synchronously.
            MainUIWindow.build();
        } catch (RuntimeException | LinkageError e) {
            LOG.error("Error during application startup", e);
        }
    }
}
