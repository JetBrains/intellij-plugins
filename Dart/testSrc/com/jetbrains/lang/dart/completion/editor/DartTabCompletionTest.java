package com.jetbrains.lang.dart.completion.editor;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

/**
 * @author: Fedor.Korotkov
 */
public class DartTabCompletionTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return PathManager.getHomePath() + FileUtil.toSystemDependentName("/plugins/Dart/testData/completion/tab");
  }

  public void doTest() {
    myFixture.configureByFile(getTestName(true) + ".dart");
    myFixture.complete(CompletionType.BASIC);
    myFixture.finishLookup(Lookup.REPLACE_SELECT_CHAR);
    myFixture.checkResultByFile(getTestName(true) + "_expected.dart");
  }

  public void testExpression1() {
    doTest();
  }

  public void testWEB_7191() {
    doTest();
  }
}
