package com.jetbrains.dart.analysisServer;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.lang.CodeInsightActions;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.util.DartTestUtils;

public class DartServerGotoSuperHandlerTest extends CodeInsightFixtureTestCase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, myFixture.getTestRootDisposable(), true);
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
  }

  @Override
  protected String getBasePath() {
    return "/analysisServer/gotoSuper";
  }

  public void testSuperClass() {
    doTest();
  }

  public void testSuperClassMethod() {
    doTest();
  }

  public void testSuperInterface() {
    doTest();
  }

  public void testSuperOperator() {
    doTest();
  }

  private void doTest() {
    myFixture.configureByFile(getTestName(false) + ".dart");
    initServer();
    final CodeInsightActionHandler handler = CodeInsightActions.GOTO_SUPER.forLanguage(DartLanguage.INSTANCE);
    handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile());
    myFixture.checkResultByFile(getTestName(false) + ".after.dart");
  }

  private void initServer() {
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
    myFixture.doHighlighting(); // make sure server is warmed up
  }
}
