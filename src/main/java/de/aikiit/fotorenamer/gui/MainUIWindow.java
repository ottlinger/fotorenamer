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
import de.aikiit.fotorenamer.util.ComponentGaugeUtil;
import de.aikiit.fotorenamer.util.Version;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

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
            File.separator + "image" + File.separator + "miniCamera.png";

    /**
     * Component containing the help window of this application.
     */
    private static final HelpWindow helpWindow = new HelpWindow();
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

    private MainUIWindow() {
        SwingUtilities.invokeLater(this::init);
    }

    /**
     * Creates the main UI window and initializes all internal UI-components.
     */
    public static void build() {
        new MainUIWindow();
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
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // select directory
        JPanel verzeichnis = new JPanel(new BorderLayout());
        this.imageDirectorySelector = new ImageDirectorySelector(
                ComponentGaugeUtil.createImageIcon(IMAGE_LOCATION));
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
        final SwingWorker<Void, Void> worker;

        // end
        if (event.getSource() == this.endButton) {
            LOG.info("Bye Bye :-)");
            System.exit(0);
        } else if (event.getSource() == this.helpButton) {
            LOG.info("Displaying help window.");
            showHelpWindow();
        } else if (event.getSource() == this.infoButton) {
            // info
            JOptionPane.showMessageDialog(null,
                    getParameterizedBundleString("fotorenamer.ui.about", VERSION, new SimpleDateFormat("yyyy").
                            format(new Date(
                                    Long.parseLong(
                                            Version.TIMESTAMP)))),
                    getBundleString("fotorenamer.ui.main.version.title"),
                    JOptionPane.INFORMATION_MESSAGE);
        } else if (event.getSource() == this.revertButton || event.getSource()
                == this.goButton) {
            worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    if (imageDirectorySelector.isWaiting()) {
                        JOptionPane.showMessageDialog(null,
                                getBundleString("fotorenamer.ui.error.nodirectory"),
                                getBundleString("fotorenamer.ui.error.nodirectory.title"),
                                JOptionPane.ERROR_MESSAGE);
                        return null;
                    }

                    // perform renaming
                    try {
                        if (event.getSource() == goButton) {
                            goButton.setEnabled(false);
                            goButton.setText(getBundleString("fotorenamer.ui.main.progress"));
                            CreationDateFromExifImageRenamer renamer =
                                    new CreationDateFromExifImageRenamer(
                                            imageDirectorySelector.getSelectedDirectory()
                                    );
                            new Thread(renamer).start();
                        } else {
                            revertButton.setEnabled(false);
                            revertButton.setText(getBundleString("fotorenamer.ui.main.progress"));
                            new RemoveExifPrefixRenamer(
                                    imageDirectorySelector.getSelectedDirectory());
                        }
                    } catch (InvalidDirectoryException uv) {
                        LOG.info("Invalid directory selected: " + uv.getMessage());
                        JOptionPane.showMessageDialog(null,
                                getParameterizedBundleString("fotorenamer.ui.error.invaliddirectory", uv.getMessage()),
                                getBundleString("fotorenamer.ui.error.invaliddirectory.title"),
                                JOptionPane.ERROR_MESSAGE);
                    } catch (NoFilesFoundException kde) {
                        LOG.info("No files found in " + kde.getMessage());
                        JOptionPane.showMessageDialog(null,
                                getParameterizedBundleString("fotorenamer.ui.error.nofiles", kde.getMessage()),
                                getBundleString("fotorenamer.ui.error.nofiles.title"),
                                JOptionPane.ERROR_MESSAGE);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    LOG.debug("Finished working, will reset UI.");
                    // reset gui if all workers terminated
                    goButton.setEnabled(true);
                    goButton.setText(getBundleString("fotorenamer.ui.main.menu.start"));
                    revertButton.setEnabled(true);
                    revertButton.setText(getBundleString("fotorenamer.ui.main.menu.revert"));
                }
            };
            // Execute the SwingWorker; GUI will not freeze
            worker.execute();
        }
    }

    /**
     * Helper to enable visibility of help window.
     *
     * @see HelpWindow
     */
    private static void showHelpWindow() {
        helpWindow.setVisible(true);
    }
}
