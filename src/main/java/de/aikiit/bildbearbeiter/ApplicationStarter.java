package de.aikiit.bildbearbeiter;

import de.aikiit.bildbearbeiter.gui.HauptGUI;
import org.apache.log4j.Logger;

import javax.swing.*;

/**
 * Main class to start the application in standalone mode.
 *
 * @version 2011-03-21, 13:06
 * @author hirsch
 */
public class ApplicationStarter {
    final static private Logger LOG = Logger.getLogger(ApplicationStarter.class);

    /**
     * Creates an application window and runs the application.
     *
     * @param args Runtime/CLI arguments.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            LOG.error("Error during application startup: " + e);
        } // end of try
        new HauptGUI();
    } // end of main
}