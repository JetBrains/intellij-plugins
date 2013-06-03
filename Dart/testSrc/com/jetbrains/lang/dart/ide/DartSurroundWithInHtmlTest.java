package com.jetbrains.lang.dart.ide;

import com.intellij.codeInsight.generation.surroundWith.SurroundWithHandler;
import com.intellij.lang.surroundWith.Surrounder;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.LightPlatformCodeInsightTestCase;
import com.jetbrains.lang.dart.ide.surroundWith.statement.DartWithIfElseSurrounder;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author: Fedor.Korotkov
 */
public class DartSurroundWithInHtmlTest extends LightPlatformCodeInsightTestCase {
  @NotNull
  @Override
  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH + FileUtil.toSystemDependentName("/surroundWith/html/");
  }

  private void doTest(final Surrounder handler) throws Exception {
    configureByFile(getTestName(false) + ".html");
    SurroundWithHandler.invoke(getProject(), getEditor(), getFile(), handler);
    checkResultByFile(getTestName(false) + ".after.html");
  }

  public void testIfElse1() throws Throwable {
    doTest(new DartWithIfElseSurrounder());
  }
}
