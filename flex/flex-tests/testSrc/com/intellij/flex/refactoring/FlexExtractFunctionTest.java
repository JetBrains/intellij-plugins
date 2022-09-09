package com.intellij.flex.refactoring;

import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.util.FlexTestUtils;
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

  public void testExpressionInClass() {
    ecmaL4Test();
  }

  private void ecmaL4Test() {
    doTest("created", "js2");
  }

  public void testLeavingReturn() {
    ecmaL4Test();
  }

  public void testLeavingReturn3() {
    ecmaL4Test();
  }

  public void testAssignExpr() {
    ecmaL4Test();
  }

  public void testAssignExprUntyped() {
    ecmaL4Test();
  }

  public void testMethodInClass() {
    doTest("addHello", "js2");
  }

  public void testSelectEntireLine() {
    doTest(
      () -> {
        JSExtractFunctionSettings.ParametersInfo parametersInfo = new JSExtractFunctionSettings.ParametersInfo();
        JSVariable var = PsiTreeUtil.getParentOfType(myFixture.getFile().findElementAt(myFixture.getEditor().getCaretModel().getOffset()), JSVariable.class);
        assertNotNull(var);
        parametersInfo.variables.add(var);
        parametersInfo.variableOptions.put(var, new JSExtractFunctionSettings.ParameterInfo(var.getName() + "2", true, null, 0));

        return new DefaultJSExtractFunctionSettings("created", true, false, JSAttributeList.AccessType.PUBLIC, parametersInfo, null, false);
      },
      "js2"
    );
  }

  public void testBasic3() {
    ecmaL4Test();
  }

  public void testBasic4() {
    ecmaL4Test();
  }

  public void testBasic5() {
    ecmaL4Test();
  }

  public void testBasic6() {
    ecmaL4Test();
  }

  public void testArguments() {
    ecmaL4Test();
  }

  public void testExtraReturn() {
    failedEcmaTest();
  }

  public void testBreak() {
    failedEcmaTest();
  }

  public void testBreak2() {
    ecmaL4Test();
  }

  public void testInsertImport() {
    ecmaL4Test();
  }

  public void testBadSelectionJS2() {
    failedEcmaTest();
  }

  public void testBadSelectionJS2_2() {
    failedEcmaTest();
  }

  public void testBadSelectionJS2_3() {
    failedEcmaTest();
  }

  public void testBadSelectionJS2_4() {
    failedEcmaTest();
  }

  public void testBadSelectionJS2_5() {
    failedEcmaTest();
  }

  private void failedEcmaTest() {
    assertFails(() -> doTest("created", "js2"));
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testMxml() {
    doTest("created", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testMxml2() {
    doTest("created", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testMxml3() {
    doTest("created", "mxml");
  }

  public void testScopeOptions3() {
    doTestWithScopeSelection(scopes -> ContainerUtil.getFirstItem(scopes), "js2");
  }

  public void testScopeOptionsMxml() {
    doTestWithScopeSelection(scopes -> ContainerUtil.getFirstItem(scopes), "mxml");
  }

  public void testWithMultipleReturnStatements() {
    ecmaL4Test();
  }
}
