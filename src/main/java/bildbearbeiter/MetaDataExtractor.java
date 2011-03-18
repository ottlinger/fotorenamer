package bildbearbeiter;

import org.apache.log4j.Logger;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * Helper class to extract metadata from given images. This class uses Apache Sanslean classes
 * to perform the metadata extraction itself.
 */
public class MetaDataExtractor {
    final static private Logger LOG = Logger.getLogger(MetaDataExtractor.class);
    public static final String EMPTY_STRING = "";
    public static final String SPACE = " ";
    public static final String UNDERSCORE = "_";
    public static final String COLON= ":";
    public static final String APOSTROPHE= "'";


    // taken from Sanslean directly
    public static void metadataExample(File file) throws ImageReadException,
            IOException {
        //        get all metadata stored in EXIF format (ie. from JPEG or TIFF).
        //            org.w3c.dom.Node node = Sanselan.getMetadataObsolete(imageBytes);
        IImageMetadata metadata = Sanselan.getMetadata(file);

        if (metadata instanceof JpegImageMetadata) {
            JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

            // Jpeg EXIF metadata is stored in a TIFF-based directory structure
            // and is identified with TIFF tags.
            // Here we look for the "x resolution" tag, but
            // we could just as easily search for any other tag.
            //
            // see the TiffConstants file for a list of TIFF tags.

            LOG.info("file: " + file.getPath());

            // print out various interesting EXIF tags.
            printTagValue(jpegMetadata, TiffConstants.TIFF_TAG_XRESOLUTION);
            printTagValue(jpegMetadata, TiffConstants.TIFF_TAG_DATE_TIME);
            printTagValue(jpegMetadata,
                    TiffConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
            printTagValue(jpegMetadata, TiffConstants.EXIF_TAG_CREATE_DATE);
            printTagValue(jpegMetadata, TiffConstants.EXIF_TAG_ISO);
            printTagValue(jpegMetadata,
                    TiffConstants.EXIF_TAG_SHUTTER_SPEED_VALUE);
            printTagValue(jpegMetadata, TiffConstants.EXIF_TAG_APERTURE_VALUE);
            printTagValue(jpegMetadata, TiffConstants.EXIF_TAG_BRIGHTNESS_VALUE);
            printTagValue(jpegMetadata, TiffConstants.GPS_TAG_GPS_LATITUDE_REF);
            printTagValue(jpegMetadata, TiffConstants.GPS_TAG_GPS_LATITUDE);
            printTagValue(jpegMetadata, TiffConstants.GPS_TAG_GPS_LONGITUDE_REF);
            printTagValue(jpegMetadata, TiffConstants.GPS_TAG_GPS_LONGITUDE);

            // simple interface to GPS data
            TiffImageMetadata exifMetadata = jpegMetadata.getExif();
            if (null != exifMetadata) {
                TiffImageMetadata.GPSInfo gpsInfo = exifMetadata.getGPS();
                if (null != gpsInfo) {
                    String gpsDescription = gpsInfo.toString();
                    double longitude = gpsInfo.getLongitudeAsDegreesEast();
                    double latitude = gpsInfo.getLatitudeAsDegreesNorth();

                    LOG.info("    " + "GPS Description: " + gpsDescription);
                    LOG.info("    " + "GPS Longitude (Degrees East): " + longitude);
                    LOG.info("    " + "GPS Latitude (Degrees North): " + latitude);
                }
            }

            ArrayList items = jpegMetadata.getItems();

            for (Object item : items) {
                LOG.info("    " + "item: " + item);
            }
        }
    }

    private static void printTagValue(JpegImageMetadata jpegMetadata,
                                      TagInfo tagInfo) {
        TiffField field = jpegMetadata.findEXIFValue(tagInfo);
        if (field == null)
            LOG.info(tagInfo.name + ": " + "Not Found.");
        else
            LOG.info(tagInfo.name + ": "
                    + field.getValueDescription());
    }

    /**
     * Returns the requested tag as String from the image file.
     *
     * @param image Image file to extract Metadata from.
     * @param tag   Tag to extract from the given file, @see TiffConstants
     * @return Returns exif tag value, in case of any errors the value is an empty String.
     * @throws IOException
     * @throws ImageReadException
     */
    public static String getExifMetadata(File image, TagInfo tag) throws IOException, ImageReadException {
        assert image != null : "Parameter image must not be null";
        assert tag != null : "Parameter tag must not be null";

        String result = EMPTY_STRING;
        IImageMetadata metadata = Sanselan.getMetadata(image);
        if (metadata instanceof JpegImageMetadata) {
            JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            TiffField field = jpegMetadata.findEXIFValue(tag);
            if (field != null) {
                result = field.getValueDescription();
                LOG.info("extraction of " + tag.getDescription() + " yields " + result);
            }
        }
        return result == null ? EMPTY_STRING : result;
    }

    /**
     * Helper to extract the date this image was created to be used during the renaming process.
     * <p/>
     * In the EXIF standard itself the following convention for dates as is defined:
     * <i>
     * D. Other Tags
     * DateTime
     * The date and time of image creation. In this standard it is the date and time the file was changed. The format is
     * "YYYY:MM:DD HH:MM:SS" with time shown in 24-hour format, and the date and time separated by one blank
     * character [20.H]. When the date and time are unknown, all the character spaces except colons (":") may be filled
     * with blank characters, or else the Interoperability field may be filled with blank characters. The character string
     * length is 20 bytes including NULL for termination. When the field is left blank, it is treated as unknown.
     * Tag = 306 (132.H)
     * Type = ASCII
     * Count = 20
     * Default = none
     * </i>
     *
     * @param image Image to extract metadata from.
     * @return the date this image was created if found, format is
     * @throws ParseException, ImageReadException, IOException - if an error occurs when accessing the image's metadata.
     * @see <a href="http://www.exif.org/samples/canon-ixus.html">Canon EXIF example page</a>
     * @see <a href="http://www.exif.org/specifications.html">EXIF specifications</a>
     * @see <a href="http://www.exif.org/Exif2-2.PDF">EXIF2-2.pdf specification</a>
     */
    public static String generateCreationDateInCorrectFormat(File image) throws ImageReadException,
            IOException, ParseException {
        String dateValue = getExifMetadata(image, TiffConstants.EXIF_TAG_CREATE_DATE);
        if (dateValue != null) {
            LOG.info("EXIF date value is: " + dateValue);

            assert dateValue.length() == 21 : "Invalid length of EXIF metadata, not complying to the standard";

            // Date parsing with apache.DateUtils or JDK-DateFormats does not work due to '-signs in the date string
            // (unparseable pattern is "'yyyy:MM:dd HH:mm:ss'")

            // replace special characters to extract digits only
            dateValue = dateValue.replaceAll(APOSTROPHE, EMPTY_STRING);
            dateValue = dateValue.replaceAll(COLON, EMPTY_STRING);
            dateValue = dateValue.replaceAll(SPACE, UNDERSCORE);
            dateValue += "_";
            //convert '2011:01:30 13:11:02' to "yyyyMMdd_HHmm_"+fileName
            dateValue += image.getName();

            LOG.info("Target filename is:" + dateValue);
            return dateValue;
        }
        LOG.info("No creation date extracted from file "+ image);

        return EMPTY_STRING;
    }
}