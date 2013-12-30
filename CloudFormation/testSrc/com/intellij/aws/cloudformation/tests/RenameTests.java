package com.intellij.aws.cloudformation.tests;

import com.intellij.codeInsight.TargetElementUtilBase;
import com.intellij.codeInsight.daemon.DaemonAnalyzerTestCase;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.rename.RenameProcessor;

public class RenameTests extends DaemonAnalyzerTestCase {
  public void testSimpleEntity() throws Exception {
    configureByFile("simpleEntity.template", null);
    PsiElement element = TargetElementUtilBase.findTargetElement(
      myEditor, TargetElementUtilBase.ELEMENT_NAME_ACCEPTED | TargetElementUtilBase.REFERENCED_ELEMENT_ACCEPTED);
    new RenameProcessor(myProject, element, "NEW_NAME", false, false).run();
    checkResultByFile("simpleEntity.after.template");
  }

  @Override
  protected String getTestDataPath() {
    return TestUtil.getTestDataPath("/rename/");
  }
}
