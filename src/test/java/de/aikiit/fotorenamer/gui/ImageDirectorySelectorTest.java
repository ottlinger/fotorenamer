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
package de.aikiit.fotorenamer.gui;

import de.aikiit.fotorenamer.gui.ImageDirectorySelector;

/**
 * Test directory selector.
 *
 * @author hirsch
 * @version 2011-04-08, 00:22
 */
public class ImageDirectorySelectorTest {
    @org.junit.Test
    public void performInit() {
        new ImageDirectorySelector(true, null);
    }

}
