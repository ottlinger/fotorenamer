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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreationDateFromExifImageRenamerTest {

    @TempDir
    Path directory;

    @Test
    void checkNPECorrectnessInConstructor() {
        assertThatThrownBy(() -> new CreationDateFromExifImageRenamer(null)).isInstanceOf(InvalidDirectoryException.class);
    }

    @Test
    void rejectsDirectoryContainingOnlyNonImages() throws Exception {
        Files.writeString(directory.resolve("notes.jpg"), "not an image");
        assertThatThrownBy(() -> new CreationDateFromExifImageRenamer(directory.toString())).isInstanceOf(NoFilesFoundException.class);
        assertThat(Files.readString(directory.resolve("notes.jpg"))).isEqualTo("not an image");
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
        assertThat(renamer.renameImage(png)).isEqualTo("no-exif.bin");
        assertThat(renamer.renameImage(jpeg)).isEqualTo("no-exif");
        assertThat(renamer.renameFiles()).isEqualTo(new RenameResult(3, 3, 1, 2, 0, false));

        assertThat(Files.exists(image)).isFalse();
        assertThat(Files.readAllBytes(directory.resolve("20110130_131102_holiday.dat"))).containsExactly(originalImage);
        assertThat(Files.readAllBytes(png)).containsExactly(originalPng);
        assertThat(Files.readAllBytes(jpeg)).containsExactly(originalJpeg);
        assertThat(Files.readString(text)).isEqualTo("not an image");
        try (var files = Files.list(directory)) {
            assertThat(files.count()).isEqualTo(4);
        }
    }

    @Test
    void skipsExactExifPrefixesAndReportsMixedBatchProgress() throws Exception {
        Path prefixed = copyExifImage("20110130_131102_holiday.JPG");
        byte[] original = Files.readAllBytes(prefixed);
        copyExifImage("new.dat");
        List<RenameProgress> progress = new CopyOnWriteArrayList<>();
        var renamer = new CreationDateFromExifImageRenamer(directory.toString());

        assertThat(renamer.renameImage(prefixed)).isEqualTo(prefixed.getFileName().toString());
        RenameResult result = renamer.renameFiles(progress::add);

        assertThat(result).isEqualTo(new RenameResult(2, 2, 1, 1, 0, false));
        assertThat(progress).hasSize(2);
        assertThat(progress.get(progress.size() - 1).result()).isEqualTo(result);
        assertThat(Files.readAllBytes(prefixed)).containsExactly(original);
        assertThat(Files.readAllBytes(directory.resolve("20110130_131102_new.dat"))).containsExactly(original);
        assertThat(Files.exists(directory.resolve("20110130_131102_20110130_131102_holiday.JPG"))).isFalse();
    }

    @Test
    void secondRunSkipsPreviouslyRenamedImage() throws Exception {
        Path image = copyExifImage("holiday");
        byte[] original = Files.readAllBytes(image);
        var firstRun = new CreationDateFromExifImageRenamer(directory.toString());
        assertThat(firstRun.renameFiles()).isEqualTo(new RenameResult(1, 1, 1, 0, 0, false));

        var secondRun = new CreationDateFromExifImageRenamer(directory.toString());
        assertThat(secondRun.renameFiles()).isEqualTo(new RenameResult(1, 1, 0, 1, 0, false));
        assertThat(Files.readAllBytes(directory.resolve("20110130_131102_holiday"))).containsExactly(original);
        try (var files = Files.list(directory)) {
            assertThat(files.count()).isEqualTo(1);
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

            assertThat(renamer.renameImage(image)).as(filename).isEqualTo(expected);
            assertThat(renamer.renameFiles()).as(filename).isEqualTo(new RenameResult(1, 1, 1, 0, 0, false));
            assertThat(Files.exists(image)).as(filename).isFalse();
            assertThat(Files.readAllBytes(caseDirectory.resolve(expected))).as(filename).containsExactly(original);
        }
    }

    @Test
    void renamesExifImageWithoutAnExtension() throws Exception {
        Path image = copyExifImage("holiday");
        byte[] original = Files.readAllBytes(image);
        CreationDateFromExifImageRenamer renamer = new CreationDateFromExifImageRenamer(directory.toString());
        assertThat(renamer.renameFiles().renamed()).isEqualTo(1);
        assertThat(Files.readAllBytes(directory.resolve("20110130_131102_holiday"))).containsExactly(original);
    }

    @Test
    void renamesTiffWithExifWithoutRelyingOnItsExtension() throws Exception {
        Path image = writeExifTiff("scan.bin", "2026:10:26 14:30:12");
        byte[] original = Files.readAllBytes(image);
        CreationDateFromExifImageRenamer renamer = new CreationDateFromExifImageRenamer(directory.toString());
        assertThat(renamer.renameFiles().renamed()).isEqualTo(1);
        assertThat(Files.exists(image)).isFalse();
        assertThat(Files.readAllBytes(directory.resolve("20261026_143012_scan.bin"))).containsExactly(original);
    }

    @Test
    void leavesImageWithInvalidExifDateUnchanged() throws Exception {
        Path image = writeExifTiff("scan", "0000:00:00 00:00:00");
        byte[] original = Files.readAllBytes(image);
        CreationDateFromExifImageRenamer renamer = new CreationDateFromExifImageRenamer(directory.toString());
        assertThat(renamer.renameImage(image)).isEqualTo("scan");
        assertThat(renamer.renameFiles()).isEqualTo(new RenameResult(1, 1, 0, 1, 0, false));
        assertThat(Files.readAllBytes(image)).containsExactly(original);
    }

    @Test
    void skipsBrokenImagesAndContinuesWithOtherImages() throws Exception {
        Path valid = copyExifImage("valid");
        byte[] truncated = Arrays.copyOf(Files.readAllBytes(valid), 20);
        Path broken = Files.write(directory.resolve("broken.jpg"), truncated);
        assertThat(new ImageContentFilter().test(broken)).isTrue();

        CreationDateFromExifImageRenamer renamer = new CreationDateFromExifImageRenamer(directory.toString());
        assertThat(renamer.renameFiles()).isEqualTo(new RenameResult(2, 2, 1, 0, 1, false));
        assertThat(Files.readAllBytes(broken)).containsExactly(truncated);
        assertThat(Files.exists(directory.resolve("20110130_131102_valid"))).isTrue();
    }

    @Test
    void skipsUnsupportedImageFormatsAndContinuesWithOtherImages() throws Exception {
        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"1\" height=\"1\"></svg>";
        Path image = Files.writeString(directory.resolve("drawing"), svg);
        assertThat(new ImageContentFilter().test(image)).isTrue();
        copyExifImage("valid");
        CreationDateFromExifImageRenamer renamer = new CreationDateFromExifImageRenamer(directory.toString());
        assertThat(renamer.renameFiles()).isEqualTo(new RenameResult(2, 2, 1, 0, 1, false));
        assertThat(Files.readString(image)).isEqualTo(svg);
        assertThat(Files.exists(directory.resolve("20110130_131102_valid"))).isTrue();
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
            assertThat(renamer.renameFiles()).isEqualTo(new RenameResult(1, 1, 0, 0, 1, false));
            assertThat(events.stream().anyMatch(event -> event.getLevel() == Level.ERROR
                    && event.getThrown() != null
                    && event.getMessage().getFormattedMessage().contains(image.toAbsolutePath().toString()))).isTrue();
        } finally {
            logger.removeAppender(appender);
            appender.stop();
        }
        assertThat(Files.readAllBytes(image)).containsExactly(original);
        assertThat(Files.readString(target)).isEqualTo("keep this file");
    }

    private Path copyExifImage(String name) throws Exception {
        Path target = directory.resolve(name);
        try (var input = getClass().getResourceAsStream("/data/IMG_7559_mini.JPG")) {
            assertThat(input).isNotNull();
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
        assertThat(ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), format, image.toFile())).isTrue();
        return image;
    }
}
