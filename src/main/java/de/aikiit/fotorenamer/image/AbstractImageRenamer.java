/**
Copyright 2011, Aiki IT, FotoRenamer

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
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
public abstract class AbstractImageRenamer implements Runnable {

    /**
     * The logger of this class.
     */
    private static final Logger LOG =
            LogManager.getLogger(AbstractImageRenamer.class);

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
    private int amountOfFiles = 0;

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
    public AbstractImageRenamer(final String directory)
            throws InvalidDirectoryException, NoFilesFoundException {

        if (directory == null) {
            throw new InvalidDirectoryException("null is not a directory");
        }

        this.currentDirectory = new File(directory);
        if (!this.currentDirectory.isDirectory()) {
            throw new InvalidDirectoryException(this.currentDirectory);
        }

        // retrieve relevant images in directory
        File[] files = this.currentDirectory.listFiles(
                new ImageFilenameFilter());
        if (files == null || files.length == 0) {
            throw new NoFilesFoundException(this.currentDirectory);
        }
        this.imageList = Lists.newArrayList(files);
        this.amountOfFiles = this.imageList.size();
    }

    /**
     * Performs the actual/technical renaming.
     */
    private void renameFiles() {
        LOG.info("Starting to rename " + this.amountOfFiles + " files.");

        Consumer<File> consumer = file -> {
            Boolean renamingResult;
            // extract EXIF data and fetch target filename
            String targetFilename = renameImage(file);

            // update progress bar (names have a different length)
            progressBar.setProgress();
            progressBar.setText(file.getName());
            progressBar.updateUI();
            // perform file renaming
            renamingResult = file.renameTo(
                    new File(file.getParent(),
                            targetFilename));
            if (renamingResult) {
                LOG.info("Renaming " + file.getName()
                        + " to " + targetFilename);
                LOG.info("Renamed file exists? "
                        + new File(file.getParent(),
                        targetFilename).exists());
            }
            // TODO add second progressbar or counter for errors
            else {
                LOG.error("Unable to rename"
                        + file.getName() + " to "
                        + targetFilename);
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
    public abstract String renameImage(File imageFile);

    /**
     * Performs the renaming and updates the UI. All error handling is done in
     * other methods.
     *
     * @see #renameFiles()
     */
    public final void run() {
        String notification;
        this.progressBar = new ProgressBar(this.amountOfFiles);

        try {
            renameFiles();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Während der Bearbeitung der Datei\n "
                            + MoreObjects.firstNonNull(e.getMessage(), e.getClass().getSimpleName()) + " trat ein Fehler beim "
                            + "Umbennen auf.",
                    "Fehler beim Umbenennen",
                    JOptionPane.ERROR_MESSAGE);

            this.amountOfFiles = 0;
        } finally {
            this.progressBar.dispose();
        }

        // show UI-notification
        switch (this.amountOfFiles) {
            case 0:
                notification = "Im Verzeichnis: "
                        + this.currentDirectory.getName()
                        + "\nwurden keine Dateien\numbenannt.\n\n";
                break;

            case 1:
                notification = "\nEs wurde eine Datei\n"
                        + "im Verzeichnis: " + this.currentDirectory.getName()
                        + "\nerfolgreich umbenannt.\n\n";
                break;
            default:
                notification = "\nEs wurden "
                        + this.amountOfFiles + " Dateien\n"
                        + "im Verzeichnis: " + this.currentDirectory.getName()
                        + "\numbenannt.\n\n";
                break;
        }
        JOptionPane.showMessageDialog(null, notification, "Erfolg",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
