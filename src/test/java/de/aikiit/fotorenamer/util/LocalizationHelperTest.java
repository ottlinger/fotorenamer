/**
 * Copyright 2011, Aiki IT, FotoRenamer
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.aikiit.fotorenamer.util;

import org.junit.Test;

import java.util.Locale;

import static de.aikiit.fotorenamer.util.LocalizationHelper.*;

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
    public final void checkValueRetrievingFromBundle() {
        assertEquals("Fortschritt", getBundleString("fotorenamer.ui.progress"));
    }

    /**
     * Retrieve a i18n-value with parameters set.
     */
    @Test
    public final void checkParametrizedValueExtraction() {
        assertEquals("Erfolg und dann folgt noch die 7", getParameterizedBundleString("fotorenamer.test.param",
                "Erfolg", 7));
        // ignore warning, we want to test what happens here! An empty String or null changes the output.
        assertEquals("{0} und dann folgt noch die {1}", getParameterizedBundleString("fotorenamer.test.param",
                new Object[]{}));
    }

    @Test
    public final void fallbackLocale() {
        // WHEN: reset system properties
        System.setProperty("user.language", "");
        System.setProperty("user.country", "");
        setLocale();

        assertEquals(Locale.GERMANY, getLocale());
        assertEquals("de", getLanguage());
    }

    @Test
    public final void setLocaleViaSystemProperties() {
        // WHEN: reset system properties
        final String french = "fr";
        System.setProperty("user.language", french);
        System.setProperty("user.country", "CA");
        setLocale();

        assertEquals(Locale.CANADA_FRENCH, getLocale());
        assertEquals(french, getLanguage());
    }
}
