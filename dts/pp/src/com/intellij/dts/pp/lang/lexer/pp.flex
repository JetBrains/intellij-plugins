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
SYMBOL            = [a-zA-Z_][a-zA-Z_0-9]+

%state WAITING_SYMBOL WAITING_HEADER WAITING_DEFINE_SYMBOL WAITING_DEFINE_VALUE

%%

<YYINITIAL> {
    "#"{LINE_WS}*"include"  { yybegin(WAITING_HEADER); return tokenTypes.getInclude(); }
    "#"{LINE_WS}*"ifdef"    { yybegin(WAITING_SYMBOL); return tokenTypes.getIfdef(); }
    "#"{LINE_WS}*"ifndef"   { yybegin(WAITING_SYMBOL); return tokenTypes.getIfndef(); }
    "#"{LINE_WS}*"endif"    { return tokenTypes.getEndif(); }
    "#"{LINE_WS}*"define"   { yybegin(WAITING_DEFINE_SYMBOL); return tokenTypes.getDefine(); }
    "#"{LINE_WS}*"undef"    { yybegin(WAITING_SYMBOL); return tokenTypes.getUndef(); }
}

<WAITING_SYMBOL> {
    {SYMBOL}                { return tokenTypes.getSymbol(); }
}

<WAITING_DEFINE_SYMBOL> {
    {SYMBOL}                { yybegin(WAITING_DEFINE_VALUE); return tokenTypes.getSymbol(); }
}

<WAITING_HEADER> {
    "<"                     { return tokenTypes.getLAngle(); }
    ">"                     { return tokenTypes.getRAngle(); }
    "\""                    { return tokenTypes.getDQuote(); }
    [^\ \t<>\"]+            { return tokenTypes.getPath(); }
}

<WAITING_DEFINE_VALUE> {
    [^]+                    { return tokenTypes.getDefineValue(); }
}

({LINE_WS} | {EOL_ESC})+    { return TokenType.WHITE_SPACE; }

[^]                         { return TokenType.BAD_CHARACTER; }
