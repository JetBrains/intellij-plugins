package com.jetbrains.lang.dart.refactoring.extract;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.ide.refactoring.extract.DartExtractMethodHandler;
import com.jetbrains.lang.dart.util.DartSdkTestUtil;

/**
 * @author: Fedor.Korotkov
 */
public class DartExtractMethodInHtmlTest extends JavaCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return PathManager.getHomePath() + FileUtil.toSystemDependentName("/web-ide/WebStorm/Dart/testData/refactoring/extractMethod/html/");
  }

  private void doTest() throws Throwable {
    DartSdkTestUtil.configFakeSdk(myFixture);
    myFixture.configureByFile(getTestName(true) + ".html");
    doTestImpl();
  }

  private void doTestImpl() {
    final DartExtractMethodHandler extractMethodHandler = new DartExtractMethodHandler();
    //noinspection NullableProblems
    extractMethodHandler.invoke(myFixture.getProject(), myFixture.getEditor(), myFixture.getFile(), null);
    myFixture.checkResultByFile(getTestName(true) + "_expected.html");
  }

  public void testExtract1() throws Throwable {
    doTest();
  }
}
