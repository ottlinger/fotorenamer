package fotoding;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 * Title:        Fotoeinlesen und HTML-Generieren
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PO-Computer Berlin
 * @author PO
 * @version 1.0
 */

public class FotoFrame extends JFrame {
private JMenuBar menuBar=new JMenuBar();
private JMenu fotogroesse=new JMenu("Fotogroesse");
private JTextPane jTextPane1 = new JTextPane();
// das Bild
private Image i; // das zu ladende Bild
private int picB;
private int picH;
// Screen
private Dimension size=Toolkit.getDefaultToolkit().getScreenSize();

  public FotoFrame() {
    this.getContentPane().setLayout(new GridLayout(3,3));
    this.setTitle("FotoStream - Testfenster");

    i=Toolkit.getDefaultToolkit().getImage("c:\\windows\\profiles\\test\\desktop\\testpic\\08310001.jpg");
      repaint();
    // rumrechnen
    picB=(i.getWidth(this));
    picH=(i.getHeight(this));
    this.setSize(i.getWidth(this),i.getHeight(this));
System.out.println(picB+" jhkjh "+picH);

    // Bild halbieren
    i=i.getScaledInstance(picB,picH,Image.SCALE_FAST);

//      fotogroesse.add(new JMenuItem("BS-Höhe = "+h));
//      fotogroesse.add(new JMenuItem("BS-Breite = "+w));
      fotogroesse.addSeparator();
      fotogroesse.add(new JMenuItem("PIC-Höhe = "+i.getHeight(this)));
      fotogroesse.add(new JMenuItem("PIC-Breite = "+i.getWidth(this)));
    menuBar.add(fotogroesse);
    jTextPane1.setText("Panel für Bilder");

    this.getContentPane().add(menuBar,  BorderLayout.NORTH);

    this.getContentPane().add(jTextPane1, BorderLayout.SOUTH);

      repaint();
      this.pack();
      this.setVisible(true);
  }

  public void paint(Graphics g) {
//      g.drawImage(i,5,50,new JFrame("Bild1"));
      g.drawImage(i,1,1,this);
  }


  public static void main(String[] args) {
    FotoFrame fotoFrame1 = new FotoFrame();
 // System.exit(1);
  }
}