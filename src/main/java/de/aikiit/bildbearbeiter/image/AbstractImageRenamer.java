package de.aikiit.bildbearbeiter.image;

import de.aikiit.bildbearbeiter.exception.InvalidDirectoryException;
import de.aikiit.bildbearbeiter.exception.NoFilesFoundException;
import de.aikiit.bildbearbeiter.exception.RenamingErrorException;
import de.aikiit.bildbearbeiter.gui.ProgressBar;
import org.apache.log4j.Logger;

import javax.swing.JOptionPane;
import java.io.File;

/**
 * Abstract class that handles image renaming and file handling.
 * <br/>
 * The onliest abstract method generates a filename from a given file
 * and should be used to provide different strategies for image renaming.
 *
 * @author hirsch
 * @version 2011-03-22, 11:43
 */
public abstract class AbstractImageRenamer implements Runnable {

    /** The logger of this class. **/
    private static final Logger LOG =
            Logger.getLogger(AbstractImageRenamer.class);

    /** The currently selected directory to work on. */
    private File currentDirectory = null;
    /** The list of all relevant files in the current directory. */
    private File[] imageList = null;

    /** Progress bar for visual feedback of what's going on. */
    private ProgressBar progressBar = null;
    /** Number of files that need processing. */
    private int amountOfFiles = 0;

    /**
     * Empty default constructor to enforce file/directory based usage of
     * subclasses.
     */
    private AbstractImageRenamer() {
     // empty default constructor
    }

    /**
     * Starts image processing on the given directory if it contains
     * relevant images. The strategy of renaming is defined by
     * subclasses implementation of @see #renameImage(File).
     *
     * @param directory Name of directory to work on.
     * @throws InvalidDirectoryException
     * If there's a problem with
     * the selected directory.
     * @throws NoFilesFoundException
     * If the selected directory is empty.
     */
    public AbstractImageRenamer(final String directory)
            throws InvalidDirectoryException, NoFilesFoundException {
        this.currentDirectory = new File(directory);
        if (!this.currentDirectory.isDirectory()) {
            throw new InvalidDirectoryException(this.currentDirectory);
        }

        // retrieve relevant images in directory
        this.imageList = this.currentDirectory.listFiles(
                new ImageFilenameFilter());
        if (this.imageList == null || this.imageList.length == 0) {
            throw new NoFilesFoundException(this.currentDirectory);
        }
        this.amountOfFiles = this.imageList.length;
    }

    /**
     * Performs the actual/technical renaming.
     *
     * @throws RenamingErrorException if any errors occur.
     */
    public final void renameFiles() throws RenamingErrorException {
        String targetFilename = "";
        Boolean renamingResult;
        LOG.info("Starting to rename " + this.amountOfFiles + " files.");
        for (int i = 0; i < this.amountOfFiles; i++) {

            // only rename regular files
            if (this.imageList[i].isFile()) {

                // extract EXIF data and fetch target filename
                targetFilename = renameImage(this.imageList[i]);

                // update progress bar (names have a different length)
                this.progressBar.setProgress(i);
                this.progressBar.setText(this.imageList[i].getName());
                this.progressBar.updateUI();

                // perform file renaming
                renamingResult = this.imageList[i].renameTo(
                        new File(this.imageList[i].getParent(),
                                targetFilename));
                if (renamingResult) {
                    LOG.info("Renaming " + this.imageList[i].getName()
                            + " to " + targetFilename);
                    LOG.info("Renamed file exists? "
                            + new File(this.imageList[i].getParent(),
                                    targetFilename).exists());
                }  else {
                    throw new RenamingErrorException("Could not rename"
                            + this.imageList[i].getName() + " to "
                            + targetFilename);
                }
            } else {
                LOG.info("Skipping " + this.imageList[i].getName()
                        + " - not a file.");
            }
        } // end of for
    }

    /**
     * This method provides a strategy to rename image files when
     * iterating over a list of files.
     * It is called during image processing.
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
        String notification;
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

            this.amountOfFiles = 0;
        } finally {
            this.progressBar.dispose();
        }

        // show UI-notification
        switch(this.amountOfFiles) {
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
        }
        JOptionPane.showMessageDialog(null, notification, "Erfolg",
                JOptionPane.INFORMATION_MESSAGE);
    } // end of run
}
