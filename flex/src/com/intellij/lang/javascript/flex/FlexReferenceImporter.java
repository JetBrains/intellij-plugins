package com.intellij.lang.javascript.flex;

import com.intellij.codeInsight.daemon.ReferenceImporter;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.resolve.JSImportHandlingUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

/**
 * @author Maxim.Mossienko
 */
public class FlexReferenceImporter implements ReferenceImporter {
  @Override
  public BooleanSupplier computeAutoImportAtOffset(@NotNull Editor editor, @NotNull PsiFile file, int offset, boolean allowCaretNearReference) {
    JSReferenceExpression expression = JSImportHandlingUtil.findUnresolvedReferenceExpression(editor, file, offset);
    if (expression != null) {
      return () -> new AddImportECMAScriptClassOrFunctionAction(editor, expression, true).execute();
    }
    return null;
  }

  @Override
  public boolean isAddUnambiguousImportsOnTheFlyEnabled(@NotNull PsiFile file) {
    return file instanceof JSFile &&
           file.getLanguage() == JavaScriptSupportLoader.ECMA_SCRIPT_L4 &&
           ActionScriptAutoImportOptionsProvider.isAddUnambiguousImportsOnTheFly();
  }
}
