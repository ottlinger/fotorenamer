package de.aikiit.bildbearbeiter.image;

import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;

import static de.aikiit.bildbearbeiter.TestConstants.*;
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

    @Test
    public void renameTestImageAndDeleteFileAfterwards() throws Exception {

        LOG.info("Working on file " + FULLPATH_TEST_IMG);
        assertTrue("Test image directory has to exist, i.e. mvn filtering was correct",
                new File(FULLPATH_TEST_IMG).exists());
        assertTrue("Test image has to exist", new File(FULLPATH_TEST_IMG).exists());

        CreationDateFromExifImageRenamer renamer = new CreationDateFromExifImageRenamer(FULLPATH_IMAGES);
        Thread t = new Thread(renamer);
        t.start();
        assertTrue(t.isAlive());

        // FIXME assertTrue("Renamed test image has to exist afterwards.",
        //        new File(FULLPATH_TEST_IMG_RENAMED).exists());
    }

}
