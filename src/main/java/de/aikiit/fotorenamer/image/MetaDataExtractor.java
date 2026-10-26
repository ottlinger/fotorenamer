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

import com.google.common.base.Strings;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Helper class to extract metadata from given images. This class uses Apache
 * Commons Imaging to perform the metadata extraction itself.
 */
final class MetaDataExtractor {
    /**
     * This class' logger.
     */
    private static final Logger LOG = Logger.getLogger(MetaDataExtractor.class);

    /**
     * Constant for an empty string.
     */
    private static final String EMPTY_STRING = "";

    /**
     * Constant for an underscore character.
     */
    private static final String UNDERSCORE = "_";

    /**
     * Constant for an apostrophe.
     */
    private static final String APOSTROPHE = "'";

    /**
     * Constant to describe a valid length of an EXIF date (currently 21).
     */
    private static final int VALID_EXIF_DATE_LENGTH = 21;

    private static final DateTimeFormatter EXIF_DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu:MM:dd HH:mm:ss").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter FILENAME_DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuuMMdd_HHmmss");

    /**
     * Constructor is not visible to avoid instantiation.
     */
    private MetaDataExtractor() {
        // prevent instantiation of this utility class
    }

    /**
     * Returns the requested tag as String from the image file.
     *
     * @param image Image file to extract Metadata from.
     * @param tag   Tag to extract from the given file, @see TiffConstants
     * @return Returns exif tag value, in case of any errors the value is an
     * empty String.
     * @throws IOException        if file cannot be accessed.
     */
    public static String getExifMetadata(final File image,
                                         final TagInfo tag)
            throws IOException {
        assert image != null : "Parameter image must not be null";
        assert tag != null : "Parameter tag must not be null";

        String result = EMPTY_STRING;
        ImageMetadata metadata = Imaging.getMetadata(image);

        TiffField field = null;
        if (metadata instanceof JpegImageMetadata jpegMetadata) {
            field = jpegMetadata.findExifValueWithExactMatch(tag);
        } else if (metadata instanceof TiffImageMetadata tiffMetadata) {
            field = tiffMetadata.findField(tag);
        }
        if (field != null) {
            result = field.getValueDescription();
            LOG.info("extraction of " + tag.getDescription()
                    + " from " + image.getAbsolutePath() + " yields " + result);
        }
        return result == null ? EMPTY_STRING : result;
    }

    /**
     * Helper to extract the date this image was created to be used during the
     * renaming process.
     * <br>
     * In the EXIF standard itself the following convention for dates as is
     * defined: <i> D. Other Tags DateTime The date and time of image creation.
     * In this standard it is the date and time the file was changed. The format
     * is "YYYY:MM:DD HH:MM:SS" with time shown in 24-hour format, and the date
     * and time separated by one blank character [20.H]. When the date and time
     * are unknown, all the character spaces except colons (":") may be filled
     * with blank characters, or else the Interoperability field may be filled
     * with blank characters. The character string length is 20 bytes including
     * NULL for termination. When the field is left blank, it is treated as
     * unknown. Tag = 306 (132.H) Type = ASCII Count = 20 Default = none </i>
     * <p>
     * If the extracted date value is empty - no new file name is generated.
     *
     * @param image Image to extract metadata from.
     * @return the date this image was created if found, format is
     * @throws IOException        If an error occurs when accessing the image's
     *                            metadata.
     * @see <a href="http://www.exif.org/samples/canon-ixus.html">Canon EXIF
     * example page</a>
     * @see <a href="http://www.exif.org/specifications.html">EXIF
     * specifications</a>
     * @see <a href="http://www.exif.org/Exif2-2.PDF">EXIF2-2.pdf
     * specification</a>
     */
    public static String generateCreationDateInCorrectFormat(final File image)
            throws IOException {

        String dateValue =
                getExifMetadata(image, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);

        LOG.info("EXIF date value is: " + dateValue);

        // Invalid length of EXIF metadata, not complying to the standard.
        if (Strings.isNullOrEmpty(dateValue) || dateValue.length() != VALID_EXIF_DATE_LENGTH) {
            LOG.info("No valid creation date extracted from file " + image);
            return EMPTY_STRING;
        }

        try {
            LocalDateTime captureDate = LocalDateTime.parse(
                    dateValue.replace(APOSTROPHE, EMPTY_STRING), EXIF_DATE_FORMAT);
            dateValue = captureDate.format(FILENAME_DATE_FORMAT) + UNDERSCORE + image.getName();
        } catch (DateTimeParseException e) {
            LOG.warn("Invalid EXIF creation date in " + image.getAbsolutePath(), e);
            return EMPTY_STRING;
        }

        LOG.info("Target filename is: " + dateValue);
        return dateValue;
    }
}
