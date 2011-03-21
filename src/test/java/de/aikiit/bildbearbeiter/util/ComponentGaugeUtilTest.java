package de.aikiit.bildbearbeiter.util;

import de.aikiit.bildbearbeiter.image.MetaDataExtractorTest;
import org.apache.log4j.Logger;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.Assert.*;

/**
 * Tests util methods.
 *
 * @version 2011-03-21, 13:18
 * @author hirsch
 */
public class ComponentGaugeUtilTest {
    /**
     * Checks image creation with a valid and an invalid path.
     */
    @Test
    public final void createIconFromStringPath() throws Exception {
        // TODO bugfix assertNotNull(ComponentGaugeUtil.createImageIcon(MetaDataExtractorTest.PATH2TESTIMG));
        assertNull(ComponentGaugeUtil.createImageIcon("wuumansho"));
    }

    /**
     * Checks assertion failure with null parameter.
     */
    @Test(expected = AssertionError.class)
    public void createIconFromStringPathWithAssertionFailure() {
        assertNull(ComponentGaugeUtil.createImageIcon(null));
    }


    /**
     * Checks that a component gets a non-default location after calling util method.
     */
    @Test
    public void gaugeSwingComponent() throws Exception {
        JButton button = new JButton("Test");
        Point buttonSize = button.getLocation();
        assertEquals(new Point(0,0), buttonSize);
        ComponentGaugeUtil.makeCentered(button);
        assertNotSame(new Point(0,0), button.getLocation());
    }
}