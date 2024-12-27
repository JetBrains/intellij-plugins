// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class CfmlResolveResult extends PsiElementResolveResult {
  public CfmlResolveResult(@NotNull PsiElement element) {
    super(element);
  }

  public static ResolveResult[] create(Collection<? extends PsiElement> from) {
    final ResolveResult[] results = !from.isEmpty() ? new ResolveResult[from.size()] : EMPTY_ARRAY;
    int i = 0;
    for (PsiElement element : from) {
      results[i++] = new CfmlResolveResult(element);
    }
    return results;
  }
}
