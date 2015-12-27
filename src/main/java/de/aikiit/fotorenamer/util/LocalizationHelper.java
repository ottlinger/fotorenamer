/**
 * Copyright 2011, Aiki IT, FotoRenamer
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.aikiit.fotorenamer.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LocalizationHelper {

    /**
     * Logger.
     */
    private static final Logger LOG =
            LogManager.getLogger(LocalizationHelper.class);

    /**
     * ResourceBundle used for this application.
     */
    private static final String BASE_NAME = "fotorenamer";
    private static final Locale FALLBACK_LOCALE = Locale.GERMANY;

    private static ResourceBundle BUNDLE;
    private static Locale LOCALE;
    private static MessageFormat FORMAT;

    static {
        setLocale();
    }

    /**
     * Set locale depending on system properties, in case of errors fallback is Locale.GERMANY.
     */
    @VisibleForTesting
    static void setLocale() {
        String userLanguage = System.getProperty("user.language");
        String userCountry = System.getProperty("user.country");

        LOG.info(String.format("Your system emits the following l10n-properties: language=%s, country=%s", userLanguage, userCountry));

        if (Strings.isNullOrEmpty(userLanguage) || Strings.isNullOrEmpty(userCountry)) {
            LOCALE = FALLBACK_LOCALE;
            LOG.info("Falling back to locale " + LOCALE);
        } else {
            LOCALE = new Locale(userLanguage, userCountry);
            LOG.info("Setting locale to " + LOCALE);
        }

        BUNDLE = ResourceBundle.getBundle(BASE_NAME, LOCALE);
        FORMAT = new MessageFormat("");
        FORMAT.setLocale(LOCALE);
    }

    /**
     * @return the currently set Locale of this application. Fallback is Locale.GERMANY.
     */
    public static Locale getLocale() {
        if (LOCALE == null) {
            LOG.warn("Returning fallback Locale for Germany - please make sure you've configured your environment correctly via system properties 'user.country'/'user.language'.");
            return FALLBACK_LOCALE;
        }
        return LOCALE;
    }

    /**
     * @return the currently set language
     */
    public static String getLanguage() {
        return getLocale().getLanguage();
    }

    /**
     * Helper function to retrieve a given key
     * from the underlying resource bundle.
     *
     * @param key Key to retrieve from the bundle,
     *            e.g. <i>fotorenamer.foo.title</i>
     * @return Returns the value from the bundle.
     */
    public static String getBundleString(final String key) {
        LOG.debug("Retrieving key " + key);
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
     * @param key        Key to retrieve from the bundle,
     *                   e.g. <i>fotorenamer.foo.parameteredtitle</i>.
     * @param parameters Object array with all parameters.
     * @return Returns the value from the bundle
     * with the given parameters applied.
     * @see <a href="http://download.oracle.com/javase/tutorial/i18n/format/messageFormat.html">
     * I18N-tutorial</a>
     */
    public static String getParameterizedBundleString(final String key, final Object... parameters) {
        LOG.debug("Applying " + ((parameters == null) ? null : parameters
                .length) + " parameters to " + key);
        FORMAT.applyPattern(getBundleString(key));
        return FORMAT.format(parameters);
    }

}
