// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.rename;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.java.resolve.BaseCucumberJavaResolveTest;

public class CucumberStepRenameTest extends BaseCucumberJavaResolveTest {

  public void testStepRename_01() {
    myFixture.copyDirectoryToProject("before", "");

    VirtualFile featureFile = myFixture.findFileInTempDir("withdraw.feature");
    VirtualFile stepDefFile = myFixture.findFileInTempDir("WithdrawSteps.java");
    PsiFile featurePsiFile = PsiManager.getInstance(myFixture.getProject()).findFile(featureFile);
    PsiFile stepDefPsiFile = PsiManager.getInstance(myFixture.getProject()).findFile(stepDefFile);
    myFixture.configureFromExistingVirtualFile(featureFile);

    checkReference("I am <caret>angry", "i_am_angry");

    PsiReference stepReference = myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());


    // Another approach tried:
    // myFixture.testRenameUsingHandler("after/withdraw.feature", "I am not happy at all");

    myFixture.renameElement(stepReference.getElement(), "I am not happy at all", false, false);

    assertTrue("The new name should be used in feature file", featurePsiFile.getText().contains("not happy at all"));
    
    // TODO: IDEA-107390
    // assertFalse("All step in feature file should have been renamed", featurePsiFile.getText().contains("angry"));
    // assertFalse("The step in step definition file should have been renamed", stepDefPsiFile.getText().contains("angry"));
  }

  @Override
  protected String getRelatedTestDataPath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "renameStep";
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber7ProjectDescriptor();
  }
}
