package com.jetbrains.lang.dart.refactoring.extract;

import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.ide.refactoring.extract.DartExtractMethodHandler;
import com.jetbrains.lang.dart.util.DartSdkTestUtil;

/**
 * @author: Fedor.Korotkov
 */
public class DartExtractMethodInHtmlTest extends DartCodeInsightFixtureTestCase {
  @Override
  protected String getBasePath() {
    return "/refactoring/extractMethod/html/";
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    DartSdkTestUtil.configFakeSdk(myFixture, "../../../sdk");
  }

  private void doTest() throws Throwable {
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
