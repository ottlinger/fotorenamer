/*
 * Created on 20.10.2003
 */
package bildbearbeiter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Sinn: Anzeige eines HTML-basierten Hilfefensters
 * 
 * @author hirsch, 20.10.2003
 * @version 2004-01-08
 * 
 */
public class Hilfefenster extends JFrame implements ActionListener {
	private JPanel oben = null; 
	private JPanel unten = null;
	private JButton ende = null;
	private JScrollPane rollpanel = null;
	private JEditorPane textfeld = null;

	/** erzeugt Hilfefenster und zeigt es aber *nicht* an
	 *
	 */
	public Hilfefenster() {
		init();
	} // end of Konstruktor
	
	/**
	 * Initialisierung der grafischen Komponenten und Fensteranzeige
	 */
	private void init() {
		this.setTitle("Programmhilfe");
		this.setResizable(false);
		this.oben = new JPanel(new FlowLayout());
		this.unten = new JPanel(new FlowLayout());

		// Inhalt des Textfeldes von einer URL laden
		this.textfeld = new JEditorPane();
		this.textfeld.setContentType("text/html");
		this.textfeld.setEditable(false);
		try {
			this.textfeld.setPage(Hilfefenster.class.getResource("html/hilfe.html"));
			this.oben.add(textfeld);
		} catch(Exception e) {
			this.oben.setLayout(new GridLayout(3,1));
			this.oben.add(new JLabel("Fehler in der Hilfe -"));
			this.oben.add(new JLabel(""+e.getMessage()));
			this.oben.add(new JLabel(""+e.getClass()));
		} // end of catch

		// Eigenschaften des Scrollpanels anpassen
		this.rollpanel = new JScrollPane(this.oben);
		this.rollpanel.setPreferredSize(new Dimension(350, 300));
		this.rollpanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	
		// Ende-Knopf	
		this.ende = new JButton("Schliessen");
		this.ende.addActionListener(this);
		this.ende.setMnemonic('S');
		this.unten.add(this.ende);

		// Zusammenbasteln ...
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(rollpanel, BorderLayout.NORTH);
		this.getContentPane().add(unten, BorderLayout.CENTER);

		// Zentrieren und Anzeigen		
		this.pack();
		FensterZentrierer.makeCentered(this);
	} // end of init
	
	/**
	 * auf Ereignisse reagieren
	 */
	public void actionPerformed(ActionEvent ereignis) {
		if( ereignis.getSource() == this.ende) {
			setVisible(false);
		}
	} // end of actionPerformed
} // end of class

