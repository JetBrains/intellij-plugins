/* Generated code. Do not modify it. */
package com.intellij.lang.ognl.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import java.util.*;
import java.lang.reflect.Field;
import org.jetbrains.annotations.NotNull;

import com.intellij.psi.TokenType;
import com.intellij.lang.ognl.OgnlTypes;

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

<YYINITIAL> "%{"      { return OgnlTypes.EXPRESSION_START; }
<YYINITIAL> "}"       { return OgnlTypes.EXPRESSION_END; }

"{"                   { yybegin(SEQUENCE_EXPRESSION); return OgnlTypes.LBRACE; }
<SEQUENCE_EXPRESSION> "}"   { yybegin(YYINITIAL); return OgnlTypes.RBRACE; }

{WHITE_SPACE_CHAR}+   { return TokenType.WHITE_SPACE; }

{INTEGER_LITERAL}     { return OgnlTypes.INTEGER_LITERAL; }
{BIG_INTEGER_LITERAL} { return OgnlTypes.BIG_INTEGER_LITERAL; }
{DOUBLE_LITERAL}      { return OgnlTypes.DOUBLE_LITERAL; }
{BIG_DECIMAL_LITERAL} { return OgnlTypes.BIG_DECIMAL_LITERAL; }

{CHARACTER_LITERAL}   { return OgnlTypes.CHARACTER_LITERAL; }
{STRING_LITERAL}      { return OgnlTypes.STRING_LITERAL; }

"shl"    { return OgnlTypes.SHIFT_LEFT_KEYWORD; }
"shr"    { return OgnlTypes.SHIFT_RIGHT_KEYWORD; }
"ushr"   { return OgnlTypes.SHIFT_RIGHT_LOGICAL_KEYWORD; }

"and"    { return OgnlTypes.AND_KEYWORD; }
"band"   { return OgnlTypes.BAND_KEYWORD; }
"or"     { return OgnlTypes.OR_KEYWORD; }
"bor"    { return OgnlTypes.BOR_KEYWORD; }
"xor"    { return OgnlTypes.XOR_KEYWORD; }
"eq"     { return OgnlTypes.EQ_KEYWORD; }
"neq"    { return OgnlTypes.NEQ_KEYWORD; }
"lt"     { return OgnlTypes.LT_KEYWORD; }
"lte"    { return OgnlTypes.LT_EQ_KEYWORD; }
"gt"     { return OgnlTypes.GT_KEYWORD; }
"gte"    { return OgnlTypes.GT_EQ_KEYWORD; }
"not in" { return OgnlTypes.NOT_IN_KEYWORD; }
"not"    { return OgnlTypes.NOT_KEYWORD; }
"in"     { return OgnlTypes.IN_KEYWORD; }

"new"    { return OgnlTypes.NEW_KEYWORD; }
"true"   { return OgnlTypes.TRUE_KEYWORD; }
"false"  { return OgnlTypes.FALSE_KEYWORD; }
"null"   { return OgnlTypes.NULL_KEYWORD; }
"instanceof" { return OgnlTypes.INSTANCEOF_KEYWORD; }

{IDENTIFIER} { return OgnlTypes.IDENTIFIER; }

"("   { return OgnlTypes.LPARENTH; }
")"   { return OgnlTypes.RPARENTH; }
"["   { return OgnlTypes.LBRACKET; }
"]"   { return OgnlTypes.RBRACKET; }

"!="  { return OgnlTypes.NOT_EQUAL; }
"!"   { return OgnlTypes.NEGATE; }
"=="  { return OgnlTypes.EQUAL; }

"<<"  { return OgnlTypes.SHIFT_LEFT; }
">>>" { return OgnlTypes.SHIFT_RIGHT_LOGICAL; }
">>"  { return OgnlTypes.SHIFT_RIGHT; }

"<="  { return OgnlTypes.LESS_EQUAL; }
">="  { return OgnlTypes.GREATER_EQUAL; }
"<"   { return OgnlTypes.LESS; }
">"   { return OgnlTypes.GREATER; }


"."  { return OgnlTypes.DOT; }
","  { return OgnlTypes.COMMA; }
"?"  { return OgnlTypes.QUESTION; }
":"  { return OgnlTypes.COLON; }
"#"  { return OgnlTypes.HASH; }
"@"  { return OgnlTypes.AT; }
"$"  { return OgnlTypes.DOLLAR; }
"="  { return OgnlTypes.EQ; }

"/"  { return OgnlTypes.DIVISION; }
"*"  { return OgnlTypes.MULTIPLY; }
"-"  { return OgnlTypes.MINUS; }
"+"  { return OgnlTypes.PLUS; }
"%"  { return OgnlTypes.MODULO; }

"&&" { return OgnlTypes.AND_AND; }
"||" { return OgnlTypes.OR_OR; }

"|"  { return OgnlTypes.OR; }
"^"  { return OgnlTypes.XOR; }
"&"  { return OgnlTypes.AND; }
"~"  { return OgnlTypes.NOT; }

.    {  yybegin(YYINITIAL); return TokenType.BAD_CHARACTER; }