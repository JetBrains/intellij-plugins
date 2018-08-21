package org.angularjs.codeInsight;

import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSElementResolveScopeProvider;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import org.angular2.index.Angular2IndexingHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSResolveScopeProvider implements JSElementResolveScopeProvider {

  @Nullable
  @Override
  public GlobalSearchScope getElementResolveScope(@NotNull PsiElement element) {
    JSClass clazz = Angular2IndexingHandler.findDirectiveClass(element);
    if (clazz != null) {
      return JSResolveUtil.getResolveScope(clazz);
    }
    return null;
  }
}
