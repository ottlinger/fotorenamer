/*
 * Created on 20.10.2003
 */
package de.aikiit.bildbearbeiter.gui;

import de.aikiit.bildbearbeiter.util.ComponentGaugeUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Sinn: Anzeige eines HTML-basierten Hilfefensters
 *
 * @author hirsch, 20.10.2003
 * @version 2004-01-08
 */
public class Hilfefenster extends JFrame implements ActionListener {
    private JButton ende = null;

    /**
     * erzeugt Hilfefenster und zeigt es aber *nicht* an
     */
    public Hilfefenster() {
        init();
    } // end of Konstruktor

    /**
     * Initialisierung der grafischen Komponenten und Fensteranzeige
     */
    private void init() {
        JPanel oben = null;
        JPanel unten = null;
        JScrollPane rollpanel = null;
        JEditorPane textfeld = null;

        this.setTitle("Programmhilfe");
        this.setResizable(false);
        oben = new JPanel(new FlowLayout());
        unten = new JPanel(new FlowLayout());

        // Inhalt des Textfeldes von einer URL laden
        textfeld = new JEditorPane();
        textfeld.setContentType("text/html");
        textfeld.setEditable(false);
        try {
            textfeld.setPage(Hilfefenster.class.getResource("../html/hilfe.html"));
            oben.add(textfeld);
        } catch (Exception e) {
            oben.setLayout(new GridLayout(3, 1));
            oben.add(new JLabel("Fehler in der Hilfe -"));
            oben.add(new JLabel("" + e.getMessage()));
            oben.add(new JLabel("" + e.getClass()));
        } // end of catch

        // Eigenschaften des Scrollpanels anpassen
        rollpanel = new JScrollPane(oben);
        rollpanel.setPreferredSize(new Dimension(350, 300));
        rollpanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Ende-Knopf
        this.ende = new JButton("Schließen");
        this.ende.addActionListener(this);
        this.ende.setMnemonic('S');
        unten.add(this.ende);

        // Zusammenbasteln ...
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(rollpanel, BorderLayout.NORTH);
        this.getContentPane().add(unten, BorderLayout.CENTER);

        // Zentrieren und Anzeigen
        this.pack();
        ComponentGaugeUtil.makeCentered(this);
    } // end of init

    /**
     * auf Ereignisse reagieren
     */
    public void actionPerformed(ActionEvent ereignis) {
        if (ereignis.getSource() == this.ende) {
            setVisible(false);
        }
    } // end of actionPerformed
} // end of class

