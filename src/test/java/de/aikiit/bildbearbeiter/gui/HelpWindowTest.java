package de.aikiit.bildbearbeiter.gui;

import static org.junit.Assert.assertNotNull;

/**
 * Call help window.
 *
 * @author hirsch
 * @version 2011-04-08, 00:24
 */
public class HelpWindowTest {
    @org.junit.Test
    public void initWindow() {
        assertNotNull(new de.aikiit.bildbearbeiter.gui.HelpWindow());
    }
}