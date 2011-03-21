package de.aikiit.bildbearbeiter.exception;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: hirsch
 * Date: 04.03.11
 * Time: 21:47
 * To change this template use File | Settings | File Templates.
 */
public class KeineDateienEnthaltenException extends Exception {

    public KeineDateienEnthaltenException(File directory) {
        super();
        System.err.println(directory);
    }

}
