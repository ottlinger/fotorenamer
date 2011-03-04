package fotoding;
/**
 * Title:        ein Bild einlesen und Höhe und Breite bestimmen
 * Company:      PO-Computer Berlin
 * @author PO
 */
import java.awt.*;
import javax.swing.*;

public class mediTracker extends Canvas {
// Bilddaten
private Image i;
private int hoehe=0;
private int breite=0;

  public mediTracker(String bildDateiName) {
    // Bild laden
    i=Toolkit.getDefaultToolkit().getImage(bildDateiName);
    MediaTracker med=new MediaTracker(this);
    med.addImage(i,0);

      try { med.waitForAll();
        // Anzeige der Bildinfo
        hoehe=i.getHeight(this);
        breite=i.getWidth(this);
      } // end of try

      catch(Exception e) {
        System.out.println("BildLadevorgang-Fehler: "+e);
      } // end catch
  } // end of Konstruktor

  // GET-Methoden für Bildinfos
  public int getBreite() { return this.breite; }
  public int getHoehe() { return this.hoehe; }


  // wird hier nicht benutzt - man kann das Ding auch anzeigen
  public void paint(Graphics g) {
    g.drawImage(i,0,0,this);
  } // end of paint


  public static void main(String[] args) {
    // Testobjekt anzeigen und erzeugen ;-)
    mediTracker mediTracker1 = new mediTracker("/home/hirsch/Documents/pic/start.png");
    JFrame f=new JFrame("Bildanzeige mit MediaTracker");
    f.getContentPane().add(mediTracker1);
    System.out.println(mediTracker1.getBreite()+" "+mediTracker1.getHoehe());
    f.pack();
    f.setVisible(true);
  } // end of main
} // end of class