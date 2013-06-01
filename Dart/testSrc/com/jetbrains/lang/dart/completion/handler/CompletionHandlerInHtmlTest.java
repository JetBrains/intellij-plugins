package com.jetbrains.lang.dart.completion.handler;

import com.intellij.openapi.util.io.FileUtil;
import com.jetbrains.lang.dart.util.DartTestUtils;

/**
 * @author: Fedor.Korotkov
 */
public class CompletionHandlerInHtmlTest extends CompletionHandlerTestBase {
  @Override
  protected String getBasePath() {
    return FileUtil.toSystemDependentName(DartTestUtils.RELATIVE_TEST_DATA_PATH + "/completion/handler/html/");
  }

  @Override
  protected String getTestFileExtension() {
    return ".html";
  }

  public void testParentheses1() throws Throwable {
    doTest();
  }
}
