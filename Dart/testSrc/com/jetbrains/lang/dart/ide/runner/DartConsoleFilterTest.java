package com.jetbrains.lang.dart.ide.runner;

import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.Trinity;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.lang.dart.ide.runner.DartPositionInfo.Type;

public class DartConsoleFilterTest extends TestCase {

  private static void doNegativeTest(final String text) {
    assertNull(DartPositionInfo.parsePositionInfo(text));
  }

  private static void doPositiveTest(final String text,
                                     final Type type,
                                     final String highlightedText,
                                     final String pathOnUnix,
                                     final int line,
                                     final int column) {

    final DartPositionInfo info = DartPositionInfo.parsePositionInfo(text);
    assertNotNull(info);
    assertEquals(type, info.type);
    final boolean trimSlash = type == Type.FILE && SystemInfo.isWindows && pathOnUnix.startsWith("/");
    assertEquals(trimSlash ? pathOnUnix.substring(1) : pathOnUnix, info.path);
    assertEquals(highlightedText, text.substring(info.highlightingStartIndex, info.highlightingEndIndex));
    assertEquals(line, info.line);
    assertEquals(column, info.column);
  }

  private static void doNegativeRelativePathsFilterTest(@NotNull final String text) {
    assertNull(DartRelativePathsConsoleFilter.getFileRelPathLineAndColumn(text));
  }

  private static void doPositiveRelativePathsFilterTest(final String text, final String relPath, final int line, final int column) {
    final Trinity<String, Integer, Integer> relPathLineAndColumn = DartRelativePathsConsoleFilter.getFileRelPathLineAndColumn(text);
    assertNotNull(relPathLineAndColumn);
    assertEquals(relPath, relPathLineAndColumn.first);
    assertEquals(line, relPathLineAndColumn.second.intValue());
    assertEquals(column, relPathLineAndColumn.third.intValue());
  }

  public void testPositionInfo() {
    doNegativeTest("");
    doNegativeTest(".dart");
    doNegativeTest("(.dart)");
    doNegativeTest("adart:foo.dart )");
    doNegativeTest("adart:foo:5");
    doNegativeTest("dart:5");
    doNegativeTest("package:foo/bar.darts");
    doNegativeTest("packages:foo/bar.dart");
    doNegativeTest("darts:foo.dart");
    doNegativeTest("'darts:foo.dart'");
    doNegativeTest("dart :foo.dart");
    doNegativeTest("abc.dart ef.dart file:foo.dart/bar.dart_baz.dart.more");

    doPositiveTest("dart:libName", Type.DART, "dart:libName", "libName", -1, -1);
    doPositiveTest("library 'dart:html_common' is", Type.DART, "dart:html_common", "html_common", -1, -1);
    doPositiveTest("packages:file:dart:libName:70: line 5 pos 9", Type.DART, "dart:libName", "libName", 69, -1);
    doPositiveTest("package:foo/bar.dart", Type.PACKAGE, "package:foo/bar.dart", "foo/bar.dart", -1, -1);
    doPositiveTest("(dart:.dart)", Type.DART, "dart:.dart", ".dart", -1, -1);
    doPositiveTest("'dart:a/b/c' pos 6 line 4", Type.DART, "dart:a", "a", 3, -1);
    doPositiveTest("x.dart (file://///a.dart:) line  05", Type.FILE, "file://///a.dart", "/a.dart", 4, -1);
    doPositiveTest("x 'package://///a.dart:xx:10'", Type.PACKAGE, "package://///a.dart", "/////a.dart", -1, -1);
    doPositiveTest("x (file:a.dart:15)", Type.FILE, "file:a.dart", "a.dart", 14, -1);
    doPositiveTest("x (file:a.dart:15:)", Type.FILE, "file:a.dart", "a.dart", 14, -1);
    doPositiveTest("xxx:file:a.dart  :  15  :  90yyy  )", Type.FILE, "file:a.dart", "a.dart", 14, 89);
    doPositiveTest("x (file:a.dart:15x:yy)", Type.FILE, "file:a.dart", "a.dart", 14, -1);
    doPositiveTest("file:a.dart:15", Type.FILE, "file:a.dart", "a.dart", 14, -1);
    doPositiveTest("file:a.dart:15z:", Type.FILE, "file:a.dart", "a.dart", 14, -1);
    doPositiveTest("file:a.dart:15:", Type.FILE, "file:a.dart", "a.dart", 14, -1);
    doPositiveTest("file:a.dart:15:z", Type.FILE, "file:a.dart", "a.dart", 14, -1);
    doPositiveTest("file:a.dart:15:5", Type.FILE, "file:a.dart", "a.dart", 14, 4);
    doPositiveTest("file:a.dart:15:5x", Type.FILE, "file:a.dart", "a.dart", 14, 4);
    doPositiveTest("file:a.dart:15:5:", Type.FILE, "file:a.dart", "a.dart", 14, 4);
    doPositiveTest("x (dart://a.dart:15:20)", Type.DART, "dart://a.dart", "//a.dart", 14, 19);
    doPositiveTest("x (package://a.dart:15:9999999999)", Type.PACKAGE, "package://a.dart", "//a.dart", 14, -1);
    doPositiveTest("x (package://a.dart:9999999999:5)", Type.PACKAGE, "package://a.dart", "//a.dart", -1, -1);
    doPositiveTest("'package:foo//bar.dart': error: line   11   pos   1: x", Type.PACKAGE, "package:foo//bar.dart", "foo//bar.dart", 10, 0);
    doPositiveTest("'package:foo//bar.dart': error: line   11   pos   : x", Type.PACKAGE, "package:foo//bar.dart", "foo//bar.dart", 10, -1);
    doPositiveTest("abc.dart ef.dart file:foo.dart/bar.dart_baz.dart.more.dart", Type.FILE, "file:foo.dart/bar.dart_baz.dart.more.dart",
                   "foo.dart/bar.dart_baz.dart.more.dart", -1, -1);
    doPositiveTest("file:foo.dart/bar.dart_baz.dart.more.dart,evenmore.dart", Type.FILE, "file:foo.dart/bar.dart_baz.dart.more.dart",
                   "foo.dart/bar.dart_baz.dart.more.dart", -1, -1);
  }

  public void testRelativePathsConsoleFilter() throws Exception {
    doNegativeRelativePathsFilterTest("");
    doNegativeRelativePathsFilterTest("foo.dart");
    doNegativeRelativePathsFilterTest("foo.dart ");
    doNegativeRelativePathsFilterTest("foo.dart 4   ");
    doNegativeRelativePathsFilterTest("foo.dart 4:   ");
    doNegativeRelativePathsFilterTest("foo.dart 4:x");
    doNegativeRelativePathsFilterTest(" foo.dart 4:15");
    doNegativeRelativePathsFilterTest("foo.txt 4:15");
    doNegativeRelativePathsFilterTest("foo.dart 4:x");

    doPositiveRelativePathsFilterTest("foo.dart 1:1", "foo.dart", 0, 0);
    doPositiveRelativePathsFilterTest("foo.dart line 5 pos 9:z", "foo.dart", 4, 8);
    doPositiveRelativePathsFilterTest("../foo\\bar.dart 4:15 x", "../foo\\bar.dart", 3, 14);
    doPositiveRelativePathsFilterTest("web\\foo.dart:566:1:", "web\\foo.dart", 565, 0);
  }
}
