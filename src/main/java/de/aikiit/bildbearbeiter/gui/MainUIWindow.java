/*
 * Created on 13.10.2003
 */
package de.aikiit.bildbearbeiter.gui;

import de.aikiit.bildbearbeiter.exception.InvalidDirectoryException;
import de.aikiit.bildbearbeiter.exception.NoFilesFoundException;
import de.aikiit.bildbearbeiter.image.DateinamenManipulierer;
import de.aikiit.bildbearbeiter.image.DateinamenZurueckUmbenenner;
import de.aikiit.bildbearbeiter.util.ComponentGaugeUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Sinn: Fenster zur Verzeichnisauswahl bieten
 *
 * @author hirsch, 13.10.2003
 * @version 2004-01-08
 */
public class MainUIWindow extends JFrame implements ActionListener {
    public static final String VERSION = "2011-03-21-0.1-SNAPSHOT";
    // REVIEW externalize into a constants class that is changed during mvn run?!
    private static String IMAGE_LOCATION = ".." + File.separator + ".." + File.separator + ".." + File.separator + ".." + File.separator + "image" + File.separator + "miniCamera.png";

    private static HelpWindow helpWindow = new HelpWindow();
    private JButton helpButton = null;
    private JButton goButton = null;
    private JButton endButton = null;
    private JButton infoButton = null;
    private JButton revertButton = null;
    private ImageDirectorySelector imageDirectorySelector = null;

    /**
     * Creates the main UI window and initializes all internal UI-components.
     */
    public MainUIWindow() {
        init();
    }

    /**
     * Helper class to perform the internal initialization of the UI.
     */
    private void init() {
        String os = "[" + System.getProperty("os.name");
        os += " " + System.getProperty("os.version");
        os += " " + System.getProperty("os.arch") + "]";

        this.setTitle("fotorenamer-DateinamenKonverter " + os);
        this.getContentPane().setLayout(new BorderLayout());
        this.setDefaultCloseOperation(3);

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
    public void actionPerformed(final ActionEvent event) {
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
                    "\nbildbearbeiter - fotorenamer\n\n" +
                            "Version: " + VERSION +
                            "\n\nAutor: P.Ottlinger, \nURL: http://www.aiki-it.de" +
                            "\n (C) 1996-2011",
                    "Versionsinfo",
                    JOptionPane.INFORMATION_MESSAGE);
        } else if ((event.getSource() == this.revertButton) ||
                (event.getSource() == this.goButton)) {
// Construct a new SwingWorker
// READ from http://www.0x13.de/index.php/code-snippets/51-swingworker-tutorial.html
            worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    // Rückgängig oder Starten machen
                    if (!imageDirectorySelector.isSelected()) {
                        JOptionPane.showMessageDialog(null,
                                "Bitte ein Verzeichnis eingeben und\ndann starten.",
                                "Ungültiges Verzeichnis angegegen",
                                JOptionPane.ERROR_MESSAGE);
                        return null;
                    } // end if

                    // Umbenennen ....
                    try {
                        if (event.getSource() == goButton) {
                            goButton.setEnabled(false);
                            goButton.setText("in progress");
                            new DateinamenManipulierer(imageDirectorySelector.toString());
                        } else {
                            new DateinamenZurueckUmbenenner(imageDirectorySelector.toString());
                            revertButton.setEnabled(false);
                            revertButton.setText("in progress");

                        } // end if
                    } catch (InvalidDirectoryException uv) {
                        JOptionPane.showMessageDialog(null,
                                "Das eingegebene Verzeichnis '" + uv.getMessage() +
                                        "' ist ungültig - bitte erneut versuchen.",
                                "Ungültiges Verzeichnis angegegen",
                                JOptionPane.ERROR_MESSAGE);
                    } catch (NoFilesFoundException kde) {
                        JOptionPane.showMessageDialog(null,
                                "Im Verzeichnis '" + kde.getMessage() +
                                        "' existieren keine umbenennbaren Dateien - bitte erneut versuchen.",
                                "Keine Dateien vorhanden",
                                JOptionPane.ERROR_MESSAGE);
                    } // end of catch kde
                    return null;
                }

                @Override
                protected void done() {
                    goButton.setEnabled(true);
                    goButton.setText("Starten");
                    revertButton.setEnabled(true);
                    revertButton.setText("Rückgängig machen");
                }
            };
            // Execute the SwingWorker; the GUI will not freeze
            worker.execute();


            /** Rückgängig oder Starten machen
             if(!imageDirectorySelector.isSelected()) {
             JOptionPane.showMessageDialog(null,
             "Bitte ein Verzeichnis eingeben und\ndann starten.",
             "Ungültiges Verzeichnis angegegen",
             JOptionPane.ERROR_MESSAGE);
             return;
             } // end if

             // Umbenennen ....
             try {
             if(event.getSource() == this.starten) {
             new DateinamenManipulierer(imageDirectorySelector.toString());
             } else {
             new DateinamenZurueckUmbenenner(imageDirectorySelector.toString());
             } // end if
             } catch(InvalidDirectoryException uv) {
             JOptionPane.showMessageDialog(null,
             "Das eingegebene Verzeichnis \t"+uv.getMessage()+
             " ist ungültig - bitte erneut versuchen.",
             "Ungültiges Verzeichnis angegegen",
             JOptionPane.ERROR_MESSAGE);
             return;
             } catch(NoFilesFoundException kde) {
             JOptionPane.showMessageDialog(null,
             "Im Verzeichnis\t"+kde.getMessage()+
             "existieren keine umbenennbaren Dateien - bitte erneut versuchen.",
             "Keine Dateien vorhanden",
             JOptionPane.ERROR_MESSAGE);
             return;
             } // end of catch kde
             */
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