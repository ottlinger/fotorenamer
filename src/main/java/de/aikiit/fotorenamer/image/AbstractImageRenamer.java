/*
 * Copyright 2011-2026, Aiki IT, FotoRenamer
 *
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
package de.aikiit.fotorenamer.image;

import com.google.common.base.MoreObjects;
import de.aikiit.fotorenamer.exception.InvalidDirectoryException;
import de.aikiit.fotorenamer.exception.NoFilesFoundException;
import de.aikiit.fotorenamer.gui.ProgressBar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static de.aikiit.fotorenamer.util.LocalizationHelper.getBundleString;
import static de.aikiit.fotorenamer.util.LocalizationHelper.getParameterizedBundleString;

/**
 * Shared NIO image discovery and renaming for both renaming strategies.
 * Files are never replaced, and individual processing errors do not stop the batch.
 */
abstract class AbstractImageRenamer implements Runnable {

    private static final Logger LOG = LogManager.getLogger(AbstractImageRenamer.class);

    private final Path currentDirectory;
    private final List<Path> imageList;
    private ProgressBar progressBar;

    /**
     * Finds image candidates without starting a worker or creating GUI components.
     *
     * @param directory Directory containing the images.
     * @throws InvalidDirectoryException If the directory cannot be opened or enumerated.
     * @throws NoFilesFoundException If the directory contains no detected images.
     */
    AbstractImageRenamer(final String directory) throws InvalidDirectoryException, NoFilesFoundException {
        if (directory == null) {
            throw new InvalidDirectoryException("null is not a directory");
        }
        try {
            this.currentDirectory = Path.of(directory).toAbsolutePath();
            // Collect before processing, and always close the directory stream.
            try (Stream<Path> entries = Files.list(this.currentDirectory)) {
                this.imageList = entries.filter(new ImageContentFilter()).toList();
            }
        } catch (IOException | UncheckedIOException | IllegalArgumentException | SecurityException e) {
            throw new InvalidDirectoryException(directory, e);
        }
        if (this.imageList.isEmpty()) {
            throw new NoFilesFoundException(this.currentDirectory);
        }
    }

    /**
     * Processes the detected images independently of the GUI.
     *
     * @param onProcessed Callback for progress reporting after each image.
     * @return Number of successfully renamed files.
     */
    final int renameFiles(final Consumer<Path> onProcessed) {
        LOG.info("Starting to process {} image files.", this.imageList.size());
        int renamed = 0;
        for (Path source : this.imageList) {
            Path target = source;
            try {
                if (!Files.readAttributes(source, BasicFileAttributes.class).isRegularFile()) {
                    LOG.info("Skipping '{}': no longer a regular file", source);
                } else {
                    String targetFilename = renameImage(source);
                    if (targetFilename != null && !targetFilename.isEmpty()
                            && !targetFilename.equals(fileName(source))) {
                        target = source.resolveSibling(targetFilename);
                        // No REPLACE_EXISTING or ATOMIC_MOVE: existing targets must be preserved.
                        Files.move(source, target);
                        renamed++;
                    }
                }
            } catch (IOException | IllegalArgumentException | SecurityException e) {
                LOG.error("Unable to rename '{}' to '{}'", source, target, e);
            }
            onProcessed.accept(source);
        }
        return renamed;
    }

    /**
     * Determines a new filename; return the original name or an empty name to skip.
     *
     * @param imageFile Image to inspect.
     * @return Target filename, without its directory.
     */
    protected abstract String renameImage(Path imageFile);

    /** Returns the final path component, or the path itself for a filesystem root. */
    protected static String fileName(final Path path) {
        Path name = path.getFileName();
        return name == null ? path.toString() : name.toString();
    }

    /** @return Localization key for the strategy's result dialog title. */
    protected String successTitleKey() {
        return "fotorenamer.ui.rename.success.title";
    }

    /** Performs renaming with progress feedback and a result dialog. */
    @Override
    public final void run() {
        this.progressBar = new ProgressBar(this.imageList.size());
        int renamedFiles = 0;
        try {
            renamedFiles = renameFiles(file -> {
                progressBar.setProgress();
                progressBar.setText(fileName(file));
                progressBar.updateUI();
            });
        } catch (Exception e) {
            LOG.error("Unable to process images in '{}'", this.currentDirectory, e);
            JOptionPane.showMessageDialog(null, getParameterizedBundleString("fotorenamer.ui.rename.error", MoreObjects.firstNonNull(e.getMessage(), e.getClass().getSimpleName())), getBundleString("fotorenamer.ui.rename.error.title"), JOptionPane.ERROR_MESSAGE);
        } finally {
            this.progressBar.dispose();
        }

        String directoryName = fileName(this.currentDirectory);
        String notification = switch (renamedFiles) {
            case 0 -> getParameterizedBundleString("fotorenamer.ui.rename.success.message.none", directoryName);
            case 1 -> getParameterizedBundleString("fotorenamer.ui.rename.success.message.one", directoryName);
            default -> getParameterizedBundleString("fotorenamer.ui.rename.success.message", renamedFiles, directoryName);
        };
        JOptionPane.showMessageDialog(null, notification + "\n\n", getBundleString(successTitleKey()), JOptionPane.INFORMATION_MESSAGE);
    }
}
