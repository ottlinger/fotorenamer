package de.aikiit.bildbearbeiter.exception;

/**
 * Exception that indicates
 * that an error occurred during processing.
 * @author hirsch
 * @version 04.03.11
 */
public class RenamingErrorException extends Exception {
    /**
     * Provide error messages for one directory.
     * @param message Current directory.
     */

    public RenamingErrorException(final String message) {
        super(message);
        System.err.println(message);
    }
}
