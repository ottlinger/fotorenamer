package de.aikiit.bildbearbeiter.exception;

import org.apache.log4j.Logger;

import java.io.File;

/**
 * Exception that indicates that an error occurred when touching the selected
 * directory.
 *
 * @author hirsch
 * @version 04.03.11
 */
public class InvalidDirectoryException extends Exception {
    /**
     * Logger.
     */
    private static final Logger LOG =
            Logger.getLogger(InvalidDirectoryException.class);

    public InvalidDirectoryException(String message) {
        super(message);
        LOG.error("invalid directory: " + message);
    }

    /**
     * Provide error messages for one directory.
     *
     * @param directory Current directory.
     */
    public InvalidDirectoryException(final File directory) {
        super(directory.toString());
        LOG.error("invalid directory: " + directory);
    }

}
