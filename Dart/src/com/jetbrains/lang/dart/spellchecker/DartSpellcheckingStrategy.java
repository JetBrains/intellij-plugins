package com.jetbrains.lang.dart.spellchecker;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.psi.DartStringLiteralExpression;
import org.jetbrains.annotations.NotNull;

public class DartSpellcheckingStrategy extends SpellcheckingStrategy {
  private final Tokenizer dartStringLiteralTokenizer = new DartStringLiteralTokenizer();

  @Override
  @NotNull
  public Tokenizer getTokenizer(final PsiElement element) {
    if (element instanceof PsiNameIdentifierOwner && !(element instanceof DartComponentName)) {
      return EMPTY_TOKENIZER;
    }
    else if (element instanceof DartStringLiteralExpression) {
      return dartStringLiteralTokenizer;
    }
    return super.getTokenizer(element);
  }
}
