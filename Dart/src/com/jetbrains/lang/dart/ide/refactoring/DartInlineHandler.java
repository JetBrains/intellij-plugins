// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
import com.jetbrains.lang.dart.DartBundle;
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

public final class DartInlineHandler extends InlineActionHandler {
  @Override
  public boolean canInlineElement(PsiElement element) {
    return false;
  }

  @Override
  public boolean canInlineElementInEditor(PsiElement element, Editor editor) {
    if (editor == null) return false;
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
    if (editor == null) return;

    final InlineRefactoringContext context = findContext(editor);
    if (context == null) return;

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
      if (showMessageIfError(project, editor, initialConditions)) {
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
      if (showMessageIfError(project, editor, finalConditions)) {
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
  static private InlineRefactoringContext findContext(@NotNull Editor editor) {
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

  public static boolean showMessageIfError(@NotNull Project project, @NotNull Editor editor, @Nullable final RefactoringStatus status) {
    if (status == null) return true;

    if (status.hasError()) {
      final String message = status.getMessage();
      assert message != null;
      CommonRefactoringUtil.showErrorHint(project, editor, message, CommonBundle.getErrorTitle(), null);
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
    setTitle(RefactoringBundle.message("inline.method.title"));
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
    return DartBundle.message("radio.inline.all.references.remove.method");
  }

  @Override
  protected String getInlineThisText() {
    return DartBundle.message("radio.inline.this.reference.leave.method");
  }

  @Override
  protected String getNameLabelText() {
    return DartBundle.message("label.method.0", refactoring.getFullName());
  }

  @Override
  protected boolean hasPreviewButton() {
    return false;
  }

  @Override
  protected boolean isInlineThis() {
    return !refactoring.isDeclaration();
  }
}
