package com.intellij.flex.refactoring;

import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.refactoring.JSInlineVarOrFunctionTestBase;
import com.intellij.testFramework.LightProjectDescriptor;

public class FlexInlineVariableTest extends JSInlineVarOrFunctionTestBase {
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("as_refactoring/inlineVariable/");
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return FlexProjectDescriptor.DESCRIPTOR;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "as_refactoring/inlineVariable/");
    FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), getClass(), myFixture.getTestRootDisposable());
  }

  private void ecmaTest() {
    doTest(getTestName(false), "js2");
  }

  public void testClassMember() {
    ecmaTest();
  }

  public void testClassMember2() {
    ecmaTest();
  }

  public void testClassMember3() {
    ecmaTest();
  }

  public void testInMxml() {
    doTest(getTestName(false), "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testIntoAttribute() {
    doTest(getTestName(false), "mxml");
  }

  public void testInlineOneFieldUsage() {
    doTest(new String[]{getTestName(false) + ".js2"}, true);
  }

  public void testReportAccessibilityProblems() {
    doTestConflicts(getTestName(false), "js2", new String[]{
      "Method Test.foo() with private visibility won't be accessible from file ReportAccessibilityProblems.js2"});
  }
}
