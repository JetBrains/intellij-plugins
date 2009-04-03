package com.intellij.tapestry.core.util;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Utility methods related to resource localization.
 */
public final class LocalizationUtils {

    private static final List<String> ALL_COUNTRIES = Arrays.asList(Locale.getISOCountries());
    private static final List<String> ALL_LANGUAGES = Arrays.asList(Locale.getISOLanguages());

    /**
     * Finds the not localized name of a file.
     *
     * @param filename the filename to unlocalize.
     * @return the given filename unlocalized.<br/><br/>
     *         For example:<br/>
     *         "Somefile_en.properties" -> "Somefile.properties"<br/>
     *         "Somefile_en_GB.properties" -> "Somefile.properties"<br/>
     *         "Somefile.properties" -> "Somefile.properties"<br/>
     */
    public static String unlocalizeFileName(String filename) {
        if (filename.indexOf('_') == -1) {
            return filename;
        }

        String lastToken = filename.substring(filename.lastIndexOf('_') + 1, filename.lastIndexOf('.'));

        // Last token is the language ?
        if (ALL_LANGUAGES.contains(lastToken)) {
            return filename.substring(0, filename.lastIndexOf('_')) + filename.substring(filename.lastIndexOf('.'));
        }

        // Last token is the country ?
        if (ALL_COUNTRIES.contains(lastToken)) {
            String filenameWithoutLastToken = filename.replace("_" + lastToken, "");
            String firstToken = filenameWithoutLastToken.substring(filenameWithoutLastToken.lastIndexOf('_') + 1, filenameWithoutLastToken.lastIndexOf('.'));

            if (ALL_LANGUAGES.contains(firstToken))
                return filenameWithoutLastToken.substring(0, filenameWithoutLastToken.lastIndexOf('_')) + filenameWithoutLastToken.substring(filenameWithoutLastToken.lastIndexOf('.'));
        }

        return filename;
    }
}
