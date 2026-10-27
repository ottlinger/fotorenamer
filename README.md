# FotoRenamer

[![Github Action master branch status](https://github.com/ottlinger/fotorenamer/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/ottlinger/fotorenamer/actions)

[![codecov](https://codecov.io/gh/ottlinger/fotorenamer/branch/master/graph/badge.svg)](https://app.codecov.io/github/ottlinger/fotorenamer)

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/d9d0476d0f3c40bcbb1c2b4dc09099b1)](https://app.codacy.com/gh/ottlinger/fotorenamer/dashboard)

# ABSTRACT
Helper to change image filenames from **IMG\_number** to **YYYYMMDD_HHMMSS\_IMG\_number** in order to allow an easier photo sorting. YYYMMDD_HHMMSS itself is extracted from the photograph's EXIF metadata.

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
  * run the executable GUI FatJAR (includes runtime dependencies)
```
$ ./mvnw clean install
$ java -jar target/fotorenamer-4.0.0-SNAPSHOT-executable.jar
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

The launcher uses `de.aikiit:fotorenamer:4.0.0-SNAPSHOT:executable` from your local
Maven repository. It does **not** currently download a published FotoRenamer release;
keep the version in `jbang/FotoRenamer.java` in sync with `pom.xml`. After changing
application code, repeat `./mvnw clean install` and use `jbang run --fresh` to avoid
reusing cached snapshot dependencies. Reinstall an installed command with
`jbang app install --force --fresh --name fotorenamer jbang/FotoRenamer.java`.

JVM properties can be passed before the alias, for example to select English:

```sh
jbang run -Duser.language=en -Duser.country=US fotorenamer@jbang-catalog.json
```

## Running from the command line

The headless console runner uses [picocli](https://picocli.info/) and the same image
processing as the GUI ([issue #15](https://github.com/ottlinger/fotorenamer/issues/15)).
Java 17 or newer is required; no graphical desktop is needed. **Back up your images
before running it.**

Build the additional CLI FatJAR, including its runtime dependencies:

```sh
./mvnw clean verify -Pcli
```

This also runs headless integration tests against the packaged CLI. The usual
`fotorenamer-4.0.0-SNAPSHOT-executable.jar` still launches the GUI; the additional
`fotorenamer-4.0.0-SNAPSHOT-cli.jar` launches only the console runner, without a
splashscreen. `./mvnw clean package -Pcli` builds both JARs without running the
packaged-JAR integration tests.

```sh
# Add the EXIF capture timestamp to image filenames
java -jar target/fotorenamer-4.0.0-SNAPSHOT-cli.jar /home/myphotos

# Remove one leading timestamp prefix (also accepts --reverse)
java -jar target/fotorenamer-4.0.0-SNAPSHOT-cli.jar -r /home/myphotos

# Show help or the application version without processing files
java -jar target/fotorenamer-4.0.0-SNAPSHOT-cli.jar --help
java -jar target/fotorenamer-4.0.0-SNAPSHOT-cli.jar --version
```

Quote directory paths containing spaces. Only images directly in the selected
directory are processed, not subdirectories. Reverse mode removes one prefix in
`yyyyMMdd_HHmmss_` or the legacy `yyyyMMdd_HHmm_` format; it does not check the EXIF
date. The console summary reports processed, renamed, skipped, and failed counts.
Existing target files are never overwritten, and individual processing failures
do not stop the remaining files.

Exit codes for scripts:

| Code | Meaning |
| --- | --- |
| `0` | Successful processing (including skipped images), help, or version |
| `1` | Processing failure, unreadable directory, or no images found |
| `2` | Invalid arguments, missing directory argument, or a path that is not a directory |

The JBang launcher above remains a **GUI launcher**, not a CLI alias. Use the CLI
FatJAR for console or server environments, optionally with `-Djava.awt.headless=true`
before `-jar`.

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

To avoid repeated renaming ([issue #593](https://github.com/ottlinger/fotorenamer/issues/593)),
images whose filename already starts with the exact EXIF capture timestamp in
`yyyyMMdd_HHmmss_` format are left unchanged and counted as skipped in the result
summary. For example, with EXIF date `2011:01:30 13:11:02`,
`20110130_131102_holiday.jpg` is skipped. A different timestamp, a legacy
minute-only prefix such as `20110130_1311_`, or a timestamp elsewhere in the name
does not match: the EXIF prefix is prepended as usual, preserving the existing name.

### Localization

Relates to [issue #10](https://github.com/ottlinger/fotorenamer/issues/10)

  As of now the application can be localized (at the moment only German and English is supported). Use the following parameters to explicitly set language and country or trust your system defaults:
```
$ mvn -Plive-demo -Duser.language=en -Duser.country=US
```

## Splashscreen image credit

The GUI splashscreen uses the photograph
["Vintage Canon film camera hanging against a sunlit white wall" by Marlon Medau](https://unsplash.com/photos/vintage-canon-film-camera-hanging-against-a-sunlit-white-wall-W77ExXvZHD4)
from Unsplash, used under the [Unsplash License](https://unsplash.com/license).
The bundled copy at `src/main/resources/image/splash-camera.jpg` is resized to
400 × 600 pixels without cropping. This photograph is licensed separately from
the project's Apache License 2.0 source code. It is included in the application,
so displaying the splashscreen does not require an internet connection. The CLI
does not show a splashscreen.

## Developer Documentation

The project is built with Maven. The [project site](https://ottlinger.github.io/fotorenamer/)
contains this introduction and usage guide as well as the generated project reports.

Generate the site locally with:

```sh
./mvnw site
```

Open `target/site/index.html` to view it. The `pre-site` phase copies this README to
the generated site sources as the homepage, so GUI FatJAR, JBang, and CLI instructions
are maintained in one place. Use the `site` lifecycle rather than calling `site:site`
directly, which skips that preparation. Run `./mvnw clean verify -Pcli` first if you
also want current test and coverage results included in the reports.

### IntelliJ IDEA import settings

Use explicit imports for individual Java classes and static members, not wildcard
imports. The shared project code style in `.idea/codeStyles/` preserves this when
running **Optimize Imports**. Other local IntelliJ settings remain ignored by Git.

To check or apply the settings manually, open **Settings / Preferences → Editor →
Code Style → Java → Imports** and select the **Project** scheme:

- Enable **Use single class import**.
- Set **Class count to use import with '*'** to `2147483647`.
- Set **Names count to use static import with '*'** to `2147483647`.
- Empty **Packages to Use Import with '*'**, including the default `java.awt` and
  `javax.swing` entries; these exceptions would otherwise still produce wildcards.

These thresholds cover both normal and static imports. If the project is already
open in IntelliJ, reload the project settings if the shared scheme is not picked
up automatically.