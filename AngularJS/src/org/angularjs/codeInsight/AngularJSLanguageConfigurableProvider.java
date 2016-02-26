package org.angularjs.codeInsight;

import com.intellij.lang.javascript.psi.JSInheritedLanguagesConfigurableProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSLanguageConfigurableProvider extends JSInheritedLanguagesConfigurableProvider {
  @Override
  public boolean isNeedToBeTerminated(@NotNull PsiElement element) {
    return false;
  }
}
