/*
Copyright 2011-2026, Aiki IT, FotoRenamer
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package de.aikiit.fotorenamer.image;

import de.aikiit.fotorenamer.exception.InvalidDirectoryException;
import de.aikiit.fotorenamer.exception.NoFilesFoundException;
import org.apache.commons.imaging.formats.tiff.TiffImageParser;
import org.apache.commons.imaging.formats.tiff.TiffImagingParameters;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

class CreationDateFromExifImageRenamerTest {

    @TempDir
    Path directory;

    @Test
    void checkNPECorrectnessInConstructor() {
        assertThrows(InvalidDirectoryException.class, () -> new CreationDateFromExifImageRenamer(null));
    }

    @Test
    void rejectsDirectoryContainingOnlyNonImages() throws Exception {
        Files.writeString(directory.resolve("notes.jpg"), "not an image");
        assertThrows(NoFilesFoundException.class,
                () -> new CreationDateFromExifImageRenamer(directory.toString()));
        assertEquals("not an image", Files.readString(directory.resolve("notes.jpg")));
    }

    @Test
    void renamesOnlyImagesWithExifAndPreservesAllFileContents() throws Exception {
        Path image = copyExifImage("holiday.dat");
        byte[] originalImage = Files.readAllBytes(image);
        Path png = writeImageWithoutExif("no-exif.bin", "png");
        byte[] originalPng = Files.readAllBytes(png);
        Path jpeg = writeImageWithoutExif("no-exif", "jpeg");
        byte[] originalJpeg = Files.readAllBytes(jpeg);
        Path text = Files.writeString(directory.resolve("notes.jpg"), "not an image");

        CreationDateFromExifImageRenamer renamer = new CreationDateFromExifImageRenamer(directory.toString());
        assertEquals("no-exif.bin", renamer.renameImage(png));
        assertEquals("no-exif", renamer.renameImage(jpeg));
        assertEquals(new RenameResult(3, 3, 1, 2, 0, false), renamer.renameFiles());

        assertFalse(Files.exists(image));
        assertArrayEquals(originalImage, Files.readAllBytes(directory.resolve("20110130_131102_holiday.dat")));
        assertArrayEquals(originalPng, Files.readAllBytes(png));
        assertArrayEquals(originalJpeg, Files.readAllBytes(jpeg));
        assertEquals("not an image", Files.readString(text));
        try (var files = Files.list(directory)) {
            assertEquals(4, files.count());
        }
    }

    @Test
    void skipsExactExifPrefixesAndReportsMixedBatchProgress() throws Exception {
        Path prefixed = copyExifImage("20110130_131102_holiday.JPG");
        byte[] original = Files.readAllBytes(prefixed);
        copyExifImage("new.dat");
        List<RenameProgress> progress = new CopyOnWriteArrayList<>();
        var renamer = new CreationDateFromExifImageRenamer(directory.toString());

        assertEquals(prefixed.getFileName().toString(), renamer.renameImage(prefixed));
        RenameResult result = renamer.renameFiles(progress::add);

        assertEquals(new RenameResult(2, 2, 1, 1, 0, false), result);
        assertEquals(2, progress.size());
        assertEquals(result, progress.get(progress.size() - 1).result());
        assertArrayEquals(original, Files.readAllBytes(prefixed));
        assertArrayEquals(original, Files.readAllBytes(directory.resolve("20110130_131102_new.dat")));
        assertFalse(Files.exists(directory.resolve("20110130_131102_20110130_131102_holiday.JPG")));
    }

    @Test
    void secondRunSkipsPreviouslyRenamedImage() throws Exception {
        Path image = copyExifImage("holiday");
        byte[] original = Files.readAllBytes(image);
        var firstRun = new CreationDateFromExifImageRenamer(directory.toString());
        assertEquals(new RenameResult(1, 1, 1, 0, 0, false), firstRun.renameFiles());

        var secondRun = new CreationDateFromExifImageRenamer(directory.toString());
        assertEquals(new RenameResult(1, 1, 0, 1, 0, false), secondRun.renameFiles());
        assertArrayEquals(original, Files.readAllBytes(directory.resolve("20110130_131102_holiday")));
        try (var files = Files.list(directory)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    void onlySkipsAnExactLeadingExifPrefix() throws Exception {
        List<String> filenames = List.of(
                "20110130_131103_holiday.jpg",
                "20110131_131102_holiday.jpg",
                "20110130_1311_holiday.jpg",
                "holiday_20110130_131102.jpg",
                "20110130_131102holiday.jpg");
        for (String filename : filenames) {
            Path caseDirectory = Files.createTempDirectory(directory, "case-");
            Path image = copyExifImage(caseDirectory.getFileName() + "/" + filename);
            byte[] original = Files.readAllBytes(image);
            String expected = "20110130_131102_" + filename;
            var renamer = new CreationDateFromExifImageRenamer(caseDirectory.toString());

            assertEquals(expected, renamer.renameImage(image), filename);
            assertEquals(new RenameResult(1, 1, 1, 0, 0, false), renamer.renameFiles(), filename);
            assertFalse(Files.exists(image), filename);
            assertArrayEquals(original, Files.readAllBytes(caseDirectory.resolve(expected)), filename);
        }
    }

    @Test
    void renamesExifImageWithoutAnExtension() throws Exception {
        Path image = copyExifImage("holiday");
        byte[] original = Files.readAllBytes(image);
        CreationDateFromExifImageRenamer renamer = new CreationDateFromExifImageRenamer(directory.toString());
        assertEquals(1, renamer.renameFiles().renamed());
        assertArrayEquals(original, Files.readAllBytes(directory.resolve("20110130_131102_holiday")));
    }

    @Test
    void renamesTiffWithExifWithoutRelyingOnItsExtension() throws Exception {
        Path image = writeExifTiff("scan.bin", "2026:10:26 14:30:12");
        byte[] original = Files.readAllBytes(image);
        CreationDateFromExifImageRenamer renamer = new CreationDateFromExifImageRenamer(directory.toString());
        assertEquals(1, renamer.renameFiles().renamed());
        assertFalse(Files.exists(image));
        assertArrayEquals(original, Files.readAllBytes(directory.resolve("20261026_143012_scan.bin")));
    }

    @Test
    void leavesImageWithInvalidExifDateUnchanged() throws Exception {
        Path image = writeExifTiff("scan", "0000:00:00 00:00:00");
        byte[] original = Files.readAllBytes(image);
        CreationDateFromExifImageRenamer renamer = new CreationDateFromExifImageRenamer(directory.toString());
        assertEquals("scan", renamer.renameImage(image));
        assertEquals(new RenameResult(1, 1, 0, 1, 0, false), renamer.renameFiles());
        assertArrayEquals(original, Files.readAllBytes(image));
    }

    @Test
    void skipsBrokenImagesAndContinuesWithOtherImages() throws Exception {
        Path valid = copyExifImage("valid");
        byte[] truncated = Arrays.copyOf(Files.readAllBytes(valid), 20);
        Path broken = Files.write(directory.resolve("broken.jpg"), truncated);
        assertTrue(new ImageContentFilter().test(broken));

        CreationDateFromExifImageRenamer renamer = new CreationDateFromExifImageRenamer(directory.toString());
        assertEquals(new RenameResult(2, 2, 1, 0, 1, false), renamer.renameFiles());
        assertArrayEquals(truncated, Files.readAllBytes(broken));
        assertTrue(Files.exists(directory.resolve("20110130_131102_valid")));
    }

    @Test
    void skipsUnsupportedImageFormatsAndContinuesWithOtherImages() throws Exception {
        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"1\" height=\"1\"></svg>";
        Path image = Files.writeString(directory.resolve("drawing"), svg);
        assertTrue(new ImageContentFilter().test(image));
        copyExifImage("valid");
        CreationDateFromExifImageRenamer renamer = new CreationDateFromExifImageRenamer(directory.toString());
        assertEquals(new RenameResult(2, 2, 1, 0, 1, false), renamer.renameFiles());
        assertEquals(svg, Files.readString(image));
        assertTrue(Files.exists(directory.resolve("20110130_131102_valid")));
    }

    @Test
    void neverOverwritesExistingTargetsAndLogsTheCause() throws Exception {
        Path image = copyExifImage("holiday.dat");
        byte[] original = Files.readAllBytes(image);
        Path target = Files.writeString(directory.resolve("20110130_131102_holiday.dat"), "keep this file");
        CreationDateFromExifImageRenamer renamer = new CreationDateFromExifImageRenamer(directory.toString());
        Logger logger = (Logger) LogManager.getLogger(AbstractImageRenamer.class);
        List<LogEvent> events = new CopyOnWriteArrayList<>();
        AbstractAppender appender = new AbstractAppender("rename-errors", null, null, false, Property.EMPTY_ARRAY) {
            @Override
            public void append(LogEvent event) {
                events.add(event.toImmutable());
            }
        };
        appender.start();
        logger.addAppender(appender);
        try {
            assertEquals(new RenameResult(1, 1, 0, 0, 1, false), renamer.renameFiles());
            assertTrue(events.stream().anyMatch(event -> event.getLevel() == Level.ERROR
                    && event.getThrown() != null
                    && event.getMessage().getFormattedMessage().contains(image.toAbsolutePath().toString())));
        } finally {
            logger.removeAppender(appender);
            appender.stop();
        }
        assertArrayEquals(original, Files.readAllBytes(image));
        assertEquals("keep this file", Files.readString(target));
    }

    private Path copyExifImage(String name) throws Exception {
        Path target = directory.resolve(name);
        try (var input = getClass().getResourceAsStream("/data/IMG_7559_mini.JPG")) {
            assertNotNull(input);
            Files.copy(input, target);
        }
        return target;
    }

    private Path writeExifTiff(String name, String date) throws Exception {
        Path image = directory.resolve(name);
        TiffOutputSet outputSet = new TiffOutputSet();
        outputSet.getOrCreateExifDirectory().add(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, date);
        try (var output = Files.newOutputStream(image)) {
            new TiffImageParser().writeImage(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), output,
                    new TiffImagingParameters().setOutputSet(outputSet));
        }
        return image;
    }

    private Path writeImageWithoutExif(String name, String format) throws Exception {
        Path image = directory.resolve(name);
        assertTrue(ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), format, image.toFile()));
        return image;
    }
}
