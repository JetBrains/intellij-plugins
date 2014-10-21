package com.jetbrains.lang.dart.ide.runner;

import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class DartPositionInfo {

  public enum Type {
    FILE, DART, PACKAGE;

    @Nullable
    public static Type getType(final String type) {
      if ("file".equals(type)) return FILE;
      if ("dart".equals(type)) return DART;
      if ("package".equals(type)) return PACKAGE;
      return null;
    }
  }

  public final @NotNull Type type;
  public final @NotNull String path;
  public final int highlightingStartIndex;
  public final int highlightingEndIndex;
  public final int line;
  public final int column;

  public DartPositionInfo(final @NotNull Type type,
                          final @NotNull String path,
                          final int highlightingStartIndex,
                          final int highlightingEndIndex,
                          final int line,
                          final int column) {
    this.type = type;
    this.path = path;
    this.highlightingStartIndex = highlightingStartIndex;
    this.highlightingEndIndex = highlightingEndIndex;
    this.line = line;
    this.column = column;
  }

  /*
  #0      min (dart:math:70)
  #0      Object.noSuchMethod (dart:core-patch/object_patch.dart:42)
  #1      SplayTreeMap.addAll (dart:collection/splay_tree.dart:373)
  #1      Sphere.copyFrom (package:vector_math/src/vector_math/sphere.dart:46:23)
  #1      StringBuffer.writeAll (dart:core/string_buffer.dart:41)
  #2      main (file:///C:/dart/DartSample2/web/Bar.dart:4:28)
  #3      _startIsolate.isolateStartHandler (dart:isolate-patch/isolate_patch.dart:190)
  #4      _RawReceivePortImpl._handleMessage (dart:isolate-patch/isolate_patch.dart:93)
  'package:DartSample2/mylib.dart': error: line 7 pos 1: 'myLibPart' is already defined
  'file:///C:/dart/DartSample2/bin/file2.dart': error: line 3 pos 1: library handler failed
   WHATEVER_NOT_ENDING_WITH_PATH_SYMBOL package:DartSample2/mylib.dart WHATEVER_ELSE_NOT_STARTING_FROM_PATH_SYMBOL
   WHATEVER_NOT_ENDING_WITH_PATH_SYMBOL dart:core/string_buffer.dart WHATEVER_ELSE_NOT_STARTING_FROM_PATH_SYMBOL
   inside WHATEVER_ELSE_STARTING_NOT_FROM_PATH_SYMBOL look for ':4:28' at the beginning or for 'line 3 pos 1' at any place
  */
  @Nullable
  public static DartPositionInfo parsePositionInfo(final @NotNull String text) {
    Couple<Integer> pathStartAndEnd = parseUrlStartAndEnd(text, "package:");
    if (pathStartAndEnd == null) pathStartAndEnd = parseUrlStartAndEnd(text, "dart:");
    if (pathStartAndEnd == null) pathStartAndEnd = parseUrlStartAndEnd(text, "file:");
    if (pathStartAndEnd == null) pathStartAndEnd = parseDartLibUrlStartAndEnd(text);
    if (pathStartAndEnd == null) return null;

    final Integer urlStartIndex = pathStartAndEnd.first;
    final Integer urlEndIndex = pathStartAndEnd.second;

    final String url = text.substring(urlStartIndex, urlEndIndex);
    final String tail = text.length() == urlEndIndex ? "" : text.substring(urlEndIndex).trim();

    Couple<Integer> lineAndColumn = parseLineAndColumnInColonFormat(tail);
    if (lineAndColumn == null) lineAndColumn = parseLineAndColumnInTextFormat(tail);

    final int colonIndexInUrl = url.indexOf(':');
    assert colonIndexInUrl > 0 : text;

    final Type type = Type.getType(url.substring(0, colonIndexInUrl));
    assert type != null : text;

    final int pathStartIndexInUrl = type == Type.FILE
                                    ? colonIndexInUrl + 1 + getPathStartIndex(url.substring(colonIndexInUrl + 1))
                                    : colonIndexInUrl + 1;
    final String path = url.substring(pathStartIndexInUrl, url.length());

    final int line = lineAndColumn == null ? -1 : lineAndColumn.first >= 0 ? lineAndColumn.first - 1 : lineAndColumn.first;
    final int column = lineAndColumn == null ? -1 : lineAndColumn.second >= 0 ? lineAndColumn.second - 1 : lineAndColumn.second;
    return new DartPositionInfo(type, path, urlStartIndex, urlEndIndex, line, column);
  }

  // WHATEVER_NOT_ENDING_WITH_PATH_SYMBOL PREFIX PATH_ENDING_WITH_DOT_DART WHATEVER_ELSE_NOT_STARTING_FROM_PATH_SYMBOL
  // Example:   'package:DartSample2/mylib.dart': error: line 7 pos 1: 'myLibPart' is already defined
  @Nullable
  private static Couple<Integer> parseUrlStartAndEnd(final String text, final String prefix) {
    final int pathStartIndex = text.indexOf(prefix);
    if (pathStartIndex < 0 ||
        pathStartIndex > 0 && !isCharAllowedBeforePath(text.charAt(pathStartIndex - 1))) {
      return null;
    }

    final String lowercased = text.toLowerCase(Locale.US);
    int dotDartIndex = pathStartIndex;
    int pathEndIndex;

    do {
      dotDartIndex = lowercased.indexOf(".dart", dotDartIndex + 1);
      pathEndIndex = dotDartIndex + ".dart".length();
    }
    while (dotDartIndex > 0 && text.length() > pathEndIndex && !isCharAllowedAfterPath(text.charAt(pathEndIndex)));

    if (dotDartIndex <= pathStartIndex ||
        text.length() > pathEndIndex && !isCharAllowedAfterPath(text.charAt(pathEndIndex))) {
      return null;
    }

    return Couple.of(pathStartIndex, pathEndIndex);
  }

  //   #0      min (dart:math:70)
  @Nullable
  private static Couple<Integer> parseDartLibUrlStartAndEnd(final String text) {
    final int pathStartIndex = text.indexOf("dart:");
    if (pathStartIndex < 0 ||
        pathStartIndex > 0 && !isCharAllowedBeforePath(text.charAt(pathStartIndex - 1))) {
      return null;
    }

    final int libNameStartIndex = pathStartIndex + "dart:".length();

    int index = libNameStartIndex;
    while (text.length() > index && Character.isLetter(text.charAt(index))) index++;

    if (index == libNameStartIndex) return null;

    return Couple.of(pathStartIndex, index);
  }

  private static boolean isCharAllowedBeforePath(final char ch) {
    return !Character.isLetterOrDigit(ch); // allow spaces, punctuation, parens, brackets, braces, e.g. almost everything
  }

  private static boolean isCharAllowedAfterPath(final char ch) {
    // heuristics for paths like foo/angular.dart/path or foo/angular.dart.examples/path
    return !Character.isLetterOrDigit(ch) && ch != '/' && ch != '.' && ch != '_';
  }

  @Nullable
  public static Couple<Integer> parseLineAndColumnInColonFormat(final @NotNull String text) {
    // "12 whatever, ":12 whatever", "12:34 whatever" or ":12:34 whatever"
    final Pair<Integer, String> lineAndRemainingText = parseNextIntSkippingColon(text);
    if (lineAndRemainingText == null) return null;

    final Pair<Integer, String> colonAndRemainingText = parseNextIntSkippingColon(lineAndRemainingText.second.trim());
    if (colonAndRemainingText == null) {
      return Couple.of(lineAndRemainingText.first, -1);
    }
    else {
      return Couple.of(lineAndRemainingText.first, colonAndRemainingText.first);
    }
  }

  @Nullable
  private static Couple<Integer> parseLineAndColumnInTextFormat(final @NotNull String text) {
    // "whatever line 12 pos 34 whatever"
    int index = text.indexOf("line ");
    if (index == -1) return null;

    index += "line ".length();
    final Pair<Integer, String> lineAndRemainingText = parseNextIntSkippingColon(text.substring(index));
    if (lineAndRemainingText == null) return null;

    Pair<Integer, String> colonAndRemainingText = null;

    final String trimmedTail = lineAndRemainingText.second.trim();
    if (trimmedTail.startsWith("pos ")) {
      colonAndRemainingText = parseNextIntSkippingColon(trimmedTail.substring("pos ".length()));
    }

    if (colonAndRemainingText == null) {
      return Couple.of(lineAndRemainingText.first, -1);
    }
    else {
      return Couple.of(lineAndRemainingText.first, colonAndRemainingText.first);
    }
  }

  @Nullable
  private static Pair<Integer, String> parseNextIntSkippingColon(final @NotNull String text) {
    // "12 whatever or ": 12 whatever"
    int index = 0;

    // skip leading colon
    if (text.length() > index && text.charAt(index) == ':') index++;

    // skip whitespaces
    while (text.length() > index && Character.isWhitespace(text.charAt(index))) index++;

    final int numberStartIndex = index;
    while (text.length() > index && text.charAt(index) >= '0' && text.charAt(index) <= '9') index++;

    final int numberEndIndex = index;
    if (numberStartIndex == numberEndIndex) return null;

    try {
      final int line = Integer.parseInt(text.substring(numberStartIndex, numberEndIndex));
      final String remainingText = text.substring(numberEndIndex);
      return Pair.create(line, remainingText);
    }
    catch (NumberFormatException e) {
      return null;
    }
  }

  // trim all leading slashes on windows or all except one on Mac/Linux
  private static int getPathStartIndex(final @NotNull String text) {
    if (text.isEmpty() || text.charAt(0) != '/') return 0;

    int index = 0;
    while (index < text.length() && text.charAt(index) == '/') index++;

    return SystemInfo.isWindows ? index : index - 1;
  }
}
