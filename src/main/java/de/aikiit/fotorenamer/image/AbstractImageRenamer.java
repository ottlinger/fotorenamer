/*
 * Copyright 2011, Aiki IT, FotoRenamer
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.aikiit.fotorenamer.image;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import de.aikiit.fotorenamer.exception.InvalidDirectoryException;
import de.aikiit.fotorenamer.exception.NoFilesFoundException;
import de.aikiit.fotorenamer.gui.ProgressBar;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static de.aikiit.fotorenamer.util.LocalizationHelper.getBundleString;
import static de.aikiit.fotorenamer.util.LocalizationHelper.getParameterizedBundleString;

/**
 * Abstract class that handles image renaming and file handling.
 * <br>
 * The onliest abstract method generates a filename from a given file
 * and should be used to provide different strategies for image renaming.
 *
 * @author hirsch
 * @version 2011-03-22, 11:43
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
abstract class AbstractImageRenamer implements Runnable {

    /**
     * The logger of this class.
     */
    private static final Logger LOG = LogManager.getLogger(AbstractImageRenamer.class);

    /**
     * The currently selected directory to work on.
     */
    private File currentDirectory = null;
    /**
     * The list of all relevant files in the current directory.
     */
    private List<File> imageList = null;

    /**
     * Progress bar for visual feedback of what's going on.
     */
    private ProgressBar progressBar = null;
    /**
     * Number of files that need processing.
     */
    private AtomicInteger amountOfFiles = new AtomicInteger(0);

    /**
     * Starts image processing on the given directory if it contains
     * relevant images. The strategy of renaming is defined by
     * subclasses implementation of @see #renameImage(File).
     *
     * @param directory Name of directory to work on.
     * @throws InvalidDirectoryException If there's a problem with
     *                                   the selected directory.
     * @throws NoFilesFoundException     If the selected directory is empty.
     */
    AbstractImageRenamer(final String directory) throws InvalidDirectoryException, NoFilesFoundException {

        if (directory == null) {
            throw new InvalidDirectoryException("null is not a directory");
        }

        this.currentDirectory = new File(directory);
        if (!this.currentDirectory.isDirectory()) {
            throw new InvalidDirectoryException(this.currentDirectory);
        }

        // retrieve relevant images in directory
        File[] files = this.currentDirectory.listFiles(new ImageFilenameFilter());
        if (files == null || files.length == 0) {
            throw new NoFilesFoundException(this.currentDirectory);
        }
        this.imageList = Lists.newArrayList(files);
        this.amountOfFiles = new AtomicInteger(this.imageList.size());
    }

    /**
     * Performs the actual/technical renaming.
     */
    private void renameFiles() {
        LOG.info("Starting to rename {} files.", this.amountOfFiles);

        Consumer<File> consumer = file -> {
            // extract EXIF data and fetch target filename
            String targetFilename = renameImage(file);

            // update progress bar (names have a different length)
            progressBar.setProgress();
            progressBar.setText(file.getName());
            progressBar.updateUI();

            // TODO add second progressbar or counter for errors
            try {
                Files.move(file.toPath(), new File(file.getParent(), targetFilename).toPath());
            } catch (IOException e) {
                LOG.error("Unable to rename '{}' to '{}'", file.getName(), targetFilename);
            }
        };
        Predicate<File> fileOnly = file -> file != null && file.isFile();

        this.imageList.parallelStream().filter(fileOnly).forEach(consumer);
    }

    /**
     * This method provides a strategy to rename image files when
     * iterating over a list of files.
     * It is called during image processing.
     *
     * @param imageFile Filename to renameFiles according to the subclass
     *                  implementation.
     * @return New filename for the given file.
     */
    protected abstract String renameImage(File imageFile);

    /**
     * Performs the renaming and updates the UI. All error handling is done in
     * other methods.
     *
     * @see #renameFiles()
     */
    public final void run() {
        this.progressBar = new ProgressBar(this.amountOfFiles.get());

        try {
            renameFiles();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, getParameterizedBundleString("fotorenamer.ui.rename.error", MoreObjects.firstNonNull(e.getMessage(), e.getClass().getSimpleName())), getBundleString("fotorenamer.ui.rename.error.title"), JOptionPane.ERROR_MESSAGE);

            this.amountOfFiles = new AtomicInteger(0);
        } finally {
            this.progressBar.dispose();
        }

        // show UI-notification
        StringBuilder notification = new StringBuilder();
        switch (this.amountOfFiles.get()) {
            case 0:
                notification.append(getParameterizedBundleString("fotorenamer.ui.rename.success.message.none", this.currentDirectory.getName()));
                break;
            case 1:
                notification.append(getParameterizedBundleString("fotorenamer.ui.rename.success.message.one", this.currentDirectory.getName()));
                break;
            default:
                notification.append(getParameterizedBundleString("fotorenamer.ui.rename.success.message", this.amountOfFiles, this.currentDirectory.getName()));
                break;
        }

        notification.append("\n\n");
        JOptionPane.showMessageDialog(null, notification.toString(), getBundleString("fotorenamer.ui.rename.success.title"), JOptionPane.INFORMATION_MESSAGE);
    }
}
