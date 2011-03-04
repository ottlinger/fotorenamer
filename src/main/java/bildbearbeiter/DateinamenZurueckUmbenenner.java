/*
 * Created on 08.12.2003
 */
package bildbearbeiter;
import java.io.*;
import javax.swing.JOptionPane;

import bildbearbeiter.ausnahmen.*;

/**
 * Sinn: soll die Dateien wieder zurückumbennen können
 * 
 * @author hirsch, 08.12.2003
 * @version 2004-01-08
 * 
 */
public class DateinamenZurueckUmbenenner implements Runnable {
	private File aktuellesVerzeichnis = null;
	private File[] dateiliste = null;

	private Fortschrittsbalken grafik = null;
	private int aktAnzahl = 0;
	private int obergrenze = 0;
	private int umbenannt = 0; 


	/**
	 * @param verzeichnis
	 * @throws UngueltigesVerzeichnis
	 * @throws KeineDateienEnthalten
	 */
	public DateinamenZurueckUmbenenner(String verzeichnis)
		throws UngueltigesVerzeichnis, KeineDateienEnthalten {
			this.aktuellesVerzeichnis = new File(verzeichnis);
			// erst starten, wenn die Eingabeprüfung erfolgreich war
			pruefeEingabeUndInit();
			new Thread(this).start();
	} // end of Konstruktor

	/**
	 * PRE: pruefeEingabenUndInit() aufgerufen  
	 * Benennt alle Dateien im Verzeichnis so um,
	 * dass vor dem IXUS-Dateinamen das Datum der letzten Änderung steht,
	 * was im Kamerfall das Aufnahmedatum ist
	 * 
	 * @see #pruefeEingabeUndInit()
	 */
	public void umbenennen() throws UmbenennenFehlgeschlagen {
		String name = "";
		String nameNeu = "";
		String muster = "\\d{8}[_]\\d{4}[_]\\p{ASCII}*";

		for(int i=0;i<this.obergrenze;i++) {
			name = this.dateiliste[i].getName();
			// würde alle Vorkommen ersetzen:
			// nameNeu = name.replaceAll("\\d{8}[_]\\d{4}[_]","");
			// Unter Linux bleibt die DateiInfo erhalten ... aber unter Windows vll. nicht ?!
			nameNeu = name.replaceFirst("\\d{8}[_]\\d{4}[_]","");

			// umzubenennende Dateien zählen
			if (nameNeu != name) this.umbenannt++;

			// Fortschrittsbalken updaten...
				this.grafik.setFortschritt(i);
				this.grafik.setText(name);
				// Da die Namen verschieden lang sind den Fortschrittsbalken updaten!
				this.grafik.updateUI();
	
			// rename nur bei Dateien
			if (this.dateiliste[i].isFile()) {
				if (!this.dateiliste[i].renameTo(
						new File(this.dateiliste[i].getParent()+File.separatorChar+nameNeu)))   
					throw new UmbenennenFehlgeschlagen("\tFehler bei Bild "
											+this.dateiliste[i].getName());
			} // end if - isFile()
		} // end of for
	} // end of umbenennen
	
	/**
	 * prüft, ob Parameter ein Verzeichnis ist und mehr als 0 Dateien enthält, 
	 * sonst Ausnahme<br>
	 * (intern werden auch Parameter gesetzt)
	 * 
	 * @throws KeineDateienEnthalten,UngueltigesVerzeichnis
	 */
	public void pruefeEingabeUndInit() 
			throws KeineDateienEnthalten, UngueltigesVerzeichnis{
		// Verzeichnis gültig ?
		if (!this.aktuellesVerzeichnis.isDirectory()) 
			throw new UngueltigesVerzeichnis(this.aktuellesVerzeichnis);
		
		// Dateien da ?
		this.dateiliste = this.aktuellesVerzeichnis.listFiles();
		if (dateiliste.length == 0 || dateiliste == null) 
			throw new KeineDateienEnthalten(this.aktuellesVerzeichnis);
		
		// internen Zustand setzen
		this.obergrenze = this.dateiliste.length;
	} // end of pruefeEingabe

	/**
	 * Fortschrittsbalken anzeigen und umbenennen starten
	 * Die Anzeige wird innerhalb von umbenennen erledigt.
	 * 
	 * Fehlerbehandlung des umbennens wird erledigt = Abbruch ;-^ 
	 * @see #umbenennen()
	 */
	public void run() {
		String meldung = "";
		this.grafik = new Fortschrittsbalken(this.obergrenze);

			try { 
				umbenennen(); 
			} 	catch(UmbenennenFehlgeschlagen uf) {
					JOptionPane.showMessageDialog(null,
								"Während der Bearbeitung der Datei\n"+
								uf.getMessage()+" trat ein Fehler beim Umbennen auf.",
								 "Fehler beim Umbenennen",
								 JOptionPane.ERROR_MESSAGE);
					return;
			} // end of catch uf
		this.grafik.dispose();
	
		// Erfolgsmeldung geben
		if (this.umbenannt == 0) {
			meldung = "Im Verzeichnis: "+this.aktuellesVerzeichnis.getName()+
						"\nwurden keine Dateien\numbenannt.\n\n";
		} else if (this.umbenannt == 1) {
			meldung = "\nEs wurde eine Datei\n"+
			"im Verzeichnis: "+this.aktuellesVerzeichnis.getName()+
			"\nerfolgreich umbenannt.\n\n";
		} else {
			meldung = "\nEs wurden "+this.umbenannt+" von "+this.obergrenze
			+" Dateien\nim Verzeichnis: "+this.aktuellesVerzeichnis.getName()+
			"\nerfolgreich umbenannt.\n\n";
		} // end of else
		JOptionPane.showMessageDialog(null, meldung, "Erfolg", 
						JOptionPane.INFORMATION_MESSAGE);
	} // end of run
} // end of class
