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
import de.aikiit.fotorenamer.util.LocalizationHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import static de.aikiit.fotorenamer.util.LocalizationHelper.getBundleString;
import static de.aikiit.fotorenamer.util.LocalizationHelper.getParameterizedBundleString;

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
    private final ImageIcon imageIcon;

    /**
     * Default constructor provides means to create an imageSelect with a given
     * image icon that is able to only work on directories.
     *
     * @param icon This icon is used as a picture in the select
     *             button.
     */
    ImageDirectorySelector(final ImageIcon icon) {
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
    }

    /**
     * This method is used as a blocking call until the user selects something
     * in the UI.
     *
     * @return Returns whether anything is selected within the current
     * configuration.
     */
    // TODO improve design, let class emit an event in case a directory was selected
    public final boolean isWaiting() {
        return Strings.isEmpty(getSelectedDirectory());
    }

    /**
     * Initialize internal UI components.
     */
    private void init() {
        createBaseLayout();
        initTextField();
        makeTextFieldDragAndDropable();
        makeTextFieldReactOnEnterOrCopyPasteFromMainUI();
    }

    private void createBaseLayout() {
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
        browseButton = this.imageIcon == null
                ? new JButton(
                getBundleString(
                        "fotorenamer.ui.selector.title"))
                : new JButton(
                getBundleString(
                        "fotorenamer.ui.selector.title"), this.imageIcon);
        browseButton.setMnemonic(getBundleString("fotorenamer.ui.selector.title.mnemonic").charAt(0));
        browseButton.setMargin(new Insets(1, 1, 1, 1));
        grid.setConstraints(browseButton, gbc);
        add(browseButton);
    }

    private void initTextField() {
        // Add action listener and take current contents of textfield as start directory
        browseButton.addActionListener(event -> {

            String currentPath = getSelectedDirectory();
            JFileChooser fileDlg = new JFileChooser(com.google.common.base.Strings.isNullOrEmpty(currentPath) ? null : currentPath);

            fileDlg.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileDlg.setDialogTitle(
                    getBundleString(
                            "fotorenamer.ui.selector.directory"));
            fileDlg.setApproveButtonText(getBundleString(
                    "fotorenamer.ui.selector.select"));

            if (fileDlg.showOpenDialog(this)
                    == JFileChooser.APPROVE_OPTION) {
                // use getCanonicalPath() to avoid ..-path manipulations and
                // try to set the selected file in the UI
                try {
                    textField.setText(
                            fileDlg.getSelectedFile().getCanonicalPath());
                } catch (IOException ioe) {
                    LOG.error("Error while selecting directory, extracted text is: {}", textField.getText());
                    LOG.error(ioe.getMessage());
                }
            }
        });
    }

    private void makeTextFieldReactOnEnterOrCopyPasteFromMainUI() {
        // make textfield react on Enter/copied over from MainUIWindow
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {

                    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() {
                            if (isWaiting()) {
                                showErrorPopup(getBundleString("fotorenamer.ui.error.nodirectory"), getBundleString("fotorenamer.ui.error.nodirectory.title"));
                                return null;
                            }

                            // perform renaming
                            try {
                                CreationDateFromExifImageRenamer renamer =
                                        new CreationDateFromExifImageRenamer(getSelectedDirectory());
                                new Thread(renamer).start();
                            } catch (InvalidDirectoryException uv) {
                                LOG.info("Invalid directory selected: {}", uv.getMessage());
                                showErrorPopup(getParameterizedBundleString("fotorenamer.ui.error.invaliddirectory", uv.getMessage()), getBundleString("fotorenamer.ui.error.invaliddirectory.title"));
                            } catch (NoFilesFoundException kde) {
                                LOG.info("No files found in {}", kde.getMessage());
                                showErrorPopup(getParameterizedBundleString("fotorenamer.ui.error.nofiles", kde.getMessage()), getBundleString("fotorenamer.ui.error.nofiles.title"));
                            }
                            return null;
                        }

                        @Override
                        protected void done() {
                            // TODO how can I communicate with the surrounding UI to block the user from pressing the buttons
                            LOG.debug("Finished working, cannot reset UI from the selector itself. Should find a way to lock the startbutton somehow.");
                        }
                    };
                    // Execute the SwingWorker; GUI will not freeze
                    worker.execute();
                }
            }

        });
    }

    private void makeTextFieldDragAndDropable() {
        // make textfield drag'n'dropable
        textField.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    Object transferData = evt
                            .getTransferable().getTransferData(
                                    DataFlavor.javaFileListFlavor);

                    if (transferData instanceof java.util.List) {
                        //noinspection unchecked
                        java.util.List<File> droppedFiles = (java.util.List<File>) transferData;
                        if (!droppedFiles.isEmpty()) {
                            for (File droppedFile : droppedFiles) {
                                if (droppedFile.isDirectory()) {
                                    final String path = droppedFile.getAbsolutePath();
                                    LOG.info("Drag'n'drop done for file: {} with {} element(s) received", path, droppedFiles.size());
                                    textField.setText(path);
                                    break;
                                }
                            }
                        }
                    }

                } catch (Exception ex) {
                    LOG.info("Drag'd'drop did not work due to exception", ex);
                }
            }
        });
    }

    void showErrorPopup(final String message, final String title) {
        JOptionPane.showMessageDialog(null,
                message,
                title,
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Current directory is the representation of this component.
     *
     * @return The currently selected directory.
     */
    final String getSelectedDirectory() {
        String currentSelection = LocalizationHelper.removeCrLf(this.textField.getText());
        if (!com.google.common.base.Strings.isNullOrEmpty(currentSelection)) {
            currentSelection = currentSelection.replaceAll("~", System.getProperty("user.home"));
            currentSelection = currentSelection.trim();
            LOG.debug("User input transformed into {}", currentSelection);
        }
        return currentSelection;
    }
}
