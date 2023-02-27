// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.refactoring;

import org.intellij.terraform.TerraformTestUtils;
import org.jetbrains.annotations.NotNull;

public class TerraformIntroduceVariableRefactoringTest extends BaseIntroduceVariableRefactoringTest {

  protected String getTestDataPath() {
    return TerraformTestUtils.getTestDataPath() + "/terraform/refactoring/extract/variable";
  }

  @NotNull
  protected BaseIntroduceOperation createIntroduceOperation(String name) {
    return new IntroduceOperation(myFixture.getProject(), myFixture.getEditor(), myFixture.getFile(), name);
  }

  @NotNull
  protected BaseIntroduceVariableHandler createHandler() {
    return new TerraformIntroduceVariableHandler();
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
