/**
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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ImageContentFilterTest {

    @TempDir
    Path directory;

    private final ImageContentFilter filter = new ImageContentFilter();

    @Test
    void nullArgumentIsSafe() {
        assertFalse(filter.test(null));
    }

    @Test
    void detectsJpegContentRegardlessOfFilename() throws Exception {
        for (String name : List.of("photo", "photo.dat", "photo.png", "photo.JpG")) {
            Path image = directory.resolve(name);
            try (var input = getClass().getResourceAsStream("/data/IMG_7559_mini.JPG")) {
                assertNotNull(input);
                Files.copy(input, image);
            }
            assertTrue(filter.test(image), name);
        }
    }

    @Test
    void detectsPngWithoutAnImageExtension() throws Exception {
        Path image = directory.resolve("image.bin");
        assertTrue(ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", image.toFile()));
        assertTrue(filter.test(image));
    }

    @Test
    void rejectsNonImagesEvenWithImageExtensions() throws Exception {
        for (String name : List.of("notes.jpg", "notes.png", "notes")) {
            Files.writeString(directory.resolve(name), "This is plain text, not an image.");
            assertFalse(filter.test(directory.resolve(name)), name);
        }
        Files.createFile(directory.resolve("empty.jpg"));
        assertFalse(filter.test(directory.resolve("empty.jpg")));
    }

    @Test
    void logsReadFailuresWithFilePathAndCause() {
        // Attribute lookup failures must retain the filesystem exception, too.
        Path missing = directory.resolve("disappeared.jpg");
        Logger logger = (Logger) LogManager.getLogger(ImageContentFilter.class);
        List<LogEvent> events = new CopyOnWriteArrayList<>();
        AbstractAppender appender = new AbstractAppender("detection-errors", null, null, false, Property.EMPTY_ARRAY) {
            @Override
            public void append(LogEvent event) {
                events.add(event.toImmutable());
            }
        };
        appender.start();
        logger.addAppender(appender);
        try {
            assertFalse(filter.test(missing));
            assertTrue(events.stream().anyMatch(event -> event.getLevel() == Level.ERROR
                    && event.getThrown() instanceof IOException
                    && event.getMessage().getFormattedMessage().contains(missing.toAbsolutePath().toString())));
        } finally {
            logger.removeAppender(appender);
            appender.stop();
        }
    }

    @Test
    void rejectsDirectoriesAndMissingFiles() throws Exception {
        Files.createDirectory(directory.resolve("folder.jpg"));
        assertFalse(filter.test(directory.resolve("folder.jpg")));
        assertFalse(filter.test(directory.resolve("missing.jpg")));
    }
}
