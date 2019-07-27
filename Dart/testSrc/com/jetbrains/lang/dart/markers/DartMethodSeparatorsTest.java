// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.markers;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzerSettings;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class DartMethodSeparatorsTest extends BasePlatformTestCase {

  protected void doTest(String fileText) {
    DaemonCodeAnalyzerSettings.getInstance().SHOW_METHOD_SEPARATORS = true;
    try {
      myFixture.configureByText("foo.dart", fileText);
      myFixture.checkHighlighting();
    }
    finally {
      DaemonCodeAnalyzerSettings.getInstance().SHOW_METHOD_SEPARATORS = false;
    }
  }

  public void testMethodSeparators() {
    doTest("firstFunction(){}\n" + // no line marker
           "<lineMarker>func</lineMarker>(){}\n" +
           "<lineMarker>/// doc</lineMarker>\n" +
           "get v => 1;\n" +
           "<lineMarker>set</lineMarker> v(i) {}\n" +
           "<lineMarker>int</lineMarker> get i => 1;\n" +
           "abstract class A {\n" +
           "  firstMethod(){}\n" + // no line marker
           "  <lineMarker>bar</lineMarker>(){}\n" +
           "   \n" +
           "  <lineMarker>//comment</lineMarker>\n" +
           "   \n" +
           "  /// doc comment\n" +
           "   \n" +
           "  baz(){}\n" +
           "\n" +
           "  <lineMarker>abstract</lineMarker> abs();\n" +
           "}\n" +
           "class A {\n" +
           "  firstMethod(){}\n" + // no line marker
           "  <lineMarker>@</lineMarker>foo\n" +
           "  get v => 1;\n" +
           "  <lineMarker>set</lineMarker> v(i) {}\n" +
           "  <lineMarker>int</lineMarker> get i => 1;\n" +
           "}\n" +
           "class A {\n" +
           "  firstMethod(){}\n" + // no line marker
           "  <lineMarker>A</lineMarker>() {}\n" +
           "  <lineMarker>A</lineMarker>.b() {}\n" +
           "}\n" +
           "class A {\n" +
           "  var b;\n" + // no line marker
           "  <lineMarker>bar</lineMarker>(){}\n" +
           "}");
  }
}
