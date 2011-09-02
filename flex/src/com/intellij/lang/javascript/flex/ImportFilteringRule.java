package com.intellij.lang.javascript.flex;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.ecmal4.JSImportStatement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.usages.Usage;
import com.intellij.usages.rules.PsiElementUsage;
import org.jetbrains.annotations.NotNull;

/**
 * User: Maxim.Mossienko
 * Date: 11.03.2010
 * Time: 19:30:53
 */
public class ImportFilteringRule extends com.intellij.usages.rules.ImportFilteringRule {
  public boolean isVisible(@NotNull Usage usage) {
    if (usage instanceof PsiElementUsage) {
      final PsiElement psiElement = ((PsiElementUsage)usage).getElement();
      final PsiFile containingFile = psiElement.getContainingFile();
      
      if (containingFile.getLanguage().is(JavaScriptSupportLoader.ECMA_SCRIPT_L4)) {
        return PsiTreeUtil.getParentOfType(psiElement, JSImportStatement.class, true) == null;
      }
    }
    return true;
  }
}
