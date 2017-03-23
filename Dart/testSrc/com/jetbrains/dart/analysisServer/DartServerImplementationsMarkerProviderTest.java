package com.jetbrains.dart.analysisServer;

import com.intellij.codeInsight.daemon.GutterMark;
import com.intellij.icons.AllIcons;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.intellij.util.TimeoutUtil;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public class DartServerImplementationsMarkerProviderTest extends CodeInsightFixtureTestCase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, myFixture.getTestRootDisposable(), true);
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
  }

  @Override
  protected String getBasePath() {
    return "/analysisServer/implementationsMarker";
  }

  private void checkHasGutterAtCaret(final String expectedText, final Icon expectedIcon) {
    final String testName = getTestName(false);
    myFixture.configureByFile(testName + ".dart");

    myFixture.doHighlighting(); // make sure server is warmed up

    if (!someGutterHasIcon(myFixture.findGuttersAtCaret(), expectedIcon)) {
      TimeoutUtil.sleep(200); // wait a bit for info about line markers 'up' to arrive
    }

    DartServerOverrideMarkerProviderTest.checkGutter(myFixture.findGuttersAtCaret(), expectedText, expectedIcon);
  }

  private static boolean someGutterHasIcon(@NotNull final List<GutterMark> gutters, @NotNull final Icon icon) {
    for (GutterMark gutter : gutters) {
      if (icon.equals(gutter.getIcon())) {
        return true;
      }
    }
    return false;
  }

  public void testClassExtended() throws Throwable {
    checkHasGutterAtCaret("Has subclasses", AllIcons.Gutter.OverridenMethod);
  }

  public void testClassImplemented() throws Throwable {
    checkHasGutterAtCaret("Has subclasses", AllIcons.Gutter.OverridenMethod);
  }

  public void testMethodExtended() throws Throwable {
    checkHasGutterAtCaret("Is overridden in subclasses", AllIcons.Gutter.OverridenMethod);
  }

  public void testMethodImplemented() throws Throwable {
    checkHasGutterAtCaret("Is overridden in subclasses", AllIcons.Gutter.OverridenMethod);
  }

  public void testOperatorOverridden() throws Throwable {
    checkHasGutterAtCaret("Is overridden in subclasses", AllIcons.Gutter.OverridenMethod);
  }
}
