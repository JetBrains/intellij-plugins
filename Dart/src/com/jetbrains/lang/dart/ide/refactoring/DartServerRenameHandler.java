/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.rename.RenameHandler;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.ide.refactoring.status.RefactoringStatus;
import org.dartlang.analysis.server.protocol.ElementKind;
import org.jetbrains.annotations.NotNull;

// todo implement ContextAwareActionHandler?
public class DartServerRenameHandler implements RenameHandler, TitledHandler {
  @Override
  public String getActionTitle() {
    return "Dart Rename Refactoring";
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file, DataContext context) {
    if (editor == null) {
      return;
    }

    PsiElement element = getElementForDataContext(context);
    InlineRefactoringContext refactoringContext = DartInlineHandler.findContext(editor);
    final PsiNameIdentifierOwner nameOwner = element instanceof PsiNameIdentifierOwner ?
                                             (PsiNameIdentifierOwner) element :
                                             PsiTreeUtil.getParentOfType(element, PsiNameIdentifierOwner.class, true);
    if (nameOwner == null ||
        refactoringContext == null ||
        !refactoringContext.kind.equals(ElementKind.LOCAL_VARIABLE) ||
        !editor.getSettings().isVariableInplaceRenameEnabled()) {
      showRenameDialog(project, editor, context);
      return;
    }

    new DartVariableInplaceRenamer(nameOwner, editor).performInplaceRename();
  }

  @Override
  public void invoke(@NotNull Project project, @NotNull PsiElement[] elements, DataContext context) {
    // Dart file rename is not handled using server yet
  }

  @Override
  public boolean isAvailableOnDataContext(@NotNull final DataContext dataContext) {
    if (getElementForDataContext(dataContext) != null) {
      return true;
    }

    // in case of comment (that also may contain reference that is valid to rename) psiElement is null
    final Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
    final PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(dataContext);
    final PsiElement elementAtOffset = psiFile == null ? null : psiFile.findElementAt(editor.getCaretModel().getOffset());
    return elementAtOffset != null && elementAtOffset.getLanguage() == DartLanguage.INSTANCE;
  }

  private static PsiElement getElementForDataContext(DataContext dataContext) {
    final Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
    if (editor == null) return null;

    final VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);
    if (!DartAnalysisServerService.isLocalAnalyzableFile(file)) return null;

    final PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
    if (psiElement == null || psiElement.getLanguage() != DartLanguage.INSTANCE || psiElement instanceof PsiFile) {
      return null;
    }

    return psiElement;
  }

  @Override
  public boolean isRenaming(@NotNull DataContext dataContext) {
    return isAvailableOnDataContext(dataContext);
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

