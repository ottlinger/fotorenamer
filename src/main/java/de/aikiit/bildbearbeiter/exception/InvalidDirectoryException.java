package de.aikiit.bildbearbeiter.exception;

import java.io.File;

/**
 * Exception that indicates that
 * an error occurred when touching the selected directory.
 *
 * @author hirsch
 * @version 04.03.11
 */
public class InvalidDirectoryException extends Exception {
    /**
     * Provide error messages for one directory.
     * @param directory Current directory.
     */
    public InvalidDirectoryException(final File directory) {
        super();
        System.err.println(directory);
    }

}
