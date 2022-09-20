/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.lang.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.protobuf.lang.lexer._StringLexer.TokenType;
import com.intellij.protobuf.lang.psi.ProtoTokenTypes;

public class StringLexer extends FlexAdapter {

  private final _StringLexer lexer;

  public StringLexer() {
    this(ProtoTokenTypes.STRING_LITERAL);
  }

  public StringLexer(IElementType literalType) {
    super(new _StringLexer(literalType));
    lexer = (_StringLexer) getFlex();
  }

  /**
   * Returns the current token value from the underlying {@link _StringLexer}, processing any valid
   * escape sequence.
   */
  public CharSequence currentTokenValue() {
    CharSequence sequenceText = lexer.yytext();
    CharSequence valueText =
        sequenceText.subSequence(lexer.lastStringTokenPos, sequenceText.length());
    return switch (lexer.lastStringTokenType) {
      case SIMPLE -> switch (valueText.charAt(0)) {
        case 'a' -> "\007"; // Bell;
        case 'b' -> "\b";
        case 'f' -> "\f";
        case 'n' -> "\n";
        case 'r' -> "\r";
        case 't' -> "\t";
        case 'v' -> "\013";
        case '\\' -> "\\";
        case '?' -> "?";
        case '\'' -> "'";
        case '"' -> "\"";
        default ->
          // Not a valid escape character. This should not happen unless these switch cases don't
          // match the lexer rules.
          "";
      };
      case OCTAL -> {
        int octValue = Integer.parseInt(valueText.toString(), 8);
        yield new String(Character.toChars(octValue));
      }
      case HEX -> {
        int hexValue = Integer.parseInt(valueText.toString(), 16);
        yield new String(Character.toChars(hexValue));
      }
      case INVALID, LITERAL -> valueText;
    };
  }

  public boolean isCurrentTokenLiteral() {
    return lexer.lastStringTokenType == TokenType.LITERAL;
  }

  public boolean isCurrentTokenInvalid() {
    return lexer.lastStringTokenType == TokenType.INVALID;
  }

  public boolean hasMoreTokens() {
    return getTokenType() != null;
  }

  /**
   * Returns a StringLexer that merges adjacent string literal tokens together. So, for example,
   * "foo" becomes one token instead of 5 (two quotes and 3 letters). However, "fo\no" becomes 3
   * tokens: the "fo prefix, \n, and the o" suffix.
   */
  public static Lexer mergingStringLexer(IElementType literalType) {
    return new MergingLexerAdapter(new StringLexer(literalType), TokenSet.create(literalType));
  }
}
