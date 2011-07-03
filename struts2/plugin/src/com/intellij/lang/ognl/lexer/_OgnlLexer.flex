/* Generated code. Do not modify it. */
package com.intellij.lang.ognl.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import com.intellij.lang.ognl.psi.OgnlTokenTypes;

%%

%{
  public _OgnlLexer(){
    this((java.io.Reader)null);
  }
%}

%unicode
%class _OgnlLexer
%public
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}

ALPHA=[:letter:]
DIGIT=[0-9]
WHITE_SPACE_CHAR=[\ \n\r\t\f]

IDENTIFIER=[:jletter:] [:jletterdigit:]*

INTEGER_LITERAL=(0|([1-9]({DIGIT})*))
BIG_INTEGER_LITERAL=({INTEGER_LITERAL})(["h""H"]?)
DOUBLE_LITERAL=({FLOATING_POINT_LITERAL1})|({FLOATING_POINT_LITERAL2})|({FLOATING_POINT_LITERAL3})
BIG_DECIMAL_LITERAL=({DOUBLE_LITERAL})(["b""B"]?)

FLOATING_POINT_LITERAL1=({DIGIT})+"."({DIGIT})*({EXPONENT_PART})?
FLOATING_POINT_LITERAL2="."({DIGIT})+({EXPONENT_PART})?
FLOATING_POINT_LITERAL3=({DIGIT})+({EXPONENT_PART})
EXPONENT_PART=[Ee]["+""-"]?({DIGIT})*

