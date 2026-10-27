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
package de.aikiit.fotorenamer.gui;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.awt.SplashScreen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StartupSplashTest {

    @Test
    void bundledSplashPhotographCanBeDecoded() throws Exception {
        var resource = StartupSplash.class.getResource(StartupSplash.IMAGE_LOCATION);
        assertThat(resource).isNotNull();
        var image = ImageIO.read(resource);
        assertThat(image).isNotNull();
        assertThat(image.getWidth()).isEqualTo(400);
        assertThat(image.getHeight()).isEqualTo(600);
    }

    @Test
    void headlessStartupDoesNotAccessNativeSplash() {
        try (var graphics = mockStatic(GraphicsEnvironment.class);
             var nativeApi = mockStatic(SplashScreen.class)) {
            graphics.when(GraphicsEnvironment::isHeadless).thenReturn(true);
            try (StartupSplash splash = StartupSplash.show()) {
                assertThatCode(splash::close).doesNotThrowAnyException();
                assertThatCode(splash::close).doesNotThrowAnyException();
            }
            nativeApi.verifyNoInteractions();
        }
    }

    @Test
    void reusesNativeSplashAndClosesItEvenOnStartupFailure() {
        SplashScreen nativeSplash = mock(SplashScreen.class);
        when(nativeSplash.isVisible()).thenReturn(true);
        try (var graphics = mockStatic(GraphicsEnvironment.class);
             var swing = mockStatic(SwingUtilities.class);
             var nativeApi = mockStatic(SplashScreen.class)) {
            graphics.when(GraphicsEnvironment::isHeadless).thenReturn(false);
            swing.when(SwingUtilities::isEventDispatchThread).thenReturn(true);
            nativeApi.when(SplashScreen::getSplashScreen).thenReturn(nativeSplash);
            assertThatThrownBy(() -> {
                try (StartupSplash splash = StartupSplash.show()) {
                    verify(nativeSplash, never()).close();
                    throw new IllegalStateException("Main window startup failed");
                }
            }).isInstanceOf(IllegalStateException.class);
            verify(nativeSplash).close();
        }
    }

    @Test
    void doesNotCloseNativeSplashAgainAfterJvmHasClosedIt() {
        SplashScreen nativeSplash = mock(SplashScreen.class);
        when(nativeSplash.isVisible()).thenReturn(false);
        StartupSplash splash = new StartupSplash(nativeSplash, null);
        splash.close();
        splash.close();
        verify(nativeSplash, never()).close();
    }

    @Test
    void disposesSwingFallbackOnlyOnce() {
        JWindow window = mock(JWindow.class);
        StartupSplash splash = new StartupSplash(null, window);
        splash.close();
        splash.close();
        verify(window).dispose();
    }

    @Test
    void nativeSplashFailureDoesNotPreventApplicationStartup() {
        try (var graphics = mockStatic(GraphicsEnvironment.class);
             var swing = mockStatic(SwingUtilities.class);
             var nativeApi = mockStatic(SplashScreen.class)) {
            graphics.when(GraphicsEnvironment::isHeadless).thenReturn(false);
            swing.when(SwingUtilities::isEventDispatchThread).thenReturn(true);
            nativeApi.when(SplashScreen::getSplashScreen).thenThrow(new IllegalStateException("Splash unavailable"));
            assertThatCode(() -> StartupSplash.show().close()).doesNotThrowAnyException();
        }
    }

    @Test
    void closeFailureDoesNotEscapeIntoApplicationStartup() {
        SplashScreen nativeSplash = mock(SplashScreen.class);
        when(nativeSplash.isVisible()).thenReturn(true);
        doThrow(new IllegalStateException("Already closed")).when(nativeSplash).close();
        StartupSplash splash = new StartupSplash(nativeSplash, null);
        assertThatCode(splash::close).doesNotThrowAnyException();
    }
}
