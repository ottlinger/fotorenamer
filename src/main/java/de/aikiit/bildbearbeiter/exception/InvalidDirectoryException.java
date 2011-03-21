package de.aikiit.bildbearbeiter.exception;

import java.io.File;

/**
 * Exception that indicates that an error occured when touching the selected directory.
 *
 * @author hirsch
 * @version 04.03.11
 */
public class InvalidDirectoryException extends Exception {
    public InvalidDirectoryException(File directory) {
        super();
        System.err.println(directory);
    }

    public InvalidDirectoryException(String message) {
        super(message);
    }

}
