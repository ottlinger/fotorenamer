/*
 * Copyright 2026, Aiki IT, FotoRenamer
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.aikiit.fotorenamer.cli;

import de.aikiit.fotorenamer.exception.InvalidDirectoryException;
import de.aikiit.fotorenamer.exception.NoFilesFoundException;
import de.aikiit.fotorenamer.image.CreationDateFromExifImageRenamer;
import de.aikiit.fotorenamer.image.ImageRenamer;
import de.aikiit.fotorenamer.image.RemoveExifPrefixRenamer;
import de.aikiit.fotorenamer.image.RenameResult;
import de.aikiit.fotorenamer.util.Version;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/** Headless console entry point sharing the GUI's image processing (issue #15). */
@Command(name = "fotorenamer", mixinStandardHelpOptions = true,
        version = "FotoRenamer " + Version.VERSION,
        description = "Rename images using their EXIF capture timestamp.",
        footer = "Back up your images before running this tool. Only the selected directory is processed.")
public final class ConsoleRunner implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "DIRECTORY", description = "Directory containing the images.")
    private Path directory;

    @Option(names = {"-r", "--reverse"}, description = "Remove one leading EXIF timestamp prefix instead of adding it.")
    private boolean reverse;

    @Spec
    private CommandSpec spec;

    /** Launches the CLI and returns its result to the operating system. */
    public static void main(final String[] args) {
        System.exit(new CommandLine(new ConsoleRunner()).execute(args));
    }

    /** Processes images synchronously without initializing Swing. */
    @Override
    public Integer call() {
        if (!Files.isDirectory(directory)) {
            throw new CommandLine.ParameterException(spec.commandLine(), "Not a directory: " + directory);
        }
        ImageRenamer renamer;
        try {
            renamer = reverse
                    ? new RemoveExifPrefixRenamer(directory.toString())
                    : new CreationDateFromExifImageRenamer(directory.toString());
        } catch (NoFilesFoundException e) {
            spec.commandLine().getErr().println("No images found in: " + directory.toAbsolutePath());
            return 1;
        } catch (InvalidDirectoryException e) {
            spec.commandLine().getErr().println("Unable to read directory: " + directory.toAbsolutePath());
            return 1;
        }
        RenameResult result = renamer.renameFiles();
        spec.commandLine().getOut().printf(
                "Directory: %s%nProcessed: %d / %d%nRenamed: %d%nSkipped: %d%nFailed: %d%n",
                directory.toAbsolutePath(), result.processed(), result.total(),
                result.renamed(), result.skipped(), result.failed());
        return result.failed() == 0 && !result.cancelled() ? 0 : 1;
    }
}
