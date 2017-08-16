package com.jetbrains.lang.dart.refactoring.introduce;

import com.intellij.openapi.util.io.FileUtil;
import com.jetbrains.lang.dart.ide.refactoring.introduce.DartIntroduceHandler;
import com.jetbrains.lang.dart.ide.refactoring.introduce.DartIntroduceVariableHandler;
import com.jetbrains.lang.dart.psi.DartCallExpression;
import com.jetbrains.lang.dart.util.DartTestUtils;

/**
 * @author: Fedor.Korotkov
 */
public class DartIntroduceVariableInHtmlTest extends DartIntroduceTestBase {
  @Override
  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH + getBasePath();
  }

  @Override
  protected String getBasePath() {
    return FileUtil.toSystemDependentName("/refactoring/introduceVariable/html/");
  }

  @Override
  protected DartIntroduceHandler createHandler() {
    return new DartIntroduceVariableHandler();
  }

  @Override
  protected String getFileExtension() {
    return ".html";
  }

  public void testReplaceAll1() {
    doTest();
  }

  public void testReplaceOne1() {
    doTest(null, false);
  }

  public void testSuggestName1() {
    doTestSuggestions(DartCallExpression.class, "test");
  }
}
