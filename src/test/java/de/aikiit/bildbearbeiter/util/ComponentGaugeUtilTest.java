package de.aikiit.bildbearbeiter.util;

import org.junit.Test;

import javax.swing.JButton;
import java.awt.Point;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

/**
 * Tests util methods.
 *
 * @author hirsch
 * @version 2011-03-21, 13:18
 */
public class ComponentGaugeUtilTest {
    /**
     * Checks image creation with a valid and an invalid path.
     */
    @Test
    public final void createIconFromStringPath() throws Exception {
        // FIXME not really clear why this location (valid with File-separators) cannot be parsed into an URL
        // assertNotNull(ComponentGaugeUtil.createImageIcon(MetaDataExtractorTest.FULLPATH_TEST_IMG));
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
     * Checks that a component gets a non-default location after calling util
     * method.
     */
    @Test
    public void gaugeSwingComponent() throws Exception {
        JButton button = new JButton("Test");
        Point buttonSize = button.getLocation();
        assertEquals(new Point(0, 0), buttonSize);
        ComponentGaugeUtil.makeCentered(button);
        assertNotSame(new Point(0, 0), button.getLocation());
    }
}