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

%state SEQUENCE_EXPRESSION

%%
<YYINITIAL> "%{"      { return OgnlTokenTypes.EXPRESSION_START; }
<YYINITIAL> "}"       { return OgnlTokenTypes.EXPRESSION_END; }

{WHITE_SPACE_CHAR}+   { return OgnlTokenTypes.WHITE_SPACE; }

"{"                   { yybegin(SEQUENCE_EXPRESSION); return OgnlTokenTypes.LBRACE; }
<SEQUENCE_EXPRESSION> "}"   { yybegin(YYINITIAL); return OgnlTokenTypes.RBRACE; }

{INTEGER_LITERAL}     { return OgnlTokenTypes.INTEGER_LITERAL; }
{BIG_INTEGER_LITERAL} { return OgnlTokenTypes.BIG_INTEGER_LITERAL; }
{DOUBLE_LITERAL}      { return OgnlTokenTypes.DOUBLE_LITERAL; }
{BIG_DECIMAL_LITERAL} { return OgnlTokenTypes.BIG_DECIMAL_LITERAL; }

{CHARACTER_LITERAL}   { return OgnlTokenTypes.CHARACTER_LITERAL; }
{STRING_LITERAL}      { return OgnlTokenTypes.STRING_LITERAL; }

"shl"    { return OgnlTokenTypes.SHIFT_LEFT_KEYWORD; }
"shr"    { return OgnlTokenTypes.SHIFT_RIGHT_KEYWORD; }
"ushr"   { return OgnlTokenTypes.SHIFT_RIGHT_LOGICAL_KEYWORD; }

"and"    { return OgnlTokenTypes.AND_KEYWORD; }
"band"   { return OgnlTokenTypes.BAND_KEYWORD; }
"or"     { return OgnlTokenTypes.OR_KEYWORD; }
"bor"    { return OgnlTokenTypes.BOR_KEYWORD; }
"xor"    { return OgnlTokenTypes.XOR_KEYWORD; }
"eq"     { return OgnlTokenTypes.EQ_KEYWORD; }
"neq"    { return OgnlTokenTypes.NEQ_KEYWORD; }
"lt"     { return OgnlTokenTypes.LT_KEYWORD; }
"lte"    { return OgnlTokenTypes.LT_EQ_KEYWORD; }
"gt"     { return OgnlTokenTypes.GT_KEYWORD; }
"gte"    { return OgnlTokenTypes.GT_EQ_KEYWORD; }
"not in" { return OgnlTokenTypes.NOT_IN_KEYWORD; }
"not"    { return OgnlTokenTypes.NOT_KEYWORD; }
"in"     { return OgnlTokenTypes.IN_KEYWORD; }
"new"    { return OgnlTokenTypes.NEW_KEYWORD; }

"true"   { return OgnlTokenTypes.TRUE_KEYWORD; }
"false"  { return OgnlTokenTypes.FALSE_KEYWORD; }
"null"   { return OgnlTokenTypes.NULL_KEYWORD; }
"instanceof" { return OgnlTokenTypes.INSTANCEOF_KEYWORD; }

{IDENTIFIER} { return OgnlTokenTypes.IDENTIFIER; }

"("   { return OgnlTokenTypes.LPARENTH; }
")"   { return OgnlTokenTypes.RPARENTH; }
"["   { return OgnlTokenTypes.LBRACKET; }
"]"   { return OgnlTokenTypes.RBRACKET; }

"!="  { return OgnlTokenTypes.NOT_EQUAL; }
"!"   { return OgnlTokenTypes.NEGATE; }
"=="  { return OgnlTokenTypes.EQUAL; }

"<<"  { return OgnlTokenTypes.SHIFT_LEFT; }
">>>" { return OgnlTokenTypes.SHIFT_RIGHT_LOGICAL; }
">>"  { return OgnlTokenTypes.SHIFT_RIGHT; }

"<="  { return OgnlTokenTypes.LESS_EQUAL; }
">="  { return OgnlTokenTypes.GREATER_EQUAL; }
"<"   { return OgnlTokenTypes.LESS; }
">"   { return OgnlTokenTypes.GREATER; }


"."  { return OgnlTokenTypes.DOT; }
","  { return OgnlTokenTypes.COMMA; }
"?"  { return OgnlTokenTypes.QUESTION; }
":"  { return OgnlTokenTypes.COLON; }
"#"  { return OgnlTokenTypes.HASH; }
"@"  { return OgnlTokenTypes.AT; }
"$"  { return OgnlTokenTypes.DOLLAR; }
"="  { return OgnlTokenTypes.EQ; }

"/"  { return OgnlTokenTypes.DIVISION; }
"*"  { return OgnlTokenTypes.MULTIPLY; }
"-"  { return OgnlTokenTypes.MINUS; }
"+"  { return OgnlTokenTypes.PLUS; }
"%"  { return OgnlTokenTypes.MODULO; }

"&&" { return OgnlTokenTypes.AND_AND; }
"||" { return OgnlTokenTypes.OR_OR; }

"|"  { return OgnlTokenTypes.OR; }
"^"  { return OgnlTokenTypes.XOR; }
"&"  { return OgnlTokenTypes.AND; }
"~"  { return OgnlTokenTypes.NOT; }

[^] { return OgnlTokenTypes.BAD_CHARACTER; }
