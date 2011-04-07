package de.aikiit.bildbearbeiter.gui;
import de.aikiit.bildbearbeiter.util.ComponentGaugeUtil;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
/**
 * This class provides a progressbar with 2 lines. One is the progress bar itself,
 * the other is a String for text messages.
 *
 * @author hirsch, 13.10.2003
 * @version 2004-01-08 
 */
public class ProgressBar extends JFrame {
    final static private Logger LOG = Logger.getLogger(ProgressBar.class);

    private JLabel textInfo = null;
    private JProgressBar progressBar = null;

    /**
     * Creates a progress bar with the given amount as 100 percent.
     * @param maxCapacity Defines the 100%-scale for this progress bar.
     */
    public ProgressBar(int maxCapacity) {
        super("Fortschritt");
        init(maxCapacity);
    } // end of Konstruktor

    /**
     * Perform internal initialization of UI components.
     * @param maxCapacity Sets the 100% value of this component to this absolute value.
     */
    private void init(int maxCapacity) {
        this.setResizable(false);
        this.progressBar = new JProgressBar(0, maxCapacity);
        JLabel info = new JLabel("Dateien werden umbenannt..");
        this.textInfo = new JLabel();
        this.progressBar.setValue(0);
        this.progressBar.setStringPainted(true);

        this.getContentPane().setLayout(new GridLayout(3, 1));
        this.getContentPane().add(info);
        this.getContentPane().add(textInfo);
        this.getContentPane().add(progressBar);
        ComponentGaugeUtil.makeCentered(this);
        this.pack();
        this.setVisible(true);
    } // end of init

    /**
     * After any changes the UI needs to be repainted and recentered which is done by that method.
     * This method waits 200ms in order to make the UI more human readable.
     */
    public void updateUI() {
        this.pack();
        ComponentGaugeUtil.makeCentered(this);

        // TODO replace with a Timer - see http://download.oracle.com/javase/tutorial/uiswing/misc/timer.html
        try {
            Thread.sleep(200);
        } catch (Exception e) {
            LOG.error("Error during repaint of ProgressBar");
        }
    } // end of updateUI

    /**
     * Set progress by setting the amount of items that are processed successfully.
     * @param step Current step's number. Should be smaller than maximal capacity of this progress bar.
     */
    public void setProgress(int step) {
        this.progressBar.setValue(step);
    } // end of setProgress

    /**
     * Sets the part of this
     * @param textContent Content to show under the graphical progress bar.
     */
    public void setText(String textContent) {
        this.textInfo.setText(textContent);
    } // end of setText

    /**
     * Getter for text value.
     * @return Current text value.
     */
    public String getText() {
        return textInfo.getText();
    }

    /**
     * Getter for current step/progress.
     * @return Current absolute progress value (step).
     */
    public int getProgress() {
        return progressBar.getValue();
    }
} // end of class ProgressBar