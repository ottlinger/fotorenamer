/*
 * Created on 08.12.2003
 */
package de.aikiit.bildbearbeiter.image;

import de.aikiit.bildbearbeiter.exception.InvalidDirectoryException;
import de.aikiit.bildbearbeiter.exception.NoFilesFoundException;
import de.aikiit.bildbearbeiter.exception.RenamingErrorException;
import de.aikiit.bildbearbeiter.gui.ProgressBar;
import org.apache.log4j.Logger;

import javax.swing.JOptionPane;
import java.io.File;

/**
 * This class rerenames files in order to be able to play them back on a camera
 * device that is not able to deal with long(er) filenames.
 *
 * @author hirsch, 08.12.2003
 * @version 2004-01-08
 */
public class DateinamenZurueckUmbenenner implements Runnable {
    private static final Logger LOG =
            Logger.getLogger(DateinamenZurueckUmbenenner.class);

    private File aktuellesVerzeichnis = null;
    private File[] dateiliste = null;

    private ProgressBar grafik = null;
    private int obergrenze = 0;
    private int umbenannt = 0;


    /**
     * Main constructor that takes a directory to work on.
     *
     * @param verzeichnis Directory to perform operation on.
     * @throws InvalidDirectoryException If directory cannot be accessed
     *                                   properly.
     * @throws NoFilesFoundException     If directory is empty.
     */
    public DateinamenZurueckUmbenenner(String verzeichnis)
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
        String name = "";
        String nameNeu = "";
        // String muster = "\\d{8}[_]\\d{4}[_]\\p{ASCII}*";

        for (int i = 0; i < this.obergrenze; i++) {
            name = this.dateiliste[i].getName();
            // würde alle Vorkommen ersetzen:
            // nameNeu = name.replaceAll("\\d{8}[_]\\d{4}[_]","");
            nameNeu = name.replaceFirst("\\d{8}[_]\\d{4}[_]", "");

            // umzubenennende Dateien zählen
            if (!nameNeu.equalsIgnoreCase(name)) {
                this.umbenannt++;
            }

            // ProgressBar updaten...
            this.grafik.setProgress(i);
            this.grafik.setText(name);
            // Da die Namen verschieden lang sind den ProgressBar updaten!
            this.grafik.updateUI();

            // TODO reversing does not work - exception is thrown here and UI is frozen (progress bar)
            // rename nur bei Dateien
            if (this.dateiliste[i].isFile() && !this.dateiliste[i].renameTo(
                    new File(this.dateiliste[i].getParent() + File.separatorChar + nameNeu))) {
                LOG.error("Problem with file " + this.dateiliste[i].getName());
                throw new RenamingErrorException("\nFehler bei Bild "
                        + this.dateiliste[i].getName());
            } // end if
        } // end of for
    } // end of rename

    /**
     * prüft, ob Parameter ein Verzeichnis ist und mehr als 0 Dateien enthält, sonst Ausnahme<br> (intern werden auch
     * Parameter gesetzt)
     *
     * @throws de.aikiit.bildbearbeiter.exception.NoFilesFoundException
     *
     * @throws de.aikiit.bildbearbeiter.exception.InvalidDirectoryException
     *
     */
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
        this.dateiliste = this.aktuellesVerzeichnis.listFiles(new ImageFilenameFilter());
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
        String meldung = "";
        this.grafik = new ProgressBar(this.obergrenze);

        try {
            umbenennen();
        } catch (RenamingErrorException uf) {
            JOptionPane.showMessageDialog(null,
                    "Während der Bearbeitung der Datei\n" +
                            uf.getMessage() + " trat ein Fehler beim Umbennen auf.",
                    "Fehler beim Umbenennen",
                    JOptionPane.ERROR_MESSAGE);
            return;
        } // end of catch uf
        this.grafik.dispose();

        // Erfolgsmeldung geben
        if (this.umbenannt == 0) {
            meldung = "Im Verzeichnis: " + this.aktuellesVerzeichnis.getName() +
                    "\nwurden keine Dateien\numbenannt.\n\n";
        } else if (this.umbenannt == 1) {
            meldung = "\nEs wurde eine Datei\n" +
                    "im Verzeichnis: " + this.aktuellesVerzeichnis.getName() +
                    "\nerfolgreich umbenannt.\n\n";
        } else {
            meldung = "\nEs wurden " + this.umbenannt + " von " + this.obergrenze
                    + " Dateien\nim Verzeichnis: " + this.aktuellesVerzeichnis.getName() +
                    "\nerfolgreich umbenannt.\n\n";
        } // end of else
        JOptionPane.showMessageDialog(null, meldung, "Erfolg",
                JOptionPane.INFORMATION_MESSAGE);
    } // end of run
} // end of class
