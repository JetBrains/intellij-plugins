package com.intellij.lang.javascript.flex;

import com.intellij.codeInsight.lookup.CharFilter;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.psi.PsiElement;

/**
 * User: Maxim.Mossienko
 * Date: 28.02.2010
 * Time: 19:07:06
 */
public class ActionScriptCharFilter extends CharFilter {
  @Override
  public Result acceptChar(char c, int prefixLength, Lookup lookup) {
    if (!lookup.isCompletion()) return null;
    Language language = null;
    
    PsiElement element = lookup.getPsiElement();
    if (element != null) language = element.getContainingFile().getLanguage();
    if (language != null && language.isKindOf(JavaScriptSupportLoader.ECMA_SCRIPT_L4)) {
      if (c == ' ') return Result.HIDE_LOOKUP;
      if (prefixLength == 0 && (c == ',' || Character.isDigit(c))) return Result.HIDE_LOOKUP;
    }
    return null;
  }
}
