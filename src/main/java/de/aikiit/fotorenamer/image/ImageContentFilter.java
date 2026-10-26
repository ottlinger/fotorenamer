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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.Tika;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Predicate;

/**
 * Detects image candidates by content, independently of their filename.
 * Detection does not guarantee that Commons Imaging can extract EXIF metadata.
 */
final class ImageContentFilter implements Predicate<Path> {

    private static final Logger LOG = LogManager.getLogger(ImageContentFilter.class);
    private final Tika tika = new Tika();

    @Override
    public boolean test(final Path file) {
        if (file == null) {
            return false;
        }
        try {
            // Follow links as before, but retain diagnostics for failed attribute reads.
            if (!Files.readAttributes(file, BasicFileAttributes.class).isRegularFile()) {
                return false;
            }
            // Do not supply a filename: detection must rely on content only.
            try (InputStream input = new BufferedInputStream(Files.newInputStream(file))) {
                return tika.detect(input).startsWith("image/");
            }
        } catch (IOException | SecurityException e) {
            LOG.error("Unable to detect image type of '{}'", file.toAbsolutePath(), e);
            return false;
        }
    }
}
