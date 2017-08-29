package com.intellij.flex.refactoring;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.refactoring.extractMethod.JSExtractFunctionBaseTest;
import com.intellij.lang.javascript.refactoring.extractMethod.JSExtractFunctionHandler;
import com.intellij.lang.javascript.refactoring.extractMethod.JSExtractFunctionSettings;
import com.intellij.lang.javascript.refactoring.extractMethod.MockJSExtractFunctionHandler;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.psi.util.PsiTreeUtil;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class FlexExtractFunctionTest extends JSExtractFunctionBaseTest {
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("refactoring/flexExtractFunction/");
  }

  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexSchemaHandler.class.getResource("z.xsd"))),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), getTestRootDisposable());
  }

  protected JSExtractFunctionHandler createMockHandler(JSExtractFunctionSettings extractFunctionSettings) {
    return new MockJSExtractFunctionHandler(extractFunctionSettings);
  }

  public void testExpressionInClass() throws Exception {
    ecmaL4Test();
  }

  private void ecmaL4Test() throws Exception {
    doTest("created", getTestName(false), "js2", true);
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
    doTest("addHello", getTestName(false), "js2", true);
  }

  public void testSelectEntireLine() throws Exception {
    doTest(
      at -> {
        JSExtractFunctionSettings.ParametersInfo parametersInfo = new JSExtractFunctionSettings.ParametersInfo();
        JSVariable var = PsiTreeUtil.getParentOfType(myFile.findElementAt(myEditor.getCaretModel().getOffset()), JSVariable.class);
        assertNotNull(var);
        parametersInfo.variables.add(var);
        parametersInfo.variableOptions.put(var, new JSExtractFunctionSettings.ParameterInfo(var.getName() + "2", true, null, 0));

        return new MockJSExtractFunctionHandler.MyJSExtractFunctionSettings(
          "created",
          true,
          false,
          JSAttributeList.AccessType.PUBLIC, parametersInfo, at
        );
      },
      getTestName(false),
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
    doTest("created", getTestName(false), "js2", false);
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
  public void testMxml() throws Exception {
    doTest("created", getTestName(false), "mxml", true);
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
  public void testMxml2() throws Exception {
    doTest("created", getTestName(false), "mxml", true);
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
  public void testMxml3() throws Exception {
    doTest("created", getTestName(false), "mxml", true);
  }

  public void testScopeOptions3() throws Exception {
    doTestForAllScopes(getTestName(false), "js2");
  }

  public void testScopeOptionsMxml() throws Exception {
    doTestForAllScopes(getTestName(false), "mxml");
  }
}
