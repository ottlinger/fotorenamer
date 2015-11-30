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
 * This class rerenames files in order to be able to play them back on a camera
 * device that is not able to deal with long(er) filenames.
 *
 * @author hirsch, 08.12.2003
 * @version 2004-01-08
 */
public class RemoveExifPrefixRenamer implements Runnable {
    private static final Logger LOG =
            LogManager.getLogger(RemoveExifPrefixRenamer.class);

    private File aktuellesVerzeichnis = null;
    private File[] dateiliste = null;

    private ProgressBar grafik = null;
    private int obergrenze = 0;
    private AtomicInteger umbenannt = new AtomicInteger(0);

    /**
     * Main constructor that takes a directory to work on.
     *
     * @param verzeichnis Directory to perform operation on.
     * @throws InvalidDirectoryException If directory cannot be accessed
     *                                   properly.
     * @throws NoFilesFoundException     If directory is empty.
     */
    public RemoveExifPrefixRenamer(final String verzeichnis)
            throws InvalidDirectoryException, NoFilesFoundException {
        this.aktuellesVerzeichnis = new File(verzeichnis);
        // erst starten, wenn die Eingabeprüfung erfolgreich war
        pruefeEingabeUndInit();
        new Thread(this).start();
    } // end of Konstruktor

    /**
     * Performs actual renaming/removing of date information from the
     * filenames.
     *
     * @throws RenamingErrorException If any error occurs.
     * @see #pruefeEingabeUndInit()
     */
    public final void umbenennen() throws RenamingErrorException {
        // String muster = "\\d{8}[_]\\d{4}[_]\\p{ASCII}*";

        for (int i = 0; i < this.obergrenze; i++) {
            String name = this.dateiliste[i].getName();
            // TODO 20110507_180520_IMG_8192small.JPG
            // würde alle Vorkommen ersetzen:
            // nameNeu = name.replaceAll("\\d{8}[_]\\d{4}[_]","");
            String nameNeu = name.replaceFirst("\\d{8}[_]\\d{4}[_]", "");

            // umzubenennende Dateien zählen
            if (!nameNeu.equalsIgnoreCase(name)) {
                umbenannt.incrementAndGet();
            }

            // ProgressBar updaten...
            this.grafik.setProgress();
            this.grafik.setText(name);
            // Da die Namen verschieden lang sind den ProgressBar updaten!
            this.grafik.updateUI();

            // renameFiles nur bei Dateien
            if (this.dateiliste[i].isFile() && !this.dateiliste[i].renameTo(
                    new File(this.dateiliste[i].getParent()
                            + File.separatorChar + nameNeu))) {
                LOG.error("Problem with file " + this.dateiliste[i].getName());
                throw new RenamingErrorException("\nFehler bei Bild "
                        + this.dateiliste[i].getName());
            } // end if
        } // end of for
    } // end of renameFiles

    /**
     * Checks whether current UI-configuration is valid in order to perform the
     * renaming itself.
     *
     * @throws NoFilesFoundException     If the directory contains no files.
     * @throws InvalidDirectoryException If the selected directory is not
     *                                   accessible.
     */
    protected final void pruefeEingabeUndInit()
            throws NoFilesFoundException, InvalidDirectoryException {
        // Verzeichnis gültig ?
        if (!this.aktuellesVerzeichnis.isDirectory()) {
            throw new InvalidDirectoryException(this.aktuellesVerzeichnis);
        }

        // Dateien da ?
        this.dateiliste = this.aktuellesVerzeichnis.listFiles(
                new ImageFilenameFilter());
        if (this.dateiliste == null || this.dateiliste.length == 0) {
            throw new NoFilesFoundException(this.aktuellesVerzeichnis);
        }

        // internen Zustand setzen
        this.obergrenze = this.dateiliste.length;
    } // end of pruefeEingabe

    /**
     * Updates the UI and performs the renaming. All error handling is done in
     * other methods.
     *
     * @see #umbenennen()
     */
    public final void run() {
        this.grafik = new ProgressBar(this.obergrenze);

        try {
            umbenennen();
        } catch (RenamingErrorException uf) {
            JOptionPane.showMessageDialog(null,
                    "Während der Bearbeitung der Datei\n"
                            + uf.getMessage() + " trat ein Fehler beim "
                            + "Umbennen auf.", "Fehler beim Umbenennen",
                    JOptionPane.ERROR_MESSAGE);
            return;
        } // end of catch uf
        this.grafik.dispose();

        // Erfolgsmeldung geben
        String meldung = "";
        if (this.umbenannt.get() == 0) {
            meldung = "Im Verzeichnis: " + this.aktuellesVerzeichnis.getName()
                    + "\nwurden keine Dateien\numbenannt.\n\n";
        } else if (this.umbenannt.get() == 1) {
            meldung = "\nEs wurde eine Datei\n"
                    + "im Verzeichnis: " + this.aktuellesVerzeichnis.getName()
                    + "\nerfolgreich umbenannt.\n\n";
        } else {
            meldung = "\nEs wurden " + this.umbenannt + " von "
                    + this.obergrenze
                    + " Dateien\nim Verzeichnis: "
                    + this.aktuellesVerzeichnis.getName()
                    + "\nerfolgreich umbenannt.\n\n";
        } // end of else
        JOptionPane.showMessageDialog(null, meldung, "Erfolg",
                JOptionPane.INFORMATION_MESSAGE);
    } // end of run
} // end of class
