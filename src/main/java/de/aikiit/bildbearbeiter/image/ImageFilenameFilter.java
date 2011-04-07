package de.aikiit.bildbearbeiter.image;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Filter to prevent wrong files from being manipulated by this tool.
 * Currently only files with the JPG-extension are considered.
 *
 * @author hirsch
 * @version 2011-04-02, 13:52
 */
public class ImageFilenameFilter implements FilenameFilter {
private static final String JPG = ".jpg";

    /**
     * @param dir Directory to filter filenames in.
     * @param name Filename to filter.
     * @return Return <code>true</code> when the given File is a directory and the file is a JPG-picture.
     */
    public boolean accept(File dir, String name) {
        return new File(dir, name).isFile() &&
               name.toLowerCase().endsWith(JPG);
    }
}