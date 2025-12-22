// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex;

import com.intellij.codeInsight.lookup.CharFilter;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public final class ActionScriptCharFilter extends CharFilter {
  @Override
  public Result acceptChar(char c, int prefixLength, @NotNull Lookup lookup) {
    if (!lookup.isCompletion()) return null;
    Language language = null;
    
    PsiElement element = lookup.getPsiElement();
    if (element != null) language = element.getContainingFile().getLanguage();
    if (language != null && language.isKindOf(FlexSupportLoader.ECMA_SCRIPT_L4)) {
      if (c == ' ') return Result.HIDE_LOOKUP;
      if (prefixLength == 0 && (c == ',' || Character.isDigit(c))) return Result.HIDE_LOOKUP;
    }
    return null;
  }
}
