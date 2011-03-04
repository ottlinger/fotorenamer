/*
 * Created on 20.10.2003
 */
package bildbearbeiter;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.ImageIcon;

/**
 * Sinn: zentriert SwingKomponenten
 * 
 * @author hirsch, 20.10.2003
 * @version 2003-10-22
 * 
 */

public class FensterZentrierer {
	/**
	 * zentriert die übergebene Swing-Komponente
	 * @param w
	 */
	public static void makeCentered(Component w)    {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension dim = toolkit.getScreenSize();
		int screenWidth = dim.width;
		int screenHeight = dim.height;
		int x = (screenWidth - w.getSize().width) / 2;
		int y = (screenHeight - w.getSize().height) / 2;
		w.setLocation(x, y);
	} // end of makeCentered	
	
	/**
	 * Übernimmt einen Pfad und macht daraus ein ImageIcon,
	 * im Fehlerfall wird einfach null zurückgegeben
	 * @param path
	 * @return ImageIcon-Bild
	 */
	 public static ImageIcon createImageIcon(String path) {
		 java.net.URL imgURL = FensterZentrierer.class.getResource(path);

		 if (imgURL != null) {
			 return new ImageIcon(imgURL);
		 } else {
			 System.err.println("FensterZentrierer.createImageIcon():" +
			 	"\nURL ungültig: " + path);
			 return null;
		 } // end if
	 } // end of createImageIcon	
} // end of class
