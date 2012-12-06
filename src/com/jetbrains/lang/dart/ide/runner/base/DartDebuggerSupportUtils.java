package com.jetbrains.lang.dart.ide.runner.base;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.jetbrains.lang.dart.util.DartElementGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author: Fedor.Korotkov
 */
public class DartDebuggerSupportUtils {
  @Nullable
  public static Document createDocument(@NotNull final String text,
                                        @NotNull final Project project,
                                        @Nullable final VirtualFile contextVirtualFile,
                                        final int contextOffset) {

    PsiElement context = null;
    if (contextVirtualFile != null) {
      context = getContextElement(contextVirtualFile, contextOffset, project);
    }
    final PsiFile codeFragment = DartElementGenerator.createExpressionCodeFragment(project, text, context, true);
    return PsiDocumentManager.getInstance(project).getDocument(codeFragment);
  }

  @Nullable
  public static PsiElement getContextElement(VirtualFile virtualFile, int offset, final @NotNull Project project) {
    Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
    PsiFile file = PsiManager.getInstance(project).findFile(virtualFile);
    if (file == null || document == null) {
      return null;
    }

    if (offset < 0) offset = 0;
    if (offset > document.getTextLength()) offset = document.getTextLength();
    int startOffset = offset;

    int lineEndOffset = document.getLineEndOffset(document.getLineNumber(offset));
    PsiElement result = null;
    do {
      PsiElement element = file.findElementAt(offset);
      if (!(element instanceof PsiWhiteSpace) && !(element instanceof PsiComment)) {
        result = element;
        break;
      }

      offset = element.getTextRange().getEndOffset() + 1;
    }
    while (offset < lineEndOffset);

    if (result == null) {
      result = file.findElementAt(startOffset);
    }
    return result;
  }
}
