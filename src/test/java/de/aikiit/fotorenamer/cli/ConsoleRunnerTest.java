/*
 * Copyright 2026, Aiki IT, FotoRenamer
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.aikiit.fotorenamer.cli;

import de.aikiit.fotorenamer.util.Version;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleRunnerTest {
    @TempDir
    Path directory;

    private final StringWriter out = new StringWriter();
    private final StringWriter err = new StringWriter();

    private int execute(String... args) {
        return new CommandLine(new ConsoleRunner())
                .setOut(new PrintWriter(out, true))
                .setErr(new PrintWriter(err, true))
                .execute(args);
    }

    @Test
    void helpAndVersionDoNotRequireDirectoryOrProcessImages() throws Exception {
        Path image = copyImage("holiday.jpg");
        assertEquals(0, execute("--help"));
        assertTrue(out.toString().contains("DIRECTORY"));
        assertTrue(out.toString().contains("--reverse"));
        assertEquals(0, execute("--version"));
        assertTrue(out.toString().contains(Version.VERSION));
        assertTrue(Files.exists(image));
        assertEquals("", err.toString());
    }

    @Test
    void rejectsMissingDirectoryAndUnknownOptions() {
        assertEquals(2, execute());
        assertTrue(err.toString().contains("DIRECTORY"));
        assertEquals(2, execute("--unknown", directory.toString()));
        assertTrue(err.toString().contains("Unknown option"));
        assertEquals(2, execute(directory.toString(), "extra"));
    }

    @Test
    void rejectsNonDirectoriesWithoutProcessingImages() throws Exception {
        Path image = copyImage("holiday.jpg");
        assertEquals(2, execute(directory.resolve("missing").toString()));
        assertEquals(2, execute(image.toString()));
        assertTrue(err.toString().contains("Not a directory"));
        assertTrue(Files.exists(image));
    }

    @Test
    void reportsEmptyDirectoryAsFailure() {
        assertEquals(1, execute(directory.toString()));
        assertTrue(err.toString().contains("No images found"));
    }

    @Test
    void renamesAndSkipsExactExifPrefixesInMixedBatch() throws Exception {
        Path source = copyImage("holiday.dat");
        byte[] content = Files.readAllBytes(source);
        Path prefixed = copyImage("20110130_131102_existing.jpg");

        assertEquals(0, execute(directory.toString()));
        assertTrue(out.toString().contains("Processed: 2 / 2"));
        assertTrue(out.toString().contains("Renamed: 1"));
        assertTrue(out.toString().contains("Skipped: 1"));
        assertTrue(out.toString().contains("Failed: 0"));
        assertFalse(Files.exists(source));
        assertArrayEquals(content, Files.readAllBytes(directory.resolve("20110130_131102_holiday.dat")));
        assertArrayEquals(content, Files.readAllBytes(prefixed));
    }

    @Test
    void supportsBothReverseOptionsAndLegacyPrefixes() throws Exception {
        for (String option : new String[]{"-r", "--reverse"}) {
            Path source = copyImage("20110130_1311_holiday.jpg");
            byte[] content = Files.readAllBytes(source);
            assertEquals(0, execute(option, directory.toString()));
            assertFalse(Files.exists(source));
            assertArrayEquals(content, Files.readAllBytes(directory.resolve("holiday.jpg")));
            Files.delete(directory.resolve("holiday.jpg"));
        }
    }

    @Test
    void countsImagesWithoutExifAsSkipped() throws Exception {
        Path image = directory.resolve("plain.png");
        assertTrue(ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", image.toFile()));
        byte[] content = Files.readAllBytes(image);
        assertEquals(0, execute(directory.toString()));
        assertTrue(out.toString().contains("Skipped: 1"));
        assertArrayEquals(content, Files.readAllBytes(image));
    }

    @Test
    void reportsFailureWithoutOverwritingTargetsAndContinues() throws Exception {
        Path source = copyImage("holiday.jpg");
        byte[] content = Files.readAllBytes(source);
        Path target = Files.writeString(directory.resolve("20110130_131102_holiday.jpg"), "keep this");
        copyImage("other.jpg");
        assertEquals(1, execute(directory.toString()));
        assertTrue(out.toString().contains("Renamed: 1"));
        assertTrue(out.toString().contains("Failed: 1"));
        assertArrayEquals(content, Files.readAllBytes(source));
        assertEquals("keep this", Files.readString(target));
        assertTrue(Files.exists(directory.resolve("20110130_131102_other.jpg")));
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
