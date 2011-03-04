package fotoding;

/**
 * Title:        Zum EInlesen der DAteien einen FIlter erstellen
 * Copyright:    Copyright (c) 2003
 * Company:      PO-Computer Berlin
 * @author PO
 * @version 1.0
 */
import java.io.*;

public class BildFilter implements FileFilter {
private String muster;

  public BildFilter(String pattern) {
    this.muster=pattern.toUpperCase();
    System.out.println("\t***BildFilter*** successfully created BildFilter for "+pattern+"-Files");
  } // end of Konstruktor


  public boolean accept(File zuPruefen) {
//System.out.println("Prüfe "+zuPruefen+" auf "+muster+"\t="+zuPruefen.getName().toUpperCase().indexOf(muster));
    return ( zuPruefen.getName().toUpperCase().indexOf(muster)!=-1);
  } // end of accept

} // end of class