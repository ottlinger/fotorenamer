package de.aikiit.bildbearbeiter.image;

import de.aikiit.bildbearbeiter.TestConstants;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * Test exif renaming class.
 *
 * @author hirsch
 * @version 2011-06-02, 13:41
 */
public class CreationDateFromExifImageRenamerTest {

    final static private Logger LOG = Logger.
            getLogger(CreationDateFromExifImageRenamerTest.class);

    @Test
    public void renameTestImage() throws Exception {
        CreationDateFromExifImageRenamer creationDateFromExifImageRenamer =
                new CreationDateFromExifImageRenamer(TestConstants.FULLPATH_IMAGES);

    }

}
