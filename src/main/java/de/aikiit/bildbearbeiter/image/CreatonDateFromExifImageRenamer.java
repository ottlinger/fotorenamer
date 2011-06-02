package de.aikiit.bildbearbeiter.image;

import org.apache.log4j.Logger;

import java.io.File;

/**
 * @author hirsch
 * @version 2011-06-02, 13:22
 */
public class CreatonDateFromExifImageRenamer extends AbstractImageRenamer {

    /** Logger for this class. */
    private static final Logger LOG =
            Logger.getLogger(CreatonDateFromExifImageRenamer.class);

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
