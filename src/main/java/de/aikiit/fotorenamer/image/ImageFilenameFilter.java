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
import com.google.common.collect.Lists;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

/**
 * Filter to prevent wrong files from being manipulated by this tool. Currently
 * only files with the extensions {@link #EXTENSIONS}.
 *
 * @author hirsch
 * @version 2011-04-02, 13:52
 */
class ImageFilenameFilter implements FilenameFilter {

    private static final List<String> EXTENSIONS = Lists.newArrayList("jpg", "jpeg", "png");

    /**
     * Filter filenames in a directory for images.
     *
     * @param dir  Directory to filter filenames in.
     * @param name Filename to filter.
     * @return Return <code>true</code> if the given File is a directory and
     * the file's prefix is part of {@link #EXTENSIONS}.
     */
    public final boolean accept(final File dir, final String name) {
        return !(Strings.isNullOrEmpty(name) || dir == null) && new File(dir, name).isFile() && isSuffixExifExtractable(name);
    }

    private static boolean isSuffixExifExtractable(final String name) {
        if (!Strings.isNullOrEmpty(name)) {
            String file = name.trim().toLowerCase();
            for (String suffix : EXTENSIONS) {
                if (file.endsWith("." + suffix)) {
                    return true;
                }
            }
        }
        return false;

    }
}
