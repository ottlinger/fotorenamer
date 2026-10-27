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
package de.aikiit.fotorenamer.gui;

import de.aikiit.fotorenamer.image.RenameProgress;
import de.aikiit.fotorenamer.image.RenameResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/** Tests real SwingWorker scheduling with plain callbacks, never a JFrame or dialog. */
@Timeout(30)
class RenameWorkerTest {

    @Test
    void runsDiscoveryAndProcessingOnOneBackgroundThreadAndCallbacksOnEdt() throws Exception {
        RecordingView view = new RecordingView();
        CountDownLatch release = new CountDownLatch(1);
        AtomicReference<Thread> factoryThread = new AtomicReference<>();
        AtomicReference<Thread> processingThread = new AtomicReference<>();
        RenameResult expected = new RenameResult(1, 1, 1, 0, 0, false);
        RenameWorker worker = new RenameWorker(() -> {
            assertThat(SwingUtilities.isEventDispatchThread()).isFalse();
            factoryThread.set(Thread.currentThread());
            return (progress, cancelled) -> {
                assertThat(SwingUtilities.isEventDispatchThread()).isFalse();
                processingThread.set(Thread.currentThread());
                progress.accept(new RenameProgress(Path.of("photo"), expected));
                try {
                    assertThat(release.await(15, TimeUnit.SECONDS)).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                }
                return expected;
            };
        }, view);
        try {
            SwingUtilities.invokeAndWait(worker::execute);
            assertThat(view.progress.poll(15, TimeUnit.SECONDS)).isNotNull();
            assertThat(worker.isDone()).isFalse();
            assertThat(view.cleanups.get()).isEqualTo(0);
        } finally {
            release.countDown();
        }
        view.awaitDelivery();
        assertThat(processingThread.get()).isSameAs(factoryThread.get());
        assertThat(view.result.get()).isEqualTo(expected);
        assertThat(view.failure.get()).isNull();
        assertThat(view.onlyEdt.get()).isTrue();
        assertThat(view.cleanups.get()).isEqualTo(1);
        assertThat(view.events).containsExactly("progress", "finished", "result");
    }

    @Test
    void unwrapsFactoryFailuresAndCleansUpBeforeReportingThem() throws Exception {
        RecordingView view = new RecordingView();
        IOException cause = new IOException("discovery failed");
        RenameWorker worker = new RenameWorker(() -> { throw cause; }, view);
        SwingUtilities.invokeAndWait(worker::execute);
        view.awaitDelivery();
        assertThat(view.failure.get()).isSameAs(cause);
        assertThat(view.result.get()).isNull();
        assertThat(view.events).containsExactly("finished", "failure");
        assertThat(view.cleanups.get()).isEqualTo(1);
        assertThat(view.onlyEdt.get()).isTrue();
    }

    @Test
    void processingFailuresAlsoRestoreTheView() throws Exception {
        RecordingView view = new RecordingView();
        IllegalStateException cause = new IllegalStateException("processing failed");
        RenameWorker worker = new RenameWorker(() -> (progress, cancelled) -> { throw cause; }, view);
        SwingUtilities.invokeAndWait(worker::execute);
        view.awaitDelivery();
        assertThat(view.failure.get()).isSameAs(cause);
        assertThat(view.events).containsExactly("finished", "failure");
        assertThat(view.onlyEdt.get()).isTrue();
    }

