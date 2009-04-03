package com.intellij.tapestry.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * Utility methods for String manipulation.
 */
public final class StringUtils {

    /**
     * Capitalizes a String.
     *
     * @param string the String to capitalize.
     * @return the given String capitalized.
     */
    public static String capitalize(String string) {
        if (string == null || string.length() == 0) {
            return string;
        }

        if (string.length() == 1) {
            return string.toUpperCase(Locale.getDefault());
        }

        // Optimization
        if (Character.isUpperCase(string.charAt(0))) {
            return string;
        }
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }

    /**
     * Unapitalizes a String.
     *
     * @param string the String to uncapitalize.
     * @return the given String uncapitalized.
     */
    public static String uncapitalize(String string) {
        if (string == null || string.length() == 0) {
            return string;
        }

        if (string.length() == 1) {
            return string.toLowerCase(Locale.getDefault());
        }

        // Optimization
        if (Character.isLowerCase(string.charAt(0))) {
            return string;
        }

        return Character.toLowerCase(string.charAt(0)) + string.substring(1);
    }

    /**
     * Checks if a string isn't null or empty
     *
     * @param value string to check
     * @return <code>false</code> if the value is null or an empty string, <code>true</code> otherwise.
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.equals("");
    }

    /**
     * Truncates a string but ensures no word gets cut in half.
     *
     * @param value         the value to truncate.
     * @param maxCharacters the max characters the string should have.
     * @return the given string truncated to a max of maxCharacters length cut off in the last white space.
     */
    public static String truncateWords(String value, int maxCharacters) {
        if (value.length() < maxCharacters)
            return value;


        return value.substring(0, value.substring(0, maxCharacters).lastIndexOf(' '));
    }

    /**
     * Parsers an InputStream to a String.
     *
     * @param stream the stream to parse.
     * @return the given stream contents.
     * @throws IOException if an error occurs parsing the stream.
     */
    public static String toString(InputStream stream) throws IOException {
        StringBuilder out = new StringBuilder();
        byte[] b = new byte[4096];

        for (int n; (n = stream.read(b)) != -1;) {
            out.append(new String(b, 0, n));
        }

        return out.toString();
    }
}
