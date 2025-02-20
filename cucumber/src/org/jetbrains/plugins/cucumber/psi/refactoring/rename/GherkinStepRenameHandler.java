// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi.refactoring.rename;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.rename.PsiElementRenameHandler;
import com.intellij.refactoring.rename.RenameDialog;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.psi.*;

public final class GherkinStepRenameHandler extends PsiElementRenameHandler {
  @Override
  public boolean isAvailableOnDataContext(@NotNull DataContext dataContext) {
    PsiElement element = getGherkinStep(dataContext);
    return element != null;
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file, @NotNull DataContext dataContext) {
    final GherkinStep step = getGherkinStep(dataContext);
    if (step == null) {
      return;
    }

    if (!step.isRenameAllowed(null)) {
      CommonRefactoringUtil.showErrorHint(project, editor, CucumberBundle.message("cucumber.refactor.rename.disabled"), "", null);
      return;
    }


    final CucumberStepRenameDialog dialog = new CucumberStepRenameDialog(project, step, null, editor);
    Disposer.register(project, dialog.getDisposable());
    RenameDialog.showRenameDialog(dataContext, dialog);
  }

  public @Nullable GherkinStep getGherkinStep(final @Nullable DataContext context) {
    PsiElement element = null;
    if (context == null) return null;
    final Editor editor = CommonDataKeys.EDITOR.getData(context);
    if (editor != null) {
      final PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(context);
      if (psiFile != null) {
        element = psiFile.findElementAt(editor.getCaretModel().getOffset());
      }
    }
    if (element == null) {
      element = CommonDataKeys.PSI_ELEMENT.getData(context);
    }

    GherkinTable table = PsiTreeUtil.getParentOfType(element, GherkinTable.class);
    if (table != null) {
      // There is GherkinInplaceRenameHandler that should handle rename inside the GherkinTable
      return null;
    }
    element = PsiTreeUtil.getParentOfType(element, GherkinPsiElement.class, false);
    if (element instanceof GherkinStepParameter || element instanceof GherkinTableCell) {
      return null;
    }
    return element instanceof GherkinStep ? (GherkinStep)element : PsiTreeUtil.getParentOfType(element, GherkinStep.class);
  }
}
