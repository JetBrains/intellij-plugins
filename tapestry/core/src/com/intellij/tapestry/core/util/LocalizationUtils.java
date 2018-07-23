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

    final int lastDot = filename.lastIndexOf('.');
    final int extension = lastDot >= 0 ? lastDot : filename.length();
    final int lastUnderscore = filename.lastIndexOf('_', extension);
    if (lastUnderscore < 1) return filename;
    String languageOrCountry = filename.substring(lastUnderscore + 1, extension);

    // Last token is the language ?
    if (ALL_LANGUAGES.contains(languageOrCountry)) return removeSubstring(filename, lastUnderscore, extension);

    // Last token is the country ?
    if (!ALL_COUNTRIES.contains(languageOrCountry)) return filename;

    final int nextUnderscore = filename.lastIndexOf('_', lastUnderscore - 1);
    if (nextUnderscore < 1) return filename;
    String language = filename.substring(nextUnderscore + 1, lastUnderscore);
    return ALL_LANGUAGES.contains(language) ? removeSubstring(filename, nextUnderscore, extension) : filename;
  }

  private static String removeSubstring(String filename, int beginIndex, int endIndex) {
    return filename.substring(0, beginIndex) + filename.substring(endIndex);
  }
}
