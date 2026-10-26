/*
 * Copyright 2011-2026, Aiki IT, FotoRenamer
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
import de.aikiit.fotorenamer.util.ComponentGaugeUtil;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

import static de.aikiit.fotorenamer.util.LocalizationHelper.getBundleString;

/** EDT-only progress view; no processing, sleeps, or artificial delays. */
public class ProgressBar extends JFrame {

    private final JLabel textInfo = new JLabel(" ");
    private final JProgressBar progressBar;

    /** Shows indeterminate progress during discovery when maxCapacity is zero. */
    public ProgressBar(final int maxCapacity) {
        setTitle(getBundleString("fotorenamer.ui.progress"));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);
        progressBar = new JProgressBar(0, maxCapacity);
        progressBar.setIndeterminate(maxCapacity == 0);
        progressBar.setStringPainted(true);
        textInfo.setText(getBundleString("fotorenamer.ui.progress.scanning"));
        textInfo.setPreferredSize(new Dimension(450, textInfo.getPreferredSize().height));
        getContentPane().setLayout(new GridLayout(3, 1));
        getContentPane().add(new JLabel(getBundleString("fotorenamer.ui.progress.title")));
        getContentPane().add(textInfo);
        getContentPane().add(progressBar);
        pack();
        ComponentGaugeUtil.makeCentered(this);
        setVisible(true);
    }

    /** Applies absolute counts, including when SwingWorker coalesces progress events. */
    public final void updateProgress(final RenameProgress progress) {
        progressBar.setIndeterminate(false);
        progressBar.setMaximum(progress.result().total());
        progressBar.setValue(progress.result().processed());
        Path name = progress.file().getFileName();
        textInfo.setText(name == null ? progress.file().toString() : name.toString());
        textInfo.setToolTipText(progress.file().toString());
    }

    public final String getText() {
        return textInfo.getText();
    }

    public final int getProgress() {
        return progressBar.getValue();
    }
}
