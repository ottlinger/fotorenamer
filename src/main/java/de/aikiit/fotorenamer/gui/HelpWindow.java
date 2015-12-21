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
     * Relative location of the UI's image icon.
     */
    private static final String HTML_HELP_LOCATION = File.separator + "html"
            + File.separator + "hilfe.html";

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
        JPanel oben = new JPanel(new FlowLayout());
        JPanel unten = new JPanel(new FlowLayout());

        // TODO read contents from URL?
        JEditorPane textfeld = new JEditorPane();
        textfeld.setContentType("text/html");
        textfeld.setEditable(false);
        try {
            textfeld.setPage(HelpWindow.class.getResource(HTML_HELP_LOCATION));
            oben.add(textfeld);
        } catch (Exception e) {
            oben.setLayout(new GridLayout(1, 1));
            oben.add(new JLabel(String.format(LocalizationHelper.getBundleString("fotorenamer.ui.help.error"), e.getMessage(), e.getClass().getSimpleName())));
        }

        JScrollPane rollpanel = new JScrollPane(oben);
        rollpanel.setPreferredSize(new Dimension(350, 300));
        rollpanel.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        this.endButton = new JButton(LocalizationHelper.getBundleString(
                                "fotorenamer.ui.help.close"));
        this.endButton.addActionListener(this);
        this.endButton.setMnemonic(LocalizationHelper.getBundleString("fotorenamer.ui.help.close.mnemonic").charAt(0));
        unten.add(this.endButton);

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(rollpanel, BorderLayout.NORTH);
        this.getContentPane().add(unten, BorderLayout.CENTER);

        this.pack();
        ComponentGaugeUtil.makeCentered(this);
        LOG.debug("HelpWindow init done.");
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
