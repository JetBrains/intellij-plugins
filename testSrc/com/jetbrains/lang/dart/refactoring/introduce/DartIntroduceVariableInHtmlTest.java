package com.jetbrains.lang.dart.refactoring.introduce;

import com.intellij.openapi.util.io.FileUtil;
import com.jetbrains.lang.dart.ide.refactoring.introduce.DartIntroduceHandler;
import com.jetbrains.lang.dart.ide.refactoring.introduce.DartIntroduceVariableHandler;
import com.jetbrains.lang.dart.psi.DartCallExpression;

/**
 * @author: Fedor.Korotkov
 */
public class DartIntroduceVariableInHtmlTest extends DartIntroduceTestBase {
  @Override
  protected String getBasePath() {
    return FileUtil.toSystemDependentName("/plugins/Dart/testData/refactoring/introduceVariable/html/");
  }

  @Override
  protected DartIntroduceHandler createHandler() {
    return new DartIntroduceVariableHandler();
  }

  @Override
  protected String getFileExtension() {
    return ".html";
  }

  public void testReplaceAll1() throws Throwable {
    doTest();
  }

  public void testReplaceOne1() throws Throwable {
    doTest(null, false);
  }

  public void testSuggestName1() throws Throwable {
    doTestSuggestions(DartCallExpression.class, "test");
  }
}
