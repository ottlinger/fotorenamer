package bildbearbeiter;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: hirsch
 * Date: 04.03.11
 * Time: 23:08
 * To change this template use File | Settings | File Templates.
 */
public class MetaDataExtractor {

    public static SimpleDateFormat PHOTO_DATE_FORMAT =
            new SimpleDateFormat("yyyyMMdd_HHmm_");


    public static void metadataExample(File file) throws ImageReadException,
            IOException {
        //        get all metadata stored in EXIF format (ie. from JPEG or TIFF).
        //            org.w3c.dom.Node node = Sanselan.getMetadataObsolete(imageBytes);
        IImageMetadata metadata = Sanselan.getMetadata(file);

        //System.out.println(metadata);

        if (metadata instanceof JpegImageMetadata) {
            JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

            // Jpeg EXIF metadata is stored in a TIFF-based directory structure
            // and is identified with TIFF tags.
            // Here we look for the "x resolution" tag, but
            // we could just as easily search for any other tag.
            //
            // see the TiffConstants file for a list of TIFF tags.

            System.out.println("file: " + file.getPath());

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

            System.out.println();

            // simple interface to GPS data
            TiffImageMetadata exifMetadata = jpegMetadata.getExif();
            if (null != exifMetadata) {
                TiffImageMetadata.GPSInfo gpsInfo = exifMetadata.getGPS();
                if (null != gpsInfo) {
                    String gpsDescription = gpsInfo.toString();
                    double longitude = gpsInfo.getLongitudeAsDegreesEast();
                    double latitude = gpsInfo.getLatitudeAsDegreesNorth();

                    System.out.println("    " + "GPS Description: " + gpsDescription);
                    System.out.println("    " + "GPS Longitude (Degrees East): " + longitude);
                    System.out.println("    " + "GPS Latitude (Degrees North): " + latitude);
                }
            }

            ArrayList items = jpegMetadata.getItems();
            for (int i = 0; i < items.size(); i++) {
                Object item = items.get(i);
                System.out.println("    " + "item: " + item);
            }

            System.out.println();
        }
    }

    private static void printTagValue(JpegImageMetadata jpegMetadata,
                                      TagInfo tagInfo) {
        TiffField field = jpegMetadata.findEXIFValue(tagInfo);
        if (field == null)
            System.out.println(tagInfo.name + ": " + "Not Found.");
        else
            System.out.println(tagInfo.name + ": "
                    + field.getValueDescription());
    }

    /**
     * Helper to extract the date this image was created to be used during the renaming process.
     *
     * @param image
     * @return the date this image was created if found, format is
     * @throws ImageReadException
     * @throws IOException
     */
    public static String generateCreationDateInCorrectFormat(File image) throws ImageReadException,
            IOException, ParseException {
        String formattedDate = "";
        IImageMetadata metadata = Sanselan.getMetadata(image);

        if (metadata instanceof JpegImageMetadata) {
            JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

            TiffField field = jpegMetadata.findEXIFValue(TiffConstants.EXIF_TAG_CREATE_DATE);

            if (field != null) {
                System.out.println("Datumswert: " + field.getValueDescription());

                System.out.println("Woo: "+Date.parse(field.getValueDescription()));
                // schlägt fehl - System.out.println(PHOTO_DATE_FORMAT.parse(field.getValueDescription()));

                formattedDate = PHOTO_DATE_FORMAT.format(PHOTO_DATE_FORMAT.parse(field.getValueDescription()));

                System.out.println("Umgewandelt: " + formattedDate);
            }

        }
        return formattedDate;

    }
}
