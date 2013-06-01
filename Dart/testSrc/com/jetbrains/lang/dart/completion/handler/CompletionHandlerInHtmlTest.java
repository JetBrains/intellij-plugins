package com.jetbrains.lang.dart.completion.handler;

import com.intellij.openapi.util.io.FileUtil;

/**
 * @author: Fedor.Korotkov
 */
public class CompletionHandlerInHtmlTest extends CompletionHandlerTestBase {
  @Override
  protected String getBasePath() {
    return FileUtil.toSystemDependentName("/plugins/Dart/testData/completion/handler/html/");
  }

  @Override
  protected String getTestFileExtension() {
    return ".html";
  }

  public void testParentheses1() throws Throwable {
    doTest();
  }
}
