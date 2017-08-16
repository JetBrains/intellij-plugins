package com.jetbrains.lang.dart.refactoring.introduce;

import com.intellij.openapi.util.io.FileUtil;
import com.jetbrains.lang.dart.ide.refactoring.introduce.DartIntroduceHandler;
import com.jetbrains.lang.dart.ide.refactoring.introduce.DartIntroduceVariableHandler;
import com.jetbrains.lang.dart.psi.DartCallExpression;
import com.jetbrains.lang.dart.util.DartTestUtils;

/**
 * @author: Fedor.Korotkov
 */
public class DartIntroduceVariableTest extends DartIntroduceTestBase {
  @Override
  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH + getBasePath();
  }

  @Override
  protected String getBasePath() {
    return FileUtil.toSystemDependentName("/refactoring/introduceVariable/");
  }

  @Override
  protected DartIntroduceHandler createHandler() {
    return new DartIntroduceVariableHandler();
  }

  public void testAfterStatement() {
    doTest();
  }

  public void testAlone() {
    doTest();
  }

  public void testIntroduceWEB6426() {
    doTestInplace(null);
  }

  public void testIntroduceWEB6458() {
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

  public void testIntroduceWEB6479() {
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

  public void testReplaceAll1() {
    doTest();
  }

  public void testReplaceAll2() {
    doTest();
  }

  public void testReplaceAll3() {
    doTestInplace(null);
  }

  public void testReplaceOne1() {
    doTest(null, false);
  }

  public void testSuggestName1() {
    doTestSuggestions(DartCallExpression.class, "test");
  }

  public void testSuggestName2() {
    doTestSuggestions(DartCallExpression.class, "test1");
  }

  public void testSuggestName3() {
    doTestSuggestions(DartCallExpression.class, "width", "canvasWidth");
  }
}
