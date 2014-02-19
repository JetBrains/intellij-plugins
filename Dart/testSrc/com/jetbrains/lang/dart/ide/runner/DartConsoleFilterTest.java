package com.jetbrains.lang.dart.ide.runner;

import com.intellij.openapi.util.SystemInfo;
import junit.framework.TestCase;

import static com.jetbrains.lang.dart.ide.runner.DartPositionInfo.Type;

public class DartConsoleFilterTest extends TestCase {

  private static void doNegativeTest(final String text) {
    assertNull(DartPositionInfo.parsePositionInfo(text));
  }

  private static void doPositiveTest(final String text,
                                     final Type type,
                                     final String pathOnUnix,
                                     final int highlightingStartIndex,
                                     final int highlightingEndIndex,
                                     final int line,
                                     final int column) {

    final DartPositionInfo info = DartPositionInfo.parsePositionInfo(text);
    assertNotNull(info);
    assertEquals(type, info.type);
    final boolean trimSlash = type == Type.FILE && SystemInfo.isWindows && pathOnUnix.startsWith("/");
    assertEquals(trimSlash ? pathOnUnix.substring(1) : pathOnUnix, info.path);
    assertEquals(highlightingStartIndex, info.highlightingStartIndex);
    assertEquals(highlightingEndIndex, info.highlightingEndIndex);
    assertEquals(line, info.line);
    assertEquals(column, info.column);
  }

  public void testPositionInfo() {
    doNegativeTest("");
    doNegativeTest(".dart");
    doNegativeTest("(.dart)");
    doNegativeTest("(dart:foo.dart )");
    doNegativeTest("(dart:foo.dart :1:2)");
    doNegativeTest("(darts:foo.dart)");
    doNegativeTest("(darts:foo.dart)");
    doNegativeTest("(dart :foo.dart)");

    doPositiveTest("(dart:.dart)", Type.DART, ".dart", 1, 11, -1, -1);
    doPositiveTest("x.dart (file://///a.dart:)", Type.FILE, "/a.dart", 8, 24, -1, -1);
    doPositiveTest("x (package://///a.dart:xx:10)", Type.PACKAGE, "/////a.dart", 3, 22, -1, -1);
    doPositiveTest("x (file:a.dart:15)", Type.FILE, "a.dart", 3, 14, 14, -1);
    doPositiveTest("x (file:a.dart:15:)", Type.FILE, "a.dart", 3, 14, 14, -1);
    doPositiveTest("x (file:a.dart:15:9y)", Type.FILE, "a.dart", 3, 14, 14, 8);
    doPositiveTest("x (file:a.dart:15x:yy)", Type.FILE, "a.dart", 3, 14, 14, -1);
    doPositiveTest("x (dart://a.dart:15:20)", Type.DART, "//a.dart", 3, 16, 14, 19);
    doPositiveTest("x (package://a.dart:15:9999999999)", Type.PACKAGE, "//a.dart", 3, 19, -1, -1);
    doPositiveTest("x (package://a.dart:9999999999:5)", Type.PACKAGE, "//a.dart", 3, 19, -1, -1);
  }
}
