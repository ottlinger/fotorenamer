package de.aikiit.bildbearbeiter.exception;

/**
 * Exception that indicates that an error occured during processing.
 * @author hirsch
 * @version 04.03.11
 */
public class RenamingErrorException extends Exception {

    public RenamingErrorException(String message) {
        super(message);
    }
}
