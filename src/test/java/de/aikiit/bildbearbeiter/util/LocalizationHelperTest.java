package de.aikiit.bildbearbeiter.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Testing resource bundling.
 *
 * @author hirsch
 * @version 23.08.11
 */
public class LocalizationHelperTest {

    @Test
    public void checkValueRetrievingFromBundle() {
         assertEquals("Fortschritt", LocalizationHelper.getBundleString("fotorenamer.ui.progress"));
    }
}
