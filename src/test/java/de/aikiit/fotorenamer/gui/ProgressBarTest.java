/*
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
package de.aikiit.fotorenamer.gui;

import de.aikiit.fotorenamer.gui.ProgressBar;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author hirsch
 * @version 2011-04-03, 20:24
 */
@Ignore("Not working in travis.")
class ProgressBarTest {
    private static final String TEXT = "Text";

    /**
     * Check progress bar initialization and value setting.
     */
    @Test
    void checkInitAndDefaults() {
        ProgressBar bar = new ProgressBar(0);
        assertNotNull(bar);
        assertEquals("", bar.getText());
        assertEquals(0, bar.getProgress());
    }

    /**
     * Manually set values and retrieve these values.
     */
    @Test
    void checkSetterGetter() {
        ProgressBar bar = new ProgressBar(10);
        assertNotNull(bar);
        bar.setProgress();
        bar.setProgress();
        bar.setProgress();
        bar.setProgress();
        bar.setProgress();
        assertEquals(4, bar.getProgress());
        bar.setText(TEXT);
        assertEquals(TEXT, bar.getText());
        // verify that no exception is thrown here
        bar.updateUI();
    }
}
