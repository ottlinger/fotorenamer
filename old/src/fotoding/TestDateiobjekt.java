package fotoding;

/**
 * Title:        Fotoeinlesen und HTML-Generieren
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PO-Computer Berlin
 * @author PO
 * @version 1.0
 */

import java.io.*;
import java.util.Vector;
import java.awt.*;

public class TestDateiobjekt {
// Klassenvars
private File original; // Originalstartverzeichnis!
private File startverz; // Arbeitsstartverzeichnis
private File[] bilddateien; // alle Dateien mit dieser Endung

// HTML-FileAttribute
private double shrink; // Faktor, um den verkleinert wird
private String headline; // Überschrift
private String background; // Hintergrundfarbe in HTML-Dokument
private int delay; // Verweildauer pro Bild in Sekunden
// IndexProgress
private int toBeDone; // #Bildfiles

  // Konstruktor
  public TestDateiobjekt(String startverzeichnis,double shrinkfaktor,String headline,String backGroundColor,int verzoegerung)
            throws KeinGueltigesVerzeichnisAngegeben {
    // 1.Formalien
    // Faktor der Bildgröße setzen
    this.shrink=shrinkfaktor;
    this.headline=headline;
    this.background=backGroundColor;
    this.delay=verzoegerung;

    // 2.Verzeichnischeck
    this.startverz=new File(startverzeichnis);

    // 3.Parameterprüfung
    if(!this.startverz.isDirectory()) throw new KeinGueltigesVerzeichnisAngegeben();
    if(!this.startverz.canWrite()) throw new KeinGueltigesVerzeichnisAngegeben();

    // 4.Kopie des Verzeichnis anlegen, da <coder>this.startverz</code>
    // beim Indexerstellen manipuliert wird
    this.original=new File(startverzeichnis);

    // Statusausgaben
    System.out.println("Pfad: "+startverz.getPath());
    this.gibDateiAnzahl();
  } // end of Konstruktor

 private void gibDateiAnzahl() {
    String[] dummy=this.startverz.list();
    System.out.println("Das Eingabeverzeichnis beinhaltet "+dummy.length+" Elemente.");
  } // end of gibDateiAnzahl

