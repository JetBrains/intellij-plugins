// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.refactoring;

import com.intellij.CommonBundle;
import com.intellij.ide.TitledHandler;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.refactoring.rename.RenameHandler;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.ide.refactoring.status.RefactoringStatus;
import org.jetbrains.annotations.NotNull;

// todo implement ContextAwareActionHandler?
public final class DartServerRenameHandler implements RenameHandler, TitledHandler {
  @Override
  public String getActionTitle() {
    return DartBundle.message("action.title.dart.rename.refactoring");
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file, DataContext context) {
    showRenameDialog(project, editor, context);
  }

  @Override
  public void invoke(@NotNull Project project, PsiElement @NotNull [] elements, DataContext context) {
    // Dart file rename is not handled using server yet
  }

  @Override
  public boolean isAvailableOnDataContext(@NotNull final DataContext dataContext) {
    final Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
    if (editor == null) return false;

    final VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);
    if (!DartAnalysisServerService.isLocalAnalyzableFile(file)) return false;

    final PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
    if (psiElement != null) {
      if (psiElement.getUseScope() instanceof LocalSearchScope) {
        // Standard VariableInplaceRenameHandler or PsiElementRenameHandler will do the trick (based on existing navigation data from Analysis Server)
        return false;
      }
      return psiElement.getLanguage() == DartLanguage.INSTANCE && !(psiElement instanceof PsiFile);
    }

    // in case of comment (that also may contain reference that is valid to rename) psiElement is null
    final PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(dataContext);
    final PsiElement elementAtOffset = psiFile == null ? null : psiFile.findElementAt(editor.getCaretModel().getOffset());

    return elementAtOffset != null && elementAtOffset.getLanguage() == DartLanguage.INSTANCE;
  }

  private static void showRenameDialog(@NotNull Project project, @NotNull Editor editor, DataContext context) {
    final PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(context);
    final VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(context);
    if (psiFile == null || virtualFile == null) {
      return;
    }
    // Prepare the offset in the editor or of the selected element.
    final int offset;
    {
      final Caret caret = CommonDataKeys.CARET.getData(context);
      final PsiElement element = CommonDataKeys.PSI_ELEMENT.getData(context);
      if (caret != null) {
        offset = caret.getOffset();
      }
      else if (element != null) {
        offset = element.getTextOffset();
      }
      else {
        return;
      }
    }
    // Create the refactoring.
    final ServerRenameRefactoring refactoring = new ServerRenameRefactoring(project, virtualFile, offset, 0);
    // Validate initial status.
    {
      final RefactoringStatus initialStatus = refactoring.checkInitialConditions();
      if (initialStatus == null) {
        return;
      }
      if (initialStatus.hasError()) {
        final String message = initialStatus.getMessage();
        assert message != null;
        CommonRefactoringUtil.showErrorHint(project, editor, message, CommonBundle.getErrorTitle(), null);
        return;
      }
    }
    // Show the rename dialog.
    new DartRenameDialog(project, editor, refactoring).show();
  }
}

