// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.codeinsight;

import com.intellij.testFramework.InspectionsKt;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.intellij.terraform.TfTestUtils;
import org.intellij.terraform.hil.inspection.HILUnresolvedReferenceInspection;
import org.jetbrains.annotations.NotNull;

public class AddVariableTest extends BasePlatformTestCase {
  @NotNull
  @Override
  protected String getTestDataPath() {
    return TfTestUtils.getTestDataPath() + "/hil/codeinsight/add-variable/";
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    InspectionsKt.enableInspectionTools(getProject(), getTestRootDisposable(), new HILUnresolvedReferenceInspection());
  }

  public void testSimpleVariable() {
    doSingleTest();
  }

  private void doSingleTest() {
    myFixture.configureByFile("before" + getTestName(false) + ".tf");
    var intention = myFixture.findSingleIntention("Add variable 'foobar'");
    myFixture.launchAction(intention);
    myFixture.checkResultByFile("after" + getTestName(false) + ".tf");
  }
}
