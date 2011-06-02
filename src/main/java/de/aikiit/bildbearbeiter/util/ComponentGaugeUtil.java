/*
 * Created on 20.10.2003
 */
package de.aikiit.bildbearbeiter.util;

import org.apache.log4j.Logger;

import javax.swing.ImageIcon;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;

/**
 * Helper class to centre Swing-components.
 *
 * @author hirsch, 20.10.2003
 * @version 2003-10-22
 */

public final class ComponentGaugeUtil {
    /**
     * Logger for this class.
     */
    private static final Logger LOG =
            Logger.getLogger(ComponentGaugeUtil.class);

    /**
     * Private default constructor to prevent instantiation of this class.
     */
    private ComponentGaugeUtil() {
        // prevent instantiation
    }

    /**
     * Gauge the given component on screen.
     *
     * @param component Swing-component to gauge.
     */
    public static void makeCentered(final Component component) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dim = toolkit.getScreenSize();
        int screenWidth = dim.width;
        int screenHeight = dim.height;
        component.setLocation((screenWidth - component.getSize().width) / 2,
                (screenHeight - component.getSize().height) / 2);
    } // end of makeCentered

    /**
     * Helper that transforms a given path into an ImageIcon. In case of errors
     * <code>null</code> is returned.
     *
     * @param path File path that is to be converted into an icon.
     * @return ImageIcon, located at the given path.
     */
    public static ImageIcon createImageIcon(final String path) {
        assert path != null : "Path for image icon needs to be set.";
        LOG.debug("Creating image icon from path " + path);
        java.net.URL imgURL = ComponentGaugeUtil.class.getResource(path);
        LOG.debug("extracted URL is: " + imgURL);

        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            LOG.error("Could not generate a valid URL from the given path: "
                    + path);
            return null;
        } // end if
    } // end of createImageIcon
} // end of class
