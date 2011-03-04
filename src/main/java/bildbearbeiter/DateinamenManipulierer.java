/*
 * Created on 12.10.2003
 */
package bildbearbeiter;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;

import bildbearbeiter.ausnahmen.*;
import org.apache.sanselan.ImageReadException;

/**
 * Sinn: CanonBilder umbenennen und BildErzeugungsdatum in Namen schreiben
 * @author hirsch, 12.10.2003
 * @version 2004-01-08
 */
public class DateinamenManipulierer implements Runnable {
	private File aktuellesVerzeichnis = null;
	private File[] dateiliste = null;
	
	private Fortschrittsbalken grafik = null;
	private int aktAnzahl = 0;
	private int obergrenze = 0; 

	/**
	 * Übernimmt ein Verzeichnis. Dessen Dateien werden in
	 * ZuletztGeändertDatum_ZuletztGeändertUhrzeit_Dateiname umbenannt.<br>
	 * Bei den IXUS-Bildern bedeutet das, dass den Dateinamen der Zeitpunkt
	 * der Bildaufnahme mit hinzugefügt wird.
	 * <br>
	 * @param verzeichnis
	 */
	public DateinamenManipulierer(String verzeichnis) 
			throws UngueltigesVerzeichnisException, KeineDateienEnthaltenException {
		this.aktuellesVerzeichnis = new File(verzeichnis);
		// erst starten, wenn die Eingabepr�fung erfolgreich war
		pruefeEingabeUndInit();

		new Thread(this).start();
	} // end of Konstruktor

	/**
	 * prüft, ob Parameter ein Verzeichnis ist und mehr als 0 Dateien enthält,
	 * sonst Ausnahme<br>
	 * (intern werden auch Parameter gesetzt)
	 * 
	 * @throws bildbearbeiter.ausnahmen.KeineDateienEnthaltenException,bildbearbeiter.ausnahmen.UngueltigesVerzeichnisException
	 */
	public void pruefeEingabeUndInit() 
			throws KeineDateienEnthaltenException, UngueltigesVerzeichnisException {
		// Verzeichnis gültig ?
		if (!this.aktuellesVerzeichnis.isDirectory()) 
			throw new UngueltigesVerzeichnisException(this.aktuellesVerzeichnis);
		
		// Dateien da ?
		this.dateiliste = this.aktuellesVerzeichnis.listFiles();
		if (dateiliste.length == 0 || dateiliste == null) 
			throw new KeineDateienEnthaltenException(this.aktuellesVerzeichnis);
		
		// internen Zustand setzen
		this.obergrenze = this.dateiliste.length;
	} // end of pruefeEingabe
	
	/**
	 * PRE: pruefeEingabenUndInit() aufgerufen  
	 * Benennt alle Dateien im Verzeichnis so um,
	 * dass vor dem IXUS-Dateinamen das Datum der letzten Änderung steht,
	 * was im Kamerafall das Aufnahmedatum ist
	 * 
	 * @see #pruefeEingabeUndInit()
     * @throws Exception if any errors occur.
	 */
	public void umbenennen() throws UmbenennenFehlgeschlagenException {
		String name = "";
		String zusatz = "";
		SimpleDateFormat sf = 
			new SimpleDateFormat(File.separator+"yyyyMMdd_HHmm_");

		for(int i=0; i<this.obergrenze; i++) {
			// Daten holen
            try {
            zusatz = MetaDataExtractor.generateCreationDateInCorrectFormat(this.dateiliste[i]);
            } catch (Exception e) {
                throw new UmbenennenFehlgeschlagenException(e.getLocalizedMessage());
            }

            // OLD version:
			// zusatz = sf.format(new Date(this.dateiliste[i].lastModified()));
            name = this.dateiliste[i].getName();

			// Fortschrittsbalken updaten...
				this.grafik.setFortschritt(i);
				this.grafik.setText(this.dateiliste[i].getName());
				// Da die Namen verschieden lang sind den Fortschrittsbalken updaten!
				this.grafik.updateUI();
	
			// rename nur bei Dateien
			if (this.dateiliste[i].isFile()) {
				if (!this.dateiliste[i].renameTo(
							new File(
								this.dateiliste[i].getParent()+zusatz+
								this.dateiliste[i].getName())))  
					throw new UmbenennenFehlgeschlagenException("\tFehler bei Bild"
													+this.dateiliste[i].getName());
			} // end if - isFile()
		} // end of for
	} // end of umbenennen
	
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
			} 	catch(UmbenennenFehlgeschlagenException uf) {
					JOptionPane.showMessageDialog(null,
								"Während der Bearbeitung der Datei\n"+
								uf.getMessage()+" trat ein Fehler beim Umbennen auf.",
								 "Fehler beim Umbenennen",
								 JOptionPane.ERROR_MESSAGE);
					return;
			} // end of catch uf

		this.grafik.dispose();
	
		// Erfolgsmeldung geben
		if (this.obergrenze == 0) {
			meldung = "Im Verzeichnis: "+this.aktuellesVerzeichnis.getName()+
						"\nwurden keine Dateien\numbenannt.\n\n";
		} else if (this.obergrenze == 1) {
			meldung = "\nEs wurde eine Datei\n"+
			"im Verzeichnis: "+this.aktuellesVerzeichnis.getName()+
			"\nerfolgreich umbenannt.\n\n";
		} else {
			meldung = "\nEs wurden "+this.obergrenze+" Dateien\n"+
			"im Verzeichnis: "+this.aktuellesVerzeichnis.getName()+
			"\nerfolgreich umbenannt.\n\n";
		} // end of else
		JOptionPane.showMessageDialog(null, meldung, "Erfolg", 
									JOptionPane.INFORMATION_MESSAGE);
	} // end of run
} // end of class