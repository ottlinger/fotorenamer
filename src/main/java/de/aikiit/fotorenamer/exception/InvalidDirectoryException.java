/*
Copyright 2011, Aiki IT, FotoRenamer
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package de.aikiit.fotorenamer.exception;

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

    /**
    * Shows and logs a given error message.
    * @param message the error message.
    */
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
