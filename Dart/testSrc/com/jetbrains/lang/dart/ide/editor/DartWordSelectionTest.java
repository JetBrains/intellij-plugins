package com.jetbrains.lang.dart.ide.editor;

import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.util.DartTestUtils;

public class DartWordSelectionTest extends DartCodeInsightFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, getTestRootDisposable(), true);
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
  }

  protected String getBasePath() {
    return "/selectWord/";
  }

  private void doTest() {
    doTest(getTestDataPath(), getTestName(false));
  }

  private void doTest(final String filePath, final String testName) {
    myFixture.configureByFiles(filePath + testName + ".dart");
    CodeInsightTestUtil.doWordSelectionTest(myFixture, testName + ".dart", testName + "_after.dart");
  }

  public void testLineDocWord1() throws Exception {
    doTest();
  }

  public void testLineDocWord2() throws Exception {
    doTest();
  }

  public void testLineDocWord3() throws Exception {
    doTest();
  }

  public void testLineWord1() throws Exception {
    doTest();
  }

  public void testBlockDocWord1() throws Exception {
    doTest();
  }

  public void testBlockDocWord2() throws Exception {
    doTest();
  }

  public void testBlockWord1() throws Exception {
    doTest();
  }
}
