package com.intellij.dts.pp.lang.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;

import com.intellij.dts.pp.lang.PpTokenTypes;

%%

%{
    private final PpTokenTypes tokenTypes;
%}

%init{
    this.tokenTypes = tokenTypes;
%init}

%class PpLexer
%implements FlexLexer
%ctorarg PpTokenTypes tokenTypes
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}

EOL               = "\n" | "\r" | "\r\n"
EOL_ESC           = "\\"{LINE_WS}*{EOL}
LINE_WS           = [\ \t]

COMMENT_EOL       = "//"[^\r\n]*
COMMENT_C         = "/*"([^*]|"*"[^/])*"*/"

IDENTIFIER        = [a-zA-Z_][a-zA-Z_0-9]+

BINARY_DIGIT      = [01]
OCTAL_DIGIT       = [0-7]
NONZERO_DIGIT     = [1-9]
DIGIT             = [0-9]
HEXA_DIGIT        = [0-9a-fA-F]

DIGIT_SEQ         = {DIGIT}("'"?{DIGIT})*
HEXA_DIGIT_SEQ    = {HEXA_DIGIT}("'"?{HEXA_DIGIT})*

UNSIGNED_SUFFIX   = "u" | "U"
LONG_SUFFIX       = "l" | "L"
LONG_LONG_SUFFIX  = "ll" | "LL"
SIZE_SUFFIX       = "z" | "Z"

INTEGER_SUFFIX    = {UNSIGNED_SUFFIX}{LONG_SUFFIX}?
                  | {UNSIGNED_SUFFIX}{LONG_LONG_SUFFIX}?
                  | {UNSIGNED_SUFFIX}{SIZE_SUFFIX}?
                  | {LONG_SUFFIX}{UNSIGNED_SUFFIX}?
                  | {LONG_LONG_SUFFIX}{UNSIGNED_SUFFIX}?
                  | {SIZE_SUFFIX}{UNSIGNED_SUFFIX}?

ENCODING_PREFIX   = "u8" | "u" | "U" | "L"

SIMPLE_ESCAPE     = "\\"[^NouUx0-8]
OCTAL_ESCAPE      = "\\"{OCTAL_DIGIT}{1,3} | "\\o{"{OCTAL_DIGIT}+"}"
HEXA_ESCAPE       = "\\x"{HEXA_DIGIT}+ | "\\x{"{HEXA_DIGIT}+"}"
ESCAPE            = {SIMPLE_ESCAPE} | {OCTAL_ESCAPE} | {HEXA_ESCAPE}

N_CHAR            = [^}\n]
U_CHAR            = "\\N{"{N_CHAR}+"}"
                  | "\\u"{HEXA_DIGIT}{4}
                  | "\\U"{HEXA_DIGIT}{8}
                  | "\\u{"{HEXA_DIGIT}+"}"
C_CHAR            = [^\'\\\n] | {ESCAPE} | {U_CHAR}
S_CHAR            = [^\"\\\n] | {ESCAPE} | {U_CHAR}
R_CHAR            = [^)] // TODO: this is not correct, but raw strings are not context free :(
D_CHAR            = [^ ()\\\t\b\t\n]

SIGN              = "+" | "-"

EXPONENT          = ("e" | "E"){SIGN}?{DIGIT_SEQ}
BINARY_EXPONENT   = ("p" | "P"){SIGN}?{DIGIT_SEQ}
FRACTIONAL        = {DIGIT_SEQ}?"."{DIGIT_SEQ} | {DIGIT_SEQ}"."
HEXA_FRACTIONAL   = {HEXA_DIGIT_SEQ}?"."{HEXA_DIGIT_SEQ} | {HEXA_DIGIT_SEQ}"."

FLOAT_SUFFIX      = "f" | "l" | "f16" | "f32" | "f64" | "f128" | "bf16" | "F" | "L" | "F16" | "F32" | "F64" | "F128" | "BF16"

HEADER_NAME       = ("<"{HCHARS}">"?) | ("\""{QCHARS}"\""?)
HCHARS            = [^\n>]*
QCHARS            = [^\n\"]*

%state WAITING_BODY WAITING_HEADER

%%

{LINE_WS}+
{ return TokenType.WHITE_SPACE; }

{EOL_ESC}
{ return tokenTypes.getLineBreak(); }

{COMMENT_C} | {COMMENT_EOL}
{ yybegin(WAITING_BODY); return tokenTypes.getComment(); }

<YYINITIAL> {
    "#"{LINE_WS}*"include"
    { yybegin(WAITING_HEADER); return tokenTypes.getDirective(); }

    "#"{LINE_WS}*{IDENTIFIER}
    { yybegin(WAITING_BODY); return tokenTypes.getDirective(); }
}

<WAITING_HEADER> {
  {HEADER_NAME}
  { yybegin(WAITING_BODY); return tokenTypes.getHeaderName(); }

  [^]
  { yybegin(WAITING_BODY); yypushback(1); }
}

<WAITING_BODY> {
  {IDENTIFIER}
  { return tokenTypes.getIdentifier(); }

  // preprocessor operator
  "#" | "##" | "%:" | "%:%:" |
  // operator or punctor
  "{" | "}" | "[" | "]" | "(" | ")" | "<:" | ":>" | "<%" | "%>" | ";" | ":" | "..." | "?" | "::" | "." | ".*" | "->" | "->*" | "~" | "!" |
  "+" | "-" | "*" | "/" | "%" | "^" | "&" | "|" | "=" | "+=" | "-=" | "*=" | "/=" | "%=" | "^=" | "&=" | "|=" | "==" | "!=" | "<" | ">" |
  "<=" | ">=" | "<=>" | "&&" | "||" | "<<" | ">>" | "<<=" | ">>=" | "++" | "--" | "," | "and" | "or" | "xor" | "not" | "bitand" | "bitor" |
  "compl" | "and_eq" | "or_eq" | "xor_eq" | "not_eq"
  { return tokenTypes.getOperatorOrPunctuator(); }

  // binary literal
  ("0b" | "0B"){BINARY_DIGIT}("'"?{BINARY_DIGIT})*{INTEGER_SUFFIX}? |
  // octal literal
  0("'"?{OCTAL_DIGIT})*{INTEGER_SUFFIX}? |
  // decimal literal
  {NONZERO_DIGIT}("'"?{DIGIT})*{INTEGER_SUFFIX}? |
  // hexa literal
  ("0x" | "0X"){HEXA_DIGIT}("'"?{HEXA_DIGIT})*{INTEGER_SUFFIX}?
  { return tokenTypes.getIntegerLiteral(); }

  {ENCODING_PREFIX}?"'"{C_CHAR}+"'"?
  { return tokenTypes.getCharLiteral(); }

  // decimal float literal
  {FRACTIONAL}{EXPONENT}?{FLOAT_SUFFIX}? |
  {DIGIT_SEQ}{EXPONENT}{FLOAT_SUFFIX}? |
  // hexa float literal
  ("0x" | "0X"){HEXA_FRACTIONAL}{BINARY_EXPONENT}{FLOAT_SUFFIX}? |
  ("0x" | "0X"){HEXA_DIGIT_SEQ}{BINARY_EXPONENT}{FLOAT_SUFFIX}?
  { return tokenTypes.getFloatLiteral(); }

  // default string
  {ENCODING_PREFIX}?"\""{S_CHAR}*"\""? |
  // raw string
  {ENCODING_PREFIX}?"R\""{D_CHAR}?"("{R_CHAR}*")"?{D_CHAR}?"\""?
  { return tokenTypes.getStringLiteral(); }
}

[^]
{ return TokenType.BAD_CHARACTER; }
