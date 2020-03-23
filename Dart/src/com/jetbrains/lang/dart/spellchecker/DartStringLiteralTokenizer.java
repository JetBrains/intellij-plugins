package com.jetbrains.lang.dart.spellchecker;

import com.intellij.codeInsight.CodeInsightUtilCore;
import com.intellij.psi.PsiElement;
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
    CodeInsightUtilCore.parseStringCharacters(text, unEscapedText, offsets);
    EscapeSequenceTokenizer.processTextWithOffsets(element, consumer, unEscapedText, offsets, 1);
  }
}
