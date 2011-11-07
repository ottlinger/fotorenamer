package de.aikiit.bildbearbeiter.exception;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test all exception classes.
 *
 * @author hirsch
 * @version 2011-11-07, 22:29
 */
public class FotorenamerExceptionTest {
    @Test
    public void planConstructors() {
        RenamingErrorException e = new RenamingErrorException("woo");
        assertTrue(e.getMessage().contains("woo"));

        InvalidDirectoryException e1 = new InvalidDirectoryException(new File
                ("woo"));
        assertNotNull(e1);

        NoFilesFoundException e2 = new NoFilesFoundException(new File
                ("woo"));
        assertNotNull(e2);
    }

}
