package com.jetbrains.lang.dart.resolve;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.lang.CodeInsightActions;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.DartLanguage;

/**
 * @author: Fedor.Korotkov
 */
public class DartGotoSuperTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return PathManager.getHomePath() + FileUtil.toSystemDependentName("/plugins/Dart/testData/gotoSuper/");
  }

  private void doTest() throws Throwable {
    myFixture.configureByFile(getTestName(false) + ".dart");
    final CodeInsightActionHandler handler = CodeInsightActions.GOTO_SUPER.forLanguage(DartLanguage.INSTANCE);
    handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile());
    myFixture.checkResultByFile(getTestName(false) + ".txt");
  }

  public void testGts1() throws Throwable {
    doTest();
  }

  public void testGts2() throws Throwable {
    doTest();
  }
}
