# FotoRenamer

[![Github Action master branch status](https://github.com/ottlinger/fotorenamer/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/ottlinger/fotorenamer/actions)

[![codecov](https://codecov.io/gh/ottlinger/fotorenamer/branch/master/graph/badge.svg)](https://codecov.io/gh/ottlinger/fotorenamer)

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/d9d0476d0f3c40bcbb1c2b4dc09099b1)](https://app.codacy.com/gh/ottlinger/fotorenamer/dashboard)

# ABSTRACT
Helper to change image filenames from **IMG\_number** to **YYYYMMDD\_IMG\_number** in order to allow an easier photo sorting. YYYMMDD itself is extracted from the photograph's EXIF metadata.

Do not forget to do backups of your photos before using this tool!

# HISTORY
## 2003 - the beginnings
The idea for this little tool appeared in 2003, when I did my first students programming courses at university and discovered that my little IXUS camera set the images date to the date the image was taken.
After several operating system upgrades this behaviour changed and I needed to find a way to extract the creation date from the images metadata. Therefore I needed to evaluate EXIF data extraction ...
## 2011 - some time
During my parental leave in 2011 I decided to use this dirty little hack to evaluate the usage of google code as a project tool and to get used to my IntelliJ :-)
## since 2015

The project moved over to GitHub :smile:

A lot of stuff I used before just vanished as deprecated or was removed from the JDK entirely:
* WebStart
* DEVHINT: start a deprecated, but old stable version [(0.1.3-SNAPSHOT)](https://www.aiki-it.de/sw/ixus/bildbearbeiter.jnlp) via WebStart
* Applets

# USAGE
## Building
This tool can be used with [Maven](https://maven.apache.org/download.html) in two ways:

  * run as a standalone application
```
$ ./run.sh 
OR
$ ./mvnw clean install -Plive-demo
```  
  * run the executable jar
```
$ ./mvnw clean install
$ java -jar target/fotorenamer-3.0.0-executable.jar
```  

## Running with JBang

Install [JBang](https://www.jbang.dev/download/) and build and install the current
snapshot into your local Maven repository once (on Windows use `mvnw.cmd`):

```sh
./mvnw clean install
jbang run --fresh fotorenamer@jbang-catalog.json
```

The catalog launches the existing Swing GUI, not a command-line renamer. A graphical
desktop session is required. JBang selects Java 17 or newer and can download a
suitable JDK if needed; the Maven build itself also requires a suitable JDK.

Alternatively, run the launcher directly:

```sh
jbang run --fresh jbang/FotoRenamer.java
```

To make the command available outside the checkout:

```sh
jbang app install --name fotorenamer jbang/FotoRenamer.java
fotorenamer
```

The launcher uses `de.aikiit:fotorenamer:3.0.0-SNAPSHOT:executable` from your local
Maven repository. It does **not** currently download a published FotoRenamer release;
keep the version in `jbang/FotoRenamer.java` in sync with `pom.xml`. After changing
application code, repeat `./mvnw clean install` and use `jbang run --fresh` to avoid
reusing cached snapshot dependencies. Reinstall an installed command with
`jbang app install --force --fresh --name fotorenamer jbang/FotoRenamer.java`.

JVM properties can be passed before the alias, for example to select English:

```sh
jbang run -Duser.language=en -Duser.country=US fotorenamer@jbang-catalog.json
```

## Running
1. Create a backup of your images.
1. Start this tool.
1. Select your image directory.
1. Hit 'Umbenennen' (Rename) to get your files renamed automatically.
1. Done :-)

### Image detection and EXIF metadata

Images are detected by their contents using Apache Tika, not by their filename or
extension ([issue #13](https://github.com/ottlinger/fotorenamer/issues/13)). For example,
a JPEG named `holiday.dat` or `holiday` is still considered for renaming, while a
text file named `notes.jpg` is ignored.

For detected images, Apache Commons Imaging attempts to read the EXIF capture date
(`DateTimeOriginal`). JPEG and TIFF EXIF dates are supported. Images without a
usable date, unsupported formats, and unreadable or damaged files are left unchanged.
Processing errors are logged with the file path and exception; processing continues
with the other files. Detection alone does not guarantee EXIF support.

Renaming preserves the original extension and file contents, and never overwrites an
existing target file. The result message counts only successful renames.

### Localization

Relates to [issue #10](https://github.com/ottlinger/fotorenamer/issues/10)

  As of now the application can be localized (at the moment only German and English is supported). Use the following parameters to explicitly set language and country or trust your system defaults:
```
$ mvn -Plive-demo -Duser.language=en -Duser.country=US
```

## Developer Documentation

The project is built with maven - you can have a look at the current [site reports](https://ottlinger.github.io/fotorenamer/).
