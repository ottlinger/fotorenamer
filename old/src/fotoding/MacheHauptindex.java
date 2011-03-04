package fotoding;

/**
 * <p>Title: Fotoeinlesen und HTML-Generieren</p>
 * @author PO
 */

public class MacheHauptindex implements Runnable {
private TestDateiobjekt meinAufrufer;
private String filter;

    /**
     * Hauptindex erstellen mit übergebener Datei
     * @param aufrufender
     */
    public MacheHauptindex(TestDateiobjekt aufrufender) {
        this.filter="jpg";
        this.meinAufrufer=aufrufender;
    } // end of Konstruktor

    /**
     * Hauptindex erstellen mit übergebener Datei
     * @param aufrufender
     * @param filterendung - Endung der Dateien
     */
    public MacheHauptindex(TestDateiobjekt aufrufender,String filterendung) {
        this.filter=filterendung;
        this.meinAufrufer=aufrufender;
    } // end of Konstruktor1

  public void run() {
    try {
      meinAufrufer.macheHauptIndex(this.filter);
    } // end of try
    catch(Exception e) {
      System.out.println(this+"- Exception beim Aufruf von MacheHauptIndex"+e);
    } // end of catch
  }// end of run
} // end of class