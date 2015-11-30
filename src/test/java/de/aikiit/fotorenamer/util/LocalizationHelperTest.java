/**
Copyright 2011, Aiki IT, FotoRenamer

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package de.aikiit.fotorenamer.util;

import de.aikiit.fotorenamer.util.LocalizationHelper;
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
    public final void checkValueRetrievingFromBundle() {
         assertEquals("Fortschritt", LocalizationHelper.getBundleString("fotorenamer.ui.progress"));
    }

    /**
     * Retrieve a i18n-value with parameters set.
     */
    @Test
    public final void checkParametrizedValueExtraction() {
        assertEquals("Erfolg und dann folgt noch die 7", LocalizationHelper.getParameterizedBundleString("fotorenamer.test.param",
                new Object[] {"Erfolg", 7}));
        assertEquals("{0} und dann folgt noch die {1}", LocalizationHelper.getParameterizedBundleString("fotorenamer.test.param",
                new Object[] {}));

    }
}
