/*
 * Created on 12.10.2003
 */
package bildbearbeiter;

import bildbearbeiter.ausnahmen.KeineDateienEnthaltenException;
import bildbearbeiter.ausnahmen.UmbenennenFehlgeschlagenException;
import bildbearbeiter.ausnahmen.UngueltigesVerzeichnisException;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.File;

/**
 * Sinn: CanonBilder rename und BildErzeugungsdatum in Namen schreiben
 *
 * @author hirsch, 12.10.2003
 * @version 2004-01-08
 */
public class DateinamenManipulierer implements Runnable {
    final static private Logger LOG = Logger.getLogger(DateinamenManipulierer.class);

    private File aktuellesVerzeichnis = null;
    private File[] dateiliste = null;

    private Fortschrittsbalken grafik = null;
    private int aktAnzahl = 0;
    private int obergrenze = 0;

    /**
     * Übernimmt ein Verzeichnis. Dessen Dateien werden in
     * ZuletztGeändertDatum_ZuletztGeändertUhrzeit_Dateiname umbenannt.<br>
     * Bei den IXUS-Bildern bedeutet das, dass den Dateinamen der Zeitpunkt
     * der Bildaufnahme mit hinzugefügt wird.
     * <br>
     *
     * @param verzeichnis
     * @throws UngueltigesVerzeichnisException
     *                                        if there's a problem with the directory selected.
     * @throws KeineDateienEnthaltenException if the selected directory is empty.
     */
    public DateinamenManipulierer(String verzeichnis)
            throws UngueltigesVerzeichnisException, KeineDateienEnthaltenException {
        this.aktuellesVerzeichnis = new File(verzeichnis);
        // erst starten, wenn die Eingabeprüfung erfolgreich war
        pruefeEingabeUndInit();

        new Thread(this).start();
    } // end of Konstruktor

    /**
     * prüft, ob Parameter ein Verzeichnis ist und mehr als 0 Dateien enthält,
     * sonst Ausnahme<br>
     * (intern werden auch Parameter gesetzt)
     *
     * @throws KeineDateienEnthaltenException
     * @throws UngueltigesVerzeichnisException
     *
     */
    public void pruefeEingabeUndInit()
            throws KeineDateienEnthaltenException, UngueltigesVerzeichnisException {
        // Verzeichnis gültig ?
        if (!this.aktuellesVerzeichnis.isDirectory())
            throw new UngueltigesVerzeichnisException(this.aktuellesVerzeichnis);

        // Dateien da ?
        this.dateiliste = this.aktuellesVerzeichnis.listFiles();
        if (this.dateiliste == null || this.dateiliste.length == 0) {
            throw new KeineDateienEnthaltenException(this.aktuellesVerzeichnis);
        }

        // internen Zustand setzen
        this.obergrenze = this.dateiliste.length;
    } // end of pruefeEingabe

    /**
     * PRE: pruefeEingabenUndInit() aufgerufen
     * Benennt alle Dateien im Verzeichnis so um,
     * dass vor dem IXUS-Dateinamen das Datum der letzten Änderung steht,
     * was im Kamerafall das Aufnahmedatum ist
     *
     * @throws Exception if any errors occur.
     * @see #pruefeEingabeUndInit()
     */
    public void rename() throws UmbenennenFehlgeschlagenException {
        String targetFilename = "";
        for (int i = 0; i < this.obergrenze; i++) {
            // Daten holen
            try {
                targetFilename = MetaDataExtractor.generateCreationDateInCorrectFormat(this.dateiliste[i]);
            } catch (Exception e) {
                throw new UmbenennenFehlgeschlagenException(e.getLocalizedMessage());
            }
            // Fortschrittsbalken updaten...
            this.grafik.setFortschritt(i);
            this.grafik.setText(this.dateiliste[i].getName());
            // Da die Namen verschieden lang sind den Fortschrittsbalken updaten!
            LOG.info("Renaming "+this.dateiliste[i].getName()+ " to "+targetFilename);
            this.grafik.updateUI();

            // rename nur bei Dateien
            if (this.dateiliste[i].isFile()) {
                if (!this.dateiliste[i].renameTo(new File(targetFilename)))
                    throw new UmbenennenFehlgeschlagenException("\tFehler bei Bild"
                            + this.dateiliste[i].getName());
            } // end if - isFile()
        } // end of for
    } // end of rename

    /**
     * Fortschrittsbalken anzeigen und rename starten
     * Die Anzeige wird innerhalb von rename erledigt.
     * <p/>
     * Fehlerbehandlung des umbennens wird erledigt = Abbruch ;-^
     *
     * @see #rename()
     */
    public void run() {
        String meldung = "";
        this.grafik = new Fortschrittsbalken(this.obergrenze);

        try {
            rename();
        } catch (UmbenennenFehlgeschlagenException uf) {
            JOptionPane.showMessageDialog(null,
                    "Während der Bearbeitung der Datei\n" +
                            uf.getMessage() + " trat ein Fehler beim Umbennen auf.",
                    "Fehler beim Umbenennen",
                    JOptionPane.ERROR_MESSAGE);
            return;
        } // end of catch uf

        this.grafik.dispose();

        // Erfolgsmeldung geben
        if (this.obergrenze == 0) {
            meldung = "Im Verzeichnis: " + this.aktuellesVerzeichnis.getName() +
                    "\nwurden keine Dateien\numbenannt.\n\n";
        } else if (this.obergrenze == 1) {
            meldung = "\nEs wurde eine Datei\n" +
                    "im Verzeichnis: " + this.aktuellesVerzeichnis.getName() +
                    "\nerfolgreich umbenannt.\n\n";
        } else {
            meldung = "\nEs wurden " + this.obergrenze + " Dateien\n" +
                    "im Verzeichnis: " + this.aktuellesVerzeichnis.getName() +
                    "\nerfolgreich umbenannt.\n\n";
        } // end of else
        JOptionPane.showMessageDialog(null, meldung, "Erfolg",
                JOptionPane.INFORMATION_MESSAGE);
    } // end of run
} // end of class