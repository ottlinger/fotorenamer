# ABSTRACT #
Helper to change image filenames from **IMG\_number** to **YYYYMMDD\_IMG\_number** in order to allow an easier photo sorting. YYYMMDD itself is extracted from the photograph's EXIF metadata.

Do not forget to do backups of your photos before using this tool!

# HISTORY #
## 2003 - the beginnings ##
The idea for this little tool appeared in 2003, when I did my first students programming courses at university and discovered that my little IXUS camera set the images date to the date the image was taken.
After several operating system upgrades this behaviour changed and I needed to find a way to extract the creation date from the images metadata. Therefore I needed to evaluate EXIF data extraction ...
## 2011 - some time ##
During my parental leave in 2011 I decided to use this dirty little hack to evaluate the usage of google code as a project tool and to get used to my IntelliJ :-)

# USAGE #
## Building ##
This tool can be used with [Maven3](http://maven.apache.org/download.html) in two ways:
  * run as a standalone application (_mvn clean install -Plive-demo_)
  * via Java Webstart (_mvn install webstart:jnlp_, after that javaws bildbearbeiter.jnlp)
  * start a last stable version [(currently: 0.1.3-SNAPSHOT)](http://www.aiki-it.de/sw/ixus/bildbearbeiter.jnlp) via Webstart

## Developer Documentation ##

The project is maintained/built with maven - you can have a look at the current [site reports](http://fotorenamer.googlecode.com/hg/webpage/site/index.html).


## Running ##
  1. Create a backup of your images.
  1. Start this tool.
  1. Select your image directory.
  1. Hit 'Umbenennen' (Rename) to get your files renamed automatically.
  1. Done :-)