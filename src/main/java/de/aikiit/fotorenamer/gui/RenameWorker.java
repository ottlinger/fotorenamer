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

import de.aikiit.fotorenamer.image.ImageRenamer;
import de.aikiit.fotorenamer.image.RenameProgress;
import de.aikiit.fotorenamer.image.RenameResult;

import javax.swing.SwingWorker;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/** Runs discovery and renaming in one worker; all view callbacks run on the EDT. */
final class RenameWorker extends SwingWorker<RenameResult, RenameProgress> {

    interface View {
        void progress(RenameProgress progress);
        void finished();
        void result(RenameResult result);
        void failure(Throwable cause);
    }

    private final Callable<? extends ImageRenamer> factory;
    private final View view;
    private final AtomicBoolean cancellationRequested = new AtomicBoolean();
    // Accessed only by process/done, both on the EDT.
    private volatile boolean finished;

    RenameWorker(Callable<? extends ImageRenamer> factory, View view) {
        this.factory = Objects.requireNonNull(factory, "factory");
        this.view = Objects.requireNonNull(view, "view");
    }

    /**
     * Do not use SwingWorker.cancel(): its done callback can run before the file
     * operation has actually stopped. Keep controls locked until cooperative completion.
     */
    void requestCancellation() {
        cancellationRequested.set(true);
    }

    @Override
    protected RenameResult doInBackground() throws Exception {
        if (cancellationRequested.get()) {
            throw new CancellationException("Cancelled before image discovery");
        }
        ImageRenamer renamer = factory.call();
        return Objects.requireNonNull(renamer.renameFiles(this::publish, cancellationRequested::get), "result");
    }

    @Override
    protected void process(List<RenameProgress> chunks) {
        if (!finished && !chunks.isEmpty()) {
            // SwingWorker coalesces events. Each snapshot contains absolute counts.
            view.progress(chunks.get(chunks.size() - 1));
        }
    }

    @Override
    protected void done() {
        RenameResult result = null;
        Throwable failure = null;
        try {
            result = get();
        } catch (ExecutionException e) {
            failure = e.getCause();
        } catch (CancellationException e) {
            failure = e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            failure = e;
        } finally {
            finished = true;
            // Always dispose progress and restore controls before opening a dialog.
            view.finished();
        }
        if (failure != null) {
            view.failure(failure);
        } else {
            view.result(result);
        }
    }
}
