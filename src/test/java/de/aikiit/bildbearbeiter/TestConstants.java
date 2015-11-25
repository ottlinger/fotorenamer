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
package de.aikiit.bildbearbeiter;

import org.junit.Ignore;

import java.io.File;

/**
 * This helper class contains relevant constants for testing.
 *
 * @author hirsch
 * @version 2011-06-02, 13:42
 */
@Ignore("Only constants defined here.")
public class TestConstants {
    /**
     * Constant to fully qualified directory with all test images.
     */
    public final static String FULLPATH_IMAGES =  System.getProperty("user.dir")
            + File.separator + "target" + File.separator + "test-classes"
            + File.separator + "data" + File.separator;

    /**
     * Constant to describe full qualified path to one test image.
     */
    public final static String FULLPATH_TEST_IMG = FULLPATH_IMAGES + "IMG_7559_mini.JPG";

    /**
     * Constant to full qualified picture url after processing.
     */
    public final static String FULLPATH_TEST_IMG_RENAMED = FULLPATH_IMAGES + "20110130_131102_IMG_7559_mini.JPG";

    /**
     * Name of the test image after renaming.
     */
    public final static String IMAGE_NAME_RENAMED =
            "20110130_131102_IMG_7559_mini.JPG";

}
