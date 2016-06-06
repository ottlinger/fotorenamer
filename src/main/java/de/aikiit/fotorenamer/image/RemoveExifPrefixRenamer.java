/**
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

import com.google.common.collect.Lists;
import de.aikiit.fotorenamer.exception.InvalidDirectoryException;
import de.aikiit.fotorenamer.exception.NoFilesFoundException;
import de.aikiit.fotorenamer.exception.RenamingErrorException;
import de.aikiit.fotorenamer.gui.ProgressBar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static de.aikiit.fotorenamer.util.LocalizationHelper.getBundleString;
import static de.aikiit.fotorenamer.util.LocalizationHelper.getParameterizedBundleString;

/**
 * This class rerenames files in order to be able to play them back onto a camera
 * device that is not able to deal with long filenames.
 *
 * @author hirsch, 08.12.2003
 * @version 2004-01-08
 */
public class RemoveExifPrefixRenamer implements Runnable {
    private static final Logger LOG =
            LogManager.getLogger(RemoveExifPrefixRenamer.class);
    /**
     * Pattern applied to find already renamed image files.
     * Should match: 20110507_180520_IMG_8192small.JPG
     */
    private static final String REPLACE_PATTERN = "\\d{8}[_]\\d{4}[_]";

    private File currentDirectory = null;
    private List<File> listOfFiles = Lists.newArrayList();
    private ProgressBar progressBar = null;
    private final AtomicInteger done = new AtomicInteger(0);

    /**
     * Main constructor that takes a directory to work on.
     *
     * @param directory Directory to perform operation on.
     * @throws InvalidDirectoryException If directory cannot be accessed
     *                                   properly.
     * @throws NoFilesFoundException     If directory is empty.
     */
    public RemoveExifPrefixRenamer(final String directory)
            throws InvalidDirectoryException, NoFilesFoundException {
        this.currentDirectory = new File(directory);
        checkInputAndInitialize();
        new Thread(this).start();
    }

    /**
     * Performs actual renaming/removing of date information from the
     * filenames.
     *
     * @throws RenamingErrorException If any error occurs.
     * @see #checkInputAndInitialize()
     */
    private void rename() throws RenamingErrorException {
        for (final File listOfFile : this.listOfFiles) {
            String name = listOfFile.getName();
            String nameNeu = name.replaceFirst(REPLACE_PATTERN, "");

            // count files to be done
            if (!nameNeu.equalsIgnoreCase(name)) {
                done.incrementAndGet();
            }

            // update UI
            this.progressBar.setProgress();
            this.progressBar.setText(name);
            this.progressBar.updateUI();

            // rename files only
            if (listOfFile.isFile() && !listOfFile.renameTo(
                    new File(listOfFile.getParent()
                            + File.separatorChar + nameNeu))) {
                LOG.error("Problem with file " + listOfFile.getName());
                throw new RenamingErrorException(getParameterizedBundleString("fotorenamer.ui.rerename.error.detail",
                        listOfFile.getName()));
            }
        }
    }

    /**
     * Checks whether current UI-configuration is valid in order to perform the
     * renaming itself.
     *
     * @throws NoFilesFoundException     If the directory contains no files.
     * @throws InvalidDirectoryException If the selected directory is not
     *                                   accessible.
     */
    private void checkInputAndInitialize()
            throws NoFilesFoundException, InvalidDirectoryException {
        // valid directory
        if (this.currentDirectory == null || !this.currentDirectory.isDirectory()) {
            throw new InvalidDirectoryException("" + this.currentDirectory);
        }

        final File[] files = this.currentDirectory.listFiles(
                new ImageFilenameFilter());

        // files available
        if(files == null || files.length == 0) {
            throw new NoFilesFoundException(this.currentDirectory);
        }

        this.listOfFiles = Arrays.asList(files);
    }

    /**
     * Updates the UI and performs the renaming. All error handling is done in
     * other methods.
     *
     * @see #rename()
     */
    public final void run() {
        this.progressBar = new ProgressBar(this.listOfFiles.size());

        try {
            rename();
        } catch (RenamingErrorException uf) {
            JOptionPane.showMessageDialog(null, getParameterizedBundleString("fotorenamer.ui.rename.error", uf.getMessage()),
                    getBundleString("fotorenamer.ui.rerename.error.title"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.progressBar.dispose();

        String statusMessage;
        if (this.done.get() == 0) {
            statusMessage = getParameterizedBundleString("fotorenamer.ui.rename.success.message.none", this.currentDirectory.getName());
        } else if (this.done.get() == 1) {
            statusMessage = getParameterizedBundleString("fotorenamer.ui.rename.success.message.one", this.currentDirectory.getName());
        } else {
            statusMessage = getParameterizedBundleString("fotorenamer.ui.rename.success.message", this.done, this.listOfFiles.size(), this.currentDirectory.getName());
        }
        JOptionPane.showMessageDialog(null, statusMessage, getBundleString("fotorenamer.ui.rerename.success.title"),
                JOptionPane.INFORMATION_MESSAGE);
    }
}
