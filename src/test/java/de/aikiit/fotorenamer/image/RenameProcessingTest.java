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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;


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
        assertThat(result).isEqualTo(new RenameResult(3, 3, 1, 1, 1, false));
        assertThat(events).hasSize(3);
        for (int i = 0; i < events.size(); i++) {
            RenameResult snapshot = events.get(i).result();
            assertThat(snapshot.total()).isEqualTo(3);
            assertThat(snapshot.processed()).isEqualTo(i + 1);
            assertThat(snapshot.renamed() + snapshot.skipped() + snapshot.failed()).isEqualTo(snapshot.processed());
        }
        assertThat(events.get(0).result().processed()).isEqualTo(1);
        assertThat(events.get(2).result()).isEqualTo(result);
        assertThat(Files.readString(directory.resolve("conflict"))).isEqualTo("keep this");
    }

    @Test
    void cancellationBeforeProcessingLeavesAllFilesUntouched() throws Exception {
        Path image = copyImage("20110130_131102_photo");
        var renamer = new RemoveExifPrefixRenamer(directory.toString());
        RenameResult result = renamer.renameFiles(progress -> fail("Unexpected progress"), () -> true);
        assertThat(result).isEqualTo(new RenameResult(1, 0, 0, 0, 0, true));
        assertThat(Files.exists(image)).isTrue();
        assertThat(Files.exists(directory.resolve("photo"))).isFalse();
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
        assertThat(result).isEqualTo(new RenameResult(3, 1, 1, 0, 0, true));
        assertThat(events).hasSize(1);
        Path moved = events.get(0).file();
        assertThat(Files.exists(moved)).isFalse();
        assertThat(Files.exists(moved.resolveSibling(moved.getFileName().toString().substring(16)))).isTrue();
        try (var files = Files.list(directory)) {
            assertThat(files.count()).isEqualTo(3);
        }
    }

    @Test
    void interruptionIsPreservedAndDoesNotTouchUnprocessedFiles() throws Exception {
        Path image = copyImage("20110130_131102_photo");
        var renamer = new RemoveExifPrefixRenamer(directory.toString());
        try {
            Thread.currentThread().interrupt();
            assertThat(renamer.renameFiles()).isEqualTo(new RenameResult(1, 0, 0, 0, 0, true));
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted();
        }
        assertThat(Files.exists(image)).isTrue();
    }

    @Test
    void interruptionAlsoStopsDiscoveryWithoutClearingInterruptStatus() throws Exception {
        Path image = copyImage("photo");
        try {
            Thread.currentThread().interrupt();
            assertThatThrownBy(() -> new CreationDateFromExifImageRenamer(directory.toString())).isInstanceOf(CancellationException.class);
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted();
        }
        assertThat(Files.exists(image)).isTrue();
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
        assertThat(result).isEqualTo(new RenameResult(2, 2, 2, 0, 0, false));
        assertThat(callbacks.get()).isEqualTo(2);
        assertThat(Files.exists(directory.resolve("one"))).isTrue();
        assertThat(Files.exists(directory.resolve("two"))).isTrue();
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
        assertThat(renamer.renameFiles()).isEqualTo(new RenameResult(2, 2, 0, 1, 1, false));
        assertThat(Files.exists(directory.resolve("bad"))).isTrue();
        assertThat(Files.exists(directory.resolve("good"))).isTrue();
    }

    @Test
    void nullCallbacksAreRejectedBeforeAnyFileIsMoved() throws Exception {
        Path image = copyImage("20110130_131102_photo");
        var renamer = new RemoveExifPrefixRenamer(directory.toString());
        assertThatThrownBy(() -> renamer.renameFiles(null, () -> false)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> renamer.renameFiles(progress -> { }, null)).isInstanceOf(NullPointerException.class);
        assertThat(Files.exists(image)).isTrue();
    }

    @Test
    void rejectsInconsistentResultCountsAndNullProgressData() {
        assertThatThrownBy(() -> new RenameResult(1, 2, 2, 0, 0, false)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RenameResult(1, 1, 0, 0, 0, false)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RenameResult(1, 0, -1, 1, 0, false)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RenameProgress(null, new RenameResult(0, 0, 0, 0, 0, false))).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new RenameProgress(directory, null)).isInstanceOf(NullPointerException.class);
    }

    private Path copyImage(String name) throws Exception {
        Path target = directory.resolve(name);
        try (var input = getClass().getResourceAsStream("/data/IMG_7559_mini.JPG")) {
            assertThat(input).isNotNull();
            Files.copy(input, target);
        }
        return target;
    }
}
