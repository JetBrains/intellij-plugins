package com.jetbrains.lang.dart;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.psi.DartStringLiteralExpression;
import org.jetbrains.annotations.NotNull;

public class DartSpellcheckingStrategy extends SpellcheckingStrategy {
  //private final Tokenizer myStringLiteralTokenizer = new DartStringLiteralTokenizer();

  @NotNull
  public Tokenizer getTokenizer(final PsiElement element) {
    if (element instanceof PsiNameIdentifierOwner && !(element instanceof DartComponentName)) {
      return EMPTY_TOKENIZER;
    }
    else if (element instanceof DartStringLiteralExpression) {
      // return myStringLiteralTokenizer; todo
    }

    return super.getTokenizer(element);
  }
}
