package com.intellij.flex.refactoring;

import com.intellij.flex.base.FlexInlineVarOrFunctionTestBase;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.ModuleType;

public class FlexInlineVariableTest extends FlexInlineVarOrFunctionTestBase {
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("as_refactoring/inlineVariable/");
  }

  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "as_refactoring/inlineVariable/");

    super.setUp();
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), getTestRootDisposable());
  }

  private void ecmaTest() throws Exception {
    doTest(getTestName(false), "js2");
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
    doTest(getTestName(false), "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
  public void testIntoAttribute() throws Exception {
    doTest(getTestName(false), "mxml");
  }

  public void testInlineOneFieldUsage() throws Exception {
    doTest(new String[]{getTestName(false) + ".js2"}, true);
  }

  public void testReportAccessibilityProblems() {
    doTestConflicts(getTestName(false), "js2", new String[]{
      "Method Test.foo() with private visibility won't be accessible from file ReportAccessibilityProblems.js2"});
  }
}
