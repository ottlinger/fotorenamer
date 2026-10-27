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
package de.aikiit.fotorenamer.exception;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class FotorenamerExceptionTest {
    @Test
    void planConstructors() {
        RenamingErrorException e = new RenamingErrorException("woo");
        assertThat(e).hasMessageContaining("woo");

        NoFilesFoundException e2 = new NoFilesFoundException(Path.of("woo"));
        assertThat(e2).isNotNull();
    }

    @Test
    void invalidDirectories() {
        InvalidDirectoryException e1 = new InvalidDirectoryException(Path.of("woo"));
        assertThat(e1).isNotNull();

        e1 = new InvalidDirectoryException("woo");
        assertThat(e1).isNotNull();
        assertThat(e1.getMessage()).isEqualTo("woo");
    }

    @Test
    void directoryAccessFailureRetainsItsCause() {
        IOException cause = new IOException("access denied");
        InvalidDirectoryException exception = new InvalidDirectoryException("photos", cause);
        assertThat(exception.getMessage()).isEqualTo("photos");
        assertThat(exception.getCause()).isSameAs(cause);
    }

}
