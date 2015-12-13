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

import de.aikiit.fotorenamer.exception.InvalidDirectoryException;
import de.aikiit.fotorenamer.exception.NoFilesFoundException;
import de.aikiit.fotorenamer.exception.RenamingErrorException;
import de.aikiit.fotorenamer.gui.ProgressBar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JOptionPane;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

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
    private File[] listOfFiles = null;
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
                throw new RenamingErrorException("\nFehler bei Bild "
                        + listOfFile.getName());
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
        if (!this.currentDirectory.isDirectory()) {
            throw new InvalidDirectoryException(this.currentDirectory);
        }

        // files available
        this.listOfFiles = this.currentDirectory.listFiles(
                new ImageFilenameFilter());
        if (this.listOfFiles == null || this.listOfFiles.length == 0) {
            throw new NoFilesFoundException(this.currentDirectory);
        }

    }

    /**
     * Updates the UI and performs the renaming. All error handling is done in
     * other methods.
     *
     * @see #rename()
     */
    public final void run() {
        this.progressBar = new ProgressBar(this.listOfFiles.length);

        try {
            rename();
        } catch (RenamingErrorException uf) {
            JOptionPane.showMessageDialog(null,
                    "Während der Bearbeitung der Datei\n"
                            + uf.getMessage() + " trat ein Fehler beim "
                            + "Umbennen auf.", "Fehler beim Umbenennen",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.progressBar.dispose();

        String statusMessage;
        if (this.done.get() == 0) {
            statusMessage = "Im Verzeichnis: " + this.currentDirectory.getName()
                    + "\nwurden keine Dateien\numbenannt.\n\n";
        } else if (this.done.get() == 1) {
            statusMessage = "\nEs wurde eine Datei\n"
                    + "im Verzeichnis: " + this.currentDirectory.getName()
                    + "\nerfolgreich umbenannt.\n\n";
        } else {
            statusMessage = "\nEs wurden " + this.done + " von "
                    + this.listOfFiles.length
                    + " Dateien\nim Verzeichnis: "
                    + this.currentDirectory.getName()
                    + "\nerfolgreich umbenannt.\n\n";
        }
        JOptionPane.showMessageDialog(null, statusMessage, "Erfolg",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
