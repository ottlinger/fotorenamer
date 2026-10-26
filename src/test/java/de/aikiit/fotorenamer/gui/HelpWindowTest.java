/*
Copyright 2011, Aiki IT, FotoRenamer
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package de.aikiit.fotorenamer.gui;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Call help window.
 *
 * @author hirsch
 * @version 2011-04-08, 00:24
 */
public class HelpWindowTest {
    @Test
    void helpPagesAndRelativeCameraImageAreReadableWithoutAWindow() throws Exception {
        Pattern imageSource = Pattern.compile("<img\\b[^>]*\\bsrc\\s*=\\s*\"([^\"]+)\"",
                Pattern.CASE_INSENSITIVE);
        for (String filename : List.of("hilfe.html", "help.html")) {
            URL page = HelpWindow.class.getResource(HelpWindow.HTML_HELP_LOCATION + filename);
            assertNotNull(page, filename);
            String html;
            try (var input = page.openStream()) {
                html = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
            assertTrue(html.contains("<html"), filename);
            var imageLink = imageSource.matcher(html);
            assertTrue(imageLink.find(), "Missing camera image in " + filename);
            assertEquals("camera.jpg", imageLink.group(1));

            // Resolve relative to the HTML URL, just as JEditorPane does. This
            // works for both classpath directories and jar: URLs on every OS.
            URL camera = new URL(page, imageLink.group(1));
            try (var input = camera.openStream()) {
                var image = ImageIO.read(input);
                assertNotNull(image, "Cannot decode camera image linked from " + filename);
                assertTrue(image.getWidth() > 0);
                assertTrue(image.getHeight() > 0);
            }
        }
    }

    @Disabled("No X11 on GHA")
    void initWindow() {
        assertNotNull(new HelpWindow());
    }
}
