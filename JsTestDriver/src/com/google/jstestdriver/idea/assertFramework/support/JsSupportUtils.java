package com.google.jstestdriver.idea.assertFramework.support;

import com.intellij.lang.javascript.index.JSNamedElementProxy;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class JsSupportUtils {
  private JsSupportUtils() {}

  public static boolean isResolvedToFunction(@NotNull PsiPolyVariantReference psiPolyVariantReference) {
    ResolveResult[] resolveResults = psiPolyVariantReference.multiResolve(false);
    for (ResolveResult resolveResult : resolveResults) {
      boolean resolvedCorrectly = isResolvedToFunction(resolveResult);
      if (resolvedCorrectly) {
        return true;
      }
    }
    return false;
  }

  private static boolean isResolvedToFunction(@NotNull ResolveResult resolveResult) {
    PsiElement resolvedElement = resolveResult.getElement();
    if (resolvedElement == null || !resolveResult.isValidResult()) {
      return false;
    }
    if (resolvedElement instanceof JSNamedElementProxy) {
      JSNamedElementProxy proxy = (JSNamedElementProxy) resolvedElement;
      PsiElement element = proxy.getElement();
      return element != null && !(element instanceof PsiComment);
    }
    return !(resolvedElement instanceof PsiComment);
  }

}
