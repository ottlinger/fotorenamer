/*
 * Copyright 2026, Aiki IT, FotoRenamer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.aikiit.fotorenamer.image;

import de.aikiit.fotorenamer.exception.InvalidDirectoryException;
import de.aikiit.fotorenamer.exception.NoFilesFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

class ImageDirectoryTest {

    @TempDir
    Path directory;

    @Test
    void emptyDirectoriesAreNotReportedAsAccessErrors() {
        assertThrows(NoFilesFoundException.class, () -> new CreationDateFromExifImageRenamer(directory.toString()));
        assertThrows(NoFilesFoundException.class, () -> new RemoveExifPrefixRenamer(directory.toString()));
    }

    @Test
    void missingDirectoriesPreserveTheFilesystemCause() {
        String missing = directory.resolve("missing").toString();
        var forward = assertThrows(InvalidDirectoryException.class,
                () -> new CreationDateFromExifImageRenamer(missing));
        var reverse = assertThrows(InvalidDirectoryException.class,
                () -> new RemoveExifPrefixRenamer(missing));
        assertInstanceOf(NoSuchFileException.class, forward.getCause());
        assertInstanceOf(NoSuchFileException.class, reverse.getCause());
    }

    @Test
    void regularFilesAreNotAcceptedAsDirectories() throws Exception {
        Path file = Files.writeString(directory.resolve("file"), "not a directory");
        var exception = assertThrows(InvalidDirectoryException.class,
                () -> new RemoveExifPrefixRenamer(file.toString()));
        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    void nullAndInvalidPathsAreReportedAsInvalidDirectories() {
        assertThrows(InvalidDirectoryException.class, () -> new RemoveExifPrefixRenamer(null));
        var exception = assertThrows(InvalidDirectoryException.class,
                () -> new RemoveExifPrefixRenamer("invalid\0path"));
        assertInstanceOf(InvalidPathException.class, exception.getCause());
    }

    @Test
    void accessDeniedIsNotMisreportedAsAnEmptyDirectory() {
        AccessDeniedException cause = new AccessDeniedException(directory.toString());
        try (var files = mockDirectoryListing(invocation -> { throw cause; })) {
            var exception = assertThrows(InvalidDirectoryException.class,
                    () -> new RemoveExifPrefixRenamer(directory.toString()));
            assertSame(cause, exception.getCause());
        }
    }

    @Test
    void closesDirectoryStreamAfterEnumerationFailureAndRetainsCause() {
        IOException cause = new IOException("directory enumeration failed");
        AtomicBoolean closed = new AtomicBoolean();
        Stream<Path> entries = Stream.<Path>generate(() -> {
            throw new UncheckedIOException(cause);
        }).limit(1).onClose(() -> closed.set(true));
        try (var files = mockDirectoryListing(invocation -> entries)) {
            var exception = assertThrows(InvalidDirectoryException.class,
                    () -> new CreationDateFromExifImageRenamer(directory.toString()));
            assertInstanceOf(UncheckedIOException.class, exception.getCause());
            assertSame(cause, exception.getCause().getCause());
            assertTrue(closed.get());
        }
    }

    @Test
    void closesDirectoryStreamBeforeReportingNoImages() {
        AtomicBoolean closed = new AtomicBoolean();
        Stream<Path> entries = Stream.<Path>empty().onClose(() -> closed.set(true));
        try (var files = mockDirectoryListing(invocation -> entries)) {
            assertThrows(NoFilesFoundException.class,
                    () -> new RemoveExifPrefixRenamer(directory.toString()));
            assertTrue(closed.get());
        }
    }

    // Only intercept this directory's listing. Class loaders and Tika must still
    // be able to use all other Files methods, even when this test runs in isolation.
    private MockedStatic<Files> mockDirectoryListing(Answer<Stream<Path>> listing) {
        return mockStatic(Files.class, invocation -> {
            if (invocation.getMethod().getName().equals("list")
                    && directory.toAbsolutePath().equals(invocation.getArgument(0))) {
                return listing.answer(invocation);
            }
            return invocation.callRealMethod();
        });
    }

    @Test
    void rootPathHasANonNullDisplayName() {
        Path root = directory.toAbsolutePath().getRoot();
        assertNotNull(root);
        assertEquals(root.toString(), AbstractImageRenamer.fileName(root));
    }
}