CHARACTER_LITERAL="'"([^\\\'\r\n]|{ESCAPE_SEQUENCE})*("'"|\\)?
STRING_LITERAL=\"([^\\\"\r\n]|{ESCAPE_SEQUENCE})*(\"|\\)?

ESCAPE_SEQUENCE=\\[^\r\n]

%%
<YYINITIAL> "%{"                  { return OgnlTokenTypes.EXPRESSION_START; }
<YYINITIAL> "}"                   { return OgnlTokenTypes.EXPRESSION_END; }

<YYINITIAL> {WHITE_SPACE_CHAR}+   { return OgnlTokenTypes.WHITE_SPACE; }

<YYINITIAL> {INTEGER_LITERAL}     { return OgnlTokenTypes.INTEGER_LITERAL; }
<YYINITIAL> {BIG_INTEGER_LITERAL} { return OgnlTokenTypes.BIG_INTEGER_LITERAL; }
<YYINITIAL> {DOUBLE_LITERAL}      { return OgnlTokenTypes.DOUBLE_LITERAL; }
<YYINITIAL> {BIG_DECIMAL_LITERAL} { return OgnlTokenTypes.BIG_DECIMAL_LITERAL; }

<YYINITIAL> {CHARACTER_LITERAL}   { return OgnlTokenTypes.CHARACTER_LITERAL; }
<YYINITIAL> {STRING_LITERAL}      { return OgnlTokenTypes.STRING_LITERAL; }

<YYINITIAL> "shl"    { return OgnlTokenTypes.SHIFT_LEFT_KEYWORD; }
<YYINITIAL> "shr"    { return OgnlTokenTypes.SHIFT_RIGHT_KEYWORD; }
<YYINITIAL> "ushr"   { return OgnlTokenTypes.SHIFT_RIGHT_LOGICAL_KEYWORD; }

<YYINITIAL> "and"    { return OgnlTokenTypes.AND_KEYWORD; }
<YYINITIAL> "band"   { return OgnlTokenTypes.BAND_KEYWORD; }
<YYINITIAL> "or"     { return OgnlTokenTypes.OR_KEYWORD; }
<YYINITIAL> "bor"    { return OgnlTokenTypes.BOR_KEYWORD; }
<YYINITIAL> "xor"    { return OgnlTokenTypes.XOR_KEYWORD; }
<YYINITIAL> "eq"     { return OgnlTokenTypes.EQ_KEYWORD; }
<YYINITIAL> "neq"    { return OgnlTokenTypes.NEQ_KEYWORD; }
<YYINITIAL> "lt"     { return OgnlTokenTypes.LT_KEYWORD; }
<YYINITIAL> "lte"    { return OgnlTokenTypes.LT_EQ_KEYWORD; }
<YYINITIAL> "gt"     { return OgnlTokenTypes.GT_KEYWORD; }
<YYINITIAL> "gte"    { return OgnlTokenTypes.GT_EQ_KEYWORD; }
<YYINITIAL> "not in" { return OgnlTokenTypes.NOT_IN_KEYWORD; }
<YYINITIAL> "not"    { return OgnlTokenTypes.NOT_KEYWORD; }
<YYINITIAL> "in"     { return OgnlTokenTypes.IN_KEYWORD; }
<YYINITIAL> "new"    { return OgnlTokenTypes.NEW_KEYWORD; }

<YYINITIAL> "true"   { return OgnlTokenTypes.TRUE_KEYWORD; }
<YYINITIAL> "false"  { return OgnlTokenTypes.FALSE_KEYWORD; }
<YYINITIAL> "null"   { return OgnlTokenTypes.NULL_KEYWORD; }
<YYINITIAL> "instanceof" { return OgnlTokenTypes.INSTANCEOF_KEYWORD; }

<YYINITIAL> {IDENTIFIER} { return OgnlTokenTypes.IDENTIFIER; }

<YYINITIAL> "("   { return OgnlTokenTypes.LPARENTH; }
<YYINITIAL> ")"   { return OgnlTokenTypes.RPARENTH; }
<YYINITIAL> "{"   { return OgnlTokenTypes.LBRACE; }
<YYINITIAL> "}"   { return OgnlTokenTypes.RBRACE; }
<YYINITIAL> "["   { return OgnlTokenTypes.LBRACKET; }
<YYINITIAL> "]"   { return OgnlTokenTypes.RBRACKET; }

<YYINITIAL> "!="  { return OgnlTokenTypes.NOT_EQUAL; }
<YYINITIAL> "!"   { return OgnlTokenTypes.NEGATE; }
<YYINITIAL> "=="  { return OgnlTokenTypes.EQUAL; }

<YYINITIAL> "<<"  { return OgnlTokenTypes.SHIFT_LEFT; }
<YYINITIAL> ">>>" { return OgnlTokenTypes.SHIFT_RIGHT_LOGICAL; }
<YYINITIAL> ">>"  { return OgnlTokenTypes.SHIFT_RIGHT; }

<YYINITIAL> "<="  { return OgnlTokenTypes.LESS_EQUAL; }
<YYINITIAL> ">="  { return OgnlTokenTypes.GREATER_EQUAL; }
<YYINITIAL> "<"   { return OgnlTokenTypes.LESS; }
<YYINITIAL> ">"   { return OgnlTokenTypes.GREATER; }


<YYINITIAL> "."  { return OgnlTokenTypes.DOT; }
<YYINITIAL> ","  { return OgnlTokenTypes.COMMA; }
<YYINITIAL> "?"  { return OgnlTokenTypes.QUESTION; }
<YYINITIAL> ":"  { return OgnlTokenTypes.COLON; }
<YYINITIAL> "#"  { return OgnlTokenTypes.HASH; }
<YYINITIAL> "@"  { return OgnlTokenTypes.AT; }
<YYINITIAL> "$"  { return OgnlTokenTypes.DOLLAR; }
<YYINITIAL> "="  { return OgnlTokenTypes.EQ; }

<YYINITIAL> "/"  { return OgnlTokenTypes.DIVISION; }
<YYINITIAL> "*"  { return OgnlTokenTypes.MULTIPLY; }
<YYINITIAL> "-"  { return OgnlTokenTypes.MINUS; }
<YYINITIAL> "+"  { return OgnlTokenTypes.PLUS; }
<YYINITIAL> "%"  { return OgnlTokenTypes.MODULO; }

<YYINITIAL> "&&" { return OgnlTokenTypes.AND_AND; }
<YYINITIAL> "||" { return OgnlTokenTypes.OR_OR; }

<YYINITIAL> "|"  { return OgnlTokenTypes.OR; }
<YYINITIAL> "^"  { return OgnlTokenTypes.XOR; }
<YYINITIAL> "&"  { return OgnlTokenTypes.AND; }
<YYINITIAL> "~"  { return OgnlTokenTypes.NOT; }

[^] { return OgnlTokenTypes.BAD_CHARACTER; }
