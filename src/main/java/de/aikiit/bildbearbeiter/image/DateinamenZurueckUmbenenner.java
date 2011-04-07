/*
 * Created on 08.12.2003
 */
package de.aikiit.bildbearbeiter.image;

import de.aikiit.bildbearbeiter.exception.InvalidDirectoryException;
import de.aikiit.bildbearbeiter.exception.NoFilesFoundException;
import de.aikiit.bildbearbeiter.exception.RenamingErrorException;
import de.aikiit.bildbearbeiter.gui.ProgressBar;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.File;

/**
 * Sinn: soll die Dateien wieder zurückumbennen können
 *
 * @author hirsch, 08.12.2003
 * @version 2004-01-08
 */
public class DateinamenZurueckUmbenenner implements Runnable {
    final static private Logger LOG = Logger.getLogger(DateinamenZurueckUmbenenner.class);

    private File aktuellesVerzeichnis = null;
    private File[] dateiliste = null;

    private ProgressBar grafik = null;
    private int obergrenze = 0;
    private int umbenannt = 0;


    /**
     * @param verzeichnis
     * @throws de.aikiit.bildbearbeiter.exception.InvalidDirectoryException
     *
     * @throws de.aikiit.bildbearbeiter.exception.NoFilesFoundException
     *
     */
    public DateinamenZurueckUmbenenner(String verzeichnis)
            throws InvalidDirectoryException, NoFilesFoundException {
        this.aktuellesVerzeichnis = new File(verzeichnis);
        // erst starten, wenn die Eingabeprüfung erfolgreich war
        pruefeEingabeUndInit();
        new Thread(this).start();
    } // end of Konstruktor

    /**
     * PRE: pruefeEingabenUndInit() aufgerufen
     * Benennt alle Dateien im Verzeichnis so um,
     * dass vor dem IXUS-Dateinamen das Datum der letzten Änderung steht,
     * was im Kamerfall das Aufnahmedatum ist
     * @throws de.aikiit.bildbearbeiter.exception.RenamingErrorException
     *
     * @see #pruefeEingabeUndInit()
     */
    public void umbenennen() throws RenamingErrorException {
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
            if (this.dateiliste[i].isFile()) {
                if (!this.dateiliste[i].renameTo(
                        new File(this.dateiliste[i].getParent() + File.separatorChar + nameNeu)))
                    LOG.error("Problem with file "+this.dateiliste[i].getName());
                    throw new RenamingErrorException("\nFehler bei Bild "
                            + this.dateiliste[i].getName());
            } // end if - isFile()
        } // end of for
    } // end of rename

    /**
     * prüft, ob Parameter ein Verzeichnis ist und mehr als 0 Dateien enthält,
     * sonst Ausnahme<br>
     * (intern werden auch Parameter gesetzt)
     *
     * @throws de.aikiit.bildbearbeiter.exception.NoFilesFoundException
     * @throws de.aikiit.bildbearbeiter.exception.InvalidDirectoryException
     */
    public void pruefeEingabeUndInit()
            throws NoFilesFoundException, InvalidDirectoryException {
        // Verzeichnis gültig ?
        if (!this.aktuellesVerzeichnis.isDirectory())
            throw new InvalidDirectoryException(this.aktuellesVerzeichnis);

        // Dateien da ?
        this.dateiliste = this.aktuellesVerzeichnis.listFiles(new ImageFilenameFilter());
        if (this.dateiliste == null || this.dateiliste.length == 0) {
            throw new NoFilesFoundException(this.aktuellesVerzeichnis);
        }

        // internen Zustand setzen
        this.obergrenze = this.dateiliste.length;
    } // end of pruefeEingabe

    /**
     * ProgressBar anzeigen und rename starten
     * Die Anzeige wird innerhalb von rename erledigt.
     * <p/>
     * Fehlerbehandlung des umbennens wird erledigt = Abbruch ;-^
     *
     * @see #umbenennen()
     */
    public void run() {
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