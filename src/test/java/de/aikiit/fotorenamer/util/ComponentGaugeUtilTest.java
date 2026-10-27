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
package de.aikiit.fotorenamer.util;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import java.awt.Point;

import static de.aikiit.fotorenamer.util.ComponentGaugeUtil.createImageIcon;
import static de.aikiit.fotorenamer.util.ComponentGaugeUtil.makeCentered;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests util methods.
 *
 * @author hirsch
 * @version 2011-03-21, 13:18
 */
@Disabled("No X11 on GHA")
class ComponentGaugeUtilTest {
    /**
     * Checks image creation with a valid and an invalid path.
     */
    @Test
    void createIconFromStringPath() {
        // FIXME not really clear why this location (valid with File-separators) cannot be parsed into an URL
        // assertThat(ComponentGaugeUtil.createImageIcon(MetaDataExtractorTest.FULLPATH_TEST_IMG)).isNotNull();
        assertThat(createImageIcon("wuumansho")).isNull();
    }

    /**
     * Checks assertion failure with null parameter.
     */
    @Test
    void createIconFromStringPathWithAssertionFailure() {
        assertThatThrownBy(() -> createImageIcon(null)).isInstanceOf(AssertionError.class);
    }

    /**
     * Checks that a component gets a non-default location after calling util
     * method.
     */
    @Test
    void gaugeSwingComponent() {
        JButton button = new JButton("Test");
        Point buttonSize = button.getLocation();
        assertThat(buttonSize).isEqualTo(new Point(0, 0));
        makeCentered(button);
        assertThat(button.getLocation()).isNotSameAs(new Point(0, 0));
    }

}
