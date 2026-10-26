/*
 * Copyright 2011-2026, Aiki IT, FotoRenamer
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

import de.aikiit.fotorenamer.exception.InvalidDirectoryException;
import de.aikiit.fotorenamer.exception.NoFilesFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.stream.Stream;

/** Shared, UI-independent NIO image discovery and renaming. */
abstract class AbstractImageRenamer implements ImageRenamer {

    private static final Logger LOG = LogManager.getLogger(AbstractImageRenamer.class);
    private final List<Path> imageList;

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
        checkInterrupted();
        Path currentDirectory;
        try {
            currentDirectory = Path.of(directory).toAbsolutePath();
            try (Stream<Path> entries = Files.list(currentDirectory)) {
                this.imageList = entries.peek(file -> checkInterrupted())
                        .filter(new ImageContentFilter()).toList();
            }
        } catch (IOException | UncheckedIOException | IllegalArgumentException | SecurityException e) {
            throw new InvalidDirectoryException(directory, e);
        }
        if (this.imageList.isEmpty()) {
            throw new NoFilesFoundException(currentDirectory);
        }
    }

    /**
     * Processes a snapshot of detected candidates. Callbacks run on the calling thread.
     * Observer failures are logged separately and do not change file outcome counts.
     * Cancellation is cooperative: already completed moves are preserved.
     */
    @Override
    public final RenameResult renameFiles(final Consumer<RenameProgress> onProgress,
                                          final BooleanSupplier cancellationRequested) {
        Objects.requireNonNull(onProgress, "onProgress");
        Objects.requireNonNull(cancellationRequested, "cancellationRequested");
        int total = this.imageList.size();
        int processed = 0;
        int renamed = 0;
        int skipped = 0;
        int failed = 0;
        LOG.info("Starting to process {} image files.", total);
        for (Path source : this.imageList) {
            if (Thread.currentThread().isInterrupted() || cancellationRequested.getAsBoolean()) {
                return new RenameResult(total, processed, renamed, skipped, failed, true);
            }
            Path target = source;
            try {
                if (!Files.readAttributes(source, BasicFileAttributes.class).isRegularFile()) {
                    LOG.info("Skipping '{}': no longer a regular file", source);
                    skipped++;
                } else {
                    String targetFilename = renameImage(source);
                    if (targetFilename == null || targetFilename.isEmpty()
                            || targetFilename.equals(fileName(source))) {
                        skipped++;
                    } else {
                        target = source.resolveSibling(targetFilename);
                        // No REPLACE_EXISTING or ATOMIC_MOVE: preserve existing targets.
                        Files.move(source, target);
                        renamed++;
                    }
                }
            } catch (CancellationException e) {
                LOG.info("Processing cancelled at '{}'", source, e);
                return new RenameResult(total, processed, renamed, skipped, failed, true);
            } catch (IOException | RuntimeException e) {
                LOG.error("Unable to process '{}' with target '{}'", source, target, e);
                if (Thread.currentThread().isInterrupted()) {
                    return new RenameResult(total, processed, renamed, skipped, failed, true);
                }
                failed++;
            }
            processed++;
            RenameResult snapshot = new RenameResult(total, processed, renamed, skipped, failed, false);
            try {
                onProgress.accept(new RenameProgress(source, snapshot));
            } catch (RuntimeException e) {
                LOG.warn("Progress observer failed for '{}'", source, e);
            }
        }
        return new RenameResult(total, processed, renamed, skipped, failed, false);
    }

    /** Determines a filename; return the original name or an empty name to skip. */
    protected abstract String renameImage(Path imageFile) throws IOException;

    /** Returns the final path component, or the path itself for a filesystem root. */
    protected static String fileName(final Path path) {
        Path name = path.getFileName();
        return name == null ? path.toString() : name.toString();
    }

    private static void checkInterrupted() {
        if (Thread.currentThread().isInterrupted()) {
            throw new CancellationException("Image discovery interrupted");
        }
    }
}
