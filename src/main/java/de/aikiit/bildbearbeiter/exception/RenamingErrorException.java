package de.aikiit.bildbearbeiter.exception;

import org.apache.log4j.Logger;

/**
 * Exception that indicates that an error occurred during processing.
 *
 * @author hirsch
 * @version 04.03.11
 */
public class RenamingErrorException extends Exception {
    /**
     * Logger.
     */
    private static final Logger LOG =
            Logger.getLogger(RenamingErrorException.class);

    /**
     * Provide error messages for one directory.
     *
     * @param message Current directory.
     */

    public RenamingErrorException(final String message) {
        super(message);
        LOG.error("Error during renaming - " + message);
    }
}
