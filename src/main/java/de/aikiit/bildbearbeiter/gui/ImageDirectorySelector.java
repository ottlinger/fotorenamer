/*
 * Created on 13.10.2003
 */
package de.aikiit.bildbearbeiter.gui;

import de.aikiit.bildbearbeiter.util.LocalizationHelper;
import org.apache.log4j.Logger;

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
import java.io.IOException;

/**
 * This component provides a means to select images that are to be renamed.
 *
 * @author hirsch, 13.10.2003
 * @version 2004-01-08
 */
public class ImageDirectorySelector extends JPanel {
    /** The logger of this class. **/
    private static final Logger LOG =
            Logger.getLogger(ImageDirectorySelector.class);

    /** Contains the selected directory as a text field or any user input. */
    private JTextField textField = null;
    /** The UI's button to start directory selection. */
    private JButton browseButton = null;
    /** An image icon that is displayed as part of the button. */
    private ImageIcon imageIcon = null;
    /** Should this component be used to select directories only, default value is <code>false</code>. **/
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
     * Provides a means to disable this component
     * (e.g. during run of file renaming).
     *
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
        return (this.textField.getText() != null);
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

        // show button
        browseButton = (this.imageIcon == null
                ? new JButton(LocalizationHelper.getBundleString("fotorenamer.ui.selector.title"))
                : new JButton(LocalizationHelper.getBundleString("fotorenamer.ui.selector.title"), this.imageIcon));
        browseButton.setMnemonic('v');
        browseButton.setMargin(new Insets(1, 1, 1, 1));
        grid.setConstraints(browseButton, gbc);
        add(browseButton);

        // TODO add method to read contents that a user typed in
        // Add action listener.
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                textField.setText("");
                JFileChooser fileDlg = new JFileChooser();

                if (directoryOnly) {
                    fileDlg.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    fileDlg.setDialogTitle(LocalizationHelper.getBundleString("fotorenamer.ui.selector.directory"));
                } else {
                    fileDlg.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    fileDlg.setDialogTitle(LocalizationHelper.getBundleString("fotorenamer.ui.selector.file"));
                } // end if
                fileDlg.setApproveButtonText(LocalizationHelper.getBundleString("fotorenamer.ui.selector.select"));

                if (fileDlg.showOpenDialog(ImageDirectorySelector.this)
                        == JFileChooser.APPROVE_OPTION) {
                    // use getCanonicalPath() to avoid ..-path manipulations and
                    // try to set the selected file in the GUI
                    try {
                        textField.setText(fileDlg.getSelectedFile().getCanonicalPath());
                    } catch (IOException ioe) {
                        LOG.error("Error while selecting directory, extracted text is: " + textField.getText());
                        LOG.error(ioe.getMessage());
                    }
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
        return this.textField.getText();
    } // end of toString
} // end of class
