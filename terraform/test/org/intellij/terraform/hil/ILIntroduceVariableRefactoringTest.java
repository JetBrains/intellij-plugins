/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.terraform.hil;

import org.intellij.terraform.TerraformTestUtils;
import org.intellij.terraform.config.refactoring.BaseIntroduceOperation;
import org.intellij.terraform.config.refactoring.BaseIntroduceVariableHandler;
import org.intellij.terraform.config.refactoring.BaseIntroduceVariableRefactoringTest;
import org.intellij.terraform.hil.refactoring.ILIntroduceVariableHandler;
import org.intellij.terraform.hil.refactoring.IntroduceOperation;
import org.jetbrains.annotations.NotNull;

public class ILIntroduceVariableRefactoringTest extends BaseIntroduceVariableRefactoringTest {

  protected String getTestDataPath() {
    return TerraformTestUtils.getTestDataPath() + "/hil/refactoring/extract/variable";
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

  protected void doTest() {
    doTest(true);
  }
}
