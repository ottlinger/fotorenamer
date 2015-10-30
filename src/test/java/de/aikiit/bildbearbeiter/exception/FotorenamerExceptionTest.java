package de.aikiit.bildbearbeiter.exception;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test all exception classes.
 *
 * @author hirsch
 * @version 2011-11-07, 22:29
 */
@org.junit.Ignore("Not working on travis.")
public class FotorenamerExceptionTest {
    @Test
    public final void planConstructors() {
        RenamingErrorException e = new RenamingErrorException("woo");
        assertTrue(e.getMessage().contains("woo"));

        NoFilesFoundException e2 = new NoFilesFoundException(new File
                ("woo"));
        assertNotNull(e2);
    }

    @Test
    public final void invalidDirectories() {
        InvalidDirectoryException e1 = new InvalidDirectoryException(new File
                ("woo"));
        assertNotNull(e1);

        e1 = new InvalidDirectoryException("woo");
        assertNotNull(e1);
        assertEquals("woo", e1.getMessage());

    }

}
