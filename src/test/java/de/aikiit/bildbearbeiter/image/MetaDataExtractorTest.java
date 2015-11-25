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
     * @throws Exception in case of errors.
     */
    @Test
    public final void readDirectoryContent() throws Exception {
        File f = new File(TestConstants.FULLPATH_TEST_IMG);
        LOG.debug("Extracting metadata from " + f.getAbsolutePath());
        assertEquals(TestConstants.IMAGE_NAME_RENAMED, MetaDataExtractor
                .generateCreationDateInCorrectFormat(f));
    }

    /**
     * Checks assertion failure with null parameter.
     * @throws Exception in case of errors.
     */
    @Test(expected = AssertionError.class)
    public void checkAssertionErrors() throws Exception {
        assertNull(MetaDataExtractor.getExifMetadata(null, null));
    }



}
