package com.intellij.flex.refactoring;

import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.refactoring.extractMethod.DefaultJSExtractFunctionSettings;
import com.intellij.lang.javascript.refactoring.extractMethod.JSExtractFunctionBaseTest;
import com.intellij.lang.javascript.refactoring.extractMethod.JSExtractFunctionSettings;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.util.containers.ContainerUtil;

public class FlexExtractFunctionTest extends JSExtractFunctionBaseTest {
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("refactoring/flexExtractFunction/");
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return FlexProjectDescriptor.DESCRIPTOR;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "refactoring/flexExtractFunction/");
    FlexTestUtils.setupFlexSdk(myFixture.getModule(), getTestName(false), getClass(), myFixture.getTestRootDisposable());
  }

  public void testExpressionInClass() throws Exception {
    ecmaL4Test();
  }

  private void ecmaL4Test() throws Exception {
    doTest("created", "js2");
  }

  public void testLeavingReturn() throws Exception {
    ecmaL4Test();
  }

  public void testLeavingReturn3() throws Exception {
    ecmaL4Test();
  }

  public void testAssignExpr() throws Exception {
    ecmaL4Test();
  }

  public void testAssignExprUntyped() throws Exception {
    ecmaL4Test();
  }

  public void testMethodInClass() throws Exception {
    doTest("addHello", "js2");
  }

  public void testSelectEntireLine() throws Exception {
    doTest(
      () -> {
        JSExtractFunctionSettings.ParametersInfo parametersInfo = new JSExtractFunctionSettings.ParametersInfo();
        JSVariable var = PsiTreeUtil.getParentOfType(myFixture.getFile().findElementAt(myFixture.getEditor().getCaretModel().getOffset()), JSVariable.class);
        assertNotNull(var);
        parametersInfo.variables.add(var);
        parametersInfo.variableOptions.put(var, new JSExtractFunctionSettings.ParameterInfo(var.getName() + "2", true, null, 0));

        return new DefaultJSExtractFunctionSettings("created", true, false, JSAttributeList.AccessType.PUBLIC, parametersInfo, null);
      },
      "js2"
    );
  }

  public void testBasic3() throws Exception {
    ecmaL4Test();
  }

  public void testBasic4() throws Exception {
    ecmaL4Test();
  }

  public void testBasic5() throws Exception {
    ecmaL4Test();
  }

  public void testBasic6() throws Exception {
    ecmaL4Test();
  }

  public void testArguments() throws Exception {
    ecmaL4Test();
  }

  public void testExtraReturn() throws Exception {
    failedEcmaTest();
  }

  public void testContinue() throws Exception {
    failedEcmaTest();
  }

  public void testContinue2() throws Exception {
    ecmaL4Test();
  }

  public void testContinue2_2() throws Exception {
    failedEcmaTest();
  }

  public void testBreak() throws Exception {
    failedEcmaTest();
  }

  public void testBreak2() throws Exception {
    ecmaL4Test();
  }

  public void testInsertImport() throws Exception {
    ecmaL4Test();
  }

  public void testBadSelectionJS2() throws Exception {
    failedEcmaTest();
  }

  public void testBadSelectionJS2_2() throws Exception {
    failedEcmaTest();
  }

  public void testBadSelectionJS2_3() throws Exception {
    failedEcmaTest();
  }

  public void testBadSelectionJS2_4() throws Exception {
    failedEcmaTest();
  }

  public void testBadSelectionJS2_5() throws Exception {
    failedEcmaTest();
  }

  private void failedEcmaTest() throws Exception {
    assertFails(() -> doTest("created", "js2"));
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
  public void testMxml() throws Exception {
    doTest("created", "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
  public void testMxml2() throws Exception {
    doTest("created", "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
  public void testMxml3() throws Exception {
    doTest("created", "mxml");
  }

  public void testScopeOptions3() {
    doTestWithScopeSelection(scopes -> ContainerUtil.getFirstItem(scopes), "js2");
  }

  public void testScopeOptionsMxml() {
    doTestWithScopeSelection(scopes -> ContainerUtil.getFirstItem(scopes), "mxml");
  }
}
