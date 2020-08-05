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

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class FotorenamerExceptionTest {
    @Test
    public final void planConstructors() {
        RenamingErrorException e = new RenamingErrorException("woo");
        assertTrue(e.getMessage().contains("woo"));

        NoFilesFoundException e2 = new NoFilesFoundException(new File
                ("woo"));
        assertNotNull(e2);
    }

    @Test
    public final void invalidDirectories() {
        InvalidDirectoryException e1 = new InvalidDirectoryException(new File
                ("woo"));
        assertNotNull(e1);

        e1 = new InvalidDirectoryException("woo");
        assertNotNull(e1);
        assertEquals("woo", e1.getMessage());
    }

}
