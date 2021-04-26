// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.intellij.protobuf.lang.lexer;

import com.intellij.psi.tree.IElementType;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.StringEscapesTokenTypes;
import com.intellij.psi.tree.IElementType;

@SuppressWarnings("fallthrough")
%%

%{

  public enum TokenType {
    // An octal sequence, such as "\123".
    OCTAL,
    // A hex escape sequence, such as "\x3F", "\uABCD", or "\U0010ABCD".
    HEX,
    // A single-character escape sequence, such as "\n".
    SIMPLE,
    // An invalid escape sequence, such as "\xZZ" or "\U99999999".
    INVALID,
    // Not an escape sequence - a literal string part.
    LITERAL
  }

  public TokenType lastStringTokenType = null;
  public int lastStringTokenPos = 0;
  private IElementType literalType;

  public _StringLexer(IElementType literalType) {
    this((java.io.Reader)null);
    this.literalType = literalType;
  }

  private void record(TokenType type, int startIndex) {
    lastStringTokenType = type;
    lastStringTokenPos = startIndex;
  }

%}

%public
%class _StringLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

Escape = [abfnrtv?\\\'\"]
OctDigit = [0-7]
HexDigit = [0-9a-fA-F]

%%

// These actions assume that a parent lexer has ensured the string starts and ends with the same
// quote type, doesn't cross newlines, etc. The goal of this lexer is to break a single string
// literal token into separate escaped and unescaped tokens.

<YYINITIAL> {
  // Valid escape: Slash followed by single letter sequence.
  \\ {Escape} { record(TokenType.SIMPLE, 1); return StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN; }

  // Valid escape: Slash followed by 1-3 octal digits.
  \\ {OctDigit} {1,3} { record(TokenType.OCTAL, 1); return StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN; }

  // Valid escape: Slash followed by 'x' and one or two hex digits.
  \\ "x" {HexDigit} {1,2} { record(TokenType.HEX, 2); return StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN; }

  // Valid escape: 'u' followed by exactly 4 hex digits.
  \\ "u" {HexDigit} {4} { record(TokenType.HEX, 2); return StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN; }

  // Valid escape: 'U' followed by 8 hex digits, in the range up to 0x10ffff.
  \\ "U" (("000" {HexDigit} {5}) | ("0010" {HexDigit} {4})) { record(TokenType.HEX, 2); return StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN; }

  // Invalid escape: 'u' followed by 0-3 hex digits.
  \\ "u" {HexDigit} {0,3} { record(TokenType.INVALID, 0); return StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN; }

  // Invalid escape: 'U' followed by 0-8 digits: either not enough digits or out of range.
  \\ "U" {HexDigit} {0,8} { record(TokenType.INVALID, 0); return StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN; }

  // Invalid escape: Slash followed by any other character.
  \\ [^] { record(TokenType.INVALID, 0); return StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN; }

  // All other characters are part of the string.
  [^] { record(TokenType.LITERAL, 0); return literalType; }
}
