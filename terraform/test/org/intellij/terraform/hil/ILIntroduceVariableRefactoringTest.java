// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil;

import org.intellij.terraform.TfTestUtils;
import org.intellij.terraform.config.refactoring.BaseIntroduceOperation;
import org.intellij.terraform.config.refactoring.BaseIntroduceVariableHandler;
import org.intellij.terraform.config.refactoring.BaseIntroduceVariableRefactoringTest;
import org.intellij.terraform.hil.refactoring.ILIntroduceVariableHandler;
import org.intellij.terraform.hil.refactoring.IntroduceOperation;
import org.jetbrains.annotations.NotNull;

public class ILIntroduceVariableRefactoringTest extends BaseIntroduceVariableRefactoringTest {

  @Override
  protected String getTestDataPath() {
    return TfTestUtils.getTestDataPath() + "/hil/refactoring/extract/variable";
  }

  @NotNull
  @Override
  protected BaseIntroduceOperation createIntroduceOperation(String name) {
    return new IntroduceOperation(myFixture.getProject(), myFixture.getEditor(), myFixture.getFile(), name);
  }

  @NotNull
  @Override
  protected BaseIntroduceVariableHandler createHandler() {
    return new ILIntroduceVariableHandler();
  }

  public void testStringExpressionSimple() throws Exception {
    doTest();
  }
}
