// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.refactoring;

import org.intellij.terraform.TfTestUtils;
import org.jetbrains.annotations.NotNull;

public class TfIntroduceVariableRefactoringTest extends BaseIntroduceVariableRefactoringTest {

  @Override
  protected String getTestDataPath() {
    return TfTestUtils.getTestDataPath() + "/terraform/refactoring/extract/variable";
  }

  @Override
  @NotNull
  protected BaseIntroduceOperation createIntroduceOperation(String name) {
    return new IntroduceOperation(myFixture.getProject(), myFixture.getEditor(), myFixture.getFile(), name);
  }

  @Override
  @NotNull
  protected BaseIntroduceVariableHandler createHandler() {
    return new TfIntroduceVariableHandler();
  }

  public void testStringExpressionSimple() throws Exception {
    doTest();
  }

  public void testStringExpressionAll() throws Exception {
    doTest(true);
  }

  public void testStringExpressionAllAutodetect() throws Exception {
    doTest(true, null);
  }

  public void testStringExpressionCaret() throws Exception {
    doTest();
  }

}