    @Test
    void cooperativeCancellationDoesNotUnlockControlsUntilProcessingActuallyStops() throws Exception {
        RecordingView view = new RecordingView();
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        RenameResult partial = new RenameResult(2, 1, 1, 0, 0, true);
        RenameWorker worker = new RenameWorker(() -> (progress, cancelled) -> {
            started.countDown();
            try {
                assertThat(release.await(15, TimeUnit.SECONDS)).isTrue();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
            assertThat(cancelled.getAsBoolean()).isTrue();
            return partial;
        }, view);
        try {
            SwingUtilities.invokeAndWait(worker::execute);
            assertThat(started.await(15, TimeUnit.SECONDS)).isTrue();
            worker.requestCancellation();
            assertThat(worker.isDone()).isFalse();
            assertThat(view.cleanups.get()).isEqualTo(0);
            assertThat(view.events).isEmpty();
        } finally {
            release.countDown();
        }
        view.awaitDelivery();
        assertThat(view.result.get()).isEqualTo(partial);
        assertThat(view.failure.get()).isNull();
        assertThat(view.events).containsExactly("finished", "result");
        assertThat(view.onlyEdt.get()).isTrue();
    }

    @Test
    void cancellationBeforeExecutionSkipsDiscoveryAndRestoresTheView() throws Exception {
        RecordingView view = new RecordingView();
        AtomicInteger discoveries = new AtomicInteger();
        RenameWorker worker = new RenameWorker(() -> {
            discoveries.incrementAndGet();
            return (progress, cancelled) -> new RenameResult(0, 0, 0, 0, 0, false);
        }, view);
        worker.requestCancellation();
        SwingUtilities.invokeAndWait(worker::execute);
        view.awaitDelivery();
        assertThat(discoveries.get()).isEqualTo(0);
        assertThat(view.failure.get()).isInstanceOf(CancellationException.class);
        assertThat(view.cleanups.get()).isEqualTo(1);
        assertThat(view.onlyEdt.get()).isTrue();
    }

    @Test
    void coalescedProgressUsesTheLatestAbsoluteSnapshot() throws Exception {
        RecordingView view = new RecordingView();
        RenameWorker worker = new RenameWorker(
                () -> (progress, cancelled) -> new RenameResult(0, 0, 0, 0, 0, false), view);
        RenameProgress first = new RenameProgress(Path.of("one"), new RenameResult(3, 1, 1, 0, 0, false));
        RenameProgress last = new RenameProgress(Path.of("three"), new RenameResult(3, 3, 2, 1, 0, false));
        SwingUtilities.invokeAndWait(() -> worker.process(List.of(first, last)));
        assertThat(view.progress.poll()).isEqualTo(last);
        assertThat(view.progress).isEmpty();
        assertThat(view.onlyEdt.get()).isTrue();
    }

    @Test
    void ignoresLateProgressAfterTheViewHasBeenCleanedUp() throws Exception {
        RecordingView view = new RecordingView();
        RenameWorker worker = new RenameWorker(
                () -> (progress, cancelled) -> new RenameResult(0, 0, 0, 0, 0, false), view);
        SwingUtilities.invokeAndWait(worker::execute);
        view.awaitDelivery();
        SwingUtilities.invokeAndWait(() -> worker.process(List.of(
                new RenameProgress(Path.of("late"), new RenameResult(1, 1, 1, 0, 0, false)))));
        assertThat(view.progress).isEmpty();
        assertThat(view.events).containsExactly("finished", "result");
    }

    private static final class RecordingView implements RenameWorker.View {
        private final CountDownLatch delivered = new CountDownLatch(1);
        private final AtomicBoolean onlyEdt = new AtomicBoolean(true);
        private final AtomicInteger cleanups = new AtomicInteger();
        private final AtomicReference<RenameResult> result = new AtomicReference<>();
        private final AtomicReference<Throwable> failure = new AtomicReference<>();
        private final BlockingQueue<RenameProgress> progress = new LinkedBlockingQueue<>();
        private final List<String> events = new CopyOnWriteArrayList<>();

        private void record(String event) {
            if (!SwingUtilities.isEventDispatchThread()) {
                onlyEdt.set(false);
            }
            events.add(event);
        }

        @Override
        public void progress(RenameProgress update) {
            record("progress");
            progress.add(update);
        }

        @Override
        public void finished() {
            record("finished");
            cleanups.incrementAndGet();
        }

        @Override
        public void result(RenameResult value) {
            record("result");
            result.set(value);
            delivered.countDown();
        }

        @Override
        public void failure(Throwable cause) {
            record("failure");
            failure.set(cause);
            delivered.countDown();
        }

        private void awaitDelivery() throws InterruptedException {
            assertThat(delivered.await(15, TimeUnit.SECONDS)).as("Worker callbacks did not finish").isTrue();
        }
    }
}
