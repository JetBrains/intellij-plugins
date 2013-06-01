package com.jetbrains.lang.dart.refactoring.extract;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.ide.refactoring.extract.DartExtractMethodHandler;
import com.jetbrains.lang.dart.util.DartSdkTestUtil;

/**
 * @author: Fedor.Korotkov
 */
public class DartExtractMethodTest extends CodeInsightFixtureTestCase {
  @Override
  protected String getBasePath() {
    return FileUtil.toSystemDependentName("/plugins/Dart/testData/refactoring/extractMethod/");
  }

  private void doTest() throws Throwable {
    DartSdkTestUtil.configFakeSdk(myFixture);
    myFixture.configureByFile(getTestName(true) + ".dart");
    doTestImpl();
  }

  private void doTestImpl() {
    final DartExtractMethodHandler extractMethodHandler = new DartExtractMethodHandler();
    //noinspection NullableProblems
    extractMethodHandler.invoke(myFixture.getProject(), myFixture.getEditor(), myFixture.getFile(), null);
    myFixture.checkResultByFile(getTestName(true) + "_expected.dart");
  }

  public void testExtract1() throws Throwable {
    doTest();
  }

  public void testExtract2() throws Throwable {
    doTest();
  }

  public void testExtract3() throws Throwable {
    doTest();
  }

  public void testExtract4() throws Throwable {
    doTest();
  }

  public void testExtract5() throws Throwable {
    doTest();
  }

  public void testExtractWEB2333() throws Throwable {
    doTest();
  }

  public void testExtractWEB2334() throws Throwable {
    doTest();
  }

  public void testExtractWEB6459() throws Throwable {
    doTest();
  }

  public void testExtractWEB6707() throws Throwable {
    doTest();
  }

  public void testExtractWI14240() throws Throwable {
    doTest();
  }

  public void testExtractWI14242() throws Throwable {
    doTest();
  }
}
