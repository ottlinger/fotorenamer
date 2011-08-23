package de.aikiit.bildbearbeiter.image;

import de.aikiit.bildbearbeiter.TestConstants;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;

import static junit.framework.Assert.assertTrue;

/**
 * Test image renaming.
 *
 * @author hirsch
 * @version 2011-06-02, 13:41
 */
// TODO On mac the dummy image gets deleted sometimes while the renamed file is existing, maybe due to different
// maven calls? compile site:site and clean install -Plive-demo (not seen on linux).
public class CreationDateFromExifImageRenamerTest {

    final static private Logger LOG = Logger.
            getLogger(CreationDateFromExifImageRenamerTest.class);

    @Test
    public void renameTestImageAndDeleteFileAfterwards() throws Exception {

        assertTrue("Test image has to exist", new File(TestConstants.FULLPATH_TEST_IMG).exists());

        // rename
        CreationDateFromExifImageRenamer creationDateFromExifImageRenamer =
                new CreationDateFromExifImageRenamer(TestConstants.FULLPATH_IMAGES);

        // FIXME assertTrue("Test image (renamed) has to exist afterwards", new File(TestConstants.FULLPATH_TEST_IMG_RENAMED).exists());

        // restore for next test
        File f = new File(TestConstants.FULLPATH_TEST_IMG_RENAMED);
        f.renameTo(new File(TestConstants.FULLPATH_TEST_IMG));
        // FIXME assertTrue("Test image has to exist afterwards", new File(TestConstants.FULLPATH_TEST_IMG).exists());

    }

}
