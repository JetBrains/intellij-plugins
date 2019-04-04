package com.jetbrains.lang.dart.ide.editor;

import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.util.DartTestUtils;

public class DartWordSelectionTest extends DartCodeInsightFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, myFixture.getTestRootDisposable(), false);
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
  }

  @Override
  protected String getBasePath() {
    return "/selectWord/";
  }

  private void doTest() {
    final String testName = getTestName(false);
    myFixture.configureByFiles(testName + ".dart");
    CodeInsightTestUtil.doWordSelectionTest(myFixture, testName + ".dart", testName + "_after.dart");
  }

  public void testLineDocWord1() {
    doTest();
  }

  public void testLineDocWord2() {
    doTest();
  }

  public void testLineDocWord3() {
    doTest();
  }

  public void testLineWord1() {
    doTest();
  }

  public void testBlockDocWord1() {
    doTest();
  }

  public void testBlockDocWord2() {
    doTest();
  }

  public void testBlockWord1() {
    doTest();
  }
}
