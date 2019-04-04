package com.google.jstestdriver.idea.rt.util;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URI;

/**
 * @author Sergey Simonchik
 */
public class PathConverter {

  private static final char UNIX_SEPARATOR = '/';
  private static final String UNIX_SEPARATOR_STR = String.valueOf(UNIX_SEPARATOR);

  private PathConverter() {}

  public static String getNormalizedPath(@NotNull File file) {
    URI uri = file.toURI();
    URI normalizedUri = uri.normalize();
    String normalizedPath = normalizedUri.getPath();
    if (normalizedPath.startsWith(UNIX_SEPARATOR_STR) && File.separatorChar != UNIX_SEPARATOR) {
      normalizedPath = normalizedPath.substring(1);
    }
    return normalizedPath;
  }

}
