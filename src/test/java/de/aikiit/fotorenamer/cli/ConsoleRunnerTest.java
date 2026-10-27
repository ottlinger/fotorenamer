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

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(execute("--help")).isEqualTo(0);
        assertThat(out.toString()).contains("DIRECTORY", "--reverse");
        assertThat(execute("--version")).isEqualTo(0);
        assertThat(out.toString()).contains(Version.VERSION);
        assertThat(Files.exists(image)).isTrue();
        assertThat(err.toString()).isEmpty();
    }

    @Test
    void rejectsMissingDirectoryAndUnknownOptions() {
        assertThat(execute()).isEqualTo(2);
        assertThat(err.toString()).contains("DIRECTORY");
        assertThat(execute("--unknown", directory.toString())).isEqualTo(2);
        assertThat(err.toString()).contains("Unknown option");
        assertThat(execute(directory.toString(), "extra")).isEqualTo(2);
    }

    @Test
    void rejectsNonDirectoriesWithoutProcessingImages() throws Exception {
        Path image = copyImage("holiday.jpg");
        assertThat(execute(directory.resolve("missing").toString())).isEqualTo(2);
        assertThat(execute(image.toString())).isEqualTo(2);
        assertThat(err.toString()).contains("Not a directory");
        assertThat(Files.exists(image)).isTrue();
    }

    @Test
    void reportsEmptyDirectoryAsFailure() {
        assertThat(execute(directory.toString())).isEqualTo(1);
        assertThat(err.toString()).contains("No images found");
    }

    @Test
    void renamesAndSkipsExactExifPrefixesInMixedBatch() throws Exception {
        Path source = copyImage("holiday.dat");
        byte[] content = Files.readAllBytes(source);
        Path prefixed = copyImage("20110130_131102_existing.jpg");

        assertThat(execute(directory.toString())).isEqualTo(0);
        assertThat(out.toString()).contains("Processed: 2 / 2", "Renamed: 1", "Skipped: 1", "Failed: 0");
        assertThat(Files.exists(source)).isFalse();
        assertThat(Files.readAllBytes(directory.resolve("20110130_131102_holiday.dat"))).containsExactly(content);
        assertThat(Files.readAllBytes(prefixed)).containsExactly(content);
    }

    @Test
    void supportsBothReverseOptionsAndLegacyPrefixes() throws Exception {
        for (String option : new String[]{"-r", "--reverse"}) {
            Path source = copyImage("20110130_1311_holiday.jpg");
            byte[] content = Files.readAllBytes(source);
            assertThat(execute(option, directory.toString())).isEqualTo(0);
            assertThat(Files.exists(source)).isFalse();
            assertThat(Files.readAllBytes(directory.resolve("holiday.jpg"))).containsExactly(content);
            Files.delete(directory.resolve("holiday.jpg"));
        }
    }

    @Test
    void countsImagesWithoutExifAsSkipped() throws Exception {
        Path image = directory.resolve("plain.png");
        assertThat(ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", image.toFile())).isTrue();
        byte[] content = Files.readAllBytes(image);
        assertThat(execute(directory.toString())).isEqualTo(0);
        assertThat(out.toString()).contains("Skipped: 1");
        assertThat(Files.readAllBytes(image)).containsExactly(content);
    }

    @Test
    void reportsFailureWithoutOverwritingTargetsAndContinues() throws Exception {
        Path source = copyImage("holiday.jpg");
        byte[] content = Files.readAllBytes(source);
        Path target = Files.writeString(directory.resolve("20110130_131102_holiday.jpg"), "keep this");
        copyImage("other.jpg");
        assertThat(execute(directory.toString())).isEqualTo(1);
        assertThat(out.toString()).contains("Renamed: 1", "Failed: 1");
        assertThat(Files.readAllBytes(source)).containsExactly(content);
        assertThat(Files.readString(target)).isEqualTo("keep this");
        assertThat(Files.exists(directory.resolve("20110130_131102_other.jpg"))).isTrue();
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
