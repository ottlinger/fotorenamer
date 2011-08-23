package de.aikiit.bildbearbeiter.util;

import org.apache.log4j.Logger;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Helper to deal with localizations and ease the usage of resource bundles within fotorenamer.
 * @author hirsch
 * @version 23.08.11
 */
public final class LocalizationHelper {

    private static final Logger LOG = Logger.getLogger(LocalizationHelper.class);
    private static final ResourceBundle BUNDLE =  ResourceBundle.getBundle("fotorenamer", Locale.GERMAN);

    private LocalizationHelper() {
        // empty to prevent instantiation
    }

    public static String getBundleString(final String key) {
        LOG.info("retrieving key " + key);
        return BUNDLE.getString(key);
        // l18n basics: http://www.kodejava.org/examples/220.html
        // l18n buttons: http://www.java2s.com/Code/Java/I18N/Createonebuttoninternationalizedly.htm
        // l18n with parameters: http://www.java2s.com/Code/Java/I18N/ResourceBundlewithparameterposition.htm
        // parameters are a but uneasier than with grails -
        // http://download.oracle.com/javase/tutorial/i18n/format/messageFormat.html
    }

}
