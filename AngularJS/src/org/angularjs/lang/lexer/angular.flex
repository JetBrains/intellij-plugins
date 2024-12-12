package org.angularjs.lang.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.lang.javascript.JSTokenTypes.*;
import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static org.angularjs.lang.lexer.AngularJSTokenTypes.ELVIS;
import static org.angularjs.lang.lexer.AngularJSTokenTypes.*;

%%

%{
  private char quote;
%}

%unicode
//%debug
%class _AngularJSLexer
%implements FlexLexer
%type IElementType

%function advance

WHITE_SPACE     = ([ \t\n\r\u000B\u00A0]|\\\n)+

DIGIT = [0-9]
NUMBER=({DIGIT}+)|({FP_LITERAL1})|({FP_LITERAL2})|({FP_LITERAL3})|({FP_LITERAL4})
FP_LITERAL1=({DIGIT})+"."({DIGIT})*({EXPONENT_PART})?
FP_LITERAL2="."({DIGIT})+({EXPONENT_PART})?
FP_LITERAL3=({DIGIT})+({EXPONENT_PART})
FP_LITERAL4=({DIGIT})+
EXPONENT_PART=[Ee]["+""-"]?({DIGIT})*


IDENT =[_$a-zA-Z][$0-9_a-zA-Z]*

%state YYSTRING

%%

<YYINITIAL> {
  "'"                         { yybegin(YYSTRING); quote = '\''; return STRING_LITERAL; }
  "\""                        { yybegin(YYSTRING); quote = '"'; return STRING_LITERAL; }
  {NUMBER}                    { return NUMERIC_LITERAL; }
  {WHITE_SPACE}               { return WHITE_SPACE; }

  "this"                      { return THIS_KEYWORD; }
  "true"                      { return TRUE_KEYWORD; }
  "false"                     { return FALSE_KEYWORD; }
  "null"                      { return NULL_KEYWORD; }
  "undefined"                 { return UNDEFINED_KEYWORD; }
  "in"                        { return IN_KEYWORD; }
  "as"                        { return AS_KEYWORD; }
  "of"                        { return OF_KEYWORD; }
  "track by"                  { return TRACK_BY_KEYWORD; }

  "trackBy"                   { return TRACK_BY_KEYWORD; }
  "let"                       { return LET_KEYWORD; }
  "then"                      { return THEN; }
  "else"                      { return ELSE_KEYWORD; }

  "as"/(\.)                   { return IDENTIFIER; }
  {IDENT}                     { return IDENTIFIER; }

  "+"                         { return PLUS; }
  "-"                         { return MINUS; }
  "*"                         { return MULT; }
  "/"                         { return DIV; }
  "%"                         { return PERC; }
  "^"                         { return XOR; }
  "="                         { return EQ; }
  "==="                       { return EQEQEQ; }
  "!=="                       { return NEQEQ; }
  "=="                        { return EQEQ; }
  "!="                        { return NE; }
  "<"                         { return LT; }
  ">"                         { return GT; }
  "<="                        { return LE; }
  ">="                        { return GE; }
  "&&"                        { return ANDAND; }
  "||"                        { return OROR; }
  "&"                         { return AND; }
  "|"                         { return OR; }
  "!"                         { return EXCL; }

  "("                         { return LPAR; }
  ")"                         { return RPAR; }
  "{"                         { return LBRACE; }
  "}"                         { return RBRACE; }
  "["                         { return LBRACKET; }
  "]"                         { return RBRACKET; }
  "."                         { return DOT; }
  "?."                        { return ELVIS; }
  "!."                        { return ASSERT_NOT_NULL; }
  ","                         { return COMMA; }
  ";"                         { return SEMICOLON; }
  ":"                         { return COLON; }
  "?"                         { return QUEST; }
  "::"                        { return ONE_TIME_BINDING; }

  [^]                         { return BAD_CHARACTER; }
}

<YYSTRING> {
  [\\][^u\n\r]                |
  [\\]u[0-9a-fA-F]{4}         { return ESCAPE_SEQUENCE; }
  [\\]u[^0-9a-fA-F]           { yypushback(1); return INVALID_ESCAPE_SEQUENCE; }
  [\\]u[0-9a-fA-F]{1,3}       { return INVALID_ESCAPE_SEQUENCE; }
  "'"                         { if (quote == '\'') yybegin(YYINITIAL); return STRING_LITERAL; }
  "\""                        { if (quote == '"') yybegin(YYINITIAL); return STRING_LITERAL; }
  [^\'\"\n\r\\]+              { return STRING_LITERAL; }
  [^]                         { yypushback(yytext().length()); yybegin(YYINITIAL); }
}