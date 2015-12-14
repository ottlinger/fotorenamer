/**
 * Copyright 2011, Aiki IT, FotoRenamer
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.aikiit.fotorenamer.exception;

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
     * Provide error messages for a directory.
     *
     * @param directory Current directory.
     */
    public NoFilesFoundException(final File directory) {
        super(directory == null ? "none" : directory.getAbsolutePath());
        LOG.error("no files found in " + directory);
    }

}
