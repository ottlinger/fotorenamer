/*
 * Copyright 2026, Aiki IT, FotoRenamer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.aikiit.fotorenamer;

import de.aikiit.fotorenamer.gui.MainUIWindow;
import de.aikiit.fotorenamer.gui.StartupSplash;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ApplicationStarterTest {

    @Test
    void schedulesStartupOnTheEventDispatchThread() {
        try (var swing = mockStatic(SwingUtilities.class)) {
            ApplicationStarter.main(new String[0]);
            swing.verify(() -> SwingUtilities.invokeLater(any(Runnable.class)));
        }
    }

    @Test
    void keepsSplashOpenUntilMainWindowHasBeenBuilt() {
        StartupSplash splash = mock(StartupSplash.class);
        try (var splashes = mockStatic(StartupSplash.class);
             var ui = mockStatic(MainUIWindow.class);
             var lookAndFeel = mockStatic(UIManager.class)) {
            splashes.when(StartupSplash::show).thenReturn(splash);
            ui.when(MainUIWindow::build).thenAnswer(invocation -> {
                verify(splash, never()).close();
                return null;
            });
            ApplicationStarter.start();
            ui.verify(MainUIWindow::build);
            verify(splash).close();
        }
    }

    @Test
    void closesSplashWhenMainWindowFailsToStart() {
        StartupSplash splash = mock(StartupSplash.class);
        try (var splashes = mockStatic(StartupSplash.class);
             var ui = mockStatic(MainUIWindow.class);
             var lookAndFeel = mockStatic(UIManager.class)) {
            splashes.when(StartupSplash::show).thenReturn(splash);
            ui.when(MainUIWindow::build).thenThrow(new IllegalStateException("Main window startup failed"));
            assertDoesNotThrow(ApplicationStarter::start);
            verify(splash).close();
        }
    }

    @Test
    void lookAndFeelFailureStillBuildsMainWindowAndClosesSplash() {
        StartupSplash splash = mock(StartupSplash.class);
        try (var splashes = mockStatic(StartupSplash.class);
             var ui = mockStatic(MainUIWindow.class);
             var lookAndFeel = mockStatic(UIManager.class)) {
            splashes.when(StartupSplash::show).thenReturn(splash);
            lookAndFeel.when(UIManager::getCrossPlatformLookAndFeelClassName).thenReturn("unavailable");
            lookAndFeel.when(() -> UIManager.setLookAndFeel("unavailable"))
                    .thenThrow(new UnsupportedLookAndFeelException("Unavailable"));
            assertDoesNotThrow(ApplicationStarter::start);
            ui.verify(MainUIWindow::build);
            verify(splash).close();
        }
    }
}
