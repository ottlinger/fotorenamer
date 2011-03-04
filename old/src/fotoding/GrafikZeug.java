/**
 * Grafische Oberfläche für FotoDing 
 * Created on 15.05.2003
* @author hirsch
*/
package fotoding;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;


public class GrafikZeug extends JFrame implements ActionListener,ChangeListener {
// Klassvars
// die panel selbst
   private JPanel oben;
   private JPanel unten; 

// unten
   private JButton ende;
   private ImageIcon iconEndeKnopf;
   private ImageIcon iconPfadKnopf;
   private JButton farbe;
   private ImageIcon iconFarbeKnopf;
   private JButton start;
   private ImageIcon iconStartKnopf;
   private JButton kopfzeile;
   private ImageIcon iconKopfzeileKnopf;
   private JButton hilfe;
   private ImageIcon iconHilfeKnopf;

// oben
    private JTextField ueberschrift;
    private JTextField maske;
	private FileEditor dir;
    private BildWartezeit interval;
    private Dimension schirmgroesse;
    private MenueAuswahl indextyp;


	/**
	 * Standardkonstruktor -
	 * INIT der GUI und Anzeige
	 *
	 */
	public GrafikZeug() {
		setScreenSize();
		init();
		this.setVisible(true);
	} // end of Konstruktor

	/** Returns an ImageIcon, or null if the path was invalid. */
	 protected static ImageIcon createImageIcon(String path) {
		 java.net.URL imgURL = GrafikZeug.class.getResource(path);
		 if (imgURL != null) {
			 return new ImageIcon(imgURL);
		 } else {
			 System.err.println("createImageIcon:URL ungültig: " + path);
			 return null;
		 }
	 } // end of createImageIcon

	/**
	 * Auflösung des aktuellen Schirms bestimmen, um Fenster mittig anzuordnen
	 */
	void setScreenSize() {
		this.schirmgroesse=Toolkit.getDefaultToolkit().getScreenSize();
	} // end of setScreenSize
	
	
	/** Komponenten mittig anordnen */
	public static void makeCentered(Component w)    {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension dim = toolkit.getScreenSize();
		int screenWidth = dim.width;
		int screenHeight = dim.height;
		int x = (screenWidth - w.getSize().width) / 2;
		int y = (screenHeight - w.getSize().height) / 2;
		w.setLocation(x, y);
	} // end of makeCentered 


	/** Grösse des aktuellen Fensters anhand der Bildschirmgroesse bestimmen
	 * Höhe: 35%
	 * Breite: 45%
	 * @return Dimension
	 */ 
	private Dimension getWindowSize() {
		return new Dimension((int)(this.schirmgroesse.getWidth()*.47),(int)(this.schirmgroesse.getHeight()*.35));
	} // end of getWindowSize

	/**
	 * auf Befehle reagieren
	 */
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		System.out.println("actionPerformed.CMD:"+command);

		if(command.equals("Ende")) {
			System.out.println("Exiting...");
            sauberBeenden();
		} // end-if ENDE

		if(command.equals("Loslegen")) {

            // Verzeichnis ausgewählt
            if(dir.isSelected()) {
                JOptionPane.showMessageDialog(this,
                        "Bitte ein Verzeichnis auswählen, dass die Bilder enthält.",
			            "Eingabe-Fehler",
                        JOptionPane.ERROR_MESSAGE);
                return;
            } // end if-dir

            System.out.println("** LOS ***");
            System.out.println("Pfad:"+dir.getSelection());
            System.out.println("Headline:"+ueberschrift.getText());
            System.out.println("Maske:"+maske.getText());
            System.out.println("Schieber:"+interval.getStatus());
            System.out.println("Menue:"+indextyp.getStatus());
            System.out.println("** /LOS ***");

            new IndexeErstellen(ueberschrift.getText(),interval.getStatus(),dir.getSelection(),maske.getText(),indextyp.getStatus(),this);
		} // end-if Farbauswahl
	} // end of actionPerformed
	
	

	/**
	 * Farbänderung sofort übernehmen in Headline
	 * @param e
	 */
	public void stateChanged(ChangeEvent e) {
		System.out.println("stateChange: "+e);
//		Color newColor = farbauswahl.getColor();
//		ueberschrift.setForeground(newColor);
	}	
	
	/**
	 * Init der GUI-Komponenten
	 *
	 */
	private void init() {
			// Frame selbst

//		statisch: this.setDefaultLookAndFeelDecorated(true);
		this.getContentPane().setLayout(new BorderLayout());
		
			// unten
			unten = new JPanel(new FlowLayout());
			// defaultVerzeichnis = Verzeichnis der CLASS-Dateien
			/**
			 * Initialisierung der Button mit Bildern und ActionListener
			 */
				iconEndeKnopf= createImageIcon("pic/ende.png");
				ende = new JButton("Ende",iconEndeKnopf);
				ende.setMnemonic('e');
				ende.addActionListener(this);
				unten.add(ende);
					
				iconFarbeKnopf= createImageIcon("pic/farbe.png");
				farbe = new JButton("Farbauswahl",iconFarbeKnopf);
				farbe.setMnemonic('f');
				farbe.addActionListener(this);
				unten.add(farbe);
					
				iconKopfzeileKnopf= createImageIcon("pic/kopf.png");
				kopfzeile = new JButton("Überschrift",iconKopfzeileKnopf);
				kopfzeile.setMnemonic('b');
				kopfzeile.addActionListener(this);
				unten.add(kopfzeile);
					
				iconHilfeKnopf= createImageIcon("pic/hilfe.png");
				hilfe = new JButton("Hilfe",iconHilfeKnopf);
				hilfe.setMnemonic('h');
				hilfe.addActionListener(this);
				unten.add(hilfe);
				
				iconStartKnopf= createImageIcon("pic/start.png");
				start = new JButton("Loslegen",iconStartKnopf);
				start.setMnemonic('l');
				start.addActionListener(this);
				unten.add(start);

				// oben 
				oben = new JPanel(new BorderLayout());

                // Menueauswahl
                indextyp=new MenueAuswahl();
                oben.add(indextyp,BorderLayout.EAST);

                // Wartezeit
                interval=new BildWartezeit(5);
                oben.add(interval,BorderLayout.WEST);

				// Verzeichnisauswahl
				iconPfadKnopf= createImageIcon("pic/pfad.png");
				dir=new FileEditor(true,iconPfadKnopf);
				oben.add(dir,BorderLayout.NORTH);

                ueberschrift = new JTextField(100);
                ueberschrift.setText("TestHeadline 2003");
                oben.add(ueberschrift,BorderLayout.SOUTH);

                maske = new JTextField(10);
                maske.setText("png");
                oben.add(maske,BorderLayout.CENTER);

				// Zusammenbasteln und Anzeigen
				unten.setVisible(true);
				oben.setVisible(true);
				
				this.getContentPane().add(oben,BorderLayout.NORTH);
				this.getContentPane().add(unten,BorderLayout.CENTER);
				this.pack();
//				this.setSize(getWindowSize());
				makeCentered(this);
				this.setTitle("FotoIndexer - (C) 2003 PO-Computer Berlin, http://www.po-computer.de");
	} // end of init


    /**
     * Damit alle SWING-Threads zuende gehen, muss mehr getan werden als nur
     * exit aufzurufen
     * @todo Hier später einbauen, dass die letzten Einstellungen irgendwie gespeichert werden oder so
     */
    private void sauberBeenden() {
        this.dispose();
        System.exit(0);
    }  // end of sauberBeenden
} // end of class
