package de.aikiit.bildbearbeiter.image;

import de.aikiit.bildbearbeiter.exception.InvalidDirectoryException;
import de.aikiit.bildbearbeiter.exception.NoFilesFoundException;
import de.aikiit.bildbearbeiter.exception.RenamingErrorException;
import de.aikiit.bildbearbeiter.gui.ProgressBar;
import org.apache.log4j.Logger;

import javax.swing.JOptionPane;
import java.io.File;

/**
 * Main implementation to renameFiles images taken with digital cameras.
 * <p/>
 * The onliest abstract method generates a filename from a given file.
 *
 * @author hirsch
 * @version 2011-03-22, 11:43
 */
public abstract class AbstractImageRenamer implements Runnable {

    private static final Logger LOG =
            Logger.getLogger(AbstractImageRenamer.class);

    private File currentDirectory = null;
    private File[] imageList = null;

    private ProgressBar progressBar = null;
    private int amountOfFiles = 0;

    /**
     * Empty default constructor to enforce file/directory based usage of
     * subclasses.
     */
    AbstractImageRenamer() {
     // empty default constructor
    }

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
    public AbstractImageRenamer(final String verzeichnis)
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
    public final void renameFiles() throws RenamingErrorException {
        String targetFilename = "";
        for (int i = 0; i < this.amountOfFiles; i++) {

            // Daten holen und umbenenen
            targetFilename = renameImage(this.imageList[i]);

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
     * This method provides a means to renameFiles files when iterating over a
     * list of files.
     *
     * @param imageFile Filename to renameFiles according to the subclass
     * implementation.
     * @return New filename for the given file.
     */
    abstract String renameImage(File imageFile);

    /**
     * Performs the renaming and updates the UI. All error handling is done in
     * other methods.
     *
     * @see #renameFiles()
     */
    public final void run() {
        String meldung = "";
        this.progressBar = new ProgressBar(this.amountOfFiles);

        try {
            renameFiles();
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
}