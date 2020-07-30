/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.highlight;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.thoughtworks.gauge.language.token.SpecTokenTypes;
import com.thoughtworks.gauge.lexer.SpecLexer;
import org.jetbrains.annotations.NotNull;

/**
 * A class that describes what highlighting styles should be applied to various tokens (headings, comments, etc) in a
 * Gauge specification (.spec) file.
 */
final class SpecSyntaxHighlighter extends SyntaxHighlighterBase {
  @NotNull
  @Override
  public Lexer getHighlightingLexer() {
    return new SpecLexer();
  }

  @Override
  public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
    if (tokenType.equals(SpecTokenTypes.SPEC_HEADING)) {
      return HighlighterTokens.SPEC_HEADING_ATTRIBUTE;
    }
    else if (tokenType.equals(SpecTokenTypes.SCENARIO_HEADING)) {
      return HighlighterTokens.SCENARIO_HEADING_ATTRIBUTE;
    }
    else if (tokenType.equals(SpecTokenTypes.STEP)) {
      return HighlighterTokens.STEP_ATTRIBUTE;
    }
    else if (tokenType.equals(SpecTokenTypes.COMMENT)) {
      return HighlighterTokens.COMMENT_ATTRIBUTE;
    }
    else if (tokenType.equals(SpecTokenTypes.TABLE_HEADER)) {
      return HighlighterTokens.TABLE_HEADER_ATTRIBUTE;
    }
    else if (tokenType.equals(SpecTokenTypes.TABLE_ROW_VALUE)) {
      return HighlighterTokens.TABLE_ROW_ATTRIBUTE;
    }
    else if (tokenType.equals(SpecTokenTypes.TAGS)) {
      return HighlighterTokens.TAGS_ATTRIBUTE;
    }
    else if (tokenType.equals(SpecTokenTypes.TABLE_BORDER)) {
      return HighlighterTokens.TABLE_BORDER_ATTRIBUTE;
    }
    else if (tokenType.equals(SpecTokenTypes.ARG_START) ||
             tokenType.equals(SpecTokenTypes.ARG) ||
             tokenType.equals(SpecTokenTypes.ARG_END)) {
      return HighlighterTokens.ARG_ATTRIBUTE;
    }
    else if (tokenType.equals(SpecTokenTypes.DYNAMIC_ARG_START) ||
             tokenType.equals(SpecTokenTypes.DYNAMIC_ARG) ||
             tokenType.equals(SpecTokenTypes.DYNAMIC_ARG_END)) {
      return HighlighterTokens.DYNAMIC_ARG_ATTRIBUTE;
    }
    else if (tokenType.equals(TokenType.BAD_CHARACTER)) {
      return HighlighterTokens.BAD_CHAR_KEYS;
    }
    else if (tokenType.equals((SpecTokenTypes.KEYWORD))) {
      return HighlighterTokens.KEYWORD_ATTRIBUTE;
    }
    else {
      return HighlighterTokens.EMPTY_KEYS;
    }
  }
}
