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

import static org.assertj.core.api.Assertions.assertThat;

class RemoveExifPrefixRenamerTest {

    @TempDir
    Path directory;

    @Test
    void roundTripRestoresOriginalFilenameAndContents() throws Exception {
        Path original = copyImage("holiday.dat");
        byte[] content = Files.readAllBytes(original);
        var forward = new CreationDateFromExifImageRenamer(directory.toString());
        assertThat(forward.renameFiles().renamed()).isEqualTo(1);
        Path prefixed = directory.resolve("20110130_131102_holiday.dat");
        assertThat(prefixed).exists();
        assertThat(original).doesNotExist();

        var reverse = new RemoveExifPrefixRenamer(directory.toString());
        // Construction no longer starts a worker or performs any renaming.
        assertThat(prefixed).exists();
        assertThat(reverse.renameFiles().renamed()).isEqualTo(1);
        assertThat(prefixed).doesNotExist();
        assertThat(Files.readAllBytes(original)).containsExactly(content);
    }

    @Test
    void supportsLegacyPrefixesAndUnicodeNamesWithoutExtensions() throws Exception {
        Path legacy = copyImage("20110130_1311_legacy.jpg");
        Path unicode = copyImage("20110130_131102_Urlaub am See äß");
        byte[] legacyContent = Files.readAllBytes(legacy);
        byte[] unicodeContent = Files.readAllBytes(unicode);
        var renamer = new RemoveExifPrefixRenamer(directory.toString());
        assertThat(renamer.renameFiles()).isEqualTo(new RenameResult(2, 2, 2, 0, 0, false));
        assertThat(Files.readAllBytes(directory.resolve("legacy.jpg"))).containsExactly(legacyContent);
        assertThat(Files.readAllBytes(directory.resolve("Urlaub am See äß"))).containsExactly(unicodeContent);
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
        assertThat(renamer.renameFiles(progress -> processed.add(progress.file()))).isEqualTo(new RenameResult(4, 4, 0, 4, 0, false));
        assertThat(processed).hasSameSizeAs(names);
        for (String name : names) {
            assertThat(directory.resolve(name)).as(name).exists();
        }
    }

    @Test
    void removesOnlyOneLeadingPrefix() throws Exception {
        Path image = copyImage("20110130_131102_20110129_1200_photo.bin");
        byte[] original = Files.readAllBytes(image);
        var renamer = new RemoveExifPrefixRenamer(directory.toString());
        assertThat(renamer.renameFiles().renamed()).isEqualTo(1);
        assertThat(Files.readAllBytes(directory.resolve("20110129_1200_photo.bin"))).containsExactly(original);
    }

    @Test
    void ignoresNonImagesAndSubdirectoriesEvenWithMatchingPrefixes() throws Exception {
        Path text = Files.writeString(directory.resolve("20110130_131102_notes.jpg"), "not an image");
        Path folder = Files.createDirectory(directory.resolve("20110130_131102_folder"));
        copyImage("20110130_131102_photo");
        var renamer = new RemoveExifPrefixRenamer(directory.toString());
        assertThat(renamer.renameFiles().renamed()).isEqualTo(1);
        assertThat(directory.resolve("photo")).exists();
        assertThat(Files.readString(text)).isEqualTo("not an image");
        assertThat(folder).isDirectory();
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
            assertThat(renamer.renameFiles()).isEqualTo(new RenameResult(2, 2, 1, 0, 1, false));
            assertThat(events.stream().anyMatch(event -> event.getLevel() == Level.ERROR
                    && event.getThrown() instanceof FileAlreadyExistsException
                    && event.getMessage().getFormattedMessage().contains(source.toString())
                    && event.getMessage().getFormattedMessage().contains(target.toString()))).isTrue();
        } finally {
            logger.removeAppender(appender);
            appender.stop();
        }
        assertThat(Files.readAllBytes(source)).containsExactly(original);
        assertThat(Files.readString(target)).isEqualTo("keep this file");
        assertThat(directory.resolve("other.dat")).exists();
    }

    @Test
    void sourceReplacedByDirectoryIsLeftUntouched() throws Exception {
        Path source = copyImage("20110130_131102_replaced");
        var renamer = new RemoveExifPrefixRenamer(directory.toString());
        Files.delete(source);
        Files.createDirectory(source);
        List<Path> processed = new ArrayList<>();
        assertThat(renamer.renameFiles(progress -> processed.add(progress.file()))).isEqualTo(new RenameResult(1, 1, 0, 1, 0, false));
        assertThat(processed).containsExactly(source);
        assertThat(source).isDirectory();
        assertThat(directory.resolve("replaced")).doesNotExist();
    }

    @Test
    void missingSourceDoesNotAbortRemainingMoves() throws Exception {
        Path missing = copyImage("20110130_131102_missing");
        copyImage("20110130_131102_existing");
        var renamer = new RemoveExifPrefixRenamer(directory.toString());
        Files.delete(missing);
        assertThat(renamer.renameFiles()).isEqualTo(new RenameResult(2, 2, 1, 0, 1, false));
        assertThat(directory.resolve("existing")).exists();
        assertThat(directory.resolve("missing")).doesNotExist();
    }

    private Path copyImage(String name) throws Exception {
        Path target = directory.resolve(name);
        try (var input = getClass().getResourceAsStream("/data/IMG_7559_mini.JPG")) {
            assertThat(input).isNotNull();
            Files.copy(input, target);
        }
        return target;
    }
}
