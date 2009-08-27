package com.intellij.tapestry.tests;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.tapestry.intellij.actions.navigation.ClassTemplateNavigation;

/**
 * @author Alexey Chmutov
 *         Date: Jul 16, 2009
 *         Time: 6:11:55 PM
 */
public class TapestryActionsTest extends TapestryBaseTestCase {
  @Override
  protected String getBasePath() {
    return "actions/";
  }

  public void testNavigateToTemplate() throws Throwable {
    VirtualFile tmlFile = initByComponent(true);

    final PsiFile psiFile = myFixture.getPsiManager().findFile(tmlFile);
    assertNotNull("No PsiFile for template", psiFile);
    VirtualFile fileFoundByAction =
        ClassTemplateNavigation.findNavigationTarget(psiFile, myFixture.getModule(), "Class <-> Template Navigation");
    assertNotNull("Java file not found by the action", fileFoundByAction);
    assertEquals(getElementClassFileName(), fileFoundByAction.getName());
  }

  public void testNavigateToClass() throws Throwable {
    VirtualFile javaFile = initByComponent(false);
    final PsiFile psiFile = myFixture.getPsiManager().findFile(javaFile);
    assertNotNull("No PsiFile for java file", psiFile);
    VirtualFile fileFoundByAction =
        ClassTemplateNavigation.findNavigationTarget(psiFile, myFixture.getModule(), "Class <-> Template Navigation");
    assertNotNull("Template file not found by the action", fileFoundByAction);
    assertEquals(getElementTemplateFileName(), fileFoundByAction.getName());
  }

}
