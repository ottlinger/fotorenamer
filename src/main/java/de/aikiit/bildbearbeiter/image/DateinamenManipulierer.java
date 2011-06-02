/*
 * Created on 12.10.2003
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
 * Rename image files based on exif metadata values. During metadata extraction
 * the date of creation is extracted and used to renameFiles the file.
 * <p/>
 * <b>ATTENTION!</b>: You should only work on copies of images with that tool
 *
 * @author hirsch, 12.10.2003
 * @version 2004-01-08
 */
public class DateinamenManipulierer implements Runnable {
    // REVIEW extract this class into an abstract pictureModifier
    // with 2 subclasses, one method to generate names should be abstract,
    // the current renameFiles should be renamed :-)
    private static final Logger LOG =
            Logger.getLogger(DateinamenManipulierer.class);

    private File currentDirectory = null;
    private File[] imageList = null;

    private ProgressBar progressBar = null;
    private int amountOfFiles = 0;

    /**
     * Übernimmt ein Verzeichnis. Dessen Dateien werden in
     * ZuletztGeändertDatum_ZuletztGeändertUhrzeit_Dateiname umbenannt.<br> Bei
     * den IXUS-Bildern bedeutet das, dass den Dateinamen der Zeitpunkt der
     * Bildaufnahme mit hinzugefügt wird. <br>
     *
     * @param verzeichnis Directory to work on.
     * @throws InvalidDirectoryException If there's a problem with the directory
     *                                   selected.
     * @throws NoFilesFoundException     if the selected directory is empty.
     */
    public DateinamenManipulierer(final String verzeichnis)
            throws InvalidDirectoryException, NoFilesFoundException {
        this.currentDirectory = new File(verzeichnis);

        // erst starten, wenn die Eingabeprüfung erfolgreich war
        validateUserSelectionAndInitInternally();

        new Thread(this).start();
    } // end of Konstruktor

    /**
     * Checks whether the selected directory contains any relevant files and
     * sets internal state appropriately.
     *
     * @throws de.aikiit.bildbearbeiter.exception.NoFilesFoundException
     *          If selected directory contains no files.
     * @throws de.aikiit.bildbearbeiter.exception.InvalidDirectoryException
     *          If any I/O-error occured when accessing the directory.
     */
    protected final void validateUserSelectionAndInitInternally()
            throws NoFilesFoundException, InvalidDirectoryException {
        // Verzeichnis gültig ?
        if (!this.currentDirectory.isDirectory()) {
            throw new InvalidDirectoryException(this.currentDirectory);
        }

        // Dateien da ?
        this.imageList = this.currentDirectory.listFiles(
                new ImageFilenameFilter());
        if (this.imageList == null || this.imageList.length == 0) {
            throw new NoFilesFoundException(this.currentDirectory);
        }

        // internen Zustand setzen
        this.amountOfFiles = this.imageList.length;
    } // end of pruefeEingabe

    /**
     * Performs the actual/technical renaming.
     *
     * @throws RenamingErrorException if any errors occur.
     * @see #validateUserSelectionAndInitInternally()
     */
    public final void rename() throws RenamingErrorException {
        String targetFilename = "";
        for (int i = 0; i < this.amountOfFiles; i++) {
            // Daten holen
            try {
                targetFilename = MetaDataExtractor.
                        generateCreationDateInCorrectFormat(this.imageList[i]);
            } catch (Exception e) {
                throw new RenamingErrorException(e.getLocalizedMessage());
            }
            // ProgressBar updaten...
            this.progressBar.setProgress(i);
            this.progressBar.setText(this.imageList[i].getName());
            // Da die Namen verschieden lang sind den ProgressBar updaten!
            LOG.info("Renaming " + this.imageList[i].getName() + " to "
                    + targetFilename);
            this.progressBar.updateUI();

            // only renameFiles files
            if (this.imageList[i].isFile() && !this.imageList[i].renameTo(
                            new File(this.imageList[i].getParent(),
                                    targetFilename))) {
                throw new RenamingErrorException("\tFehler bei Bild"
                        + this.imageList[i].getName());
            } // end if - isFile()
        } // end of for
    } // end of renameFiles

    /**
     * Performs the renaming and updates the UI. All error handling is done in
     * other methods.
     *
     * @see #rename()
     */
    public final void run() {
        String meldung = "";
        this.progressBar = new ProgressBar(this.amountOfFiles);

        try {
            rename();
        } catch (RenamingErrorException uf) {
            JOptionPane.showMessageDialog(null,
                    "Während der Bearbeitung der Datei\n"
                            + uf.getMessage() + " trat ein Fehler beim "
                            + "Umbennen auf.",
                    "Fehler beim Umbenennen",
                    JOptionPane.ERROR_MESSAGE);
            return;
        } // end of catch uf

        this.progressBar.dispose();

        // Erfolgsmeldung geben
        if (this.amountOfFiles == 0) {
            meldung = "Im Verzeichnis: " + this.currentDirectory.getName()
                    + "\nwurden keine Dateien\numbenannt.\n\n";
        } else if (this.amountOfFiles == 1) {
            meldung = "\nEs wurde eine Datei\n"
                    + "im Verzeichnis: " + this.currentDirectory.getName()
                    + "\nerfolgreich umbenannt.\n\n";
        } else {
            meldung = "\nEs wurden " + this.amountOfFiles + " Dateien\n"
                    + "im Verzeichnis: " + this.currentDirectory.getName()
                    + "\nerfolgreich umbenannt.\n\n";
        } // end of else
        JOptionPane.showMessageDialog(null, meldung, "Erfolg",
                JOptionPane.INFORMATION_MESSAGE);
    } // end of run
} // end of class
