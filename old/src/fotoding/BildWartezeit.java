package fotoding;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * Einstellen der Wartezeit/Verzögerung zwischen den einzelnen Bildern
 * User: hirsch
 * Date: 18.05.2003
 * Time: 23:45:54
 * To change this template use Options | File Templates.
 */
public class BildWartezeit extends JPanel implements ChangeListener{
    private int interval;
    private JSlider wartezeit;
    private JLabel bezeichnung;


    public BildWartezeit(int defaultWartezeit) {
        this.interval=defaultWartezeit;
        init();
        this.setVisible(true);
    } // end of Konstruktor

    /**
     * Init der grafischen Komponenten
     */
    private void init() {
        //Create the slider and its label
        bezeichnung = new JLabel("Wartezeit in Sekunden", JLabel.CENTER);
        bezeichnung.setAlignmentX(Component.CENTER_ALIGNMENT);
        wartezeit = new JSlider(JSlider.HORIZONTAL,0, 15, interval);
        wartezeit.addChangeListener(this);

        // Skala machen
        //Turn on labels at major tick marks.
        wartezeit.setMajorTickSpacing(5);
        wartezeit.setMinorTickSpacing(1);
        wartezeit.setPaintTicks(true);
        wartezeit.setPaintLabels(true);
        wartezeit.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));

        //Put everything in the pane.
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(bezeichnung);
        this.add(wartezeit);
        this.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    } // end of init

    /**
     * passt den Wert von interval an die Usereingabe an
     * @param e
     */
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
            interval = (int)source.getValue();
        } // end if
    } // end of stateChanged

    /**
     * aktuellen Interval als String zurückgeben
     * @return String
     */
    public String getStatus() {
        return ""+this.interval;
    } // end of getStatus
} // end of class
