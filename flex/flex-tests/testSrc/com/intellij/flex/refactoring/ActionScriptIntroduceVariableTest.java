// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.refactoring;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.refactoring.introduceVariable.JSIntroduceVariableFixture;
import com.intellij.lang.javascript.refactoring.introduceVariable.Settings;
import com.intellij.openapi.project.Project;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;
import static com.intellij.lang.javascript.refactoring.introduceVariable.JSIntroduceVariableFixture.assertExpressions;
import static com.intellij.lang.javascript.refactoring.introduceVariable.JSIntroduceVariableFixture.jsIntroduceVariableFixture;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestApplication
public class ActionScriptIntroduceVariableTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSIntroduceVariableFixture> js =
    jsIntroduceVariableFixture(projectFixture, new JSFixtureOptions(FlexTestUtils.getTestDataPath("refactoring/introduceVariable/")));

  private void ecmaTest() {
    js.get().doTest("created", true, ".js2");
  }

  private void ecmaTestFails() {
    assertThrows(CommonRefactoringUtil.RefactoringErrorHintException.class, this::ecmaTest);
  }

  private void simpleTest(final int variant) {
    js.get().simpleTest(variant, ".js2");
  }

  private void firstVariantEcmaCompletion() {
    js.get().doTest("xxx", false, Settings.IntroducedVarType.VAR, ".js2", jsExpressions -> jsExpressions[0]);
  }

  @Test
  public void testIntroduceAddsType() {
    ecmaTest();
  }

  @Test
  public void testIntroduceAddsType2() {
    ecmaTest();
  }

  @Test
  public void testIntroduceAddsType3() {
    ecmaTest();
  }

  @Test
  public void testIntroduceAddsType4() {
    ecmaTest();
  }

  @Test
  public void testIntroduceAddsType5() {
    ecmaTest();
  }

  @Test
  public void testIntroduceAddsType6() {
    ecmaTest();
  }

  @Test
  public void testIntroduceAddsType7() {
    js.get().doTest("created", true, Settings.IntroducedVarType.VAR, ".js2");
  }

  @Test
  public void testIntroduceAddsType8() {
    ecmaTest();
  }

  @Test
  public void testIntroduceAddsType9() {
    ecmaTest();
  }

  @Test
  public void testIntroduceAddsType10() {
    ecmaTest();
  }

  @Test
  public void testIntroduceAddsType10_2() {
    ecmaTest();
  }

  @Test
  public void testIntroduceAddsType10_3() {
    ecmaTest();
  }

  @Test
  public void testIntroduceAddsType10_4() {
    ecmaTest();
  }

  @Test
  public void testIntroduceAddsType10_5() {
    ecmaTest();
  }

  @Test
  public void testIntroduceAddsType10_6() {
    ecmaTest();
  }

  @Test
  public void testIntroduceAddsType10_7() {
    ecmaTest();
  }

  @Test
  public void testIntroduceAddsType10_8() {
    ecmaTest();
  }

  @Test
  public void testIntroduceAddsType10_9() {
    ecmaTest();
  }

  @Test
  public void testIntroduceAddsType11() {
    ecmaTest();
  }

  @Test
  public void testIntroduceAddsType11_2() {
    ecmaTest();
  }

  @Test
  public void testIntroduceAddsType12() {
    ecmaTest();
  }

  @Test
  public void testIntroduceAddsType12_2() {
    ecmaTest();
  }

  @Test
  public void testIntroduceAddsType12_3() {
    ecmaTest();
  }

  @Test
  public void testIntroduceAddsType12_4() {
    ecmaTest();
  }

  @Test
  public void testIntroduceAddsType12_5() {
    ecmaTest();
  }

  @Test
  public void testIntroduceAddsType12_6() {
    ecmaTest();
  }

  @Test
  public void testIntroduceAddsType12_7() {
    ecmaTest();
  }

  @Test
  public void testIntroduceAddsType12_8() {
    ecmaTest();
  }

  @Test
  public void testIntroduceStatic() {
    ecmaTest();
  }

  @Test
  public void testIntroduceStatic_2() {
    ecmaTest();
  }

  @Test
  public void testIntroduceOnClassLevel() {
    ecmaTest();
  }

  @Test
  public void testIntroduceForVoidExpr() {
    ecmaTestFails();
  }

  @Test
  public void testIntroduceForTypeRef() {
    ecmaTestFails();
  }

  @Test
  public void testIntroduceForTypeRef2() {
    ecmaTestFails();
  }

  @Test
  public void testIntroduceForTypeRef2_2() {
    ecmaTestFails();
  }

  @Test
  public void testIntroduceForVector() {
    simpleTest(0);
  }

  @Test
  public void testIntroduceForVector_2() {
    simpleTest(1);
  }

  @Test
  public void testIntroduceForVector_3() {
    simpleTest(0);
  }

  @Test
  public void testIntroduceForVector_4() {
    simpleTest(0);
  }

  @Test
  public void testIntroduceForVector_5() {
    simpleTest(0);
  }

  @Test
  public void testIntroduceForVector_6() {
    simpleTest(0);
  }

  @Test
  public void testIntroduceForVector_7() {
    simpleTest(0);
  }

  @Test
  public void testIntroduceForVector_8() {
    simpleTest(0);
  }

  @Test
  public void testIntroduceForActualParameter() {
    simpleTest(0);
  }

  @Test
  public void testSmartIntroduce_3() {
    ecmaTest();
  }

  @Test
  public void testSmartIntroduce_4() {
    ecmaTestFails();
  }

  @Test
  public void testSmartIntroduce_5() {
    firstVariantEcmaCompletion();
  }

  @Test
  public void testSmartIntroduce_6() {
    firstVariantEcmaCompletion();
  }

  @Test
  public void testSmartIntroduce_7() {
    firstVariantEcmaCompletion();
  }

  @Test
  public void testSmartIntroduce_8() {
    firstVariantEcmaCompletion();
  }

  @Test
  public void testSmartIntroduce_9() {
    firstVariantEcmaCompletion();
  }

  @Test
  public void testSmartIntroduce_10() {
    firstVariantEcmaCompletion();
  }

  @Test
  public void testSmartIntroduce2() {
    js.get().doTest("created", true, Settings.IntroducedVarType.VAR, ".js2",
                    jsExpressions -> jsExpressions[jsExpressions.length - 1]);
  }

  @Test
  public void testAmbiguousType() {
    ecmaTest();
  }

  @Test
  public void testNoIntroduceForVectorLiteral() {
    ecmaTestFails();
  }

  @Test
  public void testIntroduceCall() {
    js.get().doTest("created", false, ".js2");
  }

  @Test
  public void testIntroduceNamespace() {
    js.get().doTest("namespace", true, ".js2");
  }

  @Test
  public void testIntroduceFunctionFromCall() {
    js.get().doTest("fun", true, ".js2");
  }
  @Test
  public void testNewVectorOfVector() {
    js.get().simpleTest(0, ".js2");
  }

  @Test
  public void testNewVectorOfVector2() {
    js.get().simpleTest(0, ".js2");
  }

  @Test
  public void testDefinitionExprNotSuggested2() {
    js.get().doTest("created", false, Settings.IntroducedVarType.VAR, ".js2", expressions -> {
      assertExpressions(expressions, "obj", "obj.prp=2");
      return expressions[0];
    });
  }

  @Test
  public void testIntroduceInElseIf() {
    js.get().simpleTest(0, ".js2");
  }
}
