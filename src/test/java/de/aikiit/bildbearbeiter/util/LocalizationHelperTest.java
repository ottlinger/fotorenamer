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

    /**
     * Retrieve a plain i18n-value.
     */
    @Test
    public void checkValueRetrievingFromBundle() {
         assertEquals("Fortschritt", LocalizationHelper.getBundleString("fotorenamer.ui.progress"));
    }

    /**
     * Retrieve a i18n-value with parameters set.
     */
    @Test
    public void  checkParametrizedValueExtraction() {
        assertEquals("Erfolg und dann folgt noch die 7", LocalizationHelper.getParameterizedBundleString("fotorenamer.test.param",
                new Object[] { "Erfolg", Integer.valueOf(7)}));
        assertEquals("{0} und dann folgt noch die {1}", LocalizationHelper.getParameterizedBundleString("fotorenamer.test.param",
                new Object[] { }));

    }
}
