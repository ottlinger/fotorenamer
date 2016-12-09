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

import static org.junit.Assert.assertNotNull;

/**
 * Call help window.
 *
 * @author hirsch
 * @version 2011-04-08, 00:24
 */
@org.junit.Ignore("Not working in travis.")
public class HelpWindowTest {
    @org.junit.Test
    public final void initWindow() {
        assertNotNull(new HelpWindow());
    }
}
