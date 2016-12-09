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
package de.aikiit.fotorenamer.image;

import de.aikiit.fotorenamer.TestConstants;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.util.regex.Matcher;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Read properties from test images and verify exif-metadata extraction.
 *
 * @author hirsch
 */
public class MetaDataExtractorTest {
    private static final Logger LOG = Logger.getLogger(MetaDataExtractorTest.class);

    @Test
    public void makeSurePatternMatchesMultipleRenamedFiles() {
        Matcher matcher = TestConstants.IS_TEST_FILE.matcher("123123123123_20110130_131102_IMG_7559_mini.JpG");
        assertTrue(matcher.find());
    }

    @Test
    public final void ensureGivenExampleFileIsRenamed() throws Exception {
        File f = new File(TestConstants.FULLPATH_TEST_IMG);

        int minLength = TestConstants.PLAIN_FILE_NAME.length();

        if (!f.exists()) {
            LOG.debug("Switching to renamed file, since another test may have touched it before.");
            f = new File(TestConstants.FULLPATH_TEST_IMG_RENAMED);
            minLength = f.getName().length();
        }

        LOG.debug("Extracting metadata from " + f.getAbsolutePath());

        String renamedFile =
                MetaDataExtractor
                        .generateCreationDateInCorrectFormat(f);
        assertTrue(renamedFile.endsWith(TestConstants.PLAIN_FILE_NAME));
        assertTrue("Expecting at least " + minLength + " chars, got " + renamedFile, renamedFile.length() >= minLength);
    }

    /**
     * Checks assertion failure with null parameter.
     *
     * @throws Exception in case of errors.
     */
    @Test(expected = AssertionError.class)
    public final void checkAssertionErrors() throws Exception {
        assertNull(MetaDataExtractor.getExifMetadata(null, ExifTagConstants.EXIF_TAG_BRIGHTNESS));
    }

    /**
     * Checks assertion failure with null parameter.
     *
     * @throws Exception in case of errors.
     */
    @Test(expected = AssertionError.class)
    public final void checkAssertionErrorTag() throws Exception {
        assertNull(MetaDataExtractor.getExifMetadata(new File(TestConstants.FULLPATH_TEST_IMG), null));
    }

}
