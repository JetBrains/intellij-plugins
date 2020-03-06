package com.jetbrains.lang.dart.spellchecker;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.spellchecker.inspections.PlainTextSplitter;
import com.intellij.spellchecker.tokenizer.EscapeSequenceTokenizer;
import com.intellij.spellchecker.tokenizer.TokenConsumer;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import org.jetbrains.annotations.NotNull;

/**
 *  Tokenize Dart string literal to allow spell checking.
 */
class DartStringLiteralTokenizer extends Tokenizer<PsiElement> {
  @Override
  public void tokenize(@NotNull PsiElement element, TokenConsumer consumer) {
    PsiModifierListOwner listOwner = PsiTreeUtil.getParentOfType(element);
    if (listOwner != null && AnnotationUtil.isAnnotated(listOwner, AnnotationUtil.NON_NLS, 0)) {
      return;
    }
    String text = element.getText();

    if (text == null) {
      return;
    }
    if (!text.contains("\\")) {
      consumer.consumeToken(element, PlainTextSplitter.getInstance());
    }
    else {
      processTextWithEscapeSequences(element, text, consumer);
    }
  }

  void processTextWithEscapeSequences(PsiElement element, String text, TokenConsumer consumer) {
    StringBuilder unEscapedText = new StringBuilder();
    int[] offsets = new int[text.length() + 1];
    PsiLiteralExpressionImpl.parseStringCharacters(text, unEscapedText, offsets);
    EscapeSequenceTokenizer.processTextWithOffsets(element, consumer, unEscapedText, offsets, 1);
  }
}
