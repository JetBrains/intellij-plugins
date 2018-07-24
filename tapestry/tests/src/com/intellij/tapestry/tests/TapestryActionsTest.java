package com.intellij.tapestry.tests;

import com.intellij.codeInsight.actions.MultiCaretCodeInsightAction;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.tapestry.intellij.actions.navigation.ClassTemplateNavigation;
import com.intellij.testFramework.EditorTestUtil;

/**
 * @author Alexey Chmutov
 */
public class TapestryActionsTest extends TapestryBaseTestCase {
  @Override
  protected String getBasePath() {
    return "actions/";
  }

  public void testNavigateToTemplate() {
    VirtualFile tmlFile = initByComponent(true);
    final PsiFile psiFile = myFixture.getPsiManager().findFile(tmlFile);
    
    assertNotNull("No PsiFile for template", psiFile);
    VirtualFile fileFoundByAction =
      ClassTemplateNavigation.findNavigationTarget(psiFile, myFixture.getModule(), "Class <-> Template Navigation");
    assertNotNull("Java file not found by the action", fileFoundByAction);
    assertEquals(getElementClassFileName(), fileFoundByAction.getName());
  }

  public void testNavigateToClass() {
    VirtualFile javaFile = initByComponent(false);
    final PsiFile psiFile = myFixture.getPsiManager().findFile(javaFile);

    assertNotNull("No PsiFile for java file", psiFile);
    VirtualFile fileFoundByAction =
      ClassTemplateNavigation.findNavigationTarget(psiFile, myFixture.getModule(), "Class <-> Template Navigation");
    assertNotNull("Template file not found by the action", fileFoundByAction);
    assertEquals(getElementTemplateFileName(), fileFoundByAction.getName());
  }

  public void testNavigateToTemplateFromSuper() {
    VirtualFile pageTemplates = addPageToProject("StartPage");
    VirtualFile javaFile = initByComponent(false);

    final PsiFile psiFile = myFixture.getPsiManager().findFile(javaFile);
    assertNotNull("No PsiFile for java file", psiFile);
    VirtualFile fileFoundByAction =
      ClassTemplateNavigation.findNavigationTarget(psiFile, myFixture.getModule(), "Class <-> Template Navigation");
    assertNotNull("Template file not found by the action", fileFoundByAction);
    assertEquals(pageTemplates.getName(), fileFoundByAction.getName());
  }

  public void testCommentBlock() {
    doTest(IdeActions.ACTION_COMMENT_BLOCK);
  }

  public void testCommentLine() {
    doTest(IdeActions.ACTION_COMMENT_LINE);
  }

  public void testUncommentBlock() {
    doTest(IdeActions.ACTION_COMMENT_BLOCK);
  }

  public void testUncommentLine() {
    doTest(IdeActions.ACTION_COMMENT_LINE);
  }
  
  public void testInsertPairingRBrace() {
    initByComponent(true);
    EditorTestUtil.performTypingAction(myFixture.getEditor(), '{');
    checkResultByFile();
  }
  
  public void testInsertPairingRBrace2() {
    initByComponent(true);
    EditorTestUtil.performTypingAction(myFixture.getEditor(), '{');
    checkResultByFile();
  }

  private void doTest(final String actionId) {
    initByComponent(true);
    MultiCaretCodeInsightAction action = (MultiCaretCodeInsightAction)ActionManager.getInstance().getAction(actionId);
    action.actionPerformedImpl(myModule.getProject(), myFixture.getEditor());
    checkResultByFile();
  }
}
