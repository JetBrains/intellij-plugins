/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion.UI.editorActions.matchers;

import com.intellij.codeInsight.editorActions.QuoteHandler;
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;

public class CfmlQuoteHandler implements QuoteHandler {
  public boolean isClosingQuote(HighlighterIterator iterator, int offset) {
    return iterator.getTokenType() == CfmlTokenTypes.DOUBLE_QUOTE_CLOSER ||
           iterator.getTokenType() == CfmlTokenTypes.SINGLE_QUOTE_CLOSER ||
           iterator.getTokenType() == CfmlTokenTypes.DOUBLE_QUOTE_CLOSER ||
           iterator.getTokenType() == CfmlTokenTypes.SINGLE_QUOTE_CLOSER;
  }

  public boolean isOpeningQuote(HighlighterIterator iterator, int offset) {
    return iterator.getTokenType() == CfmlTokenTypes.SINGLE_QUOTE ||
           iterator.getTokenType() == CfmlTokenTypes.DOUBLE_QUOTE ||
           iterator.getTokenType() == CfmlTokenTypes.SINGLE_QUOTE ||
           iterator.getTokenType() == CfmlTokenTypes.DOUBLE_QUOTE;
  }

  public boolean hasNonClosedLiteral(Editor editor, HighlighterIterator iterator, int offset) {
    return true;
  }

  public boolean isInsideLiteral(HighlighterIterator iterator) {
    return false;
  }
}
