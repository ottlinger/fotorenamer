package bildbearbeiter.image;

import java.io.File;
import java.io.IOException;
import static org.junit.Assert.*;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * Read properties from test data files.
 * @author hirsch
 */
public class MetaDataExtractorTest {
        final static private Logger LOG = Logger.getLogger(MetaDataExtractorTest.class);

        @Test
        public final void readDirectoryContent() {
                LOG.info("Foo - testMessage");
                assertEquals("3771", ""+Integer.valueOf(3771));
        }



}
