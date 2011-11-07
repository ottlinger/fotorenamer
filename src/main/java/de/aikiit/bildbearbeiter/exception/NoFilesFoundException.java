package de.aikiit.bildbearbeiter.exception;

import org.apache.log4j.Logger;

import java.io.File;

/**
 * Exception class to indicate that a selected directory contains no relevant
 * files to be renamed.
 *
 * @author hirsch
 * @version 04.03.11
 */
public class NoFilesFoundException extends Exception {
    /**
     * Logger.
     */
    private static final Logger LOG =
            Logger.getLogger(NoFilesFoundException.class);

    /**
     * Provide error messages for one directory.
     *
     * @param directory Current directory.
     */
    public NoFilesFoundException(final File directory) {
        super();
        LOG.error("no files found in " + directory);
    }

}
