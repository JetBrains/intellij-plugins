package com.jetbrains.lang.dart.ide.editor;

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.DartTokenTypesSets;

public final class DartQuoteHandler extends SimpleTokenSetQuoteHandler {

  public DartQuoteHandler() {
    super(DartTokenTypesSets.STRINGS);
  }

  @Override
  public boolean isOpeningQuote(final HighlighterIterator iterator, final int offset) {
    final IElementType type = iterator.getTokenType();

    if (type == DartTokenTypes.OPEN_QUOTE) return true;

    if (type == DartTokenTypes.RAW_SINGLE_QUOTED_STRING && offset == iterator.getStart() + 1) {
      // start of the raw string like r'
      return true;
    }

    return false;
  }

  @Override
  public boolean isClosingQuote(final HighlighterIterator iterator, final int offset) {
    final IElementType type = iterator.getTokenType();

    if (type == DartTokenTypes.CLOSING_QUOTE) return true;

    if (type == DartTokenTypes.RAW_SINGLE_QUOTED_STRING) {
      final int start = iterator.getStart();
      final int end = iterator.getEnd();
      if (end - start > 2 && offset == end - 1) {
        final CharSequence chars = iterator.getDocument().getCharsSequence();
        return chars.charAt(start + 1) == chars.charAt(end - 1); // r'foo'
      }
    }

    return false;
  }

  @Override
  protected boolean isNonClosedLiteral(final HighlighterIterator iterator, final CharSequence chars) {
    if (iterator.getTokenType() == DartTokenTypes.RAW_SINGLE_QUOTED_STRING) {
      final int start = iterator.getStart();
      final int end = iterator.getEnd();
      return end - start <= 2 || chars.charAt(end - 1) != chars.charAt(start + 1); // not closed raw string like r' or r'foo
    }

    return super.isNonClosedLiteral(iterator, chars);
  }
}
