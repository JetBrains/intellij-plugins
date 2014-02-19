package com.jetbrains.lang.dart.ide.runner;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
  #0      Object.noSuchMethod (dart:core-patch/object_patch.dart:42)
  #1      SplayTreeMap.addAll (dart:collection/splay_tree.dart:373)
  #1      Sphere.copyFrom (package:vector_math/src/vector_math/sphere.dart:46:23)
  #1      StringBuffer.writeAll (dart:core/string_buffer.dart:41)
  #2      main (file:///C:/dart/DartSample2/web/Bar.dart:4:28)
  #3      _startIsolate.isolateStartHandler (dart:isolate-patch/isolate_patch.dart:190)
  #4      _RawReceivePortImpl._handleMessage (dart:isolate-patch/isolate_patch.dart:93)
  */
  @Nullable
  public static DartPositionInfo parsePositionInfo(final @NotNull String text) {
    final int dotDartIndex = text.toLowerCase().lastIndexOf(".dart");
    final int pathEndIndex = dotDartIndex + ".dart".length();
    if (dotDartIndex <= 0 || text.length() == pathEndIndex) return null;

    final char nextChar = text.charAt(pathEndIndex);
    if (nextChar != ':' && nextChar != ')') return null;

    final int leftParenIndex = text.substring(0, dotDartIndex).lastIndexOf("(");
    final int rightParenIndex = text.indexOf(")", dotDartIndex);
    if (leftParenIndex < 0 || rightParenIndex < 0) return null;

    final int colonIndex = text.indexOf(":", leftParenIndex);
    if (colonIndex < 0) return null;

    final Type type = Type.getType(text.substring(leftParenIndex + 1, colonIndex));
    if (type == null) return null;

    final Pair<Integer, Integer> lineAndColumn = parseLineAndColumn(text.substring(pathEndIndex, rightParenIndex));
    final int pathStartIndex = type == Type.FILE
                               ? colonIndex + 1 + getPathStartIndex(text.substring(colonIndex + 1))
                               : colonIndex + 1;
    final String path = text.substring(pathStartIndex, pathEndIndex);

    return new DartPositionInfo(type,
                                path,
                                leftParenIndex + 1,
                                pathStartIndex + path.length(),
                                lineAndColumn.first >= 0 ? lineAndColumn.first - 1 : lineAndColumn.first,
                                lineAndColumn.second >= 0 ? lineAndColumn.second - 1 : lineAndColumn.second);
  }

  @NotNull
  private static Pair<Integer, Integer> parseLineAndColumn(final @NotNull String text) {
    // "" or ":12" or ":12:34" or ":whatever"
    if (text.isEmpty() || text.charAt(0) != ':') return Pair.create(-1, -1);

    try {
      int index = 1;
      final int lineTextStartIndex = index;
      while (index < text.length() && Character.isDigit(text.charAt(index))) index++;

      if (index == lineTextStartIndex) return Pair.create(-1, -1);
      final int line = Integer.parseInt(text.substring(lineTextStartIndex, index));

      if (index == text.length() || text.charAt(index) != ':') return Pair.create(line, -1);

      index++;
      final int columnTextStartIndex = index;
      while (index < text.length() && Character.isDigit(text.charAt(index))) index++;

      if (index == columnTextStartIndex) return Pair.create(line, -1);
      final int column = Integer.parseInt(text.substring(columnTextStartIndex, index));

      return Pair.create(line, column);
    }
    catch (NumberFormatException e) {
      return Pair.create(-1, -1);
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
