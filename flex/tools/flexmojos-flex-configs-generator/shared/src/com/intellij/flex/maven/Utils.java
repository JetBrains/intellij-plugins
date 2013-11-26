package com.intellij.flex.maven;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public final class Utils {
  private static final Charset UTF_8 = Charset.forName("UTF-8");
  public static final Map<String, String> CHILD_TAG_NAME_MAP = new HashMap<String, String>(12);

  private static final ThreadLocal<char[]> CHAR_BUFFER = new ThreadLocal<char[]>() {
    protected char[] initialValue() {
      return new char[1024 * 8];
    }
  };

  static {
    CHILD_TAG_NAME_MAP.put("keep-as3-metadata", "name");
    CHILD_TAG_NAME_MAP.put("include-namespaces", "uri");
    CHILD_TAG_NAME_MAP.put("include-classes", "class");
    CHILD_TAG_NAME_MAP.put("include-libraries", "library");
    CHILD_TAG_NAME_MAP.put("locale", "locale-element");
    CHILD_TAG_NAME_MAP.put("managers", "manager-class");
    CHILD_TAG_NAME_MAP.put("externs", "symbol");
    CHILD_TAG_NAME_MAP.put("includes", "symbol");
    CHILD_TAG_NAME_MAP.put("extensions", "extension");
    CHILD_TAG_NAME_MAP.put("include-resource-bundles", "bundle");
    CHILD_TAG_NAME_MAP.put("theme", "filename");
    CHILD_TAG_NAME_MAP.put("defaults-css-files", "filename");
  }

  public static void copyFile(File fromFile, File toFile) throws IOException {
    final FileChannel fromChannel = new FileInputStream(fromFile).getChannel();
    final FileChannel toChannel = new FileOutputStream(toFile).getChannel();
    try {
      fromChannel.transferTo(0, fromFile.length(), toChannel);
      //noinspection ResultOfMethodCallIgnored
      toFile.setLastModified(fromFile.lastModified());
    }
    finally {
      fromChannel.close();
      toChannel.close();
    }
  }

  public static void write(StringBuilder stringBuilder, File out) throws IOException {
    final OutputStreamWriter streamWriter = new OutputStreamWriter(new FileOutputStream(out), UTF_8);
    final char[] chars = CHAR_BUFFER.get();
    final int totalLength = stringBuilder.length();
    int srcBegin = 0;
    int srcEnd = Math.min(chars.length, totalLength);
    try {
      while (true) {
        stringBuilder.getChars(srcBegin, srcEnd, chars, 0);
        streamWriter.write(chars, 0, srcEnd - srcBegin);

        srcBegin += chars.length;
        if (srcBegin >= totalLength) {
          break;
        }

        srcEnd += chars.length;
        if (totalLength < srcEnd) {
          srcEnd = totalLength;
        }
      }
    }
    finally {
      try {
        streamWriter.close();
      }
      catch (IOException ignored) {
      }
    }
  }
}