  // nach Maske filtern
  private Vector gibDateinamen(String maske) {
    // Dateien zur <code>Maske</code> passend generieren
    this.bilddateien=this.startverz.listFiles(new BildFilter(maske));
    Vector ergebnis=new Vector(this.bilddateien.length);
      // insgesamt zu erledigende Bilder = 100 %
      this.toBeDone=this.bilddateien.length;
    System.out.println("Davon sind "+this.toBeDone+" Files der Endung \""+maske+"\"");
      // Namen zusammensammeln
      for(int i=0;i<this.bilddateien.length;i++) {
        ergebnis.add(new String(this.bilddateien[i].getName()));
      } // end for

  /** den Vektor noch sortieren */
  ergebnis.trimToSize();
  java.util.Collections.sort(ergebnis);
  return ergebnis;
  } // end of gibDateinamen

/**
 * diese Methode erstellt einen Index
 * @param muster
 */
  public void macheAutoIndex(String muster) {
    File zielverzeichnis; // zielverzeichnis für HTML

    System.out.println("1. AutoIndex - Verzeichnis anlegen ...");
    // betroffene Bilddateien sammeln
    Vector dateien=gibDateinamen(muster);
    System.out.println("AutoIndex: Es werden "+dateien.capacity()+" HTML-Dateien erzeugt");

    // Unterverzeichnis "auto" anlegen
      String neuesVerz=this.startverz.getAbsolutePath();
      neuesVerz+=this.startverz.separator+"auto";
      // Dateipfad für HTML-Dateisprünge merken
      zielverzeichnis=new File(neuesVerz);
      zielverzeichnis.mkdir();

    // dateinamen
    String dateiName=new String(); // Name der HTML-Datei im FileSystem
    String linkName=new String();  // beinhaltet nur den Link für HTML-Seite
    String fotoName=new String();  // beinhaltet nur den Bildlink für HTML-Seite
    PrintWriter ausgabeDatei; // Schreibstrom zum Generieren der HTML-Inhalte
    Image pic; // Bild
    int breite,hoehe; // Bildzielbreite/höhe

    // los geht's - step-by-step
    String sprung; // Sprungziel

    for(int i=0;i<dateien.size();i++) {
System.out.println("AutoIndex: "+i+"/"+dateien.size());
     // 1.HTML-Dateinamen generieren
        dateiName=(String)dateien.elementAt(i);
        // extension abschneiden und Zusatzstrings on-the-fly generieren
        fotoName=dateiName;
        // Endung anhängen
        dateiName=changeExtension(dateiName,"html");
        linkName=dateiName;
        // pfad anhängen ... Fertig :-)
        dateiName=zielverzeichnis.getPath()+zielverzeichnis.separator+dateiName;

    // 2.Dateien einfach schreiben
    try {
          // MediaTracker
          // VERSUCH, Bildgröße zu lesen und zu reduzieren
            mediTracker md= new mediTracker(this.startverz.getPath()+this.startverz.separator+fotoName);
            breite= (int)(md.getBreite()*this.getShrink());
            hoehe= (int)(md.getHoehe()*this.getShrink());
          // Statusausgabe
//          System.out.println("Done: "+this.getProgress(i)+"\t"+(i+1)+".Bild="+fotoName+"\t(B= "+md.getBreite()+"\tH= "+md.getHoehe()+")\tHTML B="+breite+"\\H="+hoehe);
//          System.out.println("AUTINDEX-Done: "+this.getProgress(i)+"\t"+(i+1)+".Bild="+fotoName);

          // DateiStream öffnen und HTML-generieren
          ausgabeDatei = new PrintWriter( new BufferedOutputStream( new FileOutputStream( dateiName ) ) );

          ausgabeDatei.println("<!doctype html public \"-//w3c//dtd html 4.0 transitional//en\">");
          ausgabeDatei.println("<html>");
          ausgabeDatei.println("\t\t<head>");
          ausgabeDatei.println("\t\t<title>Automatische Fotoschau von &quot;"+this.getHeadline()+"&quot;</title>");

              // automatische Weiterleitung einbauen:
              sprung=""; // init
              // endung anfügen
              sprung=changeExtension( ((String)dateien.elementAt((i+1)%this.toBeDone)) ,"html" );

              ausgabeDatei.println("\t\t\t\t<!-- Fotolink= "+linkName+"("+((i+1)%this.toBeDone)+")\tNachfolger = "+sprung+" -->");
              ausgabeDatei.println("\t\t\t\t<meta http-equiv=\"Refresh\" content=\""+this.getDelay()+";URL="+sprung+"\">");

          ausgabeDatei.println("\t\t<meta http-equiv=\"content-type\" content=\"text/html;charset=iso-8859-1\">");
          ausgabeDatei.println("\t\t<meta name=\"Title\" content=\"AutoIndexFotoschau\">");
          ausgabeDatei.println("\t\t<meta name=\"Author\" content=\"PO Computer Berlin\">");
          ausgabeDatei.println("\t\t<meta name=\"Publisher\" content=\"PO Computer Berlin\">");
          ausgabeDatei.println("\t\t<meta name=\"Copyright\" content=\"(C) PO Computer Berlin 2002-Ver20020120\">");
          ausgabeDatei.println("\t\t<meta name=\"Revisit\" content=\"After 16 days\">");
          ausgabeDatei.println("\t\t<meta name=\"Description\" content=\"Familienseiten\">");
          ausgabeDatei.println("\t\t<meta name=\"Abstract\" content=\"Familienseiten\">");
          ausgabeDatei.println("\t\t<meta name=\"page-topic\" content=\"Dienstleistung\">");
          ausgabeDatei.println("\t\t<meta name=\"audience\" content=\"Alle\">");
          ausgabeDatei.println("\t\t<meta name=\"Robots\" content=\"INDEX,FOLLOW\">");
          ausgabeDatei.println("\t\t<meta name=\"Language\" content=\"Deutsch\">");
          ausgabeDatei.println("\t\t</head>");
          ausgabeDatei.println();
          ausgabeDatei.println("<body bgcolor=\"white\" link=\"blue\" alink=\"blue\" vlink=\"blue\">");
          ausgabeDatei.println("<center>");
          ausgabeDatei.println("<table border=\"0\" cellpadding=\"1\" cellspacing=\"1\" width=\"550\">");
          ausgabeDatei.println("<tr>");
          ausgabeDatei.println("<td>");
          ausgabeDatei.println("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
          ausgabeDatei.println("<tr>");
          ausgabeDatei.println("<td>");
          ausgabeDatei.println("<center>");
          ausgabeDatei.println("<font size=\"4\" face=\"Arial,Helvetica,Geneva,Swiss,SunSans-Regular\" color=\""+this.getBackgroundcolor()+"\">");
          // Überschrift einbauen
          ausgabeDatei.println("<b>"+this.getHeadline()+"<hr></b></font></center>");
          ausgabeDatei.println("</td>");
          ausgabeDatei.println("</tr>");
          ausgabeDatei.println("<tr>");
          ausgabeDatei.println("<td><font size=\"2\" face=\"Arial,Helvetica,Geneva,Swiss,SunSans-Regular\" color=\"black\"><b>Foto "+(i+1)+" von "+this.toBeDone);
          ausgabeDatei.println("</b>");
          ausgabeDatei.println("&nbsp;- Der Name des Fotos lautet: "+fotoName+"<hr></font></td>");
          ausgabeDatei.println("</tr>");

          // Abbruch = default-Link!!!
          ausgabeDatei.println("\t\t\t<tr>");
          ausgabeDatei.println("\t\t\t\t\t\t<td><form>");
          ausgabeDatei.println("\t\t\t\t\t\t<center><input type=\"button\" value=\"ENDE\" onclick=\"window.close()\">");
          ausgabeDatei.println("\t\t\t\t\t\t</center></form></td>");
          ausgabeDatei.println("\t\t\t</tr>");

          ausgabeDatei.println("<tr>");
          ausgabeDatei.println("<td><font size=\"2\" face=\"Arial,Helvetica,Geneva,Swiss,SunSans-Regular\" color=\"black\">");
          ausgabeDatei.println("<hr>");
          ausgabeDatei.println("\t\t<!--Rahmen um Bild-->\n\t\t<center>");
          ausgabeDatei.println("\t\t<table border=\"0\" cellpadding=\"2\" cellspacing=\"2\" width=\""+(breite+4)+"\">");
          ausgabeDatei.println("\t\t<tr>");
          ausgabeDatei.println("\t\t<td width=\"99%\">");
          ausgabeDatei.println("\t\t\t\t<!--Tabelle um Bild selbst-->");
          ausgabeDatei.println("\t\t\t\t<table border=\"0\" cellpadding=\"2\" cellspacing=\"2\" width=\"100%\" bgcolor=\""+this.getBackgroundcolor()+"\">");
          ausgabeDatei.println("\t\t\t\t<tr>");

          // berechnete Höhe einsetzen
//          ausgabeDatei.println("\t\t\t\t<td><img height=\""+hoehe+"\" width=\""+breite+"\" src=\""+fotoName+"\" alt=\""+fotoName+"\" border=\"0\"></td>");
          ausgabeDatei.println("\t\t\t\t<td><img height=\""+hoehe+"\" width=\""+breite+"\" src=\"../"+fotoName+"\" alt=\""+fotoName+"\" border=\"0\"></td>");

          ausgabeDatei.println("\t\t\t\t</tr>");
          ausgabeDatei.println("\t\t\t\t</table>");
          ausgabeDatei.println("\t\t\t\t<!--BildTabelle-Ende-->");
          ausgabeDatei.println("\t\t</td>");
          ausgabeDatei.println("\t\t</tr>");
          ausgabeDatei.println("\t\t</table>");
          ausgabeDatei.println("\t\t<!--Ende-Rahmen um Bild-->\n</center>");
          ausgabeDatei.println("</font></td>");
          ausgabeDatei.println("</tr>");
          ausgabeDatei.println("</table>");
          ausgabeDatei.println("</td>");
          ausgabeDatei.println("</tr>");
          ausgabeDatei.println("</table>");
          ausgabeDatei.println("</center>");
          ausgabeDatei.println("</body>");
          ausgabeDatei.println("</html>");

        // aktuelle Datei speichern und schließen
        ausgabeDatei.flush();
        ausgabeDatei.close();
      } // end of try

      catch(Exception e) {
          System.out.println("** macheAutoIndex-Exception ***\t"+e+"\t"+muster);
      } // end of catch
  } // end of for
// Erfolgsmeldung
System.out.println("AutoIndex wurde erfolgreich erstellt ;-)");
} // end of macheAutoIndex

  public void macheWechselIndex(String muster) {
    File zielverzeichnis; // zielverzeichnis für HTML
    String sprungAnfang; // Dateiname für Anfangssprung

    System.out.println("1. WechselIndex - Verzeichnis anlegen ...");
    // betroffene Bilddateien sammeln
    Vector dateien=gibDateinamen(muster);
    System.out.println("WechselIndex: Es werden "+dateien.capacity()+" HTML-Dateien erzeugt");

    // Unterverzeichnis "perhand" anlegen
      String neuesVerz=this.startverz.getAbsolutePath();
      neuesVerz+=this.startverz.separator+"perhand";
    // Verzeichnis merken für Dateisprünge
      zielverzeichnis=new File(neuesVerz);
      zielverzeichnis.mkdir();

    // dateinamen für Bild
    String dateiName=new String(); // Name der HTML-Datei im FileSystem
    String linkName=new String();  // beinhaltet nur den Link für HTML-Seite
    String fotoName=new String();  // beinhaltet nur den Bildlink für HTML-Seite
    PrintWriter ausgabeDatei; // Schreibstrom zum Generieren der HTML-Inhalte
    Image pic; // Bild selbst
    int breite,hoehe; // Bildzielbreite/höhe
    // Navigation
    String sprung; // Sprungziel vorwärts
    String vorgaenger; // Sprungziel rückwärts

    for(int i=0;i<dateien.size();i++) {
      System.out.println("\tWechselIndex: "+i+"/"+dateien.size());
     // 1.HTML-Dateinamen generieren
        dateiName=(String)dateien.elementAt(i);
        // extension abschneiden und Zusatzstrings on-the-fly generieren
        fotoName=dateiName;
        // Endung anhängen
        dateiName=changeExtension(dateiName,"html");
        linkName=dateiName;
        // pfad anhängen ... Fertig :-)
        dateiName=zielverzeichnis.getPath()+zielverzeichnis.separator+dateiName;

    // 2.Dateien einfach schreiben
    try {
          // MediaTracker
          // VERSUCH, Bildgröße zu lesen und zu reduzieren
            mediTracker md= new mediTracker(this.startverz.getPath()+this.startverz.separator+fotoName);
            breite= (int)(md.getBreite()*this.getShrink());
            hoehe= (int)(md.getHoehe()*this.getShrink());
          // Statusausgabe
//          System.out.println("Done: "+this.getProgress(i)+"\t"+(i+1)+".Bild="+fotoName+"\t(B= "+md.getBreite()+"\tH= "+md.getHoehe()+")\tHTML B="+breite+"\\H="+hoehe);
//          System.out.println("MANINDEX-Done: "+this.getProgress(i)+"\t"+(i+1)+".Bild="+fotoName);

          // DateiStream öffnen und HTML-generieren
          ausgabeDatei = new PrintWriter( new BufferedOutputStream( new FileOutputStream( dateiName ) ) );

              // Navigationsziele berechnen
              vorgaenger=sprung=""; // init
              // endungen anfügen
              sprung=changeExtension(((String)dateien.elementAt((i+1)%this.toBeDone)),"html");

              // bei Null per Hand auf letztes Bild stellen
              if(i!=0) vorgaenger=changeExtension(((String)dateien.elementAt((i-1)%this.toBeDone)),"html");
                      // -1 weil man die this.toBeDone-Sachen von 0..this.toBeDone-1 zählt
              else vorgaenger=changeExtension(((String)dateien.elementAt(this.toBeDone-1)),"html");

          ausgabeDatei.println("<!doctype html public \"-//w3c//dtd html 4.0 transitional//en\">");
          // Kommentar in HTML-File generieren
          ausgabeDatei.println("<!-- aktuelles Bild= "+linkName+"("+((i+1)%this.toBeDone)+")\tNachfolger = "+sprung+" Vorgaenger="+vorgaenger+" -->");
          ausgabeDatei.println("<html>");
          ausgabeDatei.println("\t\t<head>");
          ausgabeDatei.println("\t\t<title>Manuelle Fotoschau von &quot;"+this.getHeadline()+"&quot;</title>");
          ausgabeDatei.println("\t\t<meta http-equiv=\"content-type\" content=\"text/html;charset=iso-8859-1\">");
          ausgabeDatei.println("\t\t<meta name=\"Title\" content=\"WechselIndexFotoschau\">");
          ausgabeDatei.println("\t\t<meta name=\"Author\" content=\"PO Computer Berlin\">");
          ausgabeDatei.println("\t\t<meta name=\"Publisher\" content=\"PO Computer Berlin\">");
          ausgabeDatei.println("\t\t<meta name=\"Copyright\" content=\"(C) PO Computer Berlin 2002-Ver20020120\">");
          ausgabeDatei.println("\t\t<meta name=\"Revisit\" content=\"After 16 days\">");
          ausgabeDatei.println("\t\t<meta name=\"Description\" content=\"Familienseiten\">");
          ausgabeDatei.println("\t\t<meta name=\"Abstract\" content=\"Familienseiten\">");
          ausgabeDatei.println("\t\t<meta name=\"page-topic\" content=\"Dienstleistung\">");
          ausgabeDatei.println("\t\t<meta name=\"audience\" content=\"Alle\">");
          ausgabeDatei.println("\t\t<meta name=\"Robots\" content=\"INDEX,FOLLOW\">");
          ausgabeDatei.println("\t\t<meta name=\"Language\" content=\"Deutsch\">");
          ausgabeDatei.println("\t\t</head>");
          ausgabeDatei.println();
          ausgabeDatei.println("<body bgcolor=\"white\" link=\"blue\" alink=\"blue\" vlink=\"blue\">");
          ausgabeDatei.println("<center>");
          ausgabeDatei.println("<table border=\"0\" cellpadding=\"1\" cellspacing=\"1\" width=\"550\">");
          ausgabeDatei.println("<tr>");
          ausgabeDatei.println("<td>");
          ausgabeDatei.println("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
          ausgabeDatei.println("<tr>");
          ausgabeDatei.println("<td>");
          ausgabeDatei.println("<center>");
          ausgabeDatei.println("<font size=\"4\" face=\"Arial,Helvetica,Geneva,Swiss,SunSans-Regular\" color=\""+this.getBackgroundcolor()+"\">");
          // Überschrift einbauen
          ausgabeDatei.println("<b>"+this.getHeadline()+"<hr></b></font></center>");
          ausgabeDatei.println("</td>");
          ausgabeDatei.println("</tr>");
          ausgabeDatei.println("<tr>");
          ausgabeDatei.println("<td><font size=\"2\" face=\"Arial,Helvetica,Geneva,Swiss,SunSans-Regular\" color=\"black\"><b>Foto "+(i+1)+" von "+this.toBeDone);
          ausgabeDatei.println("</b>");
          ausgabeDatei.println("&nbsp;- Der Name des Fotos lautet: "+fotoName+"</font></td>");
          ausgabeDatei.println("<hr>");
          ausgabeDatei.println("</tr>");

          // Bildnavigation vor-zurück, ENDE= ../index.html !!!
          ausgabeDatei.println("\t\t<!-- Navigation durch Index -->");
          ausgabeDatei.println("\t\t<tr>");
          ausgabeDatei.println("\t\t<td><font size=\"2\" face=\"Arial,Helvetica,Geneva,Swiss,SunSans-Regular\" color=\"black\">");
          ausgabeDatei.println("\t\t<b>Bitte auf die blauen Felder klicken, um zum n&auml;chsten Bild zu gelangen.</b>");
          ausgabeDatei.println("\t\t<br><br>");
          ausgabeDatei.println("\t\t\t\t<table width=\"100%\" border=\"0\" cellspacing=\"2\" cellpadding=\"2\">");
          ausgabeDatei.println("\t\t\t\t<tr>");

          // letztes Bild
          ausgabeDatei.println("\t\t\t\t\t\t<td><a href=\""+vorgaenger+"\">&lt;&lt;&nbsp; zur&uuml;ck</a></td>");
          // Abbruch = default-Link!!!
          ausgabeDatei.println("\t\t\t\t\t\t<td><a href=\"javascript:window.close()\">ENDE</a></td>");
//          ausgabeDatei.println("\t\t\t\t\t\t<td><a href=\"../index.html\">ENDE</a></td>");
          // Sprung vorwärts
          ausgabeDatei.println("\t\t\t\t\t\t<td><a href=\""+sprung+"\">vorw&auml;rts&nbsp;&gt;&gt;</a></td>");

          ausgabeDatei.println("\t\t\t\t</tr>");
          ausgabeDatei.println("\t\t\t\t</table>");
          ausgabeDatei.println("\t\t</font></td>");
          ausgabeDatei.println("\t\t</tr>");
          ausgabeDatei.println("\t\t<!--Ende der Navigation-->");

          // das eigentliche Bild
          ausgabeDatei.println("<tr>");
          ausgabeDatei.println("<td><font size=\"2\" face=\"Arial,Helvetica,Geneva,Swiss,SunSans-Regular\" color=\"black\">");
          ausgabeDatei.println("<hr>");
          ausgabeDatei.println("\t\t<!--Rahmen um Bild-->\n\t\t<center>");
          ausgabeDatei.println("\t\t<table border=\"0\" cellpadding=\"2\" cellspacing=\"2\" width=\""+(breite+4)+"\">");
          ausgabeDatei.println("\t\t<tr>");
          ausgabeDatei.println("\t\t<td width=\"99%\">");
          ausgabeDatei.println("\t\t\t\t<!--Tabelle um Bild selbst-->");
          ausgabeDatei.println("\t\t\t\t<table border=\"0\" cellpadding=\"2\" cellspacing=\"2\" width=\"100%\" bgcolor=\""+this.getBackgroundcolor()+"\">");
          ausgabeDatei.println("\t\t\t\t<tr>");

          // berechnete Höhe einsetzen
//          ausgabeDatei.println("\t\t\t\t<td><img height=\""+hoehe+"\" width=\""+breite+"\" src=\""+fotoName+"\" alt=\""+fotoName+"\" border=\"0\"></td>");
          ausgabeDatei.println("\t\t\t\t<td><img height=\""+hoehe+"\" width=\""+breite+"\" src=\"../"+fotoName+"\" alt=\""+fotoName+"\" border=\"0\"></td>");

          ausgabeDatei.println("\t\t\t\t</tr>");
          ausgabeDatei.println("\t\t\t\t</table>");
          ausgabeDatei.println("\t\t\t\t<!--BildTabelle-Ende-->");
          ausgabeDatei.println("\t\t</td>");
          ausgabeDatei.println("\t\t</tr>");
          ausgabeDatei.println("\t\t</table>");
          ausgabeDatei.println("\t\t<!--Ende-Rahmen um Bild-->\n</center>");
          ausgabeDatei.println("</font></td>");
          ausgabeDatei.println("</tr>");
          ausgabeDatei.println("</table>");
          ausgabeDatei.println("</td>");
          ausgabeDatei.println("</tr>");
          ausgabeDatei.println("</table>");
          ausgabeDatei.println("</center>");
          ausgabeDatei.println("</body>");
          ausgabeDatei.println("</html>");

        // aktuelle Datei speichern und schließen
        ausgabeDatei.flush();
        ausgabeDatei.close();
      } // end of try

      catch(Exception e) {
          System.out.println("** macheWechselIndex-Exception ***\t"+e+"\t"+muster);
      } // end of catch
  } // end of for
// Erfolgsmeldung
System.out.println("WechselIndex wurde erfolgreich erstellt ;-)");
} // end of macheWechselIndex

  public void macheHauptIndexInRoot(String muster) {
    System.out.println("1. Hauptindex im ROOT-Verzeichnis anlegen ...");
    // betroffene Bilddateien sammeln
    Vector dateien=gibDateinamen(muster);
    System.out.println("HauptIndex: Es werden "+dateien.capacity()+" Bilder in der Indexdatei erzeugt");

    // dateinamen für Bild
    String dateiName=new String(); // Name der Bilddatei
    PrintWriter ausgabeDatei; // Schreibstrom zum Generieren der HTML-Inhalte
    Image pic; // Bild selbst
    int breite,hoehe; // Bildzielbreite/höhe

    try {
    // Erst mal INDEX.HTML-Datei anlegen
    ausgabeDatei = new PrintWriter( new BufferedOutputStream(
          new FileOutputStream( this.startverz.getPath()+this.startverz.separator+"index.html" ) ) );

    // und Zeug bis Filenames reinschieben ...
          ausgabeDatei.println("<!doctype html public \"-//w3c//dtd html 4.0 transitional//en\">");
          ausgabeDatei.println("<html>");
          ausgabeDatei.println("\t\t<head>");
          ausgabeDatei.println("\t\t<title>Manuelle Fotoschau von &quot;"+this.getHeadline()+"&quot;</title>");
          ausgabeDatei.println("\t\t<meta http-equiv=\"content-type\" content=\"text/html;charset=iso-8859-1\">");
          ausgabeDatei.println("\t\t<meta name=\"Title\" content=\"WechselIndexFotoschau\">");
          ausgabeDatei.println("\t\t<meta name=\"Author\" content=\"PO Computer Berlin\">");
          ausgabeDatei.println("\t\t<meta name=\"Publisher\" content=\"PO Computer Berlin\">");
          ausgabeDatei.println("\t\t<meta name=\"Copyright\" content=\"(C) PO Computer Berlin 2002-Ver20020120\">");
          ausgabeDatei.println("\t\t<meta name=\"Revisit\" content=\"After 16 days\">");
          ausgabeDatei.println("\t\t<meta name=\"Description\" content=\"Familienseiten\">");
          ausgabeDatei.println("\t\t<meta name=\"Abstract\" content=\"Familienseiten\">");
          ausgabeDatei.println("\t\t<meta name=\"page-topic\" content=\"Dienstleistung\">");
          ausgabeDatei.println("\t\t<meta name=\"audience\" content=\"Alle\">");
          ausgabeDatei.println("\t\t<meta name=\"Robots\" content=\"INDEX,FOLLOW\">");
          ausgabeDatei.println("\t\t<meta name=\"Language\" content=\"Deutsch\">");
          ausgabeDatei.println("\t\t</head>");
          ausgabeDatei.println();
          ausgabeDatei.println("<body bgcolor=\"white\" link=\"blue\" alink=\"blue\" vlink=\"blue\">");
          ausgabeDatei.println("<center>");
          ausgabeDatei.println("<table border=\"0\" cellpadding=\"1\" cellspacing=\"1\" width=\"550\">");
          ausgabeDatei.println("<tr>");
          ausgabeDatei.println("<td>");
          ausgabeDatei.println("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
          ausgabeDatei.println("<tr>");
          ausgabeDatei.println("<td>");
          ausgabeDatei.println("<center>");
          ausgabeDatei.println("<font size=\"4\" face=\"Arial,Helvetica,Geneva,Swiss,SunSans-Regular\" color=\""+this.getBackgroundcolor()+"\">");
          // Überschrift einbauen
          ausgabeDatei.println("<b>"+this.getHeadline()+"<hr></b></font></center>");
          ausgabeDatei.println("</td>");
          ausgabeDatei.println("</tr>");
          ausgabeDatei.println("<tr>");
          ausgabeDatei.println("<td><font size=\"2\" face=\"Arial,Helvetica,Geneva,Swiss,SunSans-Regular\" color=\"black\"><b>Fotoalbum besteht aus "+this.toBeDone+" Fotos.");
          ausgabeDatei.println("</b>");
          ausgabeDatei.println("&nbsp;-<br>Um die Fotos in Originalgr&ouml;&szlig; und Qualit&auml;t zu sehen auf Foto klicken!</font></td>");
          ausgabeDatei.println("<hr>");
          ausgabeDatei.println("</tr>");
          // Bilder anzeigen
          ausgabeDatei.println("<tr>");
          ausgabeDatei.println("<td><font size=\"2\" face=\"Arial,Helvetica,Geneva,Swiss,SunSans-Regular\" color=\"black\">");
          ausgabeDatei.println("<hr>");
          ausgabeDatei.println("<center>");

    // Tabelle aufbauen
    double zielgroesse=1; // prozentuale Zielgröße des Bildes
    int bildnummer=0; // Indexnummer in <code>dateien</code>-Vector
    int zeilenanz=(dateien.size()/3); // Anzahl der dreierZeilen
    int ueberhang=(dateien.size()%3); // Überhang, d.h. Bilder, die in einer Zeile sind.
    // wenn rest da ist, gibt es eine nicht komplette Zeile
    //(diese wird beim /3 nicht berücksichtigt)
    if(ueberhang!=0) zeilenanz++;

    // bringe 3 Bilder für alle Zeilen in Datei
    for(int j=1;j<=zeilenanz;j++) {
      System.out.println("\t\tHauptIndex: Zeile "+j+"/"+zeilenanz);

//System.out.println("j-for mit j="+j);
          // neue Tabellenspalte
          ausgabeDatei.println("<!-- 3er Bildformation -->");
          ausgabeDatei.println("<table border=\"0\" cellpadding=\"2\" cellspacing=\"2\" width=\"97%\">");
          ausgabeDatei.println("<tr>");

      // Alle Bilddateien durchklappern
      for(int i=1;i<=3;i++) {
//System.out.println("i-for mit i="+i);

       // 1.JPG-Dateinamen generieren
        dateiName=(String)dateien.elementAt(bildnummer++);

          // MediaTracker-  Bildgröße lesen und reduzieren
            mediTracker md= new mediTracker(this.startverz.getPath()+this.startverz.separator+dateiName);
                // geschrumpft prozentsatz ermitteln, um die richtige hoehe des Bildes zu erhalten
//                zielgroesse=(1/md.getBreite()*15600);
                zielgroesse=0.1;
            // Breite derzeit auf 156 reduziert!!!!
            breite= (int)(md.getBreite()*zielgroesse);
            hoehe= (int)(md.getHoehe()*zielgroesse);
          // Statusausgabe
//          System.out.println("HAUPTINDEXRoot-Done: "+this.getProgress(bildnummer)+"\t"+(bildnummer+1)+".Bild="+dateiName);

          // Kommentar in HTML-File generieren
ausgabeDatei.println("\t\t<!-- pic:"+bildnummer+" ("+i+".Bild in Zeile "+j+") -->");
ausgabeDatei.println("<td width=\"32%\">");
ausgabeDatei.println("<table border=\"0\" cellpadding=\"2\" cellspacing=\"2\" width=\"100%\" bgcolor=\""+this.getBackgroundcolor()+"\">");
ausgabeDatei.println("<tr>");
ausgabeDatei.println("<td>");
// Link
ausgabeDatei.println("<center>");
ausgabeDatei.println("<a href=\""+dateiName+"\" alt=\"Bild "+dateiName+"\" border=\"0\" target=\"_blank\">");
// das Bild selbst
//System.out.println("Bild h="+hoehe+"\tbreite="+breite);
ausgabeDatei.println("<img height=\""+hoehe+"\" width=\""+breite+"\" src=\""+dateiName+"\" border=\"0\">");
ausgabeDatei.println("</a>");
ausgabeDatei.println("</center>");
ausgabeDatei.println("</td>");
ausgabeDatei.println("</tr>");
ausgabeDatei.println("</table>");
ausgabeDatei.println("</td>");

// wenn man in Zeile ist, die kein Bild mehr hat - Abbruch
// i=ueberhang => man hat gerade das letzte Bild verarbeitet!!!
if( (j==zeilenanz) && (i==(ueberhang)) ) break;

        } // end of 3Bilder-for
ausgabeDatei.println("</tr>");
ausgabeDatei.println("</table>");
ausgabeDatei.println("<!-- Ende 3er Bildformation -->");
ausgabeDatei.println("</center>");
    } // end of Zeilen-for

      // wenn alle Dateinamen drin sind, muss noch der Rest nachgeschoben werden
          // Rest der Datei
          ausgabeDatei.println("</font></td>");
          ausgabeDatei.println("</tr>");
          ausgabeDatei.println("</table>");
          ausgabeDatei.println("</td>");
          ausgabeDatei.println("</tr>");
          ausgabeDatei.println("</table>");
          ausgabeDatei.println("</center>");
          ausgabeDatei.println("</body>");
          ausgabeDatei.println("</html>");
//System.out.println("ENDE i=");


        // aktuelle Datei speichern und schließen
        ausgabeDatei.flush();
        ausgabeDatei.close();
      } // end of try
      catch(Exception e) {
          System.out.println("** macheHauptIndexInRoot-Exception ***\t"+e+"\t"+muster);
      } // end of catch

// Erfolgsmeldung
System.out.println("HauptIndexinRoot wurde erfolgreich erstellt ;-)");
} // end of macheHauptIndexInRoot


  public void macheHauptIndex(String muster) {
    File zielverzeichnis; // zielverzeichnis für die previewbilder

    System.out.println("1. Hauptindex im ROOT-Verzeichnis anlegen,\nder aus Previewbildern besteht ...");
    // betroffene Bilddateien sammeln
    Vector dateien=gibDateinamen(muster);
    System.out.println("HauptIndex: Es werden "+dateien.capacity()+" Bilder in der Indexdatei erzeugt");


    // Unterverzeichnis "preview" anlegen
      System.out.println("2. Preview-Verzeichnis anlegen ...");
      String neuesVerz=this.startverz.getAbsolutePath();
      neuesVerz+=this.startverz.separator+"preview";
      // Dateipfad für HTML-Dateisprünge merken
      zielverzeichnis=new File(neuesVerz);
//      zielverzeichnis.mkdir();

    // dateinamen für Bild
    String dateiName=new String(); // Name der Bilddatei
    PrintWriter ausgabeDatei; // Schreibstrom zum Generieren der HTML-Inhalte
    Image pic; // Bild selbst
    int breite,hoehe; // Bildzielbreite/höhe

    try {
    // Erst mal INDEX.HTML-Datei anlegen
    ausgabeDatei = new PrintWriter( new BufferedOutputStream(
          new FileOutputStream( this.startverz.getPath()+this.startverz.separator+"index.html" ) ) );

    // und Zeug bis Filenames reinschieben ...
          ausgabeDatei.println("<!doctype html public \"-//w3c//dtd html 4.0 transitional//en\">");
          ausgabeDatei.println("<html>");
          ausgabeDatei.println("\t\t<head>");
          ausgabeDatei.println("\t\t<title>Herzlich Willkommen zu den Bildern von &quot;"+this.getHeadline()+"&quot;</title>");
          ausgabeDatei.println("\t\t<meta http-equiv=\"content-type\" content=\"text/html;charset=iso-8859-1\">");
          ausgabeDatei.println("\t\t<meta name=\"Title\" content=\"WechselIndexFotoschau\">");
          ausgabeDatei.println("\t\t<meta name=\"Author\" content=\"PO Computer Berlin\">");
          ausgabeDatei.println("\t\t<meta name=\"Publisher\" content=\"PO Computer Berlin\">");
          ausgabeDatei.println("\t\t<meta name=\"Copyright\" content=\"(C) PO Computer Berlin 2002-Ver20020120\">");
          ausgabeDatei.println("\t\t<meta name=\"Revisit\" content=\"After 16 days\">");
          ausgabeDatei.println("\t\t<meta name=\"Description\" content=\"Familienseiten\">");
          ausgabeDatei.println("\t\t<meta name=\"Abstract\" content=\"Familienseiten\">");
          ausgabeDatei.println("\t\t<meta name=\"page-topic\" content=\"Dienstleistung\">");
          ausgabeDatei.println("\t\t<meta name=\"audience\" content=\"Alle\">");
          ausgabeDatei.println("\t\t<meta name=\"Robots\" content=\"INDEX,FOLLOW\">");
          ausgabeDatei.println("\t\t<meta name=\"Language\" content=\"Deutsch\">");
          ausgabeDatei.println("\t\t</head>");
          ausgabeDatei.println();
          ausgabeDatei.println("<body bgcolor=\"white\" link=\"blue\" alink=\"blue\" vlink=\"blue\">");
          ausgabeDatei.println("<center>");
          ausgabeDatei.println("<table border=\"0\" cellpadding=\"1\" cellspacing=\"1\" width=\"550\">");
          ausgabeDatei.println("<tr>");
          ausgabeDatei.println("<td>");
          ausgabeDatei.println("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
          ausgabeDatei.println("<tr>");
          ausgabeDatei.println("<td>");
          ausgabeDatei.println("<center>");
          ausgabeDatei.println("<font size=\"4\" face=\"Arial,Helvetica,Geneva,Swiss,SunSans-Regular\" color=\""+this.getBackgroundcolor()+"\">");
          // Überschrift einbauen
          ausgabeDatei.println("<b>"+this.getHeadline()+"<hr></b></font></center>");
          ausgabeDatei.println("</td>");
          ausgabeDatei.println("</tr>");
          ausgabeDatei.println("<tr>");
          ausgabeDatei.println("<td><font size=\"2\" face=\"Arial,Helvetica,Geneva,Swiss,SunSans-Regular\" color=\"black\"><b>Fotoalbum besteht aus "+this.toBeDone+" Fotos.");
          ausgabeDatei.println("</b>");
          ausgabeDatei.println("&nbsp;-<br>Um die Fotos in Originalgr&ouml;&szlig;e und Qualit&auml;t zu sehen auf Foto klicken!</font></td>");
          ausgabeDatei.println("<hr>");
          ausgabeDatei.println("</tr>");
          // Bilder anzeigen
          ausgabeDatei.println("<tr>");
          ausgabeDatei.println("<td><font size=\"2\" face=\"Arial,Helvetica,Geneva,Swiss,SunSans-Regular\" color=\"black\">");
          ausgabeDatei.println("<hr>");
          ausgabeDatei.println("</tr>");

          // Bilderindex anzeigen
          ausgabeDatei.println("\t\t\t\t<!-- Indexlinks einbauen -->");
          ausgabeDatei.println("\t\t\t\t<tr>");
          ausgabeDatei.println("\t\t\t\t<td>\n\t\t\t\t<font size=\"2\" face=\"Arial,Helvetica,Geneva,Swiss,SunSans-Regular\" color=\"black\">");
          ausgabeDatei.println("\t\t\t\tEs gibt folgende M&ouml;glichkeiten, die Bilder anzusehen:<br>");
          ausgabeDatei.println("\t\t\t\t<a href=\"auto/"+changeExtension((String)dateien.elementAt(0),"html")+"\" target=\"_blank\"><b>Automatische Fotoschau</b></a> oder ");
          ausgabeDatei.println("\t\t\t\t<a href=\"perhand/"+changeExtension((String)dateien.elementAt(0),"html")+"\" target=\"_blank\"><b>Bild f&uuml;r Bild</b></a><br>");
//          ausgabeDatei.println("\t\t\t\t<a href=\"auto"+this.startverz.separator+changeExtension((String)dateien.elementAt(0),"html")+"\" target=\"_blank\"><b>Automatische Fotoschau</b></a> oder ");
//          ausgabeDatei.println("\t\t\t\t<a href=\"perhand"+this.startverz.separator+changeExtension((String)dateien.elementAt(0),"html")+"\" target=\"_blank\"><b>Bild f&uuml;r Bild</b></a><br>");
          ausgabeDatei.println("\t\t\t\t<hr>");
          ausgabeDatei.println("\t\t\t\t<br>");
          ausgabeDatei.println("\t\t\t\t<center>");

    // Tabelle aufbauen
    double zielgroesse=1; // prozentuale Zielgröße des Bildes
    int bildnummer=0; // Indexnummer in <code>dateien</code>-Vector
    int zeilenanz=(dateien.size()/3);
    int ueberhang=(dateien.size()%3);
    // wenn rest da ist, gibt es eine nicht komplette Zeile
    if(ueberhang!=0) zeilenanz++;

//System.out.println("#zeilen: "+zeilenanz+"\tueberhang = "+ueberhang);

    // bringe 3 Bilder für alle Zeilen in Datei
    for(int j=1;j<=zeilenanz;j++) {
      System.out.println("\t\t\tHauptIndexRoot: "+j+"/"+zeilenanz);

//System.out.println("j-for mit j="+j);
          // neue Tabellenspalte
          ausgabeDatei.println("<!-- 3er Bildformation -->");
          ausgabeDatei.println("<table border=\"0\" cellpadding=\"2\" cellspacing=\"2\" width=\"97%\">");
          ausgabeDatei.println("<tr>");

      // Alle Bilddateien durchklappern
      for(int i=1;i<=3;i++) {
//System.out.println("i-for mit i="+i);

       // 1.JPG-Dateinamen generieren
        dateiName=(String)dateien.elementAt(bildnummer++);

          // MediaTracker-  Bildgröße lesen und reduzieren
//            mediTracker md= new mediTracker(this.startverz.getPath()+this.startverz.separator+dateiName);
            mediTracker md= new mediTracker(zielverzeichnis.getPath()+this.startverz.separator+dateiName);
                // geschrumpft prozentsatz ermitteln, um die richtige hoehe des Bildes zu erhalten
/*
                zielgroesse=(md.getBreite()/156); // da prozent ohne *100
                zielgroesse*=0.1;
            // Breite derzeit auf 156 reduziert!!!!
*/
zielgroesse=0.5;


            breite= (int)(md.getBreite()*zielgroesse);
            hoehe= (int)(md.getHoehe()*zielgroesse);
          // Statusausgabe
//          System.out.println("HAUPTINDEX-Done: "+this.getProgress(bildnummer)+"\t"+bildnummer+".Bild="+dateiName);

          // Kommentar in HTML-File generieren
ausgabeDatei.println("\t\t<!-- pic:"+bildnummer+" ("+i+".Bild in Zeile "+j+") -->");
ausgabeDatei.println("\t\t\t<td width=\"32%\">");
ausgabeDatei.println("\t\t\t<table border=\"0\" cellpadding=\"2\" cellspacing=\"2\" width=\"100%\" bgcolor=\""+this.getBackgroundcolor()+"\">");
ausgabeDatei.println("\t\t\t<tr>");
ausgabeDatei.println("\t\t\t<td>");
// Link
ausgabeDatei.println("\t\t\t<center>");
ausgabeDatei.println("\t\t\t<a href=\""+dateiName+"\" alt=\"Bild "+dateiName+"\" border=\"0\" target=\"_blank\">");
// das Bild selbst
//System.out.println("Bild h="+hoehe+"\tbreite="+breite);
ausgabeDatei.println("\t\t\t<img height=\""+hoehe+"\" width=\""+breite+"\" src=\"preview/"+dateiName+"\" border=\"0\">");
ausgabeDatei.println("\t\t\t</a>");
ausgabeDatei.println("\t\t\t</center>");
ausgabeDatei.println("\t\t\t</td>");
ausgabeDatei.println("\t\t\t</tr>");
ausgabeDatei.println("\t\t\t</table>");
ausgabeDatei.println("\t\t\t</td>");

// wenn man in Zeile ist, die kein Bild mehr hat - Abbruch
// i=ueberhang => man hat gerade das letzte Bild verarbeitet!!!
if( (j==zeilenanz) && (i==(ueberhang)) ) break;
        } // end of 3Bilder-for
ausgabeDatei.println("\t\t\t</tr>");
ausgabeDatei.println("</table>");
ausgabeDatei.println("<!-- Ende 3er Bildformation -->");
ausgabeDatei.println("</center>\n");
    } // end of Zeilen-for

      // wenn alle Dateinamen drin sind, muss noch der Rest nachgeschoben werden
          // Rest der Datei
          ausgabeDatei.println("</font></td>");
          ausgabeDatei.println("</tr>");
          ausgabeDatei.println("</table>");
          ausgabeDatei.println("</td>");
          ausgabeDatei.println("</tr>");
          ausgabeDatei.println("</table>");
          ausgabeDatei.println("</center>");
          ausgabeDatei.println("</body>");
          ausgabeDatei.println("</html>");

        // aktuelle Datei speichern und schließen
        ausgabeDatei.flush();
        ausgabeDatei.close();
      } // end of try
      catch(Exception e) {
          System.out.println("** macheHauptIndex-Exception ***\t"+e+"\t"+muster);
      } // end of catch

// Erfolgsmeldung
System.out.println("HauptIndex wurde erfolgreich erstellt ;-)");
} // end of macheHauptIndex

  // macht schneidet von Dateinamen ".123" ab und hängt <code>.endung</code> an
  private static String changeExtension(String nameKomplett,String endung) {
              nameKomplett=nameKomplett.substring(0,nameKomplett.length()-3);
              // endung anfügen
              nameKomplett+="html";
    return nameKomplett;
  } // end of changeExtension

  // GET-Routinen zur Abfrage der Klassenvariablen
  public double getShrink() { return this.shrink; }
  public String getHeadline() { return this.headline; }
  public String getBackgroundcolor() { return this.background;}
  public int getDelay() { return this.delay; }
  public File getOriginalverzeichnis() { return (new File(this.original.getName())); }

  // Fortschritt berechnen in Prozent
  private String getProgress(int aktWert) {
      return ( (double)((int)((aktWert*1000)/(this.toBeDone))/10)+" %");
  } // end of getProgress

  // Hauptprogramm, was ein Testobjekt erzeugt und loslegt ;-)
  public static void main(String[] args) {
  // Intro
  System.out.println("*****************************");
  System.out.println("*** HTML-IndexErstellung  ***");
  System.out.println("*** (C) PO-Computer 2003  ***");
  System.out.println("***********************beta7*");
  System.out.println();

    // Testobjekt erzeugen
    try {
        TestDateiobjekt obj1 =
              new TestDateiobjekt
//              ("c:\\windows\\profiles\\test\\desktop\\abc",0.29,"Omas 90.Geburtstag","BLUE",5);
//              ("c:\\windows\\profiles\\test\\desktop\\testpic",0.27,"Omas 90.Geburtstag","GREEN",5);
//              ("c:\\windows\\profiles\\test\\desktop\\seidels",.29,"Baustelle Biesdorf 2001","GREEN",5);
//              ("c:\\Dokumente und Einstellungen\\hirsch\\Desktop\\preview",1,"Hans 70.Geburtstag Webansicht","BLUE",5);
        //        ("c:\\Dokumente und Einstellungen\\hirsch\\Desktop\\julia",1,"Julias Jugendweihe Webansicht","GREEN",5);
//        ("d:\\papa\\hochz",.27,"Hochzeit Nicole & Rainer 2002","GREEN",5);
//        ("d:\\papa\\data",.27,"mit Inge & Gerhard unterwegs ...","GREEN",5);
        ("/home/hirsch/Desktop/foo",1,"Test1 2003","GREEN",5);
//              ("c:\\windows\\profiles\\test\\desktop\\preview",1,"Omas 90.Geburtstag-Webansicht","GREEN",5);
//              ("c:\\windows\\profiles\\test\\desktop\\ttt",.6,"JUST LOOK AT THESE ...","GREEN",5);

/** CDs:
        1. / 2. / 4.
    Netz:
     Dateien aus Preview-Verzeichnis in root kopieren und
     in wechselindex-Methode das /preview/ wegnehmen ...
     dann: 1. / 2. / 3.
**/

Thread t1,t2,t3,t4;
// 1.AutoIndex
t1 = new Thread(new Webindex(obj1,"png"));
t1.start();
// 2.Wechselindex
t2 = new Thread(new ManuellerIndex(obj1,"png"));
t2.start();
// 3.HauptindexInRoot
t3 = new Thread(new MacheRootIndex(obj1,"png"));
t3.start();
// 4.Hauptindex
t4 = new Thread(new MacheHauptindex(obj1,"png"));
t4.start();


// Warten, bis alle tot sind ....
if(t1!=null) t1.join();
if(t2!=null) t2.join();
if(t4!=null) t4.join();
if(t3!=null) t3.join();


// alt:

        // 1.AutoIndex
//        obj1.macheAutoIndex("jpg");
        // 2.Wechselindex
//        obj1.macheWechselIndex("jpg");
        // 3.HauptindexInRoot
//        obj1.macheHauptIndexInRoot("jpg");
        // 4.Hauptindex
//        obj1.macheHauptIndex("jpg");

        // Programmende
        System.exit(1);
    } // end of try

    // Errorhandling
    catch(Exception e) {
      System.out.println("Bei der Ausführung kam es zu folgendem Fehler:\n"+e+
                        "\nBitte überprüfen Sie die Argumentangaben!");
    } // end of catch
  } // end of main
} // end of class
