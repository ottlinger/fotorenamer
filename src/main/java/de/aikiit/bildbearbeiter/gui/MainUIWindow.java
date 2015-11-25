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
package de.aikiit.bildbearbeiter.gui;

import de.aikiit.bildbearbeiter.exception.InvalidDirectoryException;
import de.aikiit.bildbearbeiter.exception.NoFilesFoundException;
import de.aikiit.bildbearbeiter.image.CreationDateFromExifImageRenamer;
import de.aikiit.bildbearbeiter.image.DateinamenZurueckUmbenenner;
import de.aikiit.bildbearbeiter.util.ComponentGaugeUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * This class holds the main application window and allows to select a directory
 * to perform the image name processing.
 *
 * @author hirsch, 13.10.2003
 * @version 2004-01-08
 */
public class MainUIWindow extends JFrame implements ActionListener {
    /**
     * Provide version information in the UI (transferred from maven).
     */
    public static final String VERSION =
            new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").
                    format(new java.util.Date(Long.parseLong(
                            de.aikiit.bildbearbeiter.util.Version.TIMESTAMP)))
                    + " / SCM-rev: "
                    + de.aikiit.bildbearbeiter.util.Version.VERSION;
    /**
     * Relative location of the UI's image icon.
     */
    private static String IMAGE_LOCATION =
            File.separator + "image" + File.separator + "miniCamera.png";

    /**
     * Component containing the help window of this application.
     */
    private static HelpWindow helpWindow = new HelpWindow();
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

    /**
     * Creates the main UI window and initializes all internal UI-components.
     */
    public MainUIWindow() {
        SwingUtilities.invokeLater(() -> {
            init();
        });
    }

    /**
     * Helper class to perform the internal initialization of the UI.
     */
    protected final void init() {
        String os = "[" + System.getProperty("os.name");
        os += " " + System.getProperty("os.version");
        os += " " + System.getProperty("os.arch") + "]";

        this.setTitle("fotorenamer-DateinamenKonverter " + os);
        this.getContentPane().setLayout(new BorderLayout());
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Dateiauswahl
        JPanel verzeichnis = new JPanel(new BorderLayout());
        this.imageDirectorySelector = new ImageDirectorySelector(true,
                ComponentGaugeUtil.createImageIcon(IMAGE_LOCATION));
        verzeichnis.add(imageDirectorySelector);

        // Hilfe
        JPanel knoepfe = new JPanel(new FlowLayout());
        this.helpButton = new JButton("Hilfe");
        this.helpButton.addActionListener(this);
        this.helpButton.setMnemonic('H');
        knoepfe.add(this.helpButton);

        // Info
        this.infoButton = new JButton("Info");
        this.infoButton.addActionListener(this);
        this.infoButton.setMnemonic('I');
        knoepfe.add(this.infoButton);

        // Programmende
        this.endButton = new JButton("Ende");
        this.endButton.addActionListener(this);
        this.endButton.setMnemonic('e');
        knoepfe.add(this.endButton);

        // Los geht's...
        this.goButton = new JButton("Starten");
        this.goButton.addActionListener(this);
        this.goButton.setMnemonic('s');
        knoepfe.add(this.goButton);

        // Rückgängig machen
        this.revertButton = new JButton("Rückgängig machen");
        this.revertButton.addActionListener(this);
        this.revertButton.setMnemonic('r');
        knoepfe.add(this.revertButton);

        // Zusammenbasteln
        this.getContentPane().add(verzeichnis, BorderLayout.NORTH);
        this.getContentPane().add(knoepfe, BorderLayout.CENTER);
        this.pack();
        ComponentGaugeUtil.makeCentered(this);
        this.setVisible(true);
    } // end of init

    /**
     * Action listener method to react on events.
     *
     * @param event Events that was fired on this component.
     */
    public final void actionPerformed(final ActionEvent event) {
        final SwingWorker<Void, Void> worker;

        // Ende
        if (event.getSource() == this.endButton) {
            System.exit(0);
        } else if (event.getSource() == this.helpButton) {
            // Hilfe
            showHelpWindow();
        } else if (event.getSource() == this.infoButton) {
            // Info
            JOptionPane.showMessageDialog(null,
                    "\nbildbearbeiter - fotorenamer\n\n"
                            + "Version: " + VERSION
                            + "\n\nAutor: P.Ottlinger, "
                            + "\nURL: http://www.aiki-it.de"
                            + "\n (C) 1996-"
                            + new java.text.SimpleDateFormat("yyyy").
                            format(new java.util.Date(
                                    Long.parseLong(
                                            de.aikiit.bildbearbeiter.util.
                                                    Version.TIMESTAMP))),
                    "Versionsinfo",
                    JOptionPane.INFORMATION_MESSAGE);
        } else if (event.getSource() == this.revertButton || event.getSource()
                == this.goButton) {
            // Construct a new SwingWorker,  read from
            // http://www.0x13.de/index.php/code-snippets/51-swingworker-tutorial.html
            worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    // Rückgängig oder Starten machen
                    if (!imageDirectorySelector.isSelected()) {
                        JOptionPane.showMessageDialog(null,
                                "Bitte ein Verzeichnis eingeben und\n"
                                        + "dann starten.",
                                "Ungültiges Verzeichnis angegegen",
                                JOptionPane.ERROR_MESSAGE);
                        return null;
                    } // end if

                    // perform renaming
                    try {
                        if (event.getSource() == goButton) {
                            goButton.setEnabled(false);
                            goButton.setText("in progress");
                            CreationDateFromExifImageRenamer renamer =
                                    new CreationDateFromExifImageRenamer(
                                            imageDirectorySelector.toString()
                                    );
                            new Thread(renamer).start();
                        } else {
                            revertButton.setEnabled(false);
                            revertButton.setText("in progress");
                            new DateinamenZurueckUmbenenner(
                                    imageDirectorySelector.toString());

                        } // end if
                    } catch (InvalidDirectoryException uv) {
                        JOptionPane.showMessageDialog(null,
                                "Das eingegebene Verzeichnis '"
                                        + uv.getMessage()
                                        + "' ist ungültig - bitte erneut "
                                        + "versuchen.",
                                "Ungültiges Verzeichnis angegegen",
                                JOptionPane.ERROR_MESSAGE);
                    } catch (NoFilesFoundException kde) {
                        JOptionPane.showMessageDialog(null,
                                "Im Verzeichnis '" + kde.getMessage()
                                        + "' existieren keine umbenennbaren "
                                        + "Dateien - bitte erneut versuchen.",
                                "Keine Dateien vorhanden",
                                JOptionPane.ERROR_MESSAGE);
                    } // end of catch kde
                    return null;
                }

                @Override
                protected void done() {
                    // reset gui if all workers terminated
                    goButton.setEnabled(true);
                    goButton.setText("Starten");
                    revertButton.setEnabled(true);
                    revertButton.setText("Rückgängig machen");
                }
            };
            // Execute the SwingWorker; the GUI will not freeze
            worker.execute();

        } // end if-getSource
    } // end of actionPerformed

    /**
     * Helper to enable visibility of help window.
     *
     * @see HelpWindow
     */
    public static void showHelpWindow() {
        helpWindow.setVisible(true);
    } // end of showHelpWindow
} // end of class
