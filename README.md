# FotoRenamer

[![Github Action master branch status](https://github.com/ottlinger/fotorenamer/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/ottlinger/fotorenamer/actions)

[![codecov](https://codecov.io/gh/ottlinger/fotorenamer/branch/master/graph/badge.svg)](https://codecov.io/gh/ottlinger/fotorenamer)

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/d9d0476d0f3c40bcbb1c2b4dc09099b1)](https://app.codacy.com/gh/ottlinger/fotorenamer/dashboard)
https://github.com/users/ottlinger/projects/4

# ABSTRACT
Helper to change image filenames from **IMG\_number** to **YYYYMMDD\_IMG\_number** in order to allow an easier photo sorting. YYYMMDD itself is extracted from the photograph's EXIF metadata.

Do not forget to do backups of your photos before using this tool!

# HISTORY
## 2003 - the beginnings
The idea for this little tool appeared in 2003, when I did my first students programming courses at university and discovered that my little IXUS camera set the images date to the date the image was taken.
After several operating system upgrades this behaviour changed and I needed to find a way to extract the creation date from the images metadata. Therefore I needed to evaluate EXIF data extraction ...
## 2011 - some time
During my parental leave in 2011 I decided to use this dirty little hack to evaluate the usage of google code as a project tool and to get used to my IntelliJ :-)
## 2015

The project moved over to GitHub :smile:

# USAGE
## Building
This tool can be used with [Maven](https://maven.apache.org/download.html) in two ways:
  * run as a standalone application
```
$ ./mvnw clean install -Plive-demo
```  
  * run the executable jar
```
$ ./mvnw clean install
$ java -jar target/fotorenamer-2.0.0-executable.jar
```  
  * start a deprecated stable version [(0.1.3-SNAPSHOT)](https://www.aiki-it.de/sw/ixus/bildbearbeiter.jnlp) via Webstart

## Running
1. Create a backup of your images.
1. Start this tool.
1. Select your image directory.
1. Hit 'Umbenennen' (Rename) to get your files renamed automatically.
1. Done :-)

### Localization

Relates to [issue #10](https://github.com/ottlinger/fotorenamer/issues/10)

  As of now the application can be localized (at the moment only German and English is supported). Use the following parameters to explicitly set language and country or trust your system defaults:
```
$ mvn -Plive-demo -Duser.language=en -Duser.country=US
```

## Developer Documentation

The project is built with maven - you can have a look at the current [site reports](https://ottlinger.github.io/fotorenamer/).
