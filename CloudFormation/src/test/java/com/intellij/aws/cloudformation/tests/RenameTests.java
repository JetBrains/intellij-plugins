package com.intellij.aws.cloudformation.tests;

import com.intellij.codeInsight.TargetElementUtilBase;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.rename.RenameProcessor;
import com.intellij.testFramework.LightCodeInsightTestCase;
import org.jetbrains.annotations.NotNull;

public class RenameTests extends LightCodeInsightTestCase {
  public void testSimpleEntity() throws Exception {
    configureByFile("simpleEntity.template");
    PsiElement element = TargetElementUtilBase.findTargetElement(
        myEditor, TargetElementUtilBase.ELEMENT_NAME_ACCEPTED | TargetElementUtilBase.REFERENCED_ELEMENT_ACCEPTED);
    new RenameProcessor(getProject(), element, "NEW_NAME", false, false).run();
    checkResultByFile("simpleEntity.after.template");
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return TestUtil.getTestDataPath("rename");
  }
}
