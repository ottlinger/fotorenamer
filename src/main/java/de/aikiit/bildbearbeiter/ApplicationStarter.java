package de.aikiit.bildbearbeiter;

import de.aikiit.bildbearbeiter.gui.MainUIWindow;
import org.apache.log4j.Logger;

import javax.swing.UIManager;

/**
 * Main class to start the application in standalone mode.
 *
 * @author hirsch
 * @version 2011-03-21, 13:06
 */
public final class ApplicationStarter {
    /**
     * Logger for this class.
     */
    private static final Logger LOG =
            Logger.getLogger(ApplicationStarter.class);

    /**
     * Prevent instantiation of this starter class.
     */
    private ApplicationStarter() {
        // prevent instantiation
    }

    /**
     * Creates an application window and runs the application.
     *
     * @param args Runtime/CLI arguments.
     */
    public static void main(final String[] args) {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            LOG.error("Error during application startup: " + e);
        } // end of try
        new MainUIWindow();
    } // end of main
}
