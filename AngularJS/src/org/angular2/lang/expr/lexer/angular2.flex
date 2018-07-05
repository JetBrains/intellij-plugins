package org.angular2.lang.lexer;

import com.intellij.psi.tree.IElementType;

import static org.angular2.lang.expr.lexer.Angular2TokenTypes.*;
import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;

%%

%{
  private char quote;
%}

%unicode
//%debug
%class _Angular2Lexer
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
  "'"                         { yybegin(YYSTRING); quote = '\''; return STRING_LITERAL_PART; }
  "\""                        { yybegin(YYSTRING); quote = '"'; return STRING_LITERAL_PART; }
  {NUMBER}                    { return NUMERIC_LITERAL; }
  {WHITE_SPACE}               { return WHITE_SPACE; }

  "var"                       { return VAR_KEYWORD; }
  "let"                       { return LET_KEYWORD; }
  "as"                        { return AS_KEYWORD; }
  "null"                      { return NULL_KEYWORD; }
  "undefined"                 { return UNDEFINED_KEYWORD; }
  "true"                      { return TRUE_KEYWORD; }
  "false"                     { return FALSE_KEYWORD; }
  "if"                        { return IF_KEYWORD; }
  "else"                      { return ELSE_KEYWORD; }
  "this"                      { return THIS_KEYWORD; }

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
  ","                         { return COMMA; }
  ";"                         { return SEMICOLON; }
  ":"                         { return COLON; }
  "?"                         { return QUEST; }
  "#"                         { return SHARP; }

  [^]                         { return BAD_CHARACTER; }
}

<YYSTRING> {
  [\\][^u\n\r]                |
  [\\]u[0-9a-fA-F]{4}         { return ESCAPE_SEQUENCE; }
  [\\]u[^0-9a-fA-F]           { yypushback(1); return INVALID_ESCAPE_SEQUENCE; }
  [\\]u[0-9a-fA-F]{1,3}       { return INVALID_ESCAPE_SEQUENCE; }
  "'"                         { if (quote == '\'') yybegin(YYINITIAL); return STRING_LITERAL_PART; }
  "\""                        { if (quote == '"') yybegin(YYINITIAL); return STRING_LITERAL_PART; }
  [^\'\"\n\r\\]+              { return STRING_LITERAL_PART; }
  [^]                         { yypushback(yytext().length()); yybegin(YYINITIAL); }
}