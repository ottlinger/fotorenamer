/*
 * Created on 13.10.2003
 */
package de.aikiit.bildbearbeiter.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Sinn: Verzeichnis zur Dateinamenänderung auswählen
 *
 * @author hirsch, 13.10.2003
 * @version 2004-01-08
 */
public class VerzeichnisWaehler extends JPanel {
    private String selection = null;
    private JTextField textField = null;
    private JButton browseButton = null;
    private ImageIcon bild = null;
    private boolean directory = false;

    /**
     * erzeugt einen neuen Verzeichniswähler, wenn nur Verzeichnisse
     * auswählbar sein sollen TRUE übergeben
     *
     * @param directoryOnly sollen nur Verzeichnisse wählbar sein ?
     */
    public VerzeichnisWaehler(boolean directoryOnly) {
        super();
        this.directory = directoryOnly;
        init();
    } // end of Konstruktor

    /**
     * erzeugt Verzeichniswähler mit einem ImageIcon als
     * Bild für den Auswahlknopf
     *
     * @param directory
     * @param icon
     */
    public VerzeichnisWaehler(boolean directory, ImageIcon icon) {
        super();
        this.bild = icon;
        this.directory = directory;
        init();
    } // end of Konstruktor1

    /**
     * schaltet Komponenten ein uns aus
     *
     * @param b
     */
    public void setEnabled(boolean b) {
        textField.setEnabled(b);
        browseButton.setEnabled(b);
    } // end of setEnabled

    /**
     * as long as there has not occured any selection
     * it's usually not of use to go on with the programm
     *
     * @return Returns whether anything is selected within the current configuration.
     */
    public boolean isSelected() {
        return (selection != null && selection != "");
    } // end of isSelected


    /**
     * GUI-Init
     */
    protected void init() {
        // Set layout.
        GridBagLayout grid = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 2, 0, 2);
        setLayout(grid);

        // Add field.
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        textField = new JTextField(60);
        grid.setConstraints(textField, gbc);
        add(textField);

        // Add button.
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;

        // mit Button ?
        if (this.bild == null) {
            browseButton = new JButton("Verzeichnisauswahl");
        } else {
            browseButton = new JButton("Verzeichnisauswahl", this.bild);
        }

        browseButton.setMnemonic('v');
        browseButton.setMargin(new Insets(1, 1, 1, 1));
        grid.setConstraints(browseButton, gbc);
        add(browseButton);

        // Add focus listener.
        textField.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent event) {
            } // end of focusGained

            public void focusLost(FocusEvent event) {
                if (!event.isTemporary()) {
                    /** i am not really sure what is going on here
                     * but i followed rick's implementation
                     */
                    selection = textField.getText();
                } // end if
            } // end of focusLost
        });

        // Add action listener.
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JFileChooser fileDlg = new JFileChooser();

                if (directory) {
                    fileDlg.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    fileDlg.setDialogTitle("Bitte Verzeichnis auswählen.");
                } else {
                    fileDlg.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    fileDlg.setDialogTitle("Bitte eine Datei auswählen");
                } // end if
                fileDlg.setApproveButtonText("Auswählen");

                if (fileDlg.showOpenDialog(VerzeichnisWaehler.this) ==
                        JFileChooser.APPROVE_OPTION) {
                    textField.setText(fileDlg.getSelectedFile().getAbsolutePath());
                    selection = textField.getText();
                } // end if
            } // end of actionPerformed
        });
    } // end of init

    /**
     * ausgewähltes Element ist Repräsentation dieses Verzeichniswählers
     */
    public String toString() {
        return this.selection;
    } // end of toString
} // end of class