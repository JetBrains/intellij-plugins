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

public class DartServerOverrideMarkerProviderTest extends CodeInsightFixtureTestCase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, myFixture.getTestRootDisposable(), true);
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
  }

  @Override
  protected String getBasePath() {
    return "/analysisServer/overrideMarker";
  }

  private void doTest(final String expectedText, final Icon expectedIcon) {
    final String testName = getTestName(false);
    myFixture.configureByFile(testName + ".dart");

    myFixture.doHighlighting(); // make sure server is warmed up

    checkGutter(myFixture.findGuttersAtCaret(), expectedText, expectedIcon);
  }

  public static void checkGutter(final List<GutterMark> gutters, final String expectedText, final Icon expectedIcon) {
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

  public void testImplementMarker() throws Throwable {
    doTest("Implements method 'm' in 'A'", AllIcons.Gutter.ImplementingMethod);
  }

  public void testOverrideMarker() throws Throwable {
    doTest("Overrides method 'm' in 'A'", AllIcons.Gutter.OverridingMethod);
  }

  public void testOverriddenOperator() throws Throwable {
    doTest("Overrides operator '==' in 'Object'", AllIcons.Gutter.OverridingMethod);
  }
}
