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
package de.aikiit.fotorenamer.image;

import de.aikiit.fotorenamer.exception.InvalidDirectoryException;
import de.aikiit.fotorenamer.exception.NoFilesFoundException;

import java.io.IOException;
import java.nio.file.Path;

/**
 * This class transforms picture file names. All relevant files
 * in the target directory get a new file name - if correct
 * metadata can be extracted from the files' EXIF file headers.
 * <br>
 * A picture <code>foo.jpg</code> is renamed to
 * <code>201108111100_foo.jpg</code>  if the picture's creation date
 * was 2011-08-11 11:00.
 * <br>
 * Files without EXIF metadata are not touched at all.
 *
 * @author hirsch
 * @version 2011-06-02, 13:22
 */
public final class CreationDateFromExifImageRenamer extends AbstractImageRenamer {

    /**
     * The given directory is scanned for image files that
     * are processed.
     *
     * @param targetDirectory Name of the directory to work on.
     * @throws InvalidDirectoryException If there's a problem with the directory
     *                                   selected.
     * @throws NoFilesFoundException     if the selected directory is empty.
     */
    public CreationDateFromExifImageRenamer(final String targetDirectory) throws
            InvalidDirectoryException, NoFilesFoundException {
        super(targetDirectory);
    }

    /**
     * Extracts the EXIF date. Missing dates leave the name unchanged; extraction
     * failures propagate to the batch processor for logging and failure counting.
     *
     * @param imageFile Filename to renameFiles according to the subclass
     *                  implementation.
     * @return New filename that includes the image's creation date.
     * @see MetaDataExtractor for more information about the file format.
     */
    @Override
    public String renameImage(final Path imageFile) throws IOException {
        String newImageName = MetaDataExtractor.generateCreationDateInCorrectFormat(imageFile);
        return newImageName == null || newImageName.isEmpty() ? fileName(imageFile) : newImageName;
    }
}
