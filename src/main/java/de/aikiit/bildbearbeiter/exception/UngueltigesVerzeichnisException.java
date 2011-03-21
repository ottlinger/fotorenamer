package de.aikiit.bildbearbeiter.exception;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: hirsch
 * Date: 04.03.11
 * Time: 21:48
 * To change this template use File | Settings | File Templates.
 */
public class UngueltigesVerzeichnisException extends Exception {
    public UngueltigesVerzeichnisException(File directory) {
        super();
        System.err.println(directory);
    }

    public UngueltigesVerzeichnisException(String message) {
        super(message);
    }

}
