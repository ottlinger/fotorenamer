package de.aikiit.bildbearbeiter.exception;

import java.io.File;

/**
 * Exception class to indicate that a selected directory
 * contains no relevant files to be renamed.
 * @author hirsch
 * @version 04.03.11
 */
public class NoFilesFoundException extends Exception {

    /**
     * Provide error messages for one directory.
     * @param directory Current directory.
     */
    public NoFilesFoundException(final File directory) {
        super();
        System.err.println(directory);
    }

}
