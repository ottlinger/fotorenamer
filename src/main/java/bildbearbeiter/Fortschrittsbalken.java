/*
 * Created on 13.10.2003
 */
package bildbearbeiter;

/**
 * Sinn: Fortschrittsbalken mit 2 Textzeilen
 * @author hirsch, 13.10.2003
 * @version 2004-01-08 
 */

import javax.swing.*;
import java.awt.*;

public class Fortschrittsbalken extends JFrame {
    private JLabel info = null;
    private JLabel detailinfo = null;
    private JProgressBar prog = null;

    /**
     * erzeugt einen Fortschrittsbalken mit obergrenze
     * als 100%-Marke
     *
     * @param obergrenze Höchststand
     */
    public Fortschrittsbalken(int obergrenze) {
        super("Fortschritt");
        this.prog = new JProgressBar(0, obergrenze);
        init();
    } // end of Konstruktor

    /**
     * grafische Komponenten initialisieren und Fortschrittsbalken
     * sichtbar machen
     */
    private void init() {
        this.setResizable(false);
        this.info = new JLabel("Dateien werden umbenannt..");
        this.detailinfo = new JLabel();
        this.prog.setValue(0);
        this.prog.setStringPainted(true);

        this.getContentPane().setLayout(new GridLayout(3, 1));
        this.getContentPane().add(info);
        this.getContentPane().add(detailinfo);
        this.getContentPane().add(prog);
        FensterZentrierer.makeCentered(this);
        this.pack();
        this.setVisible(true);
    } // end of init

    /**
     * Nach Änderung der Werte die Komponenten neu zeichnen
     * und mittig anordnen
     */
    public void updateUI() {
        this.pack();
        FensterZentrierer.makeCentered(this);

        // TODO Timer einbauen ?
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            System.err.println("Error during repaint of GUI");
        }
    } // end of updateUI

    /**
     * von draussen den Fortschritt in der Anzeige steuern
     *
     * @param step
     */
    public void setFortschritt(int step) {
        this.prog.setValue(step);
    } // end of setFortschritt

    /**
     * die 2.Zeile Info anpassen
     *
     * @param inhalt
     */
    public void setText(String inhalt) {
        this.detailinfo.setText(inhalt);
    } // end of setText
} // end of class Fortschrittsbalken
