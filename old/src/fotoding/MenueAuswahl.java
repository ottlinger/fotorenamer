package fotoding;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
/**
 * grafische Auswahl des Indextyps -
 * dabei wird der erste Buchstabe zur Auswahl des Indexes benutzt
 * @see IndexeErstellen
 * @author hirsch
 * Date: 18.05.2003
 * Time: 23:04:00
 */
public class MenueAuswahl extends JPanel implements ActionListener {
    private JRadioButton webButton;
    private JRadioButton cdButton;
    private String status;

    /**
     * Standardkonstruktor
     */
    public MenueAuswahl() {
        init();
        this.setVisible(true);
    } // end of Konstruktor

    /**
     * INIT der GUI
     */
    private void init() {
        //Create the radio buttons.
        webButton = new JRadioButton("WWW-fähigen Fotoindex");
        webButton.setMnemonic('w');
        webButton.setSelected(true);

        /**
         * ACHTUNG!<br>
         * default-Belegung machen!
         * Sonst gibt es eine NULL-Pointer-Exception wenn man
         * ohne das Menü zu verändern auf Loslegen klickt
          */
        this.status=webButton.getActionCommand();

        cdButton = new JRadioButton("CD-fähigen Fotoindex");
        cdButton.setMnemonic('c');

        //Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        group.add(webButton);
        group.add(cdButton);

        //Register a listener for the radio buttons.
        webButton.addActionListener(this);
        cdButton.addActionListener(this);

        //Put the check boxes in a column in a panel
        this.setLayout(new GridLayout(0, 1));
        this.add(cdButton);
        this.add(webButton);
    } // end of init

    /**
     * Einfach den Status zurückgeben, der als ActionCommand ausgewählt wurde
     * @return String - Status der MenueAuswahl
     */
    public String getStatus() {
        return this.status;
    }  // end of getStatus

    /**
     * Listens to the radio buttons
     *
     */
    public void actionPerformed(ActionEvent e) {
           this.status=e.getActionCommand();
    } // end of actionPerformed
} // end of class
