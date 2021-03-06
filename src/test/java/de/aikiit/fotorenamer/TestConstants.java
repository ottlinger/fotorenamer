/**
 * Copyright 2011, Aiki IT, FotoRenamer
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.aikiit.fotorenamer;

import org.junit.jupiter.api.Disabled;

import java.io.File;
import java.util.regex.Pattern;

/**
 * This helper class contains relevant constants for testing.
 *
 * @author hirsch
 * @version 2011-06-02, 13:42
 */
@Disabled("Only constants defined here.")
public class TestConstants {

    /**
     * This is the base name of the image.
     */
    public static final String PLAIN_FILE_NAME = "IMG_7559_mini.JPG";

    /**
     * Constant to fully qualified directory with all test images.
     */
    public static final String FULLPATH_IMAGES = System.getProperty("user.dir")
            + File.separator + "target" + File.separator + "test-classes"
            + File.separator + "data" + File.separator;

    /**
     * Constant to describe full qualified path to one test image.
     */
    public static final String FULLPATH_TEST_IMG = FULLPATH_IMAGES + PLAIN_FILE_NAME;

    /**
     * Constant to full qualified picture url after processing.
     */
    public static final String FULLPATH_TEST_IMG_RENAMED = FULLPATH_IMAGES + "20110130_131102_" + PLAIN_FILE_NAME;

    /**
     * Pattern to match given test image as suffix.
     */
    public static final Pattern IS_TEST_FILE = Pattern.compile(".*(" + PLAIN_FILE_NAME + ").*", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
}
