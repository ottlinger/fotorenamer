/*
 * Created on 20.10.2003
 */
package de.aikiit.bildbearbeiter.util;

import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;

/**
 * Helper class to centre Swing-components.
 *
 * @author hirsch, 20.10.2003
 * @version 2003-10-22
 */

public class ComponentGaugeUtil {
    final static private Logger LOG = Logger.getLogger(ComponentGaugeUtil.class);

    /**
     * Gauge the given component on screen.
     * @param w Swing-component to gauge.
     */
    public static void makeCentered(Component w) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dim = toolkit.getScreenSize();
        int screenWidth = dim.width;
        int screenHeight = dim.height;
        w.setLocation((screenWidth - w.getSize().width) / 2, (screenHeight - w.getSize().height) / 2);
    } // end of makeCentered

    /**
     * Helper that transforms a given path into an ImageIcon.
     * In case of errors <code>null</code> is returned.

     * @param path File path that is to be converted into an icon.
     * @return ImageIcon, located at the given path.
     */
    public static ImageIcon createImageIcon(String path) {
        assert path != null : "Path for image icon needs to be set.";
        LOG.debug("Creating image icon from path "+ path);
        java.net.URL imgURL = ComponentGaugeUtil.class.getResource(path);

        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            LOG.error("Could not generate a valid URL from the given path: "+ path);
            return null;
        } // end if
    } // end of createImageIcon
} // end of class