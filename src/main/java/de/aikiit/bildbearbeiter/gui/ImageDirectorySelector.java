/*
 * Created on 13.10.2003
 */
package de.aikiit.bildbearbeiter.gui;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * This component provides a means to select images that are to be renamed.
 *
 * @author hirsch, 13.10.2003
 * @version 2004-01-08
 */
public class ImageDirectorySelector extends JPanel {
    private String selection = null;
    private JTextField textField = null;
    private JButton browseButton = null;
    private ImageIcon imageIcon = null;
    private boolean directoryOnly = false;

    /**
     * Default constructor provides means to create an imageSelect with a given
     * image icon that is able to only work on directories.
     *
     * @param onlyDirectory Sets whether this class should only work on
     *                      directories.
     * @param icon          This icon is used as a picture in the select
     *                      button.
     */
    public ImageDirectorySelector(final boolean onlyDirectory,
                                  final ImageIcon icon) {
        super();
        this.imageIcon = icon;
        this.directoryOnly = onlyDirectory;
        init();
    }

    /**
     * @param enable Enable/disable this component.
     */
    public final void setEnabled(final boolean enable) {
        textField.setEnabled(enable);
        browseButton.setEnabled(enable);
    } // end of setEnabled

    /**
     * This method is used as a blocking call until the user selects something
     * in the UI.
     *
     * @return Returns whether anything is selected within the current
     *         configuration.
     */
    public final boolean isSelected() {
        return selection != null;
    } // end of isSelected


    /**
     * Initialize internal UI components.
     */
    protected final void init() {
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
        if (this.imageIcon == null) {
            browseButton = new JButton("Verzeichnisauswahl");
        } else {
            browseButton = new JButton("Verzeichnisauswahl", this.imageIcon);
        }

        browseButton.setMnemonic('v');
        browseButton.setMargin(new Insets(1, 1, 1, 1));
        grid.setConstraints(browseButton, gbc);
        add(browseButton);

        // Add focus listener.
        textField.addFocusListener(new FocusListener() {
            public void focusGained(final FocusEvent event) {
            } // end of focusGained

            public void focusLost(final FocusEvent event) {
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
            public void actionPerformed(final ActionEvent event) {
                JFileChooser fileDlg = new JFileChooser();

                if (directoryOnly) {
                    fileDlg.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    fileDlg.setDialogTitle("Bitte Verzeichnis auswählen.");
                } else {
                    fileDlg.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    fileDlg.setDialogTitle("Bitte eine Datei auswählen");
                } // end if
                fileDlg.setApproveButtonText("Auswählen");

                if (fileDlg.showOpenDialog(ImageDirectorySelector.this)
                        == JFileChooser.APPROVE_OPTION) {
                    textField.setText(
                            fileDlg.getSelectedFile().getAbsolutePath());
                    selection = textField.getText();
                } // end if
            } // end of actionPerformed
        });
    } // end of init

    /**
     * Current directory is the representation of this component.
     *
     * @return The currently selected directory.
     */
    @Override
    public final String toString() {
 // FIXME: http://cwe.mitre.org/data/definitions/22.html#Demonstrative%20Examples - use getCanonicalPath() in Java to avoid ..-path manipulations
        return this.selection;
    } // end of toString
} // end of class
