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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

import static de.aikiit.fotorenamer.util.LocalizationHelper.getBundleString;

/**
 * This component provides a means to select images that are to be renamed.
 *
 * @author hirsch, 13.10.2003
 * @version 2004-01-08
 */
class ImageDirectorySelector extends JPanel {
    /**
     * The logger of this class.
     **/
    private static final Logger LOG =
            LogManager.getLogger(ImageDirectorySelector.class);

    /**
     * Contains the selected directory as a text field or any user input.
     */
    private JTextField textField = null;
    /**
     * The UI's button to start directory selection.
     */
    private JButton browseButton = null;
    /**
     * An image icon that is displayed as part of the button.
     */
    private ImageIcon imageIcon = null;

    /**
     * Default constructor provides means to create an imageSelect with a given
     * image icon that is able to only work on directories.
     *
     * @param icon This icon is used as a picture in the select
     *             button.
     */
    public ImageDirectorySelector(final ImageIcon icon) {
        super();
        this.imageIcon = icon;
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
     * configuration.
     */
    public final boolean isWaiting() {
        return Strings.isEmpty(this.textField.getText());
    }


    /**
     * Initialize internal UI components.
     */
    private void init() {
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
                ? new JButton(
                getBundleString(
                        "fotorenamer.ui.selector.title"))
                : new JButton(
                getBundleString(
                        "fotorenamer.ui.selector.title"), this.imageIcon));
        browseButton.setMnemonic('v');
        browseButton.setMargin(new Insets(1, 1, 1, 1));
        grid.setConstraints(browseButton, gbc);
        add(browseButton);

        // TODO add method to read contents that a user typed in
        // Add action listener.
        browseButton.addActionListener(event -> {
            textField.setText("");
            JFileChooser fileDlg = new JFileChooser();

            fileDlg.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileDlg.setDialogTitle(
                    getBundleString(
                            "fotorenamer.ui.selector.directory"));
            fileDlg.setApproveButtonText(getBundleString(
                    "fotorenamer.ui.selector.select"));

            if (fileDlg.showOpenDialog(ImageDirectorySelector.this)
                    == JFileChooser.APPROVE_OPTION) {
                // use getCanonicalPath() to avoid ..-path manipulations and
                // try to set the selected file in the UI
                try {
                    textField.setText(
                            fileDlg.getSelectedFile().getCanonicalPath());
                } catch (IOException ioe) {
                    LOG.error("Error while selecting directory, "
                            + "extracted text is: "
                            + textField.getText());
                    LOG.error(ioe.getMessage());
                }
            }
        });
    }

    /**
     * Current directory is the representation of this component.
     *
     * @return The currently selected directory.
     */
    @Override
    public final String toString() {
        return this.textField.getText();
    }
}
