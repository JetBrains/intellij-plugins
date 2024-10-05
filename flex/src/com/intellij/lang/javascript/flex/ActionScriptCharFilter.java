package com.intellij.lang.javascript.flex;

import com.intellij.codeInsight.lookup.CharFilter;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;

public final class ActionScriptCharFilter extends CharFilter {
  @Override
  public Result acceptChar(char c, int prefixLength, Lookup lookup) {
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
