package com.intellij.tsr.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.intellij.tsr.psi.TslTokenTypes.*;
%%

%{
  public _TslLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class _TslLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

IDENTIFIER=([:jletter:]|[_])([:jletter:]|[._0-9\$])*

ESCAPE_SEQUENCE=\\[^\r\n]
DOUBLE_QUOTED_STRING=\"([^\\\"\r\n]|{ESCAPE_SEQUENCE})*(\"|\\)?
SINGLE_QUOTED_STRING=\'([^\\\'\r\n]|{ESCAPE_SEQUENCE})*(\'|\\)?

NUMBER=(-?(0|[1-9][0-9]*)(\.[0-9]+)?([eE][+-]?[0-9]*)?)|Infinity|-Infinity|NaN

WHITE_SPACE=\s+

STRUDEL_HEX=[@][a-f0-9]+

%%

<YYINITIAL> {
    ","                                  { return COMMA; }
    ":"                                  { return COLON; }
    "true"                               { return TRUE; }
    "false"                              { return FALSE; }
    "null"                               { return NULL; }
    "["                                  { return LBRACKET; }
    "]"                                  { return RBRACKET; }
    "("                                  { return LPARENTH; }
    ")"                                  { return RPARENTH; }
    "{"                                  { return LBRACE; }
    "}"                                  { return RBRACE; }
    "="                                  { return ASSIGN; }

    {NUMBER}                             { return NUMBER; }
    {DOUBLE_QUOTED_STRING}               { return DOUBLE_QUOTED_STRING; }
    {SINGLE_QUOTED_STRING}               { return SINGLE_QUOTED_STRING; }
    {STRUDEL_HEX}                        { return STRUDEL_HEX; }
    {IDENTIFIER}                         { return IDENTIFIER; }
    {WHITE_SPACE}                        { return WHITE_SPACE; }

    "."                                  { return DOT; }
    "-"                                  { return DASH; }
    "+"                                  { return PLUS; }
    "*"                                  { return STAR; }
    "#"                                  { return SHARP; }
    ";"                                  { return SEMICOLON; }
    "%"                                  { return PERCENT; }
    "/"                                  { return SLASH; }
    "\\"                                 { return BACKSLASH; }
}

[^] { return BAD_CHARACTER; }