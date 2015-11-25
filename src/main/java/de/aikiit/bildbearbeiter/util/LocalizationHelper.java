/**
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
package de.aikiit.bildbearbeiter.util;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Helper to deal with localizations and ease the usage of
 * resource bundles within fotorenamer.
 *
 * @author hirsch
 * @version 23.08.11
 */
public final class LocalizationHelper {

    /**
     * Logger.
     */
    private static final Logger LOG =
            LogManager.getLogger(LocalizationHelper.class);
    /**
     * ResourceBundle used for fotorenamer.
     * Currently it's static and German only.
     */
    private static final ResourceBundle BUNDLE =
            ResourceBundle.getBundle("fotorenamer", Locale.GERMAN);


    /**
     * Use a German message format as well
     * to process parameters to property messages.
     */
    private static final MessageFormat FORMAT;

    static {
        FORMAT = new MessageFormat("");
        FORMAT.setLocale(Locale.GERMAN);
    }

    /**
     * Empty default constructor to prevent instantiation.
     */
    private LocalizationHelper() {
        // empty to prevent instantiation
    }

    /**
     * Helper function to retrieve a given key
     * from the underlying resource bundle.
     *
     * @param key Key to retrieve from the bundle,
     * e.g. <i>fotorenamer.foo.title</i>
     * @return Returns the value from the bundle.
     */
    public static String getBundleString(final String key) {
        LOG.info("Retrieving key " + key);
        return BUNDLE.getString(key);
        // l18n basics:
        // http://www.kodejava.org/examples/220.html
        // l18n buttons:
        // http://www.java2s.com/Code/Java/I18N/Createonebuttoninternationalizedly.htm
        // l18n with parameters:
        // http://www.java2s.com/Code/Java/I18N/ResourceBundlewithparameterposition.htm
        // parameters are a but uneasier than with grails -
        // http://download.oracle.com/javase/tutorial/i18n/format/messageFormat.html
        // encoding issues / eclipse plugin:
        // http://stackoverflow.com/questions/863838/problem-with-java-properties-utf8-encoding-in-eclipse
    }

    /**
     * Helper function to retrieve a given key
     * from the underlying resource bundle while
     * applying localization parameters.
     *
     * @see
     * <a href="http://download.oracle.com/javase/tutorial/i18n/format/messageFormat.html">
     *     I18N-tutorial</a>
     *
     * @param key Key to retrieve from the bundle,
     * e.g. <i>fotorenamer.foo.parameteredtitle</i>.
     * @param parameters Object array with all parameters.
     * @return Returns the value from the bundle
     * with the given parameters applied.
     */
    public static String getParameterizedBundleString(final String key, final Object[] parameters) {
        LOG.info("Applying " + ((parameters == null) ? null : parameters
                .length) + " parameters to " + key);
        FORMAT.applyPattern(getBundleString(key));
        return FORMAT.format(parameters);
    }

}
