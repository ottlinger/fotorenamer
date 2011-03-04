package fotoding;
import java.awt.*;
import javax.swing.*;
import java.lang.*;
import javax.swing.filechooser.*;
import java.io.*;

/**
 * Title:        Fotoeinlesen und HTML-Generieren
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PO-Computer Berlin
 * @author PO
 * @version 1.0
 */

public class Oberflaeche extends JFrame {
    JFileChooser dateiauswahl=new JFileChooser();

// hier sollen die eigentlichen Kommandos hin:

  public Oberflaeche() {
    dateiauswahl.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    this.ladeInhalt();
  } // end of Konstruktor

  private void ladeInhalt() {
    this.setTitle("Bildindex - Oberfläche");
    this.setSize(200,100);
    this.getContentPane().setLayout(new GridLayout(2,1));

//    dateiauswahl.addChoosableFileFilter();
//    this.getContentPane().add(new JLabel("Herzliche Willkommen - bitte auswählen"));
    this.getContentPane().add(dateiauswahl);
//    this.getContentPane().add(new JColorChooser());

// test der anzeige
  JFileChooser jtest=new JFileChooser();
  jtest.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

File ff=dateiauswahl.getSelectedFile();
System.out.println(ff);

    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    this.pack();
    this.setVisible(true);
  } // end of ladeInhalt


  public static void main(String[] args) {
    Oberflaeche oberflaeche1 = new Oberflaeche();
  } // end of main
} // end of class