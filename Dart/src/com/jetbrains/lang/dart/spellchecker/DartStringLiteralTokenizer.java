package com.jetbrains.lang.dart.spellchecker;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.spellchecker.inspections.PlainTextSplitter;
import com.intellij.spellchecker.tokenizer.TokenConsumer;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Tokenize Dart string literal to allow spell checking.
 */
class DartStringLiteralTokenizer extends Tokenizer<PsiElement> {
  @Override
  public void tokenize(@NotNull PsiElement element, TokenConsumer consumer) {
    String text = element.getText();

    if (text == null) {
      return;
    }
    if (text.contains("$") || text.contains("\\")) {
      processInterpolationAndEscapeSequence(element, text, consumer);
    }
    else {
      consumer.consumeToken(element, text, false, 0, TextRange.from(0, text.length()), PlainTextSplitter.getInstance());
    }
  }

  private static void processInterpolationAndEscapeSequence(PsiElement element, String text, TokenConsumer consumer) {
    final List<TextRange> ranges = getRanges(element);

    ranges.forEach(range ->
                     consumer.consumeToken(element, text, false, 0, range, PlainTextSplitter.getInstance()));
  }

  @NotNull
  private static List<TextRange> getRanges(@NotNull PsiElement element) {
    final List<TextRange> result = new ArrayList<>();
    final List<TextRange> escapeSequenceTextRanges = getEscapeSequenceTextRanges(element);
    final List<TextRange> interpolationTextRanges = getInterpolationTextRanges(element);

    for (TextRange escapeRange : escapeSequenceTextRanges) {
      for (TextRange interpolationRange : interpolationTextRanges) {
        if (escapeRange.intersects(interpolationRange)) {
          result.add(escapeRange.intersection(interpolationRange));
        }
      }
    }
    return result;
  }

  @NotNull
  private static List<TextRange> getInterpolationTextRanges(@NotNull PsiElement element) {
    final PsiElement[] children = element.getChildren();
    final List<TextRange> ranges = new ArrayList<>();
    int rangeStart = 0;

    for (PsiElement child : children) {
      int rangeEnd = relativeChildStartOffset(child, element);
      ranges.add(TextRange.create(rangeStart, rangeEnd));
      rangeStart = relativeChildEndOffset(child, element);
    }
    if (rangeStart < element.getTextLength()) {
      ranges.add(TextRange.create(rangeStart, element.getTextLength()));
    }
    return ranges;
  }

  private static int relativeChildStartOffset(PsiElement child, PsiElement parent) {
    return child.getTextRange().getStartOffset() - parent.getTextRange().getStartOffset();
  }

  private static int relativeChildEndOffset(PsiElement child, PsiElement parent) {
    return child.getTextRange().getEndOffset() - parent.getTextRange().getStartOffset();
  }

  @NotNull
  private static List<TextRange> getEscapeSequenceTextRanges(@NotNull PsiElement element) {
    final List<TextRange> ranges = new ArrayList<>();
    final String text = element.getText();
    int rangeStart = 0;
    int backslashIndex = text.indexOf('\\');
    int afterControlCharacterIndex = backslashIndex + 2;
    while (backslashIndex != -1 && afterControlCharacterIndex < text.length()) {
      //skip control character, \\n, \\r and others
      ranges.add(TextRange.create(rangeStart, backslashIndex));
      rangeStart = afterControlCharacterIndex;
      char controlCharacter = text.charAt(backslashIndex + 1);
      if (controlCharacter == 'u') {
        //skip hexadecimal part of escape sequence in curly brackets, \\u{ab}, \\u{12345} and others
        int closingBraceIndex = text.indexOf('}', afterControlCharacterIndex);
        if (closingBraceIndex != -1 && text.charAt(afterControlCharacterIndex) == '{') {
          rangeStart = closingBraceIndex + 1;
        }
        //skip hexadecimal part of escape sequence, \\u1234 and others
        else if (rangeStart + 4 < text.length()) {
          rangeStart += 4;
        }
        else {
          rangeStart = text.length();
          break;
        }
      }
      //skip hexadecimal part of escape sequence, \\x09, \\x0B and others
      else if (controlCharacter == 'x' && rangeStart + 2 < text.length()) {
        rangeStart += 2;
      }
      backslashIndex = text.indexOf('\\', rangeStart);
      afterControlCharacterIndex = backslashIndex + 2;
    }
    if (rangeStart < text.length()) {
      ranges.add(TextRange.create(rangeStart, text.length()));
    }
    return ranges;
  }
}

