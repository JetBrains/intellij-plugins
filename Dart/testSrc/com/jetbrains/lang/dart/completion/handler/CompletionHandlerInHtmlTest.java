package com.jetbrains.lang.dart.completion.handler;

/**
 * @author: Fedor.Korotkov
 */
public class CompletionHandlerInHtmlTest extends CompletionHandlerTestBase {
  @Override
  protected String getBasePath() {
    return "/completion/handler/html/";
  }

  @Override
  protected String getTestFileExtension() {
    return ".html";
  }

  public void testParentheses1() throws Throwable {
    doTest();
  }
}
