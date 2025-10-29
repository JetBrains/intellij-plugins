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
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.psi.GherkinPsiElement;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.psi.GherkinStepParameter;
import org.jetbrains.plugins.cucumber.psi.GherkinTableCell;

/**
 * Handles renaming {@link GherkinStep} PSI elements.
 *
 * @see <a href="https://cucumber.io/docs/gherkin/reference#steps">Gherkin Reference | Steps</a>
 */
@NotNullByDefault
public final class GherkinStepRenameHandler extends PsiElementRenameHandler {
  @Override
  public boolean isAvailableOnDataContext(DataContext dataContext) {
    PsiElement element = getGherkinStep(dataContext);
    return element != null;
  }

  @Override
  public void invoke(Project project, Editor editor, PsiFile file, DataContext dataContext) {
    final GherkinStep step = getGherkinStep(dataContext);
    if (step == null) {
      return;
    }

    if (!step.isRenameAllowed(null)) {
      CommonRefactoringUtil.showErrorHint(project, editor, CucumberBundle.message("cucumber.refactor.rename.disabled"), "", null);
      return;
    }

    final GherkinStepRenameDialog dialog = new GherkinStepRenameDialog(project, step, null, editor);
    Disposer.register(project, dialog.getDisposable()); // FIXME(bartekpacia): Dispose the dialog in the correct way
    RenameDialog.showRenameDialog(dataContext, dialog);
  }

  private static @Nullable GherkinStep getGherkinStep(@Nullable DataContext context) {
    PsiElement element = null;
    if (context == null) {
      return null;
    }
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
    element = PsiTreeUtil.getParentOfType(element, GherkinPsiElement.class, false);
    if (element instanceof GherkinStepParameter || element instanceof GherkinTableCell) {
      // This handler cares only about renames of steps, not step parameters.
      return null;
    }
    return PsiTreeUtil.getParentOfType(element, GherkinStep.class, false);
  }
}
