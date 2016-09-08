package org.angularjs.codeInsight;

import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSElementResolveScopeProvider;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import org.angularjs.index.AngularJS2IndexingHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSResolveScopeProvider extends JSElementResolveScopeProvider {
  @Nullable
  @Override
  public GlobalSearchScope getResolveScope(@NotNull VirtualFile file, Project project) {
    return null;
  }

  @NotNull
  @Override
  public GlobalSearchScope getElementResolveScope(@NotNull PsiElement element) {
    JSClass clazz = AngularJS2IndexingHandler.findDirectiveClass(element);
    if (clazz != null) {
      return JSResolveUtil.getResolveScope(clazz);
    }
    return super.getElementResolveScope(element);
  }

  @Override
  protected boolean isApplicable(@NotNull VirtualFile file) {
    return false;
  }
}
