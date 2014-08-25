package com.intellij.lang.javascript.flex;

import com.intellij.codeInsight.daemon.ReferenceImporter;
import com.intellij.codeInsight.daemon.impl.CollectHighlightsUtil;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.UndoConfirmationPolicy;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.DocCommandGroupId;
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
    return doAutoImportReferenceAt(editor, file, editor.getCaretModel().getOffset(), false);
  }

  @Override
  public boolean autoImportReferenceAt(@NotNull final Editor editor, @NotNull final PsiFile file, final int offset) {
    // this method is only called from com.intellij.codeInsight.daemon.impl.ShowAutoImportPass.importUnambiguousImports()
    // when 'Add unambiguous imports on the fly' option is enabled
    return doAutoImportReferenceAt(editor, file, offset, true);
  }

  private static boolean doAutoImportReferenceAt(final Editor editor,
                                                 final PsiFile file,
                                                 final int offset,
                                                 final boolean unambiguousOnTheFly) {
    if (!(file instanceof JSFile) || file.getLanguage() != JavaScriptSupportLoader.ECMA_SCRIPT_L4) return false;

    Document document = editor.getDocument();
    int lineNumber = document.getLineNumber(offset);
    int startOffset = document.getLineStartOffset(lineNumber);
    int endOffset = document.getLineEndOffset(lineNumber);

    List<PsiElement> elements = CollectHighlightsUtil.getElementsInRange(file, startOffset, endOffset);
    for (PsiElement element : elements) {
      if (element instanceof JSReferenceExpression && ((JSReferenceExpression)element).getQualifier() == null) {
        if (((JSReferenceExpression)element).multiResolve(false).length == 0) {
          runImport(editor, unambiguousOnTheFly, (PsiPolyVariantReference)element);
          return true;
        }
      }
    }
    return false;
  }

  private static void runImport(final Editor editor, final boolean unambiguousOnTheFly, final PsiPolyVariantReference element) {
    Document doc = editor.getDocument();
    DocCommandGroupId group = DocCommandGroupId.noneGroupId(doc);
    CommandProcessor.getInstance().executeCommand(editor.getProject(), new Runnable() {
      @Override
      public void run() {
        new AddImportECMAScriptClassOrFunctionAction(editor, element, unambiguousOnTheFly).execute();
      }
    }, "import", group, UndoConfirmationPolicy.DEFAULT, doc);
  }
}
