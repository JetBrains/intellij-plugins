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
