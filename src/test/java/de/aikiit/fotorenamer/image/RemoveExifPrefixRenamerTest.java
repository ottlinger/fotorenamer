/*
 * Copyright 2026, Aiki IT, FotoRenamer
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

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RemoveExifPrefixRenamerTest {

    @TempDir
    Path directory;

    @Test
    void roundTripRestoresOriginalFilenameAndContents() throws Exception {
        Path original = copyImage("holiday.dat");
        byte[] content = Files.readAllBytes(original);
        var forward = new CreationDateFromExifImageRenamer(directory.toString());
        assertEquals(1, forward.renameFiles().renamed());
        Path prefixed = directory.resolve("20110130_131102_holiday.dat");
        assertTrue(Files.exists(prefixed));
        assertFalse(Files.exists(original));

        var reverse = new RemoveExifPrefixRenamer(directory.toString());
        // Construction no longer starts a worker or performs any renaming.
        assertTrue(Files.exists(prefixed));
        assertEquals(1, reverse.renameFiles().renamed());
        assertFalse(Files.exists(prefixed));
        assertArrayEquals(content, Files.readAllBytes(original));
    }

    @Test
    void supportsLegacyPrefixesAndUnicodeNamesWithoutExtensions() throws Exception {
        Path legacy = copyImage("20110130_1311_legacy.jpg");
        Path unicode = copyImage("20110130_131102_Urlaub am See äß");
        byte[] legacyContent = Files.readAllBytes(legacy);
        byte[] unicodeContent = Files.readAllBytes(unicode);
        var renamer = new RemoveExifPrefixRenamer(directory.toString());
        assertEquals(new RenameResult(2, 2, 2, 0, 0, false), renamer.renameFiles());
        assertArrayEquals(legacyContent, Files.readAllBytes(directory.resolve("legacy.jpg")));
        assertArrayEquals(unicodeContent, Files.readAllBytes(directory.resolve("Urlaub am See äß")));
    }

    @Test
    void preservesUnchangedNamesAndEmptyTargetsAndReportsProgress() throws Exception {
        List<String> names = List.of("plain.jpg", "photo_20110130_131102_test.jpg",
                "20110130_13110_invalid.jpg", "20110130_131102_");
        for (String name : names) {
            copyImage(name);
        }
        var renamer = new RemoveExifPrefixRenamer(directory.toString());
        List<Path> processed = new ArrayList<>();
        assertEquals(new RenameResult(4, 4, 0, 4, 0, false),
                renamer.renameFiles(progress -> processed.add(progress.file())));
        assertEquals(names.size(), processed.size());
        for (String name : names) {
            assertTrue(Files.exists(directory.resolve(name)), name);
        }
    }

    @Test
    void removesOnlyOneLeadingPrefix() throws Exception {
        Path image = copyImage("20110130_131102_20110129_1200_photo.bin");
        byte[] original = Files.readAllBytes(image);
        var renamer = new RemoveExifPrefixRenamer(directory.toString());
        assertEquals(1, renamer.renameFiles().renamed());
        assertArrayEquals(original, Files.readAllBytes(directory.resolve("20110129_1200_photo.bin")));
    }

    @Test
    void ignoresNonImagesAndSubdirectoriesEvenWithMatchingPrefixes() throws Exception {
        Path text = Files.writeString(directory.resolve("20110130_131102_notes.jpg"), "not an image");
        Path folder = Files.createDirectory(directory.resolve("20110130_131102_folder"));
        copyImage("20110130_131102_photo");
        var renamer = new RemoveExifPrefixRenamer(directory.toString());
        assertEquals(1, renamer.renameFiles().renamed());
        assertTrue(Files.exists(directory.resolve("photo")));
        assertEquals("not an image", Files.readString(text));
        assertTrue(Files.isDirectory(folder));
    }

    @Test
    void preservesExistingTargetsLogsCauseAndContinuesWithOtherImages() throws Exception {
        Path source = copyImage("20110130_131102_photo.dat");
        byte[] original = Files.readAllBytes(source);
        Path target = Files.writeString(directory.resolve("photo.dat"), "keep this file");
        copyImage("20110130_131102_other.dat");
        var renamer = new RemoveExifPrefixRenamer(directory.toString());
        Logger logger = (Logger) LogManager.getLogger(AbstractImageRenamer.class);
        List<LogEvent> events = new CopyOnWriteArrayList<>();
        AbstractAppender appender = new AbstractAppender("reverse-errors", null, null, false, Property.EMPTY_ARRAY) {
            @Override
            public void append(LogEvent event) {
                events.add(event.toImmutable());
            }
        };
        appender.start();
        logger.addAppender(appender);
        try {
            assertEquals(new RenameResult(2, 2, 1, 0, 1, false), renamer.renameFiles());
            assertTrue(events.stream().anyMatch(event -> event.getLevel() == Level.ERROR
                    && event.getThrown() instanceof FileAlreadyExistsException
                    && event.getMessage().getFormattedMessage().contains(source.toString())
                    && event.getMessage().getFormattedMessage().contains(target.toString())));
        } finally {
            logger.removeAppender(appender);
            appender.stop();
        }
        assertArrayEquals(original, Files.readAllBytes(source));
        assertEquals("keep this file", Files.readString(target));
        assertTrue(Files.exists(directory.resolve("other.dat")));
    }

    @Test
    void sourceReplacedByDirectoryIsLeftUntouched() throws Exception {
        Path source = copyImage("20110130_131102_replaced");
        var renamer = new RemoveExifPrefixRenamer(directory.toString());
        Files.delete(source);
        Files.createDirectory(source);
        List<Path> processed = new ArrayList<>();
        assertEquals(new RenameResult(1, 1, 0, 1, 0, false),
                renamer.renameFiles(progress -> processed.add(progress.file())));
        assertEquals(List.of(source), processed);
        assertTrue(Files.isDirectory(source));
        assertFalse(Files.exists(directory.resolve("replaced")));
    }

    @Test
    void missingSourceDoesNotAbortRemainingMoves() throws Exception {
        Path missing = copyImage("20110130_131102_missing");
        copyImage("20110130_131102_existing");
        var renamer = new RemoveExifPrefixRenamer(directory.toString());
        Files.delete(missing);
        assertEquals(new RenameResult(2, 2, 1, 0, 1, false), renamer.renameFiles());
        assertTrue(Files.exists(directory.resolve("existing")));
        assertFalse(Files.exists(directory.resolve("missing")));
    }

    private Path copyImage(String name) throws Exception {
        Path target = directory.resolve(name);
        try (var input = getClass().getResourceAsStream("/data/IMG_7559_mini.JPG")) {
            assertNotNull(input);
            Files.copy(input, target);
        }
        return target;
    }
}
