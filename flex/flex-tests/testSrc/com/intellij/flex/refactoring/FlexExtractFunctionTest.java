package com.intellij.flex.refactoring;

import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.refactoring.extractMethod.DefaultJSExtractFunctionSettings;
import com.intellij.lang.javascript.refactoring.extractMethod.FunctionDeclarationType;
import com.intellij.lang.javascript.refactoring.extractMethod.JSExtractFunctionFixture;
import com.intellij.lang.javascript.refactoring.extractMethod.JSExtractFunctionSettings;
import com.intellij.openapi.project.Project;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.util.containers.ContainerUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;
import static com.intellij.lang.javascript.refactoring.extractMethod.JSExtractFunctionFixture.assertFails;
import static com.intellij.lang.javascript.refactoring.extractMethod.JSExtractFunctionFixture.jsExtractFunctionFixture;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestApplication
public class FlexExtractFunctionTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSExtractFunctionFixture> js =
    jsExtractFunctionFixture(projectFixture, new JSFixtureOptions(FlexTestUtils.getTestDataPath("refactoring/flexExtractFunction/"))
        .withModuleType(FlexModuleType.MODULE_TYPE_ID));

  @BeforeEach
  void setUp() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "refactoring/flexExtractFunction/");
    FlexTestUtils.setupFlexSdk(myFixture.getModule(), js.get().getTestName(false), getClass(), myFixture.getTestRootDisposable());
  }

  @Test
  public void testExpressionInClass() {
    ecmaL4Test();
  }

  private void ecmaL4Test() {
    js.get().doTest("created", "js2");
  }

  @Test
  public void testLeavingReturn() {
    ecmaL4Test();
  }

  @Test
  public void testLeavingReturn3() {
    ecmaL4Test();
  }

  @Test
  public void testAssignExpr() {
    ecmaL4Test();
  }

  @Test
  public void testAssignExprUntyped() {
    ecmaL4Test();
  }

  @Test
  public void testMethodInClass() {
    js.get().doTest("addHello", "js2");
  }

  @Test
  public void testSelectEntireLine() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    js.get().doTest(
      () -> {
        JSExtractFunctionSettings.ParametersInfo parametersInfo = new JSExtractFunctionSettings.ParametersInfo();
        JSVariable var = PsiTreeUtil.getParentOfType(myFixture.getFile().findElementAt(myFixture.getEditor().getCaretModel().getOffset()), JSVariable.class);
        assertNotNull(var);
        parametersInfo.variables.add(var);
        parametersInfo.variableOptions.put(var, new JSExtractFunctionSettings.ParameterInfo(var.getName() + "2", true, null, 0));

        return new DefaultJSExtractFunctionSettings("created", true, null, FunctionDeclarationType.FUNCTION, JSAttributeList.AccessType.PUBLIC, parametersInfo, null, false);
      },
      "js2"
    );
  }

  @Test
  public void testBasic3() {
    ecmaL4Test();
  }

  @Test
  public void testBasic4() {
    ecmaL4Test();
  }

  @Test
  public void testBasic5() {
    ecmaL4Test();
  }

  @Test
  public void testBasic6() {
    ecmaL4Test();
  }

  @Test
  public void testArguments() {
    ecmaL4Test();
  }

  @Test
  public void testExtraReturn() {
    failedEcmaTest();
  }

  @Test
  public void testBreak() {
    failedEcmaTest();
  }

  @Test
  public void testBreak2() {
    ecmaL4Test();
  }

  @Test
  public void testInsertImport() {
    ecmaL4Test();
  }

  @Test
  public void testBadSelectionJS2() {
    failedEcmaTest();
  }

  @Test
  public void testBadSelectionJS2_2() {
    failedEcmaTest();
  }

  @Test
  public void testBadSelectionJS2_3() {
    failedEcmaTest();
  }

  @Test
  public void testBadSelectionJS2_4() {
    failedEcmaTest();
  }

  @Test
  public void testBadSelectionJS2_5() {
    failedEcmaTest();
  }

  private void failedEcmaTest() {
    assertFails(() -> js.get().doTest("created", "js2"));
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testMxml() {
    js.get().doTest("created", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testMxml2() {
    js.get().doTest("created", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testMxml3() {
    js.get().doTest("created", "mxml");
  }

  @Test
  public void testScopeOptions3() {
    js.get().doTestWithScopeSelection(scopes -> ContainerUtil.getFirstItem(scopes), "js2");
  }

  @Test
  public void testScopeOptionsMxml() {
    js.get().doTestWithScopeSelection(scopes -> ContainerUtil.getFirstItem(scopes), "mxml");
  }

  @Test
  public void testWithMultipleReturnStatements() {
    ecmaL4Test();
  }
}
