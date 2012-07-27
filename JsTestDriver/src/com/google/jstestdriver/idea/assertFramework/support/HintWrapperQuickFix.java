package com.google.jstestdriver.idea.assertFramework.support;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.hint.QuestionAction;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.HintAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class HintWrapperQuickFix implements LocalQuickFix, HintAction {

  private final PsiElement myPsiElement;
  private final TextRange myRangeInElement;
  private final IntentionAction myDelegate;

  public HintWrapperQuickFix(@NotNull PsiElement psiElement,
                             @NotNull TextRange rangeInElement,
                             @NotNull IntentionAction delegate) {
    myPsiElement = psiElement;
    myRangeInElement = rangeInElement;
    myDelegate = delegate;
  }

  @Override
  public boolean showHint(final Editor editor) {
    if (HintManager.getInstance().hasShownHintsThatWillHideByOtherHint(true)) {
      return false;
    }
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      return false;
    }
    String message = getText() + "? " + KeymapUtil.getFirstKeyboardShortcutText(
      ActionManager.getInstance().getAction(IdeActions.ACTION_SHOW_INTENTION_ACTIONS)
    );

    if (!myPsiElement.isValid()) {
      return false;
    }

    TextRange hintRange = calcRange();

    HintManager.getInstance().showQuestionHint(
      editor,
      message,
      hintRange.getStartOffset(),
      hintRange.getEndOffset(),
      new QuestionAction() {
        @Override
        public boolean execute() {
          myDelegate.invoke(myPsiElement.getProject(), editor, myPsiElement.getContainingFile());
          return true;
        }
      }
    );
    return true;
  }

  @NotNull
  private TextRange calcRange() {
    TextRange global = myPsiElement.getTextRange();
    return new TextRange(global.getStartOffset() + myRangeInElement.getStartOffset(),
                         global.getStartOffset() + myRangeInElement.getEndOffset());
  }

  @NotNull
  @Override
  public String getText() {
    return myDelegate.getText();
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    return myPsiElement.isValid();
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
    myDelegate.invoke(project, editor, file);
  }

  @Override
  public boolean startInWriteAction() {
    return myDelegate.startInWriteAction();
  }

  @NotNull
  @Override
  public String getName() {
    return myDelegate.getText();
  }

  @NotNull
  @Override
  public String getFamilyName() {
    return myDelegate.getFamilyName();
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    //noinspection NullableProblems
    myDelegate.invoke(project, null, myPsiElement.getContainingFile());
  }
}
