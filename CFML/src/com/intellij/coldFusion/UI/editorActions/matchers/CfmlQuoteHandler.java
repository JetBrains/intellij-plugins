// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.UI.editorActions.matchers;

import com.intellij.codeInsight.editorActions.QuoteHandler;
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;

public class CfmlQuoteHandler implements QuoteHandler {
  @Override
  public boolean isClosingQuote(HighlighterIterator iterator, int offset) {
    return iterator.getTokenType() == CfmlTokenTypes.DOUBLE_QUOTE_CLOSER ||
           iterator.getTokenType() == CfmlTokenTypes.SINGLE_QUOTE_CLOSER ||
           iterator.getTokenType() == CfmlTokenTypes.DOUBLE_QUOTE_CLOSER ||
           iterator.getTokenType() == CfmlTokenTypes.SINGLE_QUOTE_CLOSER;
  }

  @Override
  public boolean isOpeningQuote(HighlighterIterator iterator, int offset) {
    return iterator.getTokenType() == CfmlTokenTypes.SINGLE_QUOTE ||
           iterator.getTokenType() == CfmlTokenTypes.DOUBLE_QUOTE ||
           iterator.getTokenType() == CfmlTokenTypes.SINGLE_QUOTE ||
           iterator.getTokenType() == CfmlTokenTypes.DOUBLE_QUOTE;
  }

  @Override
  public boolean hasNonClosedLiteral(Editor editor, HighlighterIterator iterator, int offset) {
    return true;
  }

  @Override
  public boolean isInsideLiteral(HighlighterIterator iterator) {
    return false;
  }
}
