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
package de.aikiit.fotorenamer.image;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class RenameProcessingTest {

    @TempDir
    Path directory;

    @Test
    void progressSnapshotsHaveAbsoluteConsistentCountsAndMatchFinalResult() throws Exception {
        copyImage("20110130_131102_renamed");
        copyImage("unchanged");
        copyImage("20110130_131102_conflict");
        Files.writeString(directory.resolve("conflict"), "keep this");
        var renamer = new RemoveExifPrefixRenamer(directory.toString());
        List<RenameProgress> events = new ArrayList<>();
        RenameResult result = renamer.renameFiles(events::add);
        assertEquals(new RenameResult(3, 3, 1, 1, 1, false), result);
        assertEquals(3, events.size());
        for (int i = 0; i < events.size(); i++) {
            RenameResult snapshot = events.get(i).result();
            assertEquals(3, snapshot.total());
            assertEquals(i + 1, snapshot.processed());
            assertEquals(snapshot.processed(), snapshot.renamed() + snapshot.skipped() + snapshot.failed());
        }
        assertEquals(1, events.get(0).result().processed());
        assertEquals(result, events.get(2).result());
        assertEquals("keep this", Files.readString(directory.resolve("conflict")));
    }

    @Test
    void cancellationBeforeProcessingLeavesAllFilesUntouched() throws Exception {
        Path image = copyImage("20110130_131102_photo");
        var renamer = new RemoveExifPrefixRenamer(directory.toString());
        RenameResult result = renamer.renameFiles(progress -> fail("Unexpected progress"), () -> true);
        assertEquals(new RenameResult(1, 0, 0, 0, 0, true), result);
        assertTrue(Files.exists(image));
        assertFalse(Files.exists(directory.resolve("photo")));
    }

    @Test
    void cancellationBetweenFilesPreservesCompletedMovesAndPartialCounts() throws Exception {
        copyImage("20110130_131102_one");
        copyImage("20110130_131102_two");
        copyImage("20110130_131102_three");
        var renamer = new RemoveExifPrefixRenamer(directory.toString());
        AtomicBoolean cancel = new AtomicBoolean();
        List<RenameProgress> events = new ArrayList<>();
        RenameResult result = renamer.renameFiles(progress -> {
            events.add(progress);
            cancel.set(true);
        }, cancel::get);
        assertEquals(new RenameResult(3, 1, 1, 0, 0, true), result);
        assertEquals(1, events.size());
        Path moved = events.get(0).file();
        assertFalse(Files.exists(moved));
        assertTrue(Files.exists(moved.resolveSibling(moved.getFileName().toString().substring(16))));
        try (var files = Files.list(directory)) {
            assertEquals(3, files.count());
        }
    }

    @Test
    void interruptionIsPreservedAndDoesNotTouchUnprocessedFiles() throws Exception {
        Path image = copyImage("20110130_131102_photo");
        var renamer = new RemoveExifPrefixRenamer(directory.toString());
        try {
            Thread.currentThread().interrupt();
            assertEquals(new RenameResult(1, 0, 0, 0, 0, true), renamer.renameFiles());
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
        assertTrue(Files.exists(image));
    }

    @Test
    void interruptionAlsoStopsDiscoveryWithoutClearingInterruptStatus() throws Exception {
        Path image = copyImage("photo");
        try {
            Thread.currentThread().interrupt();
            assertThrows(CancellationException.class,
                    () -> new CreationDateFromExifImageRenamer(directory.toString()));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
        assertTrue(Files.exists(image));
    }

    @Test
    void observerFailuresDoNotChangeOutcomesOrAbortTheBatch() throws Exception {
        copyImage("20110130_131102_one");
        copyImage("20110130_131102_two");
        var renamer = new RemoveExifPrefixRenamer(directory.toString());
        AtomicInteger callbacks = new AtomicInteger();
        RenameResult result = renamer.renameFiles(progress -> {
            callbacks.incrementAndGet();
            throw new IllegalStateException("observer failed");
        });
        assertEquals(new RenameResult(2, 2, 2, 0, 0, false), result);
        assertEquals(2, callbacks.get());
        assertTrue(Files.exists(directory.resolve("one")));
        assertTrue(Files.exists(directory.resolve("two")));
    }

    @Test
    void unexpectedPerFileRuntimeFailuresAreCountedAndOtherFilesContinue() throws Exception {
        copyImage("bad");
        copyImage("good");
        var renamer = new AbstractImageRenamer(directory.toString()) {
            @Override
            protected String renameImage(Path imageFile) {
                if (fileName(imageFile).equals("bad")) {
                    throw new IllegalStateException("unexpected metadata failure");
                }
                return fileName(imageFile);
            }
        };
        assertEquals(new RenameResult(2, 2, 0, 1, 1, false), renamer.renameFiles());
        assertTrue(Files.exists(directory.resolve("bad")));
        assertTrue(Files.exists(directory.resolve("good")));
    }

    @Test
    void nullCallbacksAreRejectedBeforeAnyFileIsMoved() throws Exception {
        Path image = copyImage("20110130_131102_photo");
        var renamer = new RemoveExifPrefixRenamer(directory.toString());
        assertThrows(NullPointerException.class, () -> renamer.renameFiles(null, () -> false));
        assertThrows(NullPointerException.class, () -> renamer.renameFiles(progress -> { }, null));
        assertTrue(Files.exists(image));
    }

    @Test
    void rejectsInconsistentResultCountsAndNullProgressData() {
        assertThrows(IllegalArgumentException.class, () -> new RenameResult(1, 2, 2, 0, 0, false));
        assertThrows(IllegalArgumentException.class, () -> new RenameResult(1, 1, 0, 0, 0, false));
        assertThrows(IllegalArgumentException.class, () -> new RenameResult(1, 0, -1, 1, 0, false));
        assertThrows(NullPointerException.class,
                () -> new RenameProgress(null, new RenameResult(0, 0, 0, 0, 0, false)));
        assertThrows(NullPointerException.class, () -> new RenameProgress(directory, null));
    }

    private Path copyImage(String name) throws Exception {
        Path target = directory.resolve(name);
        try (var input = getClass().getResourceAsStream("/data/IMG_7559_mini.JPG")) {
            assertNotNull(input);
            Files.copy(input, target);
        }
        return target;
    }
}
