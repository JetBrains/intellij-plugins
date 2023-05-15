// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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
