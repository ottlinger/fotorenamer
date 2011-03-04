/*
 * Created on 13.10.2003
 */
package bildbearbeiter;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import bildbearbeiter.ausnahmen.*;

/**
 * Sinn: Fenster zur Verzeichnisauswahl bieten
 * @author hirsch, 13.10.2003
 * @version 2004-01-08
 */
public class HauptGUI extends JFrame implements ActionListener {
	public static String version = "2011-03-04-0.1-SNAPSHOT";
	private static Hilfefenster hilfefenster = new Hilfefenster();
	private JButton hilfe = null;
	private JButton starten = null;
	private JButton ende = null;
	private JButton info = null;
	private JButton rueckgaengig = null;
	private JPanel verzeichnis = null;
	private VerzeichnisWaehler verzeichnisauswahl = null;
	private JPanel knoepfe = null;

	/**
	 * erzeugt die GUI und initialisiert die Komponenten
	 */
	public HauptGUI() {
		init();
	} // end of Konstruktor

	/**
	 * initialisiert die eigentliche GUI und hinterlegt die Logik der Komponenten
	 */
	private void init() {
		String os = new String("["+System.getProperty("os.name"));
		os+=" "+System.getProperty("os.version");
		os+=" "+System.getProperty("os.arch")+"]";
		
		this.setTitle("IXUS-DateinamenKonverter "+os);
		this.getContentPane().setLayout(new BorderLayout());
		this.setDefaultCloseOperation(3);
		
		// Dateiauswahl
		this.verzeichnis = new JPanel(new BorderLayout());
		this.verzeichnisauswahl = new VerzeichnisWaehler(true, 
							FensterZentrierer.createImageIcon("image/ixusButton.png"));
		this.verzeichnis.add(verzeichnisauswahl);
		
		// Hilfe
		this.knoepfe = new JPanel(new FlowLayout());
		this.hilfe = new JButton("Hilfe");
		this.hilfe.addActionListener(this);
		this.hilfe.setMnemonic('H');
		this.knoepfe.add(this.hilfe);

		// Info
		this.info = new JButton("Info");
		this.info.addActionListener(this);
		this.info.setMnemonic('I');
		this.knoepfe.add(this.info);

		// Programmende
		this.ende = new JButton("Ende");
		this.ende.addActionListener(this);
		this.ende.setMnemonic('e');
		this.knoepfe.add(this.ende);
		
		// Los geht's...
		this.starten = new JButton("Starten");
		this.starten.addActionListener(this);
		this.starten.setMnemonic('s');
		this.knoepfe.add(this.starten);
		
		// R�ckg�ngig machen
		this.rueckgaengig = new JButton("Rückgängig machen");
		this.rueckgaengig.addActionListener(this);
		this.rueckgaengig.setMnemonic('r');
		this.knoepfe.add(this.rueckgaengig);
		
		// Zusammenbasteln
		this.getContentPane().add(this.verzeichnis,BorderLayout.NORTH);
		this.getContentPane().add(this.knoepfe,BorderLayout.CENTER);
		this.pack();
		FensterZentrierer.makeCentered(this);
		this.setVisible(true);
	} // end of init

	public void actionPerformed(ActionEvent ereignis) {
		// Ende
		if( ereignis.getSource() == this.ende ) {
			System.exit(0);
		} else if( ereignis.getSource() == this.hilfe ) {
		// Hilfe
			zeigeHilfeAn();
		} else if( ereignis.getSource() == this.info ) {
		// Info
			JOptionPane.showMessageDialog(null,
						"\nbildbearbeiter - fotorenamer\n\n"+
						"Version: "+version+
						"\n\nAutor: P.Ottlinger, \nURL: http://www.aiki-it.de" +
						"\n (C) 1996-2011",
						 "Versionsinfo",
						 JOptionPane.INFORMATION_MESSAGE);
		} else if( (ereignis.getSource() == this.rueckgaengig) || 
					(ereignis.getSource() == this.starten)) {   
		// Rückgängig oder Starten machen
				if(!verzeichnisauswahl.isSelected()) {
					JOptionPane.showMessageDialog(null,
						"Bitte ein Verzeichnis eingeben und\ndann starten.",
						 "Ungültiges Verzeichnis angegegen",
						 JOptionPane.ERROR_MESSAGE);
					return;
				} // end if
				
				// Umbenennen ....
				try {
					if(ereignis.getSource() == this.starten) {
						new DateinamenManipulierer(verzeichnisauswahl.toString());
					} else {
						new DateinamenZurueckUmbenenner(verzeichnisauswahl.toString()); 
					} // end if
				} catch(UngueltigesVerzeichnis uv) {
					JOptionPane.showMessageDialog(null,
						"Das eingegebene Verzeichnis \t"+uv.getMessage()+
						" ist ungültig - bitte erneut versuchen.",
						 "Ungültiges Verzeichnis angegegen",
						 JOptionPane.ERROR_MESSAGE);
					return;
				} catch(KeineDateienEnthalten kde) {
					JOptionPane.showMessageDialog(null,
					"Im Verzeichnis\t"+kde.getMessage()+
					"existieren keine umbenennbaren Dateien - bitte erneut versuchen.",
					 "Keine Dateien vorhanden",
					 JOptionPane.ERROR_MESSAGE);
					return;
				} // end of catch kde
		} // end if-getSource  
	} // end of actionPerformed

	/** 
	 * zeigt Hilfefenster an
	 * @see Hilfefenster
	 */
	public static void zeigeHilfeAn() {
		hilfefenster.setVisible(true);
	} // end of zeigeHilfeAn 

	/**
	 * erzeugt Haupfenster der Anwendung 
	 * @param args
	 */
	public static void main(String[] args) {
		// Look and Feel an Plattform anpassen
		try {
			 UIManager.setLookAndFeel(
			 	UIManager.getCrossPlatformLookAndFeelClassName());
		 } catch (Exception e) {
		 	System.out.println("Fehler beim UI-Wechsel: "+e); 
		 } // end of try		
		// Hauptfenster starten
		new HauptGUI();
	} // end of main
} // end of class
