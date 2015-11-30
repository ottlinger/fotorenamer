/**
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
