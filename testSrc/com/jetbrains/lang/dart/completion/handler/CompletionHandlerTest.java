package com.jetbrains.lang.dart.completion.handler;

import com.intellij.openapi.util.io.FileUtil;

/**
 * @author: Fedor.Korotkov
 */
public class CompletionHandlerTest extends CompletionHandlerTestBase {
  @Override
  protected String getBasePath() {
    return FileUtil.toSystemDependentName("/plugins/Dart/testData/completion/handler");
  }

  @Override
  protected String getTestFileExtension() {
    return ".dart";
  }

  public void testConstructor1() throws Throwable {
    doTest();
  }

  public void testConstructor2() throws Throwable {
    doTest();
  }

  public void testConstructor3() throws Throwable {
    doTest();
  }

  public void testParentheses1() throws Throwable {
    doTest();
  }

  public void testParentheses2() throws Throwable {
    doTest();
  }
}
