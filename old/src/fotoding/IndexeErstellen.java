package fotoding;

import javax.swing.*;

/**
 * Übernimmt die EIngaben aus der GUI und startet die Indexerstellung autonom -
 *
 * @todo ProgessBar-Einbindung in die einzelnen Index-Klassen
 *
 * @todo SINGELTON-Pattern!
 *
 * @author hirsch
 * Date: 19.05.2003
 * Time: 00:18:38
 */
public class IndexeErstellen {
    private GrafikZeug gt;
    private TestDateiobjekt param;
    private Thread t1,t2,t3,t4;

    public IndexeErstellen(String headline,String interval,String verzeichnis,String maske,String indextyp,GrafikZeug aufrufer) {
        try {
            this.param=new TestDateiobjekt(verzeichnis,1,headline,"GREEN",Integer.parseInt(interval));
            this.gt = aufrufer;

            /**
             *  Indextyp:
             *  zur Auswahl wird nur die 1.Stelle des indextyp-Strings herangezogen
             *  C - CD-index
             *  W - Web-index
              */
            switch(indextyp.charAt(0)) {
                case 'C':
                        cdIndex(maske);
                        break;
                case 'W':
                        webIndex(maske);
                        break;
                default:
                    JOptionPane.showMessageDialog(this.gt,
                            "Kein gültiger Indextyp ausgewählt - nichts zu tun ;-)",
                            "FotoIndexer",
                            JOptionPane.INFORMATION_MESSAGE);

            }   // end of switch

        } // end of try

        catch(Exception e) {
            JOptionPane.showMessageDialog(this.gt,
                    "Die Meldung der Ausnahme lautet:\n"+e.getMessage()+"\n\nMessage:\n"+e.getLocalizedMessage(),
                    "Fehler bei der Indexerstellung - "+e.toString(),
                    JOptionPane.ERROR_MESSAGE);

        } // end of catch
    } // end of Konstruktor

    /**
     * Erstellen eines WEB-Indexes; dazu:
     *
     * Dateien aus Preview-Verzeichnis in root kopieren und
     * in wechselindex-Methode das /preview/ wegnehmen ...
     * dann: 1. / 2. / 3.
     *
     * @param maske
     * @throws Exception
     */

    private void webIndex(String maske) throws Exception {
// 1.AutoIndex
        t1 = new Thread(new Webindex(this.param,maske));
        t1.start();
// 2.Wechselindex
        t2 = new Thread(new ManuellerIndex(this.param,maske));
        t2.start();
// 3.HauptindexInRoot
        t3 = new Thread(new MacheRootIndex(this.param,maske));
        t3.start();

        // Beenden ....
        // Warten, bis alle tot sind ....
        if(t1!=null) t1.join();
        if(t2!=null) t2.join();
        if(t3!=null) t3.join();
    } // end of webIndex


    /**
     * Erstellen eines CD-fähigen Indexes
     * (alte Datei:
     * CDs:
     *   1. / 2. / 4.
     * @param maske
     * @throws Exception
     */
    private void cdIndex(String maske) throws Exception {
// 1.AutoIndex
        t1 = new Thread(new Webindex(this.param,maske));
        t1.start();
// 2.Wechselindex
        t2 = new Thread(new ManuellerIndex(this.param,maske));
        t2.start();
// 4.Hauptindex
        t4 = new Thread(new MacheHauptindex(this.param,maske));
        t4.start();

        // Beenden ....
        // Warten, bis alle tot sind ....
        if(t1!=null) t1.join();
        if(t2!=null) t2.join();
        if(t4!=null) t4.join();
    } // end of cdIndex
}  // end of class
