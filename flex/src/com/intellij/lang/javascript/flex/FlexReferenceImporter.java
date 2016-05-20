package com.intellij.lang.javascript.flex;

import com.intellij.codeInsight.daemon.ReferenceImporter;
import com.intellij.codeInsight.daemon.impl.CollectHighlightsUtil;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPolyVariantReference;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Maxim.Mossienko
 *         Date: Apr 14, 2008
 *         Time: 11:43:22 PM
 */
public class FlexReferenceImporter implements ReferenceImporter {
  @Override
  public boolean autoImportReferenceAtCursor(@NotNull final Editor editor, @NotNull final PsiFile file) {
    return doAutoImportReferenceAt(editor, file, editor.getCaretModel().getOffset());
  }

  @Override
  public boolean autoImportReferenceAt(@NotNull final Editor editor, @NotNull final PsiFile file, final int offset) {
    return doAutoImportReferenceAt(editor, file, offset);
  }

  private static boolean doAutoImportReferenceAt(final Editor editor,
                                                 final PsiFile file,
                                                 final int offset) {
    if (!ActionScriptAutoImportOptionsProvider.isAddUnambiguousImportsOnTheFly()) return false;
    if (!(file instanceof JSFile) || file.getLanguage() != JavaScriptSupportLoader.ECMA_SCRIPT_L4) return false;

    Document document = editor.getDocument();
    int lineNumber = document.getLineNumber(offset);
    int startOffset = document.getLineStartOffset(lineNumber);
    int endOffset = document.getLineEndOffset(lineNumber);

    List<PsiElement> elements = CollectHighlightsUtil.getElementsInRange(file, startOffset, endOffset);
    for (PsiElement element : elements) {
      if (element instanceof JSReferenceExpression && ((JSReferenceExpression)element).getQualifier() == null) {
        if (((JSReferenceExpression)element).multiResolve(false).length == 0) {
          new AddImportECMAScriptClassOrFunctionAction(editor, (PsiPolyVariantReference)element, true).execute();
          return true;
        }
      }
    }
    return false;
  }
}
