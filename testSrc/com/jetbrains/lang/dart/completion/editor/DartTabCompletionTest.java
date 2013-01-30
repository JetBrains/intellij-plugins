package com.jetbrains.lang.dart.completion.editor;

import com.intellij.codeInsight.completion.LightFixtureCompletionTestCase;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;

/**
 * @author: Fedor.Korotkov
 */
public class DartTabCompletionTest extends LightFixtureCompletionTestCase {
  @Override
  protected String getTestDataPath() {
    return PathManager.getHomePath() + FileUtil.toSystemDependentName("/plugins/Dart/testData/completion/tab");
  }

  @Override
  protected void complete() {
    super.complete();
    selectItem(myItems[0], '\t');
  }

  public void doTest() {
    configureByFile(getTestName(true) + ".dart");
    checkResultByFile(getTestName(true) + "_expected.dart");
  }

  public void testExpression1() {
    doTest();
  }
}
