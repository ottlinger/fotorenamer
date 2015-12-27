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

import de.aikiit.fotorenamer.util.ComponentGaugeUtil;
import de.aikiit.fotorenamer.util.LocalizationHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * This class represents a help window component. It loads a HTML-page to show
 * as application help.
 *
 * @author hirsch, 20.10.2003
 * @version 2004-01-08
 */
class HelpWindow extends JFrame implements ActionListener {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LogManager.getLogger(HelpWindow.class);
    /**
     * End button needs to be visible inside the class to perform application
     * exit.
     */
    private JButton endButton = null;

    /**
     * Relative location of the UI's help page.
     */
    private static final String HTML_HELP_LOCATION = File.separator + "html"
            + File.separator;

    /**
     * Creates a HelpWindow, initializes its components but does
     * <strong>not</strong> show the window.
     */
    public HelpWindow() {
        SwingUtilities.invokeLater(this::init);
    }

    /**
     * Initialize UI-compontens and make them visible.
     */
    private void init() {
        this.setTitle(LocalizationHelper.getBundleString(
                "fotorenamer.ui.help.title"));
        this.setResizable(false);
        JPanel top = new JPanel(new FlowLayout());
        JPanel bottom = new JPanel(new FlowLayout());

        JEditorPane textfield = new JEditorPane();
        textfield.setContentType("text/html");
        textfield.setEditable(false);
        loadHelpContents(top, textfield);

        JScrollPane rollpanel = new JScrollPane(top);
        rollpanel.setPreferredSize(new Dimension(350, 300));
        rollpanel.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        this.endButton = new JButton(LocalizationHelper.getBundleString(
                "fotorenamer.ui.help.close"));
        this.endButton.addActionListener(this);
        this.endButton.setMnemonic(LocalizationHelper.getBundleString("fotorenamer.ui.help.close.mnemonic").charAt(0));
        bottom.add(this.endButton);

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(rollpanel, BorderLayout.NORTH);
        this.getContentPane().add(bottom, BorderLayout.CENTER);

        this.pack();
        ComponentGaugeUtil.makeCentered(this);
        LOG.debug("HelpWindow init done.");
    }

    /**
     * Load page contents depending on available languages (fallback is English).
     *
     * @param base base panel.
     * @param textfield where to put the contents to.
     */
    private void loadHelpContents(final JPanel base, final JEditorPane textfield) {
        try {
            String helpPageLocation = HTML_HELP_LOCATION;

            if(LocalizationHelper.getLanguage().startsWith("de")) {
                helpPageLocation += "hilfe.html";
            } else {
                helpPageLocation += "help.html";
            }

            textfield.setPage(HelpWindow.class.getResource(helpPageLocation));
            base.add(textfield);
        } catch (Exception e) {
            base.setLayout(new GridLayout(1, 1));
            base.add(new JLabel(LocalizationHelper.getParameterizedBundleString("fotorenamer.ui.help.error", e.getMessage(), e.getClass().getSimpleName())));
        }
    }

    /**
     * Make this component react to close button.
     *
     * @param event Event to react on in this UI-component.
     */
    public final void actionPerformed(final ActionEvent event) {
        if (event.getSource() == this.endButton) {
            LOG.debug("Disabling visibility of helpWindow.");
            setVisible(false);
        }
    }
}
