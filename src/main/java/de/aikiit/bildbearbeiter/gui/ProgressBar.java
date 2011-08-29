package de.aikiit.bildbearbeiter.gui;

import de.aikiit.bildbearbeiter.util.ComponentGaugeUtil;
import de.aikiit.bildbearbeiter.util.LocalizationHelper;
import org.apache.log4j.Logger;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import java.awt.GridLayout;

/**
 * This class provides a progressbar with 2 lines. One is the progress bar
 * itself, the other is a String for text messages.
 *
 * @author hirsch, 13.10.2003
 * @version 2004-01-08
 */
public class ProgressBar extends JFrame {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(ProgressBar.class);
    /**
     * By default the UI sleeps for 200 ms to be able to read the file names
     * during operation.
     */
    private static final int DEFAULT_UI_DELAY = 200;

    /** Text field to show current file or other information during run. */
    private JLabel textInfo = null;
    /** The visual progress bar. */
    private JProgressBar progressBar = null;
    /**
     * Amount of seconds the UI sleeps in order to give a
     * user a chance to read the screen,
     * this value is calculated dynamically
     * depending on the amount of pictures to be processed.
     */
    private int delayInUI = -1;

    /**
     * Creates a progress bar with the given amount as 100 percent.
     *
     * @param maxCapacity Defines the 100%-scale for this progress bar.
     */
    public ProgressBar(final int maxCapacity) {
        super();
        init(maxCapacity);
    }

    /**
     * Perform internal initialization of UI components.
     *
     * @param maxCapacity Sets the 100% value of this component to this absolute
     *                    value.
     */
    protected final void init(final int maxCapacity) {
        // set window title
        this.setTitle(
                LocalizationHelper.getBundleString("fotorenamer.ui.progress"));

        this.setResizable(false);
        this.progressBar = new JProgressBar(0, maxCapacity);

        JLabel info = new JLabel(
                LocalizationHelper.getBundleString(
                        "fotorenamer.ui.progress.title"));
        this.textInfo = new JLabel();
        this.progressBar.setValue(0);
        this.progressBar.setStringPainted(true);
        // default value: 200 ms
        // calculate delay in UI depending on maximum capacity of the bar
        // itself; the more files the lower the delay
        this.delayInUI = ((maxCapacity < 35)
                ? DEFAULT_UI_DELAY : (DEFAULT_UI_DELAY / 3));

        this.getContentPane().setLayout(new GridLayout(3, 1));
        this.getContentPane().add(info);
        this.getContentPane().add(textInfo);
        this.getContentPane().add(progressBar);
        ComponentGaugeUtil.makeCentered(this);
        this.pack();
        this.setVisible(true);
    } // end of init

    /**
     * After any changes the UI needs to be repainted and recentered which is
     * done by that method. This method waits 200ms in order to make the UI more
     * human readable.
     */
    public final void updateUI() {
        this.pack();
        ComponentGaugeUtil.makeCentered(this);

        // TODO replace with a Timer - see
        // http://download.oracle.com/javase/tutorial/uiswing/misc/timer.html
        try {
            Thread.sleep(this.delayInUI);
        } catch (Exception e) {
            LOG.error("Error during repaint of ProgressBar, " + e.getMessage());
        }
    } // end of updateUI

    /**
     * Set progress by setting the amount of items that are processed
     * successfully.
     *
     * @param step Current step's number. Should be smaller than maximal
     *             capacity of this progress bar.
     */
    public final void setProgress(final int step) {
        this.progressBar.setValue(step);
    } // end of setProgress

    /**
     * Sets the part of this component that shows above the graphical bar.
     *
     * @param textContent Content to show under the graphical progress bar.
     */
    public final void setText(final String textContent) {
        this.textInfo.setText(textContent);
    } // end of setText

    /**
     * Getter for text value.
     *
     * @return Current text value.
     */
    public final String getText() {
        return textInfo.getText();
    }

    /**
     * Getter for current step/progress.
     *
     * @return Current absolute progress value (step).
     */
    public final int getProgress() {
        return progressBar.getValue();
    }
} // end of class ProgressBar
