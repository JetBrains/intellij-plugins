package com.jetbrains.lang.dart.validation.fixes;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class FixAndIntentionAction implements LocalQuickFix, IntentionAction {
  @Nullable
  protected PsiElement myElement = null;

  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    applyFix(project, descriptor.getPsiElement(), null);
  }

  @NotNull
  public String getFamilyName() {
    return getName();
  }

  @NotNull
  public String getText() {
    return getName();
  }

  public boolean startInWriteAction() {
    return false;
  }

  public void setElement(@Nullable PsiElement element) {
    myElement = element;
  }

  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    if (myElement == null) return false;
    return isAvailable(project, myElement, editor, file);
  }

  protected boolean isAvailable(Project project, @Nullable PsiElement element, Editor editor, PsiFile file) {
    return true;
  }

  public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
    if (!FileModificationService.getInstance().prepareFileForWrite(file)) return;
    if (myElement == null) return;
    applyFix(project, myElement, editor);
  }

  protected abstract void applyFix(Project project, @NotNull PsiElement psiElement, @Nullable Editor editor);
}