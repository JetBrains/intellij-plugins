package org.angularjs.findUsages;

import com.intellij.lang.javascript.findUsages.JavaScriptFindUsagesHandlerFactory;
import com.intellij.psi.PsiElement;
import org.angularjs.codeInsight.DirectiveUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSFindUsagesHandlerFactory extends JavaScriptFindUsagesHandlerFactory {
  @Override
  public boolean canFindUsages(@NotNull PsiElement element) {
    return DirectiveUtil.getDirective(element) != null;
  }
}
