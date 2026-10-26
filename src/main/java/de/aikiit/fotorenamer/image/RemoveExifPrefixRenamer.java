/*
 * Copyright 2011-2026, Aiki IT, FotoRenamer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.aikiit.fotorenamer.image;

import de.aikiit.fotorenamer.exception.InvalidDirectoryException;
import de.aikiit.fotorenamer.exception.NoFilesFoundException;

import java.nio.file.Path;
import java.util.regex.Pattern;

/** Removes a leading EXIF date prefix using the shared, non-overwriting NIO renamer. */
public final class RemoveExifPrefixRenamer extends AbstractImageRenamer {

    // Current yyyyMMdd_HHmmss_ and legacy yyyyMMdd_HHmm_ prefixes, only at the start.
    private static final Pattern EXIF_PREFIX = Pattern.compile("^\\d{8}_\\d{4}(?:\\d{2})?_");

    /**
     * Finds images without automatically starting a worker or creating a GUI.
     *
     * @param directory Directory containing images to rename.
     * @throws InvalidDirectoryException If the directory cannot be read.
     * @throws NoFilesFoundException If no images are detected.
     */
    public RemoveExifPrefixRenamer(final String directory)
            throws InvalidDirectoryException, NoFilesFoundException {
        super(directory);
    }

    @Override
    public String renameImage(final Path imageFile) {
        return EXIF_PREFIX.matcher(fileName(imageFile)).replaceFirst("");
    }

}
