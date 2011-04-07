package de.aikiit.bildbearbeiter.gui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author hirsch
 * @version 2011-04-03, 20:24
 */
public class ProgressBarTest {
    private static final String TEXT = "Text";

    /**
     * Check progress bar initialization and value setting.
     */
    @Test
    public void checkInitAndDefaults() {
        ProgressBar bar = new ProgressBar(0);
        assertNotNull(bar);
        assertEquals("", bar.getText());
        assertEquals(0, bar.getProgress());
    }

    /**
     * Manually set values and retrieve these values.
     */
    @Test
    public void checkSetterGetter() {
        ProgressBar bar = new ProgressBar(10);
        assertNotNull(bar);
        bar.setProgress(5);
        assertEquals(5, bar.getProgress());
        bar.setText(TEXT);
        assertEquals(TEXT, bar.getText());
        // verify that no exception is thrown here
        bar.updateUI();
    }
}