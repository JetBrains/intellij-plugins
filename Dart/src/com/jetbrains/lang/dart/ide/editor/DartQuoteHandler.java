package com.jetbrains.lang.dart.ide.editor;

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.DartTokenTypesSets;

public class DartQuoteHandler extends SimpleTokenSetQuoteHandler {

  public DartQuoteHandler() {
    super(DartTokenTypesSets.STRINGS);
  }

  public boolean isOpeningQuote(final HighlighterIterator iterator, final int offset) {
    return iterator.getTokenType() == DartTokenTypes.OPEN_QUOTE;
  }

  public boolean isClosingQuote(final HighlighterIterator iterator, final int offset) {
    return iterator.getTokenType() == DartTokenTypes.CLOSING_QUOTE;
  }
}
