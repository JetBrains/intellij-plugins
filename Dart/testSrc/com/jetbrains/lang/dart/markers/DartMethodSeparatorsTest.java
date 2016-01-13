/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.lang.dart.markers;

import com.intellij.codeInsight.daemon.DaemonAnalyzerTestCase;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzerSettings;
import com.jetbrains.lang.dart.DartFileType;

public class DartMethodSeparatorsTest extends DaemonAnalyzerTestCase {
  @Override
  protected boolean doTestLineMarkers() {
    return true;
  }

  public void testMethodSeparators() throws Exception {
    DaemonCodeAnalyzerSettings.getInstance().SHOW_METHOD_SEPARATORS = true;
    try {
      // methods
      configureByText(DartFileType.INSTANCE, "abstract class A {\n" +
                                             "  firstMethod(){}\n" + // no line marker
                                             "  <lineMarker>bar(){}</lineMarker>\n" +
                                             "   \n" +
                                             "  <lineMarker>//comment</lineMarker>\n" +
                                             "   \n" +
                                             "  /// doc comment\n" +
                                             "   \n" +
                                             "  baz(){}\n" +
                                             "\n" +
                                             "  <lineMarker>abstract abs();</lineMarker>\n" +
                                             "}");
      doDoTest(false, false);

      // getters and setters
      configureByText(DartFileType.INSTANCE, "class A {\n" +
                                             "  firstMethod(){}\n" + // no line marker
                                             "  <lineMarker>get v => 1;</lineMarker>\n" +
                                             "  <lineMarker>set v(i) {}</lineMarker>\n" +
                                             "  <lineMarker>int get i => 1;</lineMarker>\n" +
                                             "}");
      doDoTest(false, false);

      // constructors
      configureByText(DartFileType.INSTANCE, "class A {\n" +
                                             "  firstMethod(){}\n" + // no line marker
                                             "  <lineMarker>A() {}</lineMarker>\n" +
                                             "  <lineMarker>A.b() {}</lineMarker>\n" +
                                             "}");
      doDoTest(false, false);

      // globals
      configureByText(DartFileType.INSTANCE, "  firstFunction(){}\n" + // no line marker
                                             "  <lineMarker>func(){}</lineMarker>\n" +
                                             "  <lineMarker>get v => 1;</lineMarker>\n" +
                                             "  <lineMarker>set v(i) {}</lineMarker>\n" +
                                             "  <lineMarker>int get i => 1;</lineMarker>");
      doDoTest(false, false);

      // any preceding sibling implies that the method will be marked
      configureByText(DartFileType.INSTANCE, "class A {\n" +
                                             "  var b;\n" + // no line marker
                                             "  <lineMarker>bar(){}</lineMarker>\n" +
                                             "}");
      doDoTest(false, false);

      // ignore when enclosed in something that is not a class: method
      configureByText(DartFileType.INSTANCE, "bar() {\n" +
                                             "  foo() {}\n"+
                                             "  foo1() {}\n"+
                                             "}");
      doDoTest(false, false);
    }
    finally {
      DaemonCodeAnalyzerSettings.getInstance().SHOW_METHOD_SEPARATORS = false;
    }
  }
}
