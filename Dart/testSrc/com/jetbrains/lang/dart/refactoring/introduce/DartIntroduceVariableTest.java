package com.jetbrains.lang.dart.refactoring.introduce;

import com.intellij.openapi.util.io.FileUtil;
import com.jetbrains.lang.dart.ide.refactoring.introduce.DartIntroduceHandler;
import com.jetbrains.lang.dart.ide.refactoring.introduce.DartIntroduceVariableHandler;
import com.jetbrains.lang.dart.psi.DartCallExpression;

/**
 * @author: Fedor.Korotkov
 */
public class DartIntroduceVariableTest extends DartIntroduceTestBase {
  @Override
  protected String getBasePath() {
    return FileUtil.toSystemDependentName("/plugins/Dart/testData/refactoring/introduceVariable/");
  }

  @Override
  protected DartIntroduceHandler createHandler() {
    return new DartIntroduceVariableHandler();
  }

  public void testAfterStatement() throws Throwable {
    doTest();
  }

  public void testAlone() throws Throwable {
    doTest();
  }

  public void testIntroduceWEB6426() throws Throwable {
    doTestInplace(null);
  }

  public void testIntroduceWEB6458() throws Throwable {
    try {
      doTest();
    }
    catch (RuntimeException e) {
      assertEquals("Cannot perform refactoring.\n" +
                   "Can't find place for the result", e.getMessage());
      doCheck();
      return;
    }
    fail("Expected: Can't find place for the result");
  }

  public void testIntroduceWEB6479() throws Throwable {
    try {
      doTest();
    }
    catch (RuntimeException e) {
      assertEquals("Cannot perform refactoring.\n" +
                   "Can't find place for the result", e.getMessage());
      doCheck();
      return;
    }
    fail("Expected: Can't find place for the result");
  }

  public void testReplaceAll1() throws Throwable {
    doTest();
  }

  public void testReplaceAll2() throws Throwable {
    doTest();
  }

  public void testReplaceAll3() throws Throwable {
    doTestInplace(null);
  }

  public void testReplaceOne1() throws Throwable {
    doTest(null, false);
  }

  public void testSuggestName1() throws Throwable {
    doTestSuggestions(DartCallExpression.class, "test");
  }

  public void testSuggestName2() throws Throwable {
    doTestSuggestions(DartCallExpression.class, "test1");
  }

  public void testSuggestName3() throws Throwable {
    doTestSuggestions(DartCallExpression.class, "width", "canvasWidth");
  }
}
