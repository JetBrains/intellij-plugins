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
package org.intellij.terraform.hil.codeinsight;

import com.intellij.codeInsight.daemon.quickFix.LightQuickFixTestCase;
import org.intellij.terraform.TerraformTestUtils;
import org.intellij.terraform.hil.inspection.HILUnresolvedReferenceInspection;
import org.jetbrains.annotations.NotNull;

public class AddVariableTest extends LightQuickFixTestCase {

  @NotNull
  @Override
  protected String getTestDataPath() {
    return TerraformTestUtils.getTestDataPath() + "/hil/codeinsight/add-variable/";
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    enableInspectionTools(new HILUnresolvedReferenceInspection());
  }

  public void testSimpleVariable() throws Exception {
    doSingleTest();
  }

  private void doSingleTest() {
    doSingleTest(getTestName(false) + ".tf");
  }
}
