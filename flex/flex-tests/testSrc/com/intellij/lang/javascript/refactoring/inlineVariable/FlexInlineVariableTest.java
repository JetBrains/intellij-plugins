package com.intellij.lang.javascript.refactoring.inlineVariable;

import com.intellij.flex.FlexTestUtils;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.refactoring.BaseRefactoringProcessor;

public class FlexInlineVariableTest extends JSInlineVariableTestBase {
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("as_refactoring/inlineVariable/");
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
  }

  private void ecmaTest() throws Exception {
    doTest(getTestName(false), "js2", true);
  }

  public void testClassMember() throws Exception {
    ecmaTest();
  }

  public void testClassMember2() throws Exception {
    ecmaTest();
  }

  public void testClassMember3() throws Exception {
    ecmaTest();
  }

  public void testInMxml() throws Exception {
    doTest(getTestName(false), "mxml", true);
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
  public void testIntoAttribute() throws Exception {
    doTest(getTestName(false), "mxml", true);
  }

  public void testInliningFunExpr3() throws Exception {
    ecmaTest();
  }

  public void testInlineOneFieldUsage() throws Exception {
    doTest(getTestName(false), "js2", true, true);
  }

  public void testReportAccessibilityProblems() throws Exception {
    try {
      doItBase(getTestName(false), "js2", false);
      assertFalse("Should find conflicts!", true);
    }
    catch (BaseRefactoringProcessor.ConflictsInTestsException e) {
      assertEquals(
        "Method <b><code>Test.foo()</code></b> with private visibility won't be accessible from file <b><code>ReportAccessibilityProblems.js2</code></b>",
        e.getMessage());
    }
  }
}
