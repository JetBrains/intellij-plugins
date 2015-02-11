package com.jetbrains.lang.dart.quickfix;

import com.intellij.openapi.util.io.FileUtil;
import com.jetbrains.lang.dart.analyzer.DartAnalyzerTestBase;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public abstract class DartQuickFixTest extends DartAnalyzerTestBase {

  @NotNull
  @Override
  protected String getBasePath() {
    return FileUtil.toSystemDependentName("/quickfix/");
  }

  void doQuickFixTest(final String fixLabel) throws IOException {
    doTestWithoutCheck(fixLabel);
    myFixture.checkResultByFile(getTestName(false) + ".txt");
  }

  public void testImplement_WEB_14410() throws Throwable {
    doQuickFixTest("The function 'ngBootstrap' is not defined");
  }

}
