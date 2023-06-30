package com.intellij.dts.lang.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;

import com.intellij.dts.lang.psi.DtsTypes;

%%

%class PpLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}

EOL               = "\n" | "\r" | "\r\n"
EOL_ESC           = "\\"{LINE_WS}*{EOL}
LINE_WS           = [\ \t]
SYMBOL            = [a-zA-Z_][a-zA-Z_0-9]+

%state WAITING_SYMBOL WAITING_HEADER WAITING_DEFINE_SYMBOL WAITING_DEFINE_VALUE

%%

<YYINITIAL> {
    "#"{LINE_WS}*"include"  { yybegin(WAITING_HEADER); return DtsTypes.PP_INCLUDE; }
    "#"{LINE_WS}*"ifdef"    { yybegin(WAITING_SYMBOL); return DtsTypes.PP_IFDEF; }
    "#"{LINE_WS}*"ifndef"   { yybegin(WAITING_SYMBOL); return DtsTypes.PP_IFNDEF; }
    "#"{LINE_WS}*"endif"    { return DtsTypes.PP_ENDIF; }
    "#"{LINE_WS}*"define"   { yybegin(WAITING_DEFINE_SYMBOL); return DtsTypes.PP_DEFINE; }
    "#"{LINE_WS}*"undef"    { yybegin(WAITING_SYMBOL); return DtsTypes.PP_UNDEF; }
}

<WAITING_SYMBOL> {
    {SYMBOL}                { return DtsTypes.PP_SYMBOL; }
}

<WAITING_DEFINE_SYMBOL> {
    {SYMBOL}                { yybegin(WAITING_DEFINE_VALUE); return DtsTypes.PP_SYMBOL; }
}

<WAITING_HEADER> {
    "<"                     { return DtsTypes.PP_LANGLE; }
    ">"                     { return DtsTypes.PP_RANGLE; }
    "\""                    { return DtsTypes.PP_DQUOTE; }
    [^\ \t<>\"]+            { return DtsTypes.PP_PATH; }
}

<WAITING_DEFINE_VALUE> {
    [^]+                    { return DtsTypes.PP_DEFINE_VALUE; }
}

({LINE_WS} | {EOL_ESC})+    { return TokenType.WHITE_SPACE; }

[^]                         { return TokenType.BAD_CHARACTER; }
