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

import com.intellij.protobuf.lang.lexer.ProtoLexer.CommentStyle;
import com.intellij.protobuf.lang.psi.ProtoTokenTypes;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

@SuppressWarnings("fallthrough")
%%

%{
  private CommentStyle commentStyle;
  private boolean allowFloatCast;
  private boolean returnKeywords;

  public _ProtoLexer(CommentStyle commentStyle, boolean allowFloatCast, boolean returnKeywords) {
    this(null);
    this.commentStyle = commentStyle;
    this.allowFloatCast = allowFloatCast;
    this.returnKeywords = returnKeywords;
  }

  private IElementType keyword(IElementType type) {
    return returnKeywords ? type : ProtoTokenTypes.IDENTIFIER_LITERAL;
  }
%}

%public
%class _ProtoLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode
%state COMMENT, AFTER_NUMBER

// General classes
Alpha = [a-zA-Z_]
Digit = [0-9]
NonZeroDigit = [1-9]
HexDigit = [0-9a-fA-F]
OctDigit = [0-7]
Alphanumeric = {Alpha} | {Digit}

// Catch-all for symbols not handled elsewhere.
//
// From tokenizer.h:
//   Any other printable character, like '!' or '+'. Symbols are always a single character, so
//   "!+$%" is four tokens.
Symbol = [!#$%&()*+,-./:;<=>?@\[\\\]\^`{|}~]

// Whitespace.
WhitespaceNoNewline = [\ \t\r\f\x0b] // '\x0b' is '\v' (vertical tab) in C.
Whitespace = ({WhitespaceNoNewline} | "\n")+

// Comments.
CLineComment = "//" [^\n]*
CBlockComment = "/*" !([^]* "*/" [^]*) "*/"?
ShLineComment = "#" [^\n]*

// Identifiers.
//
// From tokenizer.h:
//   A sequence of letters, digits, and underscores, not starting with a digit.  It is an error for
//   a number to be followed by an identifier with no space in between.
Identifier = {Alpha} {Alphanumeric}*

// Integers.
//
// From tokenizer.h:
//   A sequence of digits representing an integer.  Normally the digits are decimal, but a prefix of
//   "0x" indicates a hex number and a leading zero indicates octal, just like with C numeric
//   literals.  A leading negative sign is NOT included in the token; it's up to the parser to
//   interpret the unary minus operator on its own.
DecInteger = "0" | {NonZeroDigit} {Digit}*
OctInteger = "0" {OctDigit}+
HexInteger = "0" [xX] {HexDigit}+
Integer = {DecInteger} | {OctInteger} | {HexInteger}

// Floats.
//
// From tokenizer.h:
//   A floating point literal, with a fractional part and/or an exponent.  Always in decimal.
//   Again, never negative.
Float = ("." {Digit}+ {Exponent}? | {DecInteger} "." {Digit}* {Exponent}? | {DecInteger} {Exponent})
Exponent = [eE] [-+]? {Digit}+
FloatWithF = {Float} [fF]
IntegerWithF = {DecInteger} [fF]

// Strings.
//
// From tokenizer.h:
//   A quoted sequence of escaped characters.  Either single or double quotes can be used, but they
//   must match. A string literal cannot cross a line break.
SingleQuotedString = \' ([^\\\'\n] | \\[^\n])* (\' | \\)?
DoubleQuotedString = \" ([^\\\"\n] | \\[^\n])* (\" | \\)?
String = {SingleQuotedString} | {DoubleQuotedString}

%%

<YYINITIAL> {
  {Whitespace}              { return com.intellij.psi.TokenType.WHITE_SPACE; }

  "="                       { return ProtoTokenTypes.ASSIGN; }
  ":"                       { return ProtoTokenTypes.COLON; }
  ","                       { return ProtoTokenTypes.COMMA; }
  "."                       { return ProtoTokenTypes.DOT; }
  ">"                       { return ProtoTokenTypes.GT; }
  "{"                       { return ProtoTokenTypes.LBRACE; }
  "["                       { return ProtoTokenTypes.LBRACK; }
  "("                       { return ProtoTokenTypes.LPAREN; }
  "<"                       { return ProtoTokenTypes.LT; }
  "-"                       { return ProtoTokenTypes.MINUS; }
  "}"                       { return ProtoTokenTypes.RBRACE; }
  "]"                       { return ProtoTokenTypes.RBRACK; }
  ")"                       { return ProtoTokenTypes.RPAREN; }
  ";"                       { return ProtoTokenTypes.SEMI; }
  "/"                       { return ProtoTokenTypes.SLASH; }

  "default"                 { return keyword(ProtoTokenTypes.DEFAULT); }
  "enum"                    { return keyword(ProtoTokenTypes.ENUM); }
  "extend"                  { return keyword(ProtoTokenTypes.EXTEND); }
  "extensions"              { return keyword(ProtoTokenTypes.EXTENSIONS); }
  "group"                   { return keyword(ProtoTokenTypes.GROUP); }
  "import"                  { return keyword(ProtoTokenTypes.IMPORT); }
  "json_name"               { return keyword(ProtoTokenTypes.JSON_NAME); }
  "map"                     { return keyword(ProtoTokenTypes.MAP); }
  "max"                     { return keyword(ProtoTokenTypes.MAX); }
  "message"                 { return keyword(ProtoTokenTypes.MESSAGE); }
  "oneof"                   { return keyword(ProtoTokenTypes.ONEOF); }
  "option"                  { return keyword(ProtoTokenTypes.OPTION); }
  "optional"                { return keyword(ProtoTokenTypes.OPTIONAL); }
  "package"                 { return keyword(ProtoTokenTypes.PACKAGE); }
  "public"                  { return keyword(ProtoTokenTypes.PUBLIC); }
  "repeated"                { return keyword(ProtoTokenTypes.REPEATED); }
  "required"                { return keyword(ProtoTokenTypes.REQUIRED); }
  "reserved"                { return keyword(ProtoTokenTypes.RESERVED); }
  "returns"                 { return keyword(ProtoTokenTypes.RETURNS); }
  "rpc"                     { return keyword(ProtoTokenTypes.RPC); }
  "service"                 { return keyword(ProtoTokenTypes.SERVICE); }
  "stream"                  { return keyword(ProtoTokenTypes.STREAM); }
  "syntax"                  { return keyword(ProtoTokenTypes.SYNTAX); }
  "to"                      { return keyword(ProtoTokenTypes.TO); }
  "true"                    { return keyword(ProtoTokenTypes.TRUE); }
  "weak"                    { return keyword(ProtoTokenTypes.WEAK); }

  {Identifier}              { return ProtoTokenTypes.IDENTIFIER_LITERAL; }
  {String}                  { return ProtoTokenTypes.STRING_LITERAL; }
  {Integer}                 { yybegin(AFTER_NUMBER); return ProtoTokenTypes.INTEGER_LITERAL; }
  {Float}                   { yybegin(AFTER_NUMBER); return ProtoTokenTypes.FLOAT_LITERAL; }

  {IntegerWithF} {
    yybegin(AFTER_NUMBER);
    if (allowFloatCast) {
      return ProtoTokenTypes.FLOAT_LITERAL;
    } else {
      yypushback(1); // Push the 'f' back
      return ProtoTokenTypes.INTEGER_LITERAL;
    }
  }
  {FloatWithF} {
    yybegin(AFTER_NUMBER);
    if (!allowFloatCast) {
      yypushback(1); // Push the 'f' back
    }
    return ProtoTokenTypes.FLOAT_LITERAL;
  }

  // C-style comments, allowed when injected into protobuf.
  "/*" | "//" {
    if (commentStyle == CommentStyle.C_STYLE) {
      // Push back both characters and match with either CLineComment or CBlockComment in the
      // COMMENT state.
      yypushback(2);
      yybegin(COMMENT);
    } else {
      // Push back the trailing '/' or '*' and return SLASH for the leading '/'.
      yypushback(1);
      return ProtoTokenTypes.SLASH;
    }
  }

  // sh-style comments, allowed in standalone mode.
  "#" {
    if (commentStyle == CommentStyle.SH_STYLE) {
      // Push back the symbol and enter COMMENT state to match the comment.
      yypushback(1);
      yybegin(COMMENT);
    } else {
      // Return SYMBOL for the '#'.
      return ProtoTokenTypes.SYMBOL;
    }
  }

  // Additional unmatched symbols are matched individually as SYMBOL.
  {Symbol} { return ProtoTokenTypes.SYMBOL; }

  // All other unmatched characters.
  [^] { return com.intellij.psi.TokenType.BAD_CHARACTER; }
}

<COMMENT> {
  {ShLineComment}           { yybegin(YYINITIAL); return ProtoTokenTypes.LINE_COMMENT; }
  {CLineComment}            { yybegin(YYINITIAL); return ProtoTokenTypes.LINE_COMMENT; }
  {CBlockComment}           { yybegin(YYINITIAL); return ProtoTokenTypes.BLOCK_COMMENT; }
}

<AFTER_NUMBER> {
  // An identifier immediately following a number (with no whitespace) is an error. We return
  // the special IDENTIFIER_AFTER_NUMBER token type to signal this scenario.
  {Identifier} { yybegin(YYINITIAL); return ProtoTokenTypes.IDENTIFIER_AFTER_NUMBER; }

  // Any other token is valid. Push the token back and return to the initial state.
  [^] { yybegin(YYINITIAL); yypushback(yylength()); }
}
