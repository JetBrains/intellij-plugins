// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.refactoring;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.refactoring.introduceVariable.JSIntroduceVariableTestCase;
import com.intellij.lang.javascript.refactoring.introduceVariable.Settings;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import org.jetbrains.annotations.NotNull;

public class ActionScriptIntroduceVariableTest extends JSIntroduceVariableTestCase {

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("refactoring/introduceVariable/");
  }

  private void ecmaTest() {
    doTest("created", true, ".js2");
  }

  private void ecmaTestFails() {
    assertThrows(CommonRefactoringUtil.RefactoringErrorHintException.class, this::ecmaTest);
  }

  private void simpleTest(final int variant) {
    simpleTest(variant, ".js2");
  }

  private void firstVariantEcmaCompletion() {
    doTest("xxx", false, Settings.IntroducedVarType.VAR, ".js2", jsExpressions -> jsExpressions[0]);
  }

  public void testIntroduceAddsType() {
    ecmaTest();
  }

  public void testIntroduceAddsType2() {
    ecmaTest();
  }

  public void testIntroduceAddsType3() {
    ecmaTest();
  }

  public void testIntroduceAddsType4() {
    ecmaTest();
  }

  public void testIntroduceAddsType5() {
    ecmaTest();
  }

  public void testIntroduceAddsType6() {
    ecmaTest();
  }

  public void testIntroduceAddsType7() {
    doTest("created", true, Settings.IntroducedVarType.VAR, ".js2");
  }

  public void testIntroduceAddsType8() {
    ecmaTest();
  }

  public void testIntroduceAddsType9() {
    ecmaTest();
  }

  public void testIntroduceAddsType10() {
    ecmaTest();
  }

  public void testIntroduceAddsType10_2() {
    ecmaTest();
  }

  public void testIntroduceAddsType10_3() {
    ecmaTest();
  }

  public void testIntroduceAddsType10_4() {
    ecmaTest();
  }

  public void testIntroduceAddsType10_5() {
    ecmaTest();
  }

  public void testIntroduceAddsType10_6() {
    ecmaTest();
  }

  public void testIntroduceAddsType10_7() {
    ecmaTest();
  }

  public void testIntroduceAddsType10_8() {
    ecmaTest();
  }

  public void testIntroduceAddsType10_9() {
    ecmaTest();
  }

  public void testIntroduceAddsType11() {
    ecmaTest();
  }

  public void testIntroduceAddsType11_2() {
    ecmaTest();
  }

  public void testIntroduceAddsType12() {
    ecmaTest();
  }

  public void testIntroduceAddsType12_2() {
    ecmaTest();
  }

  public void testIntroduceAddsType12_3() {
    ecmaTest();
  }

  public void testIntroduceAddsType12_4() {
    ecmaTest();
  }

  public void testIntroduceAddsType12_5() {
    ecmaTest();
  }

  public void testIntroduceAddsType12_6() {
    ecmaTest();
  }

  public void testIntroduceAddsType12_7() {
    ecmaTest();
  }

  public void testIntroduceAddsType12_8() {
    ecmaTest();
  }

  public void testIntroduceStatic() {
    ecmaTest();
  }

  public void testIntroduceStatic_2() {
    ecmaTest();
  }

  public void testIntroduceOnClassLevel() {
    ecmaTest();
  }

  public void testIntroduceForVoidExpr() {
    ecmaTestFails();
  }

  public void testIntroduceForTypeRef() {
    ecmaTestFails();
  }

  public void testIntroduceForTypeRef2() {
    ecmaTestFails();
  }

  public void testIntroduceForTypeRef2_2() {
    ecmaTestFails();
  }

  public void testIntroduceForVector() {
    simpleTest(0);
  }

  public void testIntroduceForVector_2() {
    simpleTest(1);
  }

  public void testIntroduceForVector_3() {
    simpleTest(0);
  }

  public void testIntroduceForVector_4() {
    simpleTest(0);
  }

  public void testIntroduceForVector_5() {
    simpleTest(0);
  }

  public void testIntroduceForVector_6() {
    simpleTest(0);
  }

  public void testIntroduceForVector_7() {
    simpleTest(0);
  }

  public void testIntroduceForVector_8() {
    simpleTest(0);
  }

  public void testIntroduceForActualParameter() {
    simpleTest(0);
  }

  public void testSmartIntroduce_3() {
    ecmaTest();
  }

  public void testSmartIntroduce_4() {
    ecmaTestFails();
  }

  public void testSmartIntroduce_5() {
    firstVariantEcmaCompletion();
  }

  public void testSmartIntroduce_6() {
    firstVariantEcmaCompletion();
  }

  public void testSmartIntroduce_7() {
    firstVariantEcmaCompletion();
  }

  public void testSmartIntroduce_8() {
    firstVariantEcmaCompletion();
  }

  public void testSmartIntroduce_9() {
    firstVariantEcmaCompletion();
  }

  public void testSmartIntroduce_10() {
    firstVariantEcmaCompletion();
  }

  public void testSmartIntroduce2() {
    doTest("created", true, Settings.IntroducedVarType.VAR, ".js2",
           jsExpressions -> jsExpressions[jsExpressions.length - 1]);
  }

  public void testAmbiguousType() {
    ecmaTest();
  }

  public void testNoIntroduceForVectorLiteral() {
    ecmaTestFails();
  }

  public void testIntroduceCall() {
    doTest("created", false, ".js2");
  }

  public void testIntroduceNamespace() {
    doTest("namespace", true, ".js2");
  }

  public void testIntroduceFunctionFromCall() {
    doTest("fun", true, ".js2");
  }
  public void testNewVectorOfVector() {
    simpleTest(0, ".js2");
  }

  public void testNewVectorOfVector2() {
    simpleTest(0, ".js2");
  }

  public void testDefinitionExprNotSuggested2() {
    doTest("created", false, Settings.IntroducedVarType.VAR, ".js2", expressions -> {
      assertExpressions(expressions, "obj", "obj.prp=2");
      return expressions[0];
    });
  }

  public void testIntroduceInElseIf() {
    simpleTest(0, ".js2");
  }

}
