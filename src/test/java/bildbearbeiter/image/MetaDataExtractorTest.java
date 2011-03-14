package bildbearbeiter.image;

import static org.junit.Assert.*;

import bildbearbeiter.MetaDataExtractor;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;

/**
 * Read properties from test data files.
 * @author hirsch
 */
public class MetaDataExtractorTest {
        final static private Logger LOG = Logger.getLogger(MetaDataExtractorTest.class);

        @Test
        public final void readDirectoryContent() throws Exception {
//                assertEquals("3771", ""+Integer.valueOf(3771));
            // assertNull(MetaDataExtractor.generateCreationDateInCorrectFormat(new File("data/IMG_7559_mini.JPG")));
            LOG.debug("Scheiße, oder ....");
        LOG.debug(getClass().getClassLoader().getResourceAsStream("/IMG_7559_mini.JPG"));
            assertNotNull(getClass().getClassLoader().getResourceAsStream("/IMG_7559_mini.JPG"));

        }



}
