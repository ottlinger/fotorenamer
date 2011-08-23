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
    public final static String FULLPATH_IMAGES = System.getProperty("user.dir")
            + File.separator + "src" + File.separator + "test" + File.separator + "data" + File.separator;

    /**
     * Constant to describe full qualified path to one test image.
     */
    public final static String FULLPATH_TEST_IMG = FULLPATH_IMAGES + "IMG_7559_mini.JPG";

    /**
     * Constant to full qualified picture url after processing (is needed to cleanup after testing).
     */
    public final static String FULLPATH_TEST_IMG_RENAMED = FULLPATH_IMAGES + "20110130_131102_IMG_7559_mini.JPG";

}
