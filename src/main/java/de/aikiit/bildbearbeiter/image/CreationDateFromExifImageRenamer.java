package de.aikiit.bildbearbeiter.image;

import de.aikiit.bildbearbeiter.exception.InvalidDirectoryException;
import de.aikiit.bildbearbeiter.exception.NoFilesFoundException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.File;

/**
 * This class transforms picture file names. All relevant files
 * in the target directory get a new file name - if correct
 * metadata can be extracted from the files' EXIF file headers.
 * <br>
 * A picture <code>foo.jpg</code> is renamed to
 * <code>201108111100_foo.jpg</code>  if the picture's creation date
 * was 2011-08-11 11:00.
 * <br>
 * Files with no exif metadata are not changed at all.
 *
 * @author hirsch
 * @version 2011-06-02, 13:22
 */
public class CreationDateFromExifImageRenamer extends AbstractImageRenamer {

    /** Logger for this class. */
    private static final Logger LOG =
            LogManager.getLogger(CreationDateFromExifImageRenamer.class);

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
     * Extracts creation date from EXIF information and returns new filename. If
     * an error occurs during EXIF data extraction the original filename is
     * returned.
     *
     * @param imageFile Filename to renameFiles according to the subclass
     *                  implementation.
     * @return New filename that includes the image's creation date.
     * @see MetaDataExtractor for more information about the file format.
     */
    @Override
    final String renameImage(final File imageFile) {
        String newImageName = imageFile.getName();
        LOG.info("Start renaming in CreationDateFromExifImageRenamer");

        try {
            newImageName =
                    MetaDataExtractor.
                            generateCreationDateInCorrectFormat(imageFile);
        } catch (Exception e) {
            LOG.error("Error during exif date extraction: ", e);
            return newImageName;
        }

        return newImageName;
    }
}
