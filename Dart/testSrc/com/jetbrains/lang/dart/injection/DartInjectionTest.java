package com.jetbrains.lang.dart.injection;

import com.intellij.codeInsight.injected.InjectedLanguageTestCase;
import com.intellij.testFramework.ParsingTestCase;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;

public class DartInjectionTest extends InjectedLanguageTestCase {
  @NotNull
  @Override
  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH + "/injection/";
  }

  private void doTest() throws Exception {
    configureByFile(getTestName(false) + ".dart");
    ParsingTestCase.doCheckResult(getTestDataPath(), getTestName(false) + "." + "txt", toParseTreeText(myFile));
  }

  public void testHtmlInStrings() throws Exception {
    doTest();
  }
}
