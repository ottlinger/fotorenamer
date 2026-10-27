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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests the packaged artifact in a separate headless JVM with mvn -Pcli verify. */
class ConsoleJarIT {
    @TempDir
    Path directory;

    @Test
    void preservesGuiEntryPointAndExcludesSplashFromCliManifest() throws Exception {
        try (JarFile cli = new JarFile(System.getProperty("cli.jar"));
             JarFile gui = new JarFile(System.getProperty("gui.jar"))) {
            var cliAttributes = cli.getManifest().getMainAttributes();
            assertEquals(ConsoleRunner.class.getName(), cliAttributes.getValue("Main-Class"));
            assertNull(cliAttributes.getValue("SplashScreen-Image"));
            var guiAttributes = gui.getManifest().getMainAttributes();
            assertEquals("de.aikiit.fotorenamer.ApplicationStarter", guiAttributes.getValue("Main-Class"));
            String splashPath = guiAttributes.getValue("SplashScreen-Image");
            assertEquals("image/splash-camera.jpg", splashPath);
            var splashEntry = gui.getJarEntry(splashPath);
            assertNotNull(splashEntry);
            try (var input = gui.getInputStream(splashEntry)) {
                var image = ImageIO.read(input);
                assertNotNull(image);
                assertEquals(400, image.getWidth());
                assertEquals(600, image.getHeight());
            }
        }
    }

    @Test
    void supportsHelpAndUsageErrorsHeadlessly() throws Exception {
        assertTrue(run(0, "--help").contains("Usage: fotorenamer"));
        assertTrue(run(2).contains("DIRECTORY"));
        assertTrue(run(1, directory.toString()).contains("No images found"));
    }

    @Test
    void renamesSkipsAndReversesInHeadlessJvm() throws Exception {
        Path image = directory.resolve("holiday.dat");
        try (var input = getClass().getResourceAsStream("/data/IMG_7559_mini.JPG")) {
            assertNotNull(input);
            Files.copy(input, image);
        }
        byte[] content = Files.readAllBytes(image);
        assertTrue(run(0, directory.toString()).contains("Renamed: 1"));
        Path target = directory.resolve("20110130_131102_holiday.dat");
        assertArrayEquals(content, Files.readAllBytes(target));
        assertTrue(run(0, directory.toString()).contains("Skipped: 1"));
        assertTrue(run(0, "-r", directory.toString()).contains("Renamed: 1"));
        assertArrayEquals(content, Files.readAllBytes(image));
        assertFalse(Files.exists(target));
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
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "CLI process timed out");
            String text = Files.readString(output);
            assertEquals(expectedExitCode, process.exitValue(), text);
            return text;
        } finally {
            process.destroyForcibly();
        }
    }
}
