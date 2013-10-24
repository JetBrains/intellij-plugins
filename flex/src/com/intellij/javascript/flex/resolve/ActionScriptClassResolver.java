package com.intellij.javascript.flex.resolve;

import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.index.JavaScriptIndex;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSClassResolver;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin.Ulitin
 */
public class ActionScriptClassResolver extends JSClassResolver {

  @Override
  public PsiElement findClassByQName(@NotNull String link, @NotNull PsiElement context) {
    return findClassByQNameStatic(link, context);
  }

  public static PsiElement findClassByQNameStatic(@NotNull String link, @NotNull PsiElement context) {
    return findClassByQName(link, JavaScriptIndex.getInstance(context.getProject()), JSResolveUtil.getResolveScope(context), DialectOptionHolder.ECMA_4);
  }

  /** AS and TS */
  public static PsiElement findClassByQName(@NotNull final String link, final JavaScriptIndex index, final Module module) {
    GlobalSearchScope searchScope = JSInheritanceUtil.getEnforcedScope();
    if (searchScope == null) {
      searchScope =
        module != null ? GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module) : GlobalSearchScope.allScope(index.getProject());
    }
    return findClassByQName(link, index, searchScope, DialectOptionHolder.ECMA_4);
  }

  public static boolean isParentClass(JSClass clazz, String className) {
    return isParentClass(clazz, className, true);
  }

  public static boolean isParentClass(JSClass clazz, String className, boolean strict) {
    final PsiElement parentClass = JSResolveUtil.unwrapProxy(findClassByQName(
      className, clazz.getResolveScope()));
    if (!(parentClass instanceof JSClass)) return false;

    return JSInheritanceUtil.isParentClass(clazz, (JSClass)parentClass, strict);
  }
}

