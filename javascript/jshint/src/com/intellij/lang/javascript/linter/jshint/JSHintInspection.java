package com.intellij.lang.javascript.linter.jshint;

import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInsight.daemon.impl.actions.SuppressByCommentFix;
import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.lang.javascript.inspections.JSInspectionSuppressor;
import com.intellij.lang.javascript.linter.JSLinterInspection;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JSHintInspection extends JSLinterInspection {

  @Override
  protected @NotNull JSHintExternalAnnotator getExternalAnnotatorForBatchInspection() {
    return JSHintExternalAnnotator.getInstanceForBatchInspection();
  }

  @Override
  public SuppressQuickFix @NotNull [] getBatchSuppressActions(@Nullable PsiElement element) {
    return new SuppressQuickFix[] {
      new JSHintSuppressByCommentFix(HighlightDisplayKey.find(getShortName()), JSInspectionSuppressor.getHolderClass(element))
    };
  }

  public static class JSHintSuppressByCommentFix extends SuppressByCommentFix {
    public JSHintSuppressByCommentFix(HighlightDisplayKey key, Class<? extends PsiElement> suppressionHolderClass) {
      super(key, suppressionHolderClass);
    }

    @Override
    public @NotNull String getText() {
      return JSHintBundle.message("jshint.suppress.text.suppress.for.line");
    }

    @Override
    protected void createSuppression(@NotNull Project project, @NotNull PsiElement element, @NotNull PsiElement container)
      throws IncorrectOperationException {
      if (!element.isValid()) {
        return;
      }
      PsiFile psiFile = element.getContainingFile();
      if (psiFile != null) {
        psiFile = psiFile.getOriginalFile();
      }
      if (psiFile == null || !psiFile.isValid()) {
        return;
      }
      final Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
      if (document == null) {
        return;
      }
      int lineNo = document.getLineNumber(element.getTextOffset());
      final int lineEndOffset = document.getLineEndOffset(lineNo);
      CommandProcessor.getInstance().executeCommand(project, () -> document.insertString(lineEndOffset, " // jshint ignore:line"), null, null);
    }
  }
}
