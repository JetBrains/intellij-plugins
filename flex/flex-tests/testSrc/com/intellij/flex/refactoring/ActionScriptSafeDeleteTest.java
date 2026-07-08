// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.refactoring;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.ide.DataManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.safeDelete.SafeDeleteHandler;
import com.intellij.refactoring.safeDelete.SafeDeleteProcessor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class ActionScriptSafeDeleteTest extends BasePlatformTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.setTestDataPath(FlexTestUtils.getTestDataPath("/safeDelete/"));
  }

  public void testSafeDelete5() {
    doTest("", "js2");
  }

  public void testSafeDelete6() {
    doTest("_2", "js2");
    doTest("_3", "js2");
  }

  public void testDeleteClass() {
    doTest("_1", "as", false);
    doTest("_2", "as", false);
    doTest("_3", "as", true);
  }

  private void doTest(String suffix, String ext) {
    doTest(suffix, ext, false);
  }

  private void doTest(String suffix, String ext, boolean checkFileDeleted) {
    final String testName = getTestName(true);
    PsiFile psiFile = myFixture.configureByFile(testName + suffix + "." + ext);
    final PsiElement psiElement = myFixture.getElementAtCaret();
    assertNotNull(psiElement);
    assertTrue("Safe delete should be available", SafeDeleteProcessor.validElement(psiElement));

    new SafeDeleteHandler().invoke(
      myFixture.getProject(),
      new PsiElement[]{psiElement},
      DataManager.getInstance().getDataContext()
    );

    if (checkFileDeleted) {
      assertFalse(psiFile.getName() + " not deleted", psiFile.isValid());
    }
    else {
      myFixture.checkResultByFile(testName + suffix + "_after." + ext);
    }
  }
}
