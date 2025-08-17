// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex;

import com.intellij.lang.javascript.JSExtendedLanguagesTokenSetProvider;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSImportStatement;
import com.intellij.lang.javascript.psi.ecmal4.JSIncludeDirective;
import com.intellij.lang.javascript.psi.ecmal4.JSPackageStatement;
import com.intellij.lang.javascript.psi.impl.JSStubElementImpl;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Maxim.Mossienko
 */
public final class PsiScopedImportSet extends ScopedImportSet {
  @Override
  protected @NotNull Map<String, Object> getUpToDateMap(@NotNull PsiElement scope) {
    return CachedValuesManager.getProjectPsiDependentCache(scope, owner -> {
      Map<String,Object> result = new HashMap<>();
      collect(result, owner, null);
      return result;
    });
  }

  private static void collect(final Map<String, Object> result, final PsiElement owner, Set<PsiFile> visitedIncludes) {
    PsiElement[] children = PsiElement.EMPTY_ARRAY;

    if (owner instanceof JSIncludeDirective) {
      final PsiFile file = ((JSIncludeDirective)owner).resolveFile();
      if (file != null && (visitedIncludes == null || !visitedIncludes.contains(file))) {
        if(visitedIncludes == null) visitedIncludes = new HashSet<>();
        visitedIncludes.add(file);
        children = JSStubBasedPsiTreeUtil.getChildrenByType(file, JSExtendedLanguagesTokenSetProvider.SOURCE_ELEMENTS);
      }
    } else
    if (owner instanceof JSFile || owner instanceof JSStubElementImpl) {
      children = JSResolveUtil.getSourceElements(owner);
    } else {
      children = owner.getChildren();
    }

    for(PsiElement c:children) {
      if (c instanceof JSImportStatement s) {

        if (s.getImportText() != null) {
          appendToMap(result, s);
        }
      } else if (!(c instanceof JSPackageStatement) && !(c instanceof JSFunction)) {
        collect(result, c, visitedIncludes);
      }
    }
  }
}