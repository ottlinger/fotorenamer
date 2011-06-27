package de.aikiit.bildbearbeiter.image;

import de.aikiit.bildbearbeiter.TestConstants;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Read properties from test images and verify exif-metadata extraction.
 *
 * @author hirsch
 */
public class MetaDataExtractorTest {
    final static private Logger LOG = Logger.getLogger(MetaDataExtractorTest.class);

    /**
     * Test metadata extraction from above example image.
     *
     * @throws Exception
     */
    @Test
    public final void readDirectoryContent() throws Exception {
        File f = new File(TestConstants.FULLPATH_TEST_IMG);
        LOG.debug("Extracting metadata from " + f.getAbsolutePath());
        assertEquals("20110130_131102_IMG_7559_mini.JPG", MetaDataExtractor.generateCreationDateInCorrectFormat(f));
    }

    /**
     * Checks assertion failure with null parameter.
     */
    @Test(expected = AssertionError.class)
    public void checkAssertionErrors() throws Exception {
        assertNull(MetaDataExtractor.getExifMetadata(null, null));
    }

}