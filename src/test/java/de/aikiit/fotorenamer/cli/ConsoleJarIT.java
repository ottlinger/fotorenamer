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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;

import static org.assertj.core.api.Assertions.assertThat;

/** Tests the packaged artifact in a separate headless JVM with mvn -Pcli verify. */
class ConsoleJarIT {
    @TempDir
    Path directory;

    @Test
    void preservesGuiEntryPointAndExcludesSplashFromCliManifest() throws Exception {
        try (JarFile cli = new JarFile(System.getProperty("cli.jar"));
             JarFile gui = new JarFile(System.getProperty("gui.jar"))) {
            var cliAttributes = cli.getManifest().getMainAttributes();
            assertThat(cliAttributes.getValue("Main-Class")).isEqualTo(ConsoleRunner.class.getName());
            assertThat(cliAttributes.getValue("SplashScreen-Image")).isNull();
            var guiAttributes = gui.getManifest().getMainAttributes();
            assertThat(guiAttributes.getValue("Main-Class")).isEqualTo("de.aikiit.fotorenamer.ApplicationStarter");
            String splashPath = guiAttributes.getValue("SplashScreen-Image");
            assertThat(splashPath).isEqualTo("image/splash-camera.jpg");
            var splashEntry = gui.getJarEntry(splashPath);
            assertThat(splashEntry).isNotNull();
            try (var input = gui.getInputStream(splashEntry)) {
                var image = ImageIO.read(input);
                assertThat(image).isNotNull();
                assertThat(image.getWidth()).isEqualTo(400);
                assertThat(image.getHeight()).isEqualTo(600);
            }
        }
    }

    @Test
    void supportsHelpAndUsageErrorsHeadlessly() throws Exception {
        assertThat(run(0, "--help")).contains("Usage: fotorenamer");
        assertThat(run(2)).contains("DIRECTORY");
        assertThat(run(1, directory.toString())).contains("No images found");
    }

    @Test
    void renamesSkipsAndReversesInHeadlessJvm() throws Exception {
        Path image = directory.resolve("holiday.dat");
        try (var input = getClass().getResourceAsStream("/data/IMG_7559_mini.JPG")) {
            assertThat(input).isNotNull();
            Files.copy(input, image);
        }
        byte[] content = Files.readAllBytes(image);
        assertThat(run(0, directory.toString())).contains("Renamed: 1");
        Path target = directory.resolve("20110130_131102_holiday.dat");
        assertThat(Files.readAllBytes(target)).containsExactly(content);
        assertThat(run(0, directory.toString())).contains("Skipped: 1");
        assertThat(run(0, "-r", directory.toString())).contains("Renamed: 1");
        assertThat(Files.readAllBytes(image)).containsExactly(content);
        assertThat(target).doesNotExist();
    }

    private String run(int expectedExitCode, String... args) throws Exception {
        String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        List<String> command = new ArrayList<>(List.of(
                Path.of(System.getProperty("java.home"), "bin", executable).toString(),
                "-Djava.awt.headless=true", "-jar", System.getProperty("cli.jar")));
        command.addAll(List.of(args));
        Path output = Files.createTempFile(directory, "cli-output-", ".txt");
        Process process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(output.toFile()).start();
        try {
            assertThat(process.waitFor(30, TimeUnit.SECONDS)).as("CLI process timed out").isTrue();
            String text = Files.readString(output);
            assertThat(process.exitValue()).as(text).isEqualTo(expectedExitCode);
            return text;
        } finally {
            process.destroyForcibly();
        }
    }
}
