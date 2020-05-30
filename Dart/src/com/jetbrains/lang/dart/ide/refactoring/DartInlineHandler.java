/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
import com.intellij.lang.Language;
import com.intellij.lang.refactoring.InlineActionHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.inline.InlineOptionsDialog;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.analyzer.DartServerData.DartNavigationRegion;
import com.jetbrains.lang.dart.analyzer.DartServerData.DartNavigationTarget;
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.assists.DartSourceEditException;
import com.jetbrains.lang.dart.ide.refactoring.status.RefactoringStatus;
import org.dartlang.analysis.server.protocol.ElementKind;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DartInlineHandler extends InlineActionHandler {
  @Override
  public boolean canInlineElement(PsiElement element) {
    return false;
  }

  @Override
  public boolean canInlineElementInEditor(PsiElement element, Editor editor) {
    final InlineRefactoringContext context = findContext(editor);
    if (context == null) return false;
    final String kind = context.kind;
    return ElementKind.LOCAL_VARIABLE.equals(kind) ||
           ElementKind.METHOD.equals(kind) ||
           ElementKind.FUNCTION.equals(kind) ||
           ElementKind.GETTER.equals(kind) ||
           ElementKind.SETTER.equals(kind);
  }

  @Override
  public void inlineElement(@NotNull final Project project, @Nullable final Editor editor, PsiElement element) {
    final InlineRefactoringContext context = findContext(editor);
    if (context == null) {
      return;
    }
    // create refactoring
    final ServerRefactoring refactoring;
    if (ElementKind.LOCAL_VARIABLE.equals(context.kind)) {
      refactoring = new ServerInlineLocalRefactoring(project, context.virtualFile, context.offset, 0);
    }
    else {
      refactoring = new ServerInlineMethodRefactoring(project, context.virtualFile, context.offset, 0);
    }
    // validate initial status
    {
      final RefactoringStatus initialConditions = refactoring.checkInitialConditions();
      if (showMessageIfError(editor, initialConditions)) {
        return;
      }
    }
    // configure using dialog
    if (refactoring instanceof ServerInlineMethodRefactoring) {
      boolean dialogOK = new InlineMethodDialog(project, element, (ServerInlineMethodRefactoring)refactoring).showAndGet();
      if (!dialogOK) {
        return;
      }
    }
    // validate final status
    {
      final RefactoringStatus finalConditions = refactoring.checkFinalConditions();
      if (showMessageIfError(editor, finalConditions)) {
        return;
      }
    }
    // Apply the change.
    ApplicationManager.getApplication().runWriteAction(() -> {
      final SourceChange change = refactoring.getChange();
      assert change != null;
      try {
        AssistUtils.applySourceChange(project, change, false);
      }
      catch (DartSourceEditException e) {
        CommonRefactoringUtil.showErrorHint(project, editor, e.getMessage(), CommonBundle.getErrorTitle(), null);
      }
    });
  }

  @Override
  public boolean isEnabledForLanguage(Language l) {
    return l == DartLanguage.INSTANCE;
  }

  @Override
  public boolean isEnabledOnElement(PsiElement element, @Nullable Editor editor) {
    return canInlineElementInEditor(element, editor);
  }

  @Nullable
  static private InlineRefactoringContext findContext(@Nullable Editor editor) {
    if (editor == null) {
      return null;
    }
    // prepare project
    final Project project = editor.getProject();
    if (project == null) {
      return null;
    }
    // prepare files
    final Document document = editor.getDocument();
    final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
    if (psiFile == null) {
      return null;
    }
    final VirtualFile virtualFile = psiFile.getVirtualFile();
    // prepare navigation regions
    final int offset = editor.getCaretModel().getOffset();
    final List<DartNavigationRegion> navigationRegions = DartAnalysisServerService.getInstance(project).getNavigation(virtualFile);
    // find the navigation region
    for (DartNavigationRegion region : navigationRegions) {
      if (region.getOffset() <= offset && offset <= region.getOffset() + region.getLength()) {
        final List<DartNavigationTarget> targets = region.getTargets();
        final String kind = targets.get(0).getKind();
        return new InlineRefactoringContext(virtualFile, offset, kind);
      }
    }
    // fail
    return null;
  }

  private static boolean showMessageIfError(@Nullable Editor editor, @Nullable final RefactoringStatus status) {
    if (status == null) {
      return true;
    }
    if (status.hasError()) {
      final String message = status.getMessage();
      assert message != null;
      if (editor != null) {
        CommonRefactoringUtil.showErrorHint(editor.getProject(), editor, message, CommonBundle.getErrorTitle(), null);
      }
      return true;
    }
    return false;
  }
}

class InlineRefactoringContext {
  final VirtualFile virtualFile;
  final String kind;
  final int offset;

  InlineRefactoringContext(VirtualFile virtualFile, int offset, String kind) {
    this.virtualFile = virtualFile;
    this.kind = kind;
    this.offset = offset;
  }
}

class InlineMethodDialog extends InlineOptionsDialog {
  private final ServerInlineMethodRefactoring refactoring;

  protected InlineMethodDialog(Project project, PsiElement element, ServerInlineMethodRefactoring refactoring) {
    super(project, true, element);
    this.refactoring = refactoring;
    setTitle(getRefactoringName());
    myInvokedOnReference = !refactoring.isDeclaration();
    init();
  }

  @Override
  protected void doAction() {
    if (!isInlineThisOnly()) {
      refactoring.setInlineAll(true);
      refactoring.setDeleteSource(true);
    }
    close(DialogWrapper.OK_EXIT_CODE);
  }

  @Override
  protected String getBorderTitle() {
    return RefactoringBundle.message("inline.method.border.title"); // not used actually
  }

  @Override
  protected String getInlineAllText() {
    return "Inline all references and remove the method";
  }

  @Override
  protected String getInlineThisText() {
    return "Inline this reference and leave the method";
  }

  @Override
  protected String getNameLabelText() {
    return "Method " + refactoring.getFullName();
  }

  @Override
  protected boolean hasPreviewButton() {
    return false;
  }

  @Override
  protected boolean isInlineThis() {
    return !refactoring.isDeclaration();
  }

  public static String getRefactoringName() {
    return RefactoringBundle.message("inline.method.title");
  }
}
