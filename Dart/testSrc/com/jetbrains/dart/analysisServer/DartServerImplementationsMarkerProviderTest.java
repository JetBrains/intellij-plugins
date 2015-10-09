/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package com.jetbrains.dart.analysisServer;

import com.google.common.collect.Lists;
import com.intellij.codeInsight.daemon.GutterMark;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.jetbrains.lang.dart.util.DartTestUtils;

import javax.swing.*;
import java.util.List;

public class DartServerImplementationsMarkerProviderTest extends CodeInsightFixtureTestCase {

  protected String getBasePath() {
    return "/analysisServer/implementationsMarker";
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, getTestRootDisposable(), true);
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
  }

  public void testClassExtended() throws Throwable {
    final String testName = getTestName(false);
    myFixture.configureByFile(testName + ".dart");
    ensureImplementsMarkers();
    checkHasGutterAtCaret("Has implementations", AllIcons.Gutter.ImplementedMethod);
  }

  public void testClassImplemented() throws Throwable {
    final String testName = getTestName(false);
    myFixture.configureByFile(testName + ".dart");
    ensureImplementsMarkers();
    checkHasGutterAtCaret("Has implementations", AllIcons.Gutter.ImplementedMethod);
  }

  public void testMethodExtended() throws Throwable {
    final String testName = getTestName(false);
    myFixture.configureByFile(testName + ".dart");
    ensureImplementsMarkers();
    checkHasGutterAtCaret("Has implementations", AllIcons.Gutter.ImplementedMethod);
  }

  public void testMethodImplemented() throws Throwable {
    final String testName = getTestName(false);
    myFixture.configureByFile(testName + ".dart");
    ensureImplementsMarkers();
    checkHasGutterAtCaret("Has implementations", AllIcons.Gutter.ImplementedMethod);
  }

  private void checkHasGutterAtCaret(final String expectedText, final Icon expectedIcon) {
    final List<GutterMark> gutters = myFixture.findGuttersAtCaret();
    final List<String> textList = Lists.newArrayList();
    for (GutterMark gutter : gutters) {
      final String text = gutter.getTooltipText();
      textList.add(text);
      if (expectedText.equals(text) && expectedIcon.equals(gutter.getIcon())) {
        return;
      }
    }
    fail("Not found gutter mark: " + expectedText + "  " + expectedIcon + "\nin\n" + StringUtil.join(textList, "\n"));
  }

  private void ensureImplementsMarkers() {
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
    myFixture.doHighlighting(); // make sure server is warmed up
  }
}
