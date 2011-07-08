package com.intellij.flex.compiler;

import flex2.compiler.config.ConfigurationException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FlexCompilerUtil {

  private static final String LOCALE = "{locale}";

  public static void ensureFileCanBeCreated(final File file) throws ConfigurationException {
    if (file.isDirectory()) {
      throw new ConfigurationException(file + " is a directory");
    }
    if (!file.exists()) {
      final File parent = file.getParentFile();
      if (parent != null && !parent.exists()) {
        final boolean ok = parent.mkdirs();
        if (!ok && !parent.exists()) { // check exists() once more because it could be created in another thread
          throw new ConfigurationException("Can't create directory '" + parent + "'");
        }
      }
      try {
        new FileOutputStream(file).close();
        file.delete();
      }
      catch (IOException e) {
        throw new ConfigurationException("Can't create file '" + file.getPath() + "': " + e.getMessage());
      }
    }
  }

  /*
 We can't pass unexpanded paths to oemConfig.setSourcePath() because paths relative to config file won't be resolved (IDEA-61189)
 Also we can't pass expanded paths because we'll loose i18n - source path chain will be the same for all locales (IDEA-71381)
 So we must prepare absolute (i.e. expanded) paths with locale specific part substituted back to {locale} token.
  */
  public static File[] getPathsWithLocaleToken(final File[] unexpandedPaths, final File[] expandedPaths, final String[] locales) {
    final File[] result = new File[unexpandedPaths.length];

    for (int i = 0, j = 0; i < unexpandedPaths.length; i++) {
      final String unexpandedPath = unexpandedPaths[i].getPath();

      if (unexpandedPath.contains(LOCALE)) {
        final String resultPath = getExpandedPathWithLocaleToken(unexpandedPath, expandedPaths, j, locales);
        if (resultPath == null) {
          error(unexpandedPaths, expandedPaths, locales);
        }

        result[i] = new File(resultPath);

        // just some extra check that we do everything correctly
        final int numberOfMatches = getNumberOfMatches(unexpandedPath, LOCALE);
        for (final String locale : locales) {
          final String expandedPath = expandedPaths[j].getPath();
          if (numberOfMatches == getNumberOfMatches(expandedPath, locale) && !resultPath.equals(expandedPath.replace(locale, LOCALE))) {
            return error(unexpandedPaths, expandedPaths, locales);
          }
          j++;
        }
      }
      else {
        result[i] = expandedPaths[j];
        j++;
      }
    }
    return result;
  }

  private static String getExpandedPathWithLocaleToken(final String unexpandedPath,
                                                       final File[] expandedPaths,
                                                       final int index,
                                                       final String[] locales) {
    final int numberOfMatches = getNumberOfMatches(unexpandedPath, LOCALE);

    for (int i = 0; i < locales.length; i++) {
      final String expandedPath = expandedPaths[index + i].getPath();
      if (numberOfMatches == getNumberOfMatches(expandedPath, locales[i])) {
        return expandedPath.replace(locales[i], LOCALE);
      }
    }

    if (locales.length == 1) {
      return expandedPaths[index].getPath().replace(locales[0], LOCALE);
    }

    return null; // no idea which parts of absolute must be substituted by {locale} token
  }

  private static int getNumberOfMatches(final String s, final String substring) {
    int num = 0;
    int lastIndex = 0;
    while ((lastIndex = s.indexOf(substring, lastIndex == 0 ? 0 : lastIndex + 1)) > 0) {
      num++;
    }
    return num;
  }

  private static File[] error(final File[] unexpandedPaths, final File[] expandedPaths, final String[] locales) {
    final StringBuilder message = new StringBuilder();
    message.append("Unexpected paths.\nUnexpanded=[");
    for (final File file : unexpandedPaths) {
      message.append(file.getPath()).append(",");
    }
    message.append("]\nExpanded=[");
    for (final File file : expandedPaths) {
      message.append(file.getPath()).append(",");
    }
    message.append("]\nLocales=[");
    for (final String loc : locales) {
      message.append(loc).append(",");
    }
    message.append("]");
    throw new RuntimeException(message.toString());
  }
}
