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
package de.aikiit.fotorenamer.gui;

import de.aikiit.fotorenamer.exception.InvalidDirectoryException;
import de.aikiit.fotorenamer.exception.NoFilesFoundException;
import de.aikiit.fotorenamer.image.CreationDateFromExifImageRenamer;
import de.aikiit.fotorenamer.image.RemoveExifPrefixRenamer;
import de.aikiit.fotorenamer.image.RenameProgress;
import de.aikiit.fotorenamer.image.RenameResult;
import de.aikiit.fotorenamer.util.ComponentGaugeUtil;
import de.aikiit.fotorenamer.util.Version;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CancellationException;

import static de.aikiit.fotorenamer.util.LocalizationHelper.getBundleString;
import static de.aikiit.fotorenamer.util.LocalizationHelper.getParameterizedBundleString;

/**
 * This class holds the main application window and allows to select a directory
 * to perform the image name processing.
 *
 * @author hirsch, 13.10.2003
 * @version 2004-01-08
 */
public final class MainUIWindow extends JFrame implements ActionListener {
    /**
     * The logger of this class.
     **/
    private static final Logger LOG =
            LogManager.getLogger(MainUIWindow.class);

    /**
     * Provide version information in the UI (taken from maven).
     */
    private static final String VERSION = getParameterizedBundleString("fotorenamer.ui.main.version", //
            new SimpleDateFormat(getBundleString("fotorenamer.datepattern")).
                    format(new Date(Long.parseLong(
                            Version.TIMESTAMP))), //
            Version.VERSION, //
            Version.TIMESTAMP
    );
    /**
     * Relative location of the UI's image icon.
     */
    private static final String IMAGE_LOCATION =
            "/image/miniCamera.png";

    /**
     * Component containing the help window of this application.
     */
    private final HelpWindow helpWindow = new HelpWindow();
    /**
     * The UI's help button.
     */
    private JButton helpButton = null;
    /**
     * The UI's start button.
     */
    private JButton goButton = null;
    /**
     * The UI's exit button.
     */
    private JButton endButton = null;
    /**
     * The UI's info/versioning button.
     */
    private JButton infoButton = null;
    /**
     * The UI's revert renaming button.
     */
    private JButton revertButton = null;
    /**
     * Component that selects the directory to work on.
     */
    private ImageDirectorySelector imageDirectorySelector = null;
    // Accessed only on the EDT, including the selector's Enter action.
    private boolean renaming;

    private MainUIWindow() {
        init();
    }

    /**
     * Creates and shows the main UI window on the event dispatch thread.
     * Completes synchronously when called on that thread; otherwise queues startup.
     */
    public static void build() {
        if (SwingUtilities.isEventDispatchThread()) {
            new MainUIWindow();
        } else {
            SwingUtilities.invokeLater(MainUIWindow::new);
        }
    }

    /**
     * Helper class to perform the internal initialization of the UI.
     */
    private void init() {
        String os = "[" + System.getProperty("os.name");
        os += " " + System.getProperty("os.version");
        os += " " + System.getProperty("os.arch") + "]";

        this.setTitle(getParameterizedBundleString("fotorenamer.ui.main.title", os));
        this.getContentPane().setLayout(new BorderLayout());
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        // select directory
        JPanel verzeichnis = new JPanel(new BorderLayout());
        this.imageDirectorySelector = new ImageDirectorySelector(
                ComponentGaugeUtil.createImageIcon(IMAGE_LOCATION));
        imageDirectorySelector.onStart(() -> startRenaming(false));
        verzeichnis.add(imageDirectorySelector);

        // help
        JPanel knoepfe = new JPanel(new FlowLayout());
        this.helpButton = new JButton(getBundleString("fotorenamer.ui.main.menu.help"));
        this.helpButton.addActionListener(this);
        this.helpButton.setMnemonic(getBundleString("fotorenamer.ui.main.menu.help.mnemonic").charAt(0));
        knoepfe.add(this.helpButton);

        // info
        this.infoButton = new JButton(getBundleString("fotorenamer.ui.main.menu.info"));
        this.infoButton.addActionListener(this);
        this.infoButton.setMnemonic(getBundleString("fotorenamer.ui.main.menu.info.mnemonic").charAt(0));
        knoepfe.add(this.infoButton);

        // exit
        this.endButton = new JButton(getBundleString("fotorenamer.ui.main.menu.end"));
        this.endButton.addActionListener(this);
        this.endButton.setMnemonic(getBundleString("fotorenamer.ui.main.menu.end.mnemonic").charAt(0));
        knoepfe.add(this.endButton);

        // start
        this.goButton = new JButton(getBundleString("fotorenamer.ui.main.menu.start"));
        this.goButton.addActionListener(this);
        this.goButton.setMnemonic(getBundleString("fotorenamer.ui.main.menu.start.mnemonic").charAt(0));
        knoepfe.add(this.goButton);

        // revert
        this.revertButton = new JButton(getBundleString("fotorenamer.ui.main.menu.revert"));
        this.revertButton.addActionListener(this);
        this.revertButton.setMnemonic(getBundleString("fotorenamer.ui.main.menu.revert.mnemonic").charAt(0));
        knoepfe.add(this.revertButton);

        this.getContentPane().add(verzeichnis, BorderLayout.NORTH);
        this.getContentPane().add(knoepfe, BorderLayout.CENTER);
        this.pack();
        ComponentGaugeUtil.makeCentered(this);
        this.setVisible(true);
    }

