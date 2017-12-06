package com.intellij.lang.javascript.flex;

import com.intellij.codeInsight.daemon.ReferenceImporter;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.resolve.JSImportHandlingUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim.Mossienko
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

    JSReferenceExpression expression = JSImportHandlingUtil.findUnresolvedReferenceExpression(editor, file, offset);
    if (expression != null) {
      new AddImportECMAScriptClassOrFunctionAction(editor, expression, true).execute();
      return true;
    }
    return false;
  }
}
