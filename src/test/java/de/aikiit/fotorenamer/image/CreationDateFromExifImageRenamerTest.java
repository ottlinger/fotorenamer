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
package de.aikiit.fotorenamer.image;

import de.aikiit.fotorenamer.exception.InvalidDirectoryException;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;

import static de.aikiit.fotorenamer.TestConstants.FULLPATH_IMAGES;
import static de.aikiit.fotorenamer.TestConstants.FULLPATH_TEST_IMG;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Test image renaming.
 *
 * @author hirsch
 * @version 2011-06-02, 13:41
 */
public class CreationDateFromExifImageRenamerTest {

    final static private Logger LOG = Logger.
            getLogger(CreationDateFromExifImageRenamerTest.class);

    /**
     * Ensure that no NullPointerException is thrown with null arguments.
     * @throws Exception in case of errors.
     */
    @Test(expected = InvalidDirectoryException.class)
    public final void checkNPECorrectnessInConstructor() throws Exception {
        CreationDateFromExifImageRenamer imageRenamer = new
                CreationDateFromExifImageRenamer(null);
        // just to avoid compiler warnings, code will not be reached
        assertNotNull(imageRenamer);
    }

    /**
     * Perform file renaming (while waiting for Thread to finish).
     * @throws Exception in case of errors.
     */
    // TODO redesign application - component mingles function and GUI and is
    // not clearly testable
    @Test
    public final void renameTestImageAndDeleteFileAfterwards() throws
            Exception {

        LOG.info("Working on file " + FULLPATH_TEST_IMG);
        assertTrue("Test image directory has to exist, i.e. mvn filtering was correct",
                new File(FULLPATH_TEST_IMG).exists());
        assertTrue("Test image has to exist", new File(FULLPATH_TEST_IMG).exists());

        CreationDateFromExifImageRenamer renamer = new CreationDateFromExifImageRenamer(FULLPATH_IMAGES);
        Thread t = new Thread(renamer);
        t.start();
        assertTrue(t.isAlive());
        // wait until thread is finished
        t.join();
        assertEquals(Thread.State.TERMINATED, t.getState());

        // FIXME since MetadataExtractorTest renames the image,
        // it has twice the prefix
/*
        assertTrue("Renamed test image has to exist afterwards.",
                new File
                        (TestConstants.FULLPATH_IMAGES +
                                "20110130_131102_20110130_131102_IMG_7559_mini.JPG").exists());
                                */
    }

}