    /**
     * Action listener method to react on events.
     *
     * @param event Events that was fired on this component.
     */
    public void actionPerformed(final ActionEvent event) {
        Object source = event.getSource();
        // end
        if (this.endButton.equals(source)) {
            LOG.info("Bye Bye :-)");
            System.exit(0);
        } else if (this.helpButton.equals(source)) {
            LOG.info("Displaying help window.");
            showHelpWindow();
        } else if (this.infoButton.equals(source)) {
            // info
            JOptionPane.showMessageDialog(null,
                    getParameterizedBundleString("fotorenamer.ui.about", VERSION, new SimpleDateFormat("yyyy").
                            format(new Date(
                                    Long.parseLong(
                                            Version.TIMESTAMP)))),
                    getBundleString("fotorenamer.ui.main.version.title"),
                    JOptionPane.INFORMATION_MESSAGE);
        } else if (this.revertButton.equals(source) || this.goButton.equals(source)) {
            startRenaming(revertButton.equals(source));
        }
    }

    /** Takes a snapshot of UI input before starting a single background job. */
    private void startRenaming(final boolean reverse) {
        if (renaming) {
            return;
        }
        String directory = imageDirectorySelector.getSelectedDirectory();
        if (directory == null || directory.isBlank()) {
            JOptionPane.showMessageDialog(this, getBundleString("fotorenamer.ui.error.nodirectory"),
                    getBundleString("fotorenamer.ui.error.nodirectory.title"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        setProcessing(true, reverse);
        ProgressBar progress;
        try {
            progress = new ProgressBar(0);
        } catch (RuntimeException e) {
            setProcessing(false, reverse);
            showFailure(directory, e);
            return;
        }
        RenameWorker.View view = new RenameWorker.View() {
            @Override
            public void progress(RenameProgress update) {
                progress.updateProgress(update);
            }

            @Override
            public void finished() {
                try {
                    progress.dispose();
                } finally {
                    setProcessing(false, reverse);
                }
            }

            @Override
            public void result(RenameResult result) {
                showResult(directory, reverse, result);
            }

            @Override
            public void failure(Throwable cause) {
                showFailure(directory, cause);
            }
        };
        RenameWorker worker = new RenameWorker(() -> reverse
                ? new RemoveExifPrefixRenamer(directory) : new CreationDateFromExifImageRenamer(directory), view);
        progress.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                worker.requestCancellation();
                progress.setTitle(getBundleString("fotorenamer.ui.progress.cancelling"));
            }
        });
        try {
            worker.execute();
        } catch (RuntimeException e) {
            view.finished();
            view.failure(e);
        }
    }

    private void setProcessing(final boolean busy, final boolean reverse) {
        renaming = busy;
        goButton.setEnabled(!busy);
        revertButton.setEnabled(!busy);
        imageDirectorySelector.setEnabled(!busy);
        goButton.setText(getBundleString(busy && !reverse
                ? "fotorenamer.ui.main.progress" : "fotorenamer.ui.main.menu.start"));
        revertButton.setText(getBundleString(busy && reverse
                ? "fotorenamer.ui.main.progress" : "fotorenamer.ui.main.menu.revert"));
    }

    private void showResult(final String directory, final boolean reverse, final RenameResult result) {
        String titleKey = result.cancelled() ? "fotorenamer.ui.rename.cancelled"
                : result.failed() > 0 ? "fotorenamer.ui.rename.result.warning"
                : reverse ? "fotorenamer.ui.rerename.success.title" : "fotorenamer.ui.rename.success.title";
        String message = getParameterizedBundleString("fotorenamer.ui.rename.result.summary", directory,
                result.processed(), result.total(), result.renamed(), result.skipped(), result.failed());
        JOptionPane.showMessageDialog(this, message, getBundleString(titleKey),
                result.cancelled() || result.failed() > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    private void showFailure(final String directory, final Throwable cause) {
        if (cause instanceof CancellationException) {
            LOG.info("Processing cancelled for '{}'", directory);
            JOptionPane.showMessageDialog(this, getBundleString("fotorenamer.ui.rename.cancelled"),
                    getBundleString("fotorenamer.ui.rename.cancelled"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LOG.error("Unable to process images in '{}'", directory, cause);
        String messageKey = cause instanceof InvalidDirectoryException ? "fotorenamer.ui.error.invaliddirectory"
                : cause instanceof NoFilesFoundException ? "fotorenamer.ui.error.nofiles" : "fotorenamer.ui.rename.error";
        String titleKey = cause instanceof InvalidDirectoryException ? "fotorenamer.ui.error.invaliddirectory.title"
                : cause instanceof NoFilesFoundException ? "fotorenamer.ui.error.nofiles.title" : "fotorenamer.ui.rename.error.title";
        String detail = cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
        JOptionPane.showMessageDialog(this, getParameterizedBundleString(messageKey, detail),
                getBundleString(titleKey), JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Helper to enable visibility of help window.
     *
     * @see HelpWindow
     */
    private void showHelpWindow() {
        helpWindow.setVisible(true);
    }
}
